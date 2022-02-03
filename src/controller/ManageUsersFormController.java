package controller;

import db.DBConnection;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import view.TM.userTM;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ManageUsersFormController {
    public TextField txtUsername;
    public TextField txtName;
    public PasswordField txtConfirmPassword;
    public Button btnCreateRole;
    public MenuButton menuRole;
    public Button btnUpdateRole;
    public TableView<userTM> tblUser;
    public PasswordField txtPassword;

    public void initialize(){
        loadAllUsers();
        ObservableList<MenuItem> items = menuRole.getItems();
        items.clear();
        MenuItem item1 = new MenuItem("USER");
        MenuItem item2 = new MenuItem("ADMIN");
        item1.setOnAction(event -> menuRole.setText("USER"));
        item2.setOnAction(event -> menuRole.setText("ADMIN"));
        items.add(item1);
        items.add(item2);
        disableControls(true);

        tblUser.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("username"));
        tblUser.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblUser.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("password"));
        tblUser.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("role"));
        tblUser.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("removeButton"));

    }

    private void disableControls(boolean b) {
        btnCreateRole.setDisable(b);
        btnUpdateRole.setDisable(b);
        txtUsername.setDisable(b);
        txtName.setDisable(b);
        txtPassword.setDisable(b);
        txtConfirmPassword.setDisable(b);
        menuRole.setDisable(b);
    }

    private void loadAllUsers() {
        Connection connection = DBConnection.getInstance().getConnection();
        tblUser.getItems().clear();
        try {
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery("SELECT * FROM user");
            while (rst.next()){
                ObservableList<userTM> items = tblUser.getItems();
                items.clear();
                Button button = new Button("Remove");
                button.setOnAction(event -> removeUserOnAction());

                items.add(new userTM(rst.getString("username"),rst.getString("name"),
                        rst.getString("password"),rst.getString("role"),button));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeUserOnAction() {

    }

    public void btnUpdateRoleOnAction(ActionEvent actionEvent) {
    }

    public void btnCreateRoleOnAction(ActionEvent actionEvent) {
    }

    public void btnAddNewUserOnAction(ActionEvent actionEvent) {
        disableControls(false);
        txtUsername.requestFocus();
    }
}
