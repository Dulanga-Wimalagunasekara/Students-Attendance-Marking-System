package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import security.SecurityContextHolder;


import java.io.IOException;
import java.nio.file.Files;

public class AdminHomeFormController {
    public Label lblHover;
    public Button btnSignOut;
    public Button btnBackupRestore;
    public Button btnManageUsers;
    public Button btnUserProfile;
    public Button btnViewReports;
    public Button btnRecordAttendance;
    public Label lblPrincipal;
    public Button btnAddStudents;

    String initialText;
    public void initialize(){

        initialText=lblHover.getText();
        btnRecordAttendance.setOnMouseEntered(event -> AdminHomeFormController.this.setHover((Button) event.getSource()));
        btnViewReports.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnUserProfile.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnManageUsers.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnBackupRestore.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnSignOut.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnAddStudents.setOnMouseEntered(event -> setHover((Button) event.getSource()));

        btnRecordAttendance.setOnMouseExited(event ->setInitialText());
        btnViewReports.setOnMouseExited(event ->setInitialText());
        btnUserProfile.setOnMouseExited(event ->setInitialText());
        btnManageUsers.setOnMouseExited(event ->setInitialText());
        btnBackupRestore.setOnMouseExited(event ->setInitialText());
        btnSignOut.setOnMouseExited(event ->setInitialText());
        btnAddStudents.setOnMouseExited(event -> setInitialText());

        lblPrincipal.setText("Welcome "+SecurityContextHolder.getPrincipal().getName()+"!");

    }

    public void setHover(Button btn){
        lblHover.setText(btn.getAccessibleText());
    }

    public void setInitialText(){
        lblHover.setText(initialText);
    }

    public void btnManageUsers_OnAction(ActionEvent actionEvent){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/ManageUsersForm.fxml"));
            AnchorPane load = fxmlLoader.load();
            Scene scene = new Scene(load);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Admin Home: Manage Users");
            stage.sizeToScene();
            stage.setResizable(false);
            stage.initOwner(btnRecordAttendance.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnBackupRestore_OnAction(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/BackupAndRestoreForm.fxml"));
        AnchorPane load = fxmlLoader.load();
        Scene scene = new Scene(load);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.setTitle("Admin Home: Backup and Restore");
        stage.sizeToScene();
        stage.setResizable(false);
        stage.initOwner(btnRecordAttendance.getScene().getWindow());
        stage.show();
    }

    public void btnSignOut_OnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        AnchorPane load = FXMLLoader.load(getClass().getResource("/view/LoginForm.fxml"));
        Scene scene = new Scene(load);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.setTitle("Student Attendance System: Log In");
        stage.centerOnScreen();
        stage.show();
        ((Stage)btnSignOut.getScene().getWindow()).close();
    }

    public void btnRecordAttendance_OnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/RecordAttendenceForm.fxml"));
            AnchorPane load = fxmlLoader.load();
            Scene scene = new Scene(load);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.sizeToScene();
            stage.setTitle("Admin Home: Record Attendance");
            stage.setResizable(false);
            stage.initOwner(btnRecordAttendance.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void btnViewReports_OnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/viewReportsForm.fxml"));
            AnchorPane load = fxmlLoader.load();
            Scene scene = new Scene(load);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Admin Home: View Reports");
            stage.sizeToScene();
            stage.setResizable(false);
            stage.initOwner(btnRecordAttendance.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void btnUserProfile_OnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/userProfileForm.fxml"));
            AnchorPane load = fxmlLoader.load();
            Scene scene = new Scene(load);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Admin Home: User Profile");
            stage.sizeToScene();
            stage.setResizable(false);
            stage.initOwner(btnRecordAttendance.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnOnKeyPressedOnAction(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            case F1:
                btnRecordAttendance.fire();
                break;
            case F2:
                btnViewReports.fire();
                break;
            case F3:
                btnUserProfile.fire();
                break;
            case F4:
                btnManageUsers.fire();
                break;
            case F5:
                btnBackupRestore.fire();
                break;
            case F6:
                btnAddStudents.fire();
                break;
            case F12:
                btnSignOut.fire();
                break;
        }
    }

    public void btnAddStudentsOnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        AnchorPane load = FXMLLoader.load(getClass().getResource("/view/AddStudentsForm.fxml"));
        stage.setScene(new Scene(load));
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setResizable(false);
        stage.initOwner(btnRecordAttendance.getScene().getWindow());
        stage.show();
    }
}
