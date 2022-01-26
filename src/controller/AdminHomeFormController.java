package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AdminHomeFormController {
    public Label lblHover;
    public Button btnSignOut;
    public Button btnBackupRestore;
    public Button btnManageUsers;
    public Button btnUserProfile;
    public Button btnViewReports;
    public Button btnRecordAttendance;

    String initialText;
    public void initialize(){

        initialText=lblHover.getText();
        btnRecordAttendance.setOnMouseEntered(event ->setHover((Button) event.getSource()));
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

    public void btnSignOut_OnAction(ActionEvent actionEvent) {
    }

    public void btnRecordAttendance_OnAction(ActionEvent actionEvent) {
    }

    public void btnViewReports_OnAction(ActionEvent actionEvent) {
    }

    public void btnUserProfile_OnAction(ActionEvent actionEvent) {
    }
}
