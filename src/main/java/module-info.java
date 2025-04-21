module com.novalearn {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // PDFBox modules
    requires org.apache.pdfbox;
    requires org.apache.fontbox;

    // JPA / Hibernate
    requires jakarta.persistence;
    requires jakarta.validation;

    // Open FXML controller packages for reflection
    opens com.novalearn.controller to javafx.fxml;
    opens com.novalearn.controller.user to javafx.fxml;
    opens com.novalearn.controller.quiz to javafx.fxml;
    opens com.novalearn.controller.question to javafx.fxml;
    opens com.novalearn.controller.submission to javafx.fxml;

    // Open views for reflection
    opens com.novalearn.view to javafx.fxml;

    // Open entities for Hibernate and JavaFX
    opens com.novalearn.entity to javafx.base;

    // Export packages
    exports com.novalearn.application;
    exports com.novalearn.controller;
    exports com.novalearn.entity;
    exports com.novalearn.utils;
}
