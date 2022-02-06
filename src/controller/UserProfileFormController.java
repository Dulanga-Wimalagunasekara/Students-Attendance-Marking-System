package controller;

import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import security.SecurityContextHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserProfileFormController {

    public Label lblUser;
    public Text txtUserName;
    public TextField txtUser;
    public TextField txtPassword;
    public PasswordField txtConfirmPassword;
    public Button btnChange;
    private String username;

    public void initialize(){
        disableControls(true);
    }

    private void disableControls(boolean val) {
        txtUser.setDisable(val);
        txtPassword.setDisable(val);
        txtConfirmPassword.setDisable(val);

        setValues();
    }

    private void setValues() {
        Connection connection = DBConnection.getInstance().getConnection();
        username = SecurityContextHolder.getPrincipal().getUsername();
        String name = SecurityContextHolder.getPrincipal().getName();

        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM user WHERE username=? AND name=?");
            stm.setString(1,username);
            stm.setString(2,name);

            ResultSet rst = stm.executeQuery();
            if (rst.next()){
                txtUserName.setText(rst.getString("name"));
                lblUser.setText(rst.getString("role"));
                txtUser.setText(rst.getString("username"));
                txtPassword.setText(rst.getString("password"));
            }else {
                new Alert(Alert.AlertType.ERROR,"Something went wrong! Please try again!").show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void btnChangeOnAction(ActionEvent actionEvent) {
        if (btnChange.getText().equals("Update")){
            if (txtPassword.getText().equals(txtConfirmPassword.getText()) && !txtUserName.getText().isEmpty() && !txtPassword.getText().isEmpty()){
                updateDatabase();
            }else {
                new Alert(Alert.AlertType.ERROR,"Please Enter the Fields Correctly",ButtonType.OK).show();
            }
        }else {
            disableControls(false);
            txtUser.setDisable(true);
            btnChange.setStyle("-fx-background-color: #ff7373");
            btnChange.setText("Update");
        }

    }

    private void updateDatabase() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("UPDATE user SET password=? WHERE username=?");
            stm.setString(1,txtPassword.getText());
            stm.setString(2,this.username);
            int i = stm.executeUpdate();
            if (i==1){
                disableControls(true);
                btnChange.setText("Change");
                btnChange.setStyle("-fx-background-color:  #3bff6c");
                new Alert(Alert.AlertType.CONFIRMATION,"Update Success!",ButtonType.OK).showAndWait();
                ((Stage)btnChange.getScene().getWindow()).close();
            }else {
                new Alert(Alert.AlertType.ERROR,"Something Went Wrong! Please Try Again!",ButtonType.OK).show();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Something Went Wrong! Please Try Again!",ButtonType.OK).show();
        }

    }
}
