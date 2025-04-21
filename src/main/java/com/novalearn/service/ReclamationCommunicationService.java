package com.novalearn.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ReclamationCommunicationService {
    private static ReclamationCommunicationService instance;
    private Connection connection;
    private static final String UPLOAD_DIR = "uploads/reclamations/";

    private ReclamationCommunicationService() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/novalearn", "root", "");
            createUploadDirectory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ReclamationCommunicationService getInstance() {
        if (instance == null) {
            instance = new ReclamationCommunicationService();
        }
        return instance;
    }

    private void createUploadDirectory() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
    }

    public void addComment(int reclamationId, int userId, String commentaire) {
        String query = "INSERT INTO reclamation_commentaires (reclamation_id, user_id, commentaire) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reclamationId);
            stmt.setInt(2, userId);
            stmt.setString(3, commentaire);
            stmt.executeUpdate();
            
            // Créer une notification pour tous les utilisateurs concernés
            createCommentNotification(reclamationId, userId, commentaire);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Comment> getComments(int reclamationId) {
        List<Comment> comments = new ArrayList<>();
        String query = "SELECT c.*, u.nom, u.prenom FROM reclamation_commentaires c " +
                      "JOIN users u ON c.user_id = u.id " +
                      "WHERE c.reclamation_id = ? " +
                      "ORDER BY c.date_creation DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reclamationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Comment comment = new Comment(
                    rs.getInt("id"),
                    rs.getInt("reclamation_id"),
                    rs.getInt("user_id"),
                    rs.getString("commentaire"),
                    rs.getTimestamp("date_creation").toLocalDateTime(),
                    rs.getString("nom") + " " + rs.getString("prenom")
                );
                comments.add(comment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public String addAttachment(int reclamationId, File file) {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getName();
        String relativePath = UPLOAD_DIR + reclamationId + "/" + uniqueFileName;
        
        try {
            // Créer le dossier pour la réclamation si nécessaire
            File reclamationDir = new File(UPLOAD_DIR + reclamationId);
            if (!reclamationDir.exists()) {
                reclamationDir.mkdirs();
            }
            
            // Copier le fichier
            Path destination = Paths.get(relativePath);
            Files.copy(file.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            
            // Enregistrer dans la base de données
            String query = "INSERT INTO reclamation_pieces_jointes " +
                         "(reclamation_id, nom_fichier, type_fichier, chemin_fichier) " +
                         "VALUES (?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, reclamationId);
                stmt.setString(2, file.getName());
                stmt.setString(3, Files.probeContentType(file.toPath()));
                stmt.setString(4, relativePath);
                stmt.executeUpdate();
            }
            
            return relativePath;
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Attachment> getAttachments(int reclamationId) {
        List<Attachment> attachments = new ArrayList<>();
        String query = "SELECT * FROM reclamation_pieces_jointes WHERE reclamation_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reclamationId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Attachment attachment = new Attachment(
                    rs.getInt("id"),
                    rs.getInt("reclamation_id"),
                    rs.getString("nom_fichier"),
                    rs.getString("type_fichier"),
                    rs.getString("chemin_fichier"),
                    rs.getTimestamp("date_upload").toLocalDateTime()
                );
                attachments.add(attachment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attachments;
    }

    private void createCommentNotification(int reclamationId, int commentUserId, String commentaire) {
        // Récupérer tous les utilisateurs concernés (créateur et admin)
        String query = "SELECT DISTINCT user_id, admin_id FROM reclamations WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reclamationId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                int userId = rs.getInt("user_id");
                Integer adminId = rs.getObject("admin_id") != null ? rs.getInt("admin_id") : null;
                
                // Créer une notification pour l'utilisateur si ce n'est pas lui qui a commenté
                if (userId != commentUserId) {
                    createNotification(reclamationId, userId, 
                        "Nouveau commentaire sur votre réclamation : " + 
                        (commentaire.length() > 50 ? commentaire.substring(0, 47) + "..." : commentaire));
                }
                
                // Créer une notification pour l'admin si ce n'est pas lui qui a commenté
                if (adminId != null && adminId != commentUserId) {
                    createNotification(reclamationId, adminId,
                        "Nouveau commentaire sur la réclamation #" + reclamationId + " : " +
                        (commentaire.length() > 50 ? commentaire.substring(0, 47) + "..." : commentaire));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createNotification(int reclamationId, int userId, String message) {
        String query = "INSERT INTO reclamation_notifications (reclamation_id, user_id, message) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, reclamationId);
            stmt.setInt(2, userId);
            stmt.setString(3, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static class Comment {
        private int id;
        private int reclamationId;
        private int userId;
        private String commentaire;
        private LocalDateTime dateCreation;
        private String userName;

        public Comment(int id, int reclamationId, int userId, String commentaire, 
                      LocalDateTime dateCreation, String userName) {
            this.id = id;
            this.reclamationId = reclamationId;
            this.userId = userId;
            this.commentaire = commentaire;
            this.dateCreation = dateCreation;
            this.userName = userName;
        }

        // Getters
        public int getId() { return id; }
        public int getReclamationId() { return reclamationId; }
        public int getUserId() { return userId; }
        public String getCommentaire() { return commentaire; }
        public LocalDateTime getDateCreation() { return dateCreation; }
        public String getUserName() { return userName; }
    }

    public static class Attachment {
        private int id;
        private int reclamationId;
        private String nomFichier;
        private String typeFichier;
        private String cheminFichier;
        private LocalDateTime dateUpload;

        public Attachment(int id, int reclamationId, String nomFichier, 
                        String typeFichier, String cheminFichier, LocalDateTime dateUpload) {
            this.id = id;
            this.reclamationId = reclamationId;
            this.nomFichier = nomFichier;
            this.typeFichier = typeFichier;
            this.cheminFichier = cheminFichier;
            this.dateUpload = dateUpload;
        }

        // Getters
        public int getId() { return id; }
        public int getReclamationId() { return reclamationId; }
        public String getNomFichier() { return nomFichier; }
        public String getTypeFichier() { return typeFichier; }
        public String getCheminFichier() { return cheminFichier; }
        public LocalDateTime getDateUpload() { return dateUpload; }
    }
} 