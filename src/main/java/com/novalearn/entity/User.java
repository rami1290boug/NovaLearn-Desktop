package com.novalearn.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "unique_email"),
        @UniqueConstraint(columnNames = "num_tel", name = "unique_num_tel")
})
public class User implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "bigint")
    private Long id;

    @Column(length = 255)
    private String role;

    @Column(name = "id_fils", nullable = true)
    private Integer idFils;

    @Column(length = 255)
    private String nom;

    @Column(length = 255)
    private String prenom;

    @NotNull
    @Column(nullable = false)
    private Integer age;

    @NotNull(message = "Le numéro de téléphone ne peut pas être vide.")
    @Column(name = "num_tel", nullable = false, unique = true)
    private Integer numTel;

    @Column(length = 255, nullable = true)
    private String difficulte;

    @Column(name = "niv_difficulte", length = 255, nullable = true)
    private String nivDifficulte;

    @NotBlank(message = "L'email ne peut pas être vide.")
    @Email(message = "Veuillez saisir un email valide.")
    @Column(length = 255, unique = true, nullable = false)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;

    @Column(length = 255, nullable = false)
    private String genre;

    @Column(length = 255, nullable = true)
    private String specialite;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = true)
    private String verificationToken;

    public User() {}

    public User(Long id, String role, Integer idFils, String nom, String prenom, Integer age, Integer numTel, String difficulte, String nivDifficulte, String email, String password, String genre, String specialite, Boolean isVerified, String verificationToken) {
        this.id = id;
        this.role = role;
        this.idFils = idFils;
        this.nom = nom;
        this.prenom = prenom;
        this.age = age;
        this.numTel = numTel;
        this.difficulte = difficulte;
        this.nivDifficulte = nivDifficulte;
        this.email = email;
        this.password = password;
        this.genre = genre;
        this.specialite = specialite;
        this.isVerified = isVerified;
        this.verificationToken = verificationToken;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public Integer getIdFils() {
        return idFils;
    }
    public void setIdFils(Integer idFils) {
        this.idFils = idFils;
    }

    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }

    public Integer getNumTel() {
        return numTel;
    }
    public void setNumTel(Integer numTel) {
        this.numTel = numTel;
    }

    public String getDifficulte() {
        return difficulte;
    }
    public void setDifficulte(String difficulte) {
        this.difficulte = difficulte;
    }

    public String getNivDifficulte() {
        return nivDifficulte;
    }
    public void setNivDifficulte(String nivDifficulte) {
        this.nivDifficulte = nivDifficulte;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSpecialite() {
        return specialite;
    }
    public void setSpecialite(String specialite) {
        this.specialite = specialite;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }
    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }
    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}
