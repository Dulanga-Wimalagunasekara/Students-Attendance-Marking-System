package controller;

import db.DBConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import netscape.security.UserTarget;
import security.Principal;
import security.SecurityContextHolder;

import java.io.IOException;
import java.sql.*;
import java.util.Objects;

public class LoginFormController {
    public TextField txtUserName;
    public PasswordField txtPassword;
    public Button btnSignIn;

    public void btnSignIn_OnAction(ActionEvent actionEvent) {
        if (isValidated()) {
            String username = txtUserName.getText().trim();
            String password = txtPassword.getText().trim();
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement stm = connection.prepareStatement("SELECT name,role FROM user WHERE username=? AND password=?");
                stm.setString(1, username);
                stm.setString(2, password);
                ResultSet resultSet = stm.executeQuery();
                if (resultSet.next()) {
                    SecurityContextHolder.setPrincipal(new Principal(username,resultSet.getString("name"),Principal.UserRole.valueOf(resultSet.getString("role"))));
                    String path = null;
                    if (resultSet.getString("role").equals("ADMIN")) {
                        path = "/view/AdminHomeForm.fxml";
                    } else {
                        path = "/view/UserHomeForm.fxml";
                    }

                    FXMLLoader fxmlLoader = new FXMLLoader(this.getClass().getResource(path));
                    AnchorPane load = fxmlLoader.load();
                    Scene scene = new Scene(load);
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.sizeToScene();
                    stage.centerOnScreen();
                    stage.setResizable(false);
                    stage.show();
                    ((Stage) btnSignIn.getScene().getWindow()).close();
                }

            } catch (SQLException | IOException e) {
                new Alert(Alert.AlertType.WARNING, "Something went wrong! Please contact Dulanga : 0768952222 ").show();
                e.printStackTrace();
            }

        }

    }

    public boolean isValidated() {
        String username = txtUserName.getText();
        String password = txtPassword.getText().trim();
        if (!username.matches("[A-Za-z0-9]{4,}") || password.length() < 4) {
            new Alert(Alert.AlertType.WARNING, "Invalid Username or Password! Please Try Again!", ButtonType.OK).show();
            return false;
        }
        return true;
    }
}
