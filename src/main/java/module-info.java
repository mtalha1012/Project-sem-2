module com.talha.quiz.projectsem2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires jbcrypt;
    requires mysql.connector.j;
    requires java.desktop;

    opens com.talha.quiz.projectsem2 to javafx.fxml;
    opens com.talha.quiz.projectsem2.controller to javafx.fxml;
    opens com.talha.quiz.projectsem2.model to javafx.fxml;
    exports com.talha.quiz.projectsem2;
}