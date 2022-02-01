package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import security.SecurityContextHolder;

import java.io.IOException;

public class AdminHomeFormController {
    public Label lblHover;
    public Button btnSignOut;
    public Button btnBackupRestore;
    public Button btnManageUsers;
    public Button btnUserProfile;
    public Button btnViewReports;
    public Button btnRecordAttendance;
    public Label lblPrincipal;

    String initialText;
    public void initialize(){

        initialText=lblHover.getText();
        btnRecordAttendance.setOnMouseEntered(event -> AdminHomeFormController.this.setHover((Button) event.getSource()));
        btnViewReports.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnUserProfile.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnManageUsers.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnBackupRestore.setOnMouseEntered(event ->setHover((Button) event.getSource()));
        btnSignOut.setOnMouseEntered(event ->setHover((Button) event.getSource()));

        btnRecordAttendance.setOnMouseExited(event ->setInitialText());
        btnViewReports.setOnMouseExited(event ->setInitialText());
        btnUserProfile.setOnMouseExited(event ->setInitialText());
        btnManageUsers.setOnMouseExited(event ->setInitialText());
        btnBackupRestore.setOnMouseExited(event ->setInitialText());
        btnSignOut.setOnMouseExited(event ->setInitialText());

        lblPrincipal.setText("Welcome "+SecurityContextHolder.getPrincipal().getName()+"!");

    }

    public void setHover(Button btn){
        lblHover.setText(btn.getAccessibleText());
    }

    public void setInitialText(){
        lblHover.setText(initialText);
    }

    public void btnManageUsers_OnAction(ActionEvent actionEvent) {

    }

    public void btnBackupRestore_OnAction(ActionEvent actionEvent) {

    }

    public void btnSignOut_OnAction(ActionEvent actionEvent) throws IOException {
        Stage stage = new Stage();
        AnchorPane load = FXMLLoader.load(getClass().getResource("/view/LoginForm.fxml"));
        Scene scene = new Scene(load);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.sizeToScene();
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
            stage.setResizable(false);
            stage.initOwner(btnRecordAttendance.getScene().getWindow());
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void btnViewReports_OnAction(ActionEvent actionEvent) {

    }

    public void btnUserProfile_OnAction(ActionEvent actionEvent) {

    }

}
