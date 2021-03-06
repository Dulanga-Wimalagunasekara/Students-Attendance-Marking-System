package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import security.SecurityContextHolder;

import java.io.IOException;

public class UserHomeFormController {
    public Button btnRecordAttendance;
    public Button btnViewReports;
    public Button btnUserProfile;
    public Button btnSignOut;
    public Label lblOption;
    public Label lblUserName;

    String initialText;
    public void initialize(){
        initialText = lblOption.getText();
        btnRecordAttendance.setOnMouseEntered(event -> setHover(btnRecordAttendance));
        btnViewReports.setOnMouseEntered(event -> setHover(btnViewReports));
        btnUserProfile.setOnMouseEntered(event -> setHover(btnUserProfile));
        btnSignOut.setOnMouseEntered(event -> setHover(btnSignOut));

        btnRecordAttendance.setOnMouseExited(event -> setInitialText() );
        btnViewReports.setOnMouseExited(event -> setInitialText() );
        btnUserProfile.setOnMouseExited(event -> setInitialText() );
        btnSignOut.setOnMouseExited(event -> setInitialText() );

        lblUserName.setText("Welcome "+SecurityContextHolder.getPrincipal().getUsername()+"!");
    }

    private void setInitialText() {
        lblOption.setText(initialText);
    }

    private void setHover(Button btn) {
        lblOption.setText(btn.getAccessibleText());
    }

    public void btnRecordAttendance_OnAction(ActionEvent actionEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/RecordAttendenceForm.fxml"));
            AnchorPane load = fxmlLoader.load();
            Scene scene = new Scene(load);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("User Home: Record Attendance");
            stage.sizeToScene();
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
            stage.sizeToScene();
            stage.setTitle("User Home: View Reports");
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
            stage.sizeToScene();
            stage.setTitle("User Home: User Profile");
            stage.setResizable(false);
            stage.initOwner(btnRecordAttendance.getScene().getWindow());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnSignOut_OnAction(ActionEvent actionEvent) throws IOException {
        Stage stage1 = new Stage();
        AnchorPane load = FXMLLoader.load(getClass().getResource("/view/LoginForm.fxml"));
        Scene scene = new Scene(load);
        stage1.setScene(scene);
        stage1.setResizable(false);
        stage1.sizeToScene();
        stage1.centerOnScreen();
        stage1.setTitle("Student Attendance System: Log In");
        stage1.show();
        ((Stage)btnSignOut.getScene().getWindow()).close();
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
            case F12 :
                btnSignOut.fire();
                break;
        }
    }
}
