package com.novalearn.controller.user;

import com.novalearn.dao.UserDao;
import com.novalearn.entity.User;
import com.novalearn.service.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;

public class userController {

    @FXML private TableView<User> table;
    @FXML private TableColumn<User, Long> colId;
    @FXML private TableColumn<User, String> colEmail;

    @FXML private VBox listPane;
    @FXML private ScrollPane formPane;   // updated

    @FXML private TextField tfEmail, tfPassword, tfNom, tfPrenom;
    @FXML private TextField tfPhone, tfAge, tfGenre, tfRole;
    @FXML private TextField tfIdFils, tfDifficulte, tfNivDifficulte, tfSpecialite;
    @FXML private CheckBox cbVerified;
    @FXML private TextField tfVerificationToken;

    private final UserService service;
    private User currentUser;

    public userController() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("NovalearnPU");
        EntityManager em = emf.createEntityManager();
        this.service = new UserService(new UserDao(em));
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(c.getValue().getId()));
        colEmail.setCellValueFactory(c -> new ReadOnlyStringWrapper(c.getValue().getEmail()));
        loadTable();
    }

    private void loadTable() {
        table.getItems().setAll(service.getAllUsers());
    }

    @FXML private void onNew() {
        currentUser = new User();
        showForm(true);
    }

    @FXML private void onEdit() {
        currentUser = table.getSelectionModel().getSelectedItem();
        if (currentUser == null) return;
        showForm(false);
        // populate fields
        tfEmail.setText(currentUser.getEmail());
        tfPassword.setText(currentUser.getPassword());
        tfNom.setText(currentUser.getNom());
        tfPrenom.setText(currentUser.getPrenom());
        tfPhone.setText(currentUser.getNumTel().toString());
        tfAge.setText(currentUser.getAge().toString());
        tfGenre.setText(currentUser.getGenre());
        tfRole.setText(currentUser.getRole());
        tfIdFils.setText(currentUser.getIdFils() != null ? currentUser.getIdFils().toString() : "");
        tfDifficulte.setText(currentUser.getDifficulte());
        tfNivDifficulte.setText(currentUser.getNivDifficulte());
        tfSpecialite.setText(currentUser.getSpecialite());
        cbVerified.setSelected(currentUser.getIsVerified());
        tfVerificationToken.setText(currentUser.getVerificationToken());
    }

    @FXML private void onDelete() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u != null) {
            service.deleteUser(u);
            loadTable();
        }
    }

    @FXML private void onSave() {
        // validate required
        if (tfEmail.getText().isBlank()
                || tfPassword.getText().isBlank()
                || tfPhone.getText().isBlank()
                || tfAge.getText().isBlank()
                || tfGenre.getText().isBlank()) {
            new Alert(Alert.AlertType.ERROR,
                    "Please fill in all required (*) fields.",
                    ButtonType.OK).showAndWait();
            return;
        }

        currentUser.setEmail(tfEmail.getText());
        currentUser.setPassword(tfPassword.getText());
        currentUser.setNom(tfNom.getText());
        currentUser.setPrenom(tfPrenom.getText());
        currentUser.setNumTel(Integer.valueOf(tfPhone.getText()));
        currentUser.setAge(Integer.valueOf(tfAge.getText()));
        currentUser.setGenre(tfGenre.getText());
        currentUser.setRole(tfRole.getText());
        currentUser.setIdFils(tfIdFils.getText().isBlank() ? null
                : Integer.valueOf(tfIdFils.getText()));
        currentUser.setDifficulte(tfDifficulte.getText());
        currentUser.setNivDifficulte(tfNivDifficulte.getText());
        currentUser.setSpecialite(tfSpecialite.getText());
        currentUser.setIsVerified(cbVerified.isSelected());
        currentUser.setVerificationToken(tfVerificationToken.getText());

        service.saveUser(currentUser);
        hideForm();
        loadTable();
    }

    @FXML private void onCancel() {
        hideForm();
    }

    private void showForm(boolean isNew) {
        listPane.setVisible(false);
        formPane.setVisible(true);
        if (isNew) {
            tfEmail.clear(); tfPassword.clear(); tfNom.clear(); tfPrenom.clear();
            tfPhone.clear(); tfAge.clear(); tfGenre.clear(); tfRole.clear();
            tfIdFils.clear(); tfDifficulte.clear(); tfNivDifficulte.clear();
            tfSpecialite.clear(); cbVerified.setSelected(false);
            tfVerificationToken.clear();
        }
    }

    private void hideForm() {
        formPane.setVisible(false);
        listPane.setVisible(true);
    }
}
