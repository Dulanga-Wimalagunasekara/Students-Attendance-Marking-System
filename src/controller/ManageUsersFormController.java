package controller;

import db.DBConnection;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import view.TM.userTM;


import java.sql.*;
import java.util.Optional;

public class ManageUsersFormController {
    public TextField txtUsername;
    public TextField txtName;
    public PasswordField txtConfirmPassword;
    public Button btnCreateRole;
    public MenuButton menuRole;
    public Button btnUpdateRole;
    public TableView<userTM> tblUser;
    public PasswordField txtPassword;
    public Button btnAddNewUser;

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

        tblUser.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue!=null){
                disableControls(true);
                try {
                    String password=decryptPassword(newValue);
                    txtUsername.setText(newValue.getUsername());
                    txtPassword.setText(password);
                    txtConfirmPassword.setText(password);
                    txtName.setText(newValue.getName());
                    menuRole.setText(newValue.getRole());
                    btnUpdateRole.setDisable(false);

                    newValue.setPassword(password);
                    tblUser.refresh();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (oldValue!=null){
                oldValue.setPassword("*********************");
                tblUser.refresh();
            }
        });

        txtPassword.setOnAction(event -> {
            txtConfirmPassword.clear();
        });

    }

    private String decryptPassword(userTM tm) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        PreparedStatement stm = connection.prepareStatement("SELECT password FROM user WHERE username=?");
        stm.setString(1,tm.getUsername());
        ResultSet rst = stm.executeQuery();
        rst.next();
        return rst.getString("password");
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
                Button button = new Button("Remove");
                userTM userTM = new userTM(rst.getString("username"), rst.getString("name"),
                        "*********************", rst.getString("role"), button);
                items.add(userTM);
                button.setOnAction(event -> {
                    try {
                        removeUserOnAction(userTM);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removeUserOnAction(userTM tm) throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        if (tm.getRole().equals("ADMIN")){
            PreparedStatement stm1 = connection.prepareStatement("SELECT role FROM user WHERE role='ADMIN'");
            ResultSet resultSet = stm1.executeQuery();
            int i=0;
            while (resultSet.next()){
                i++;
            }
            if (i==1){
                new Alert(Alert.AlertType.WARNING,"Can't Remove. At least one ADMIN should be existed!",ButtonType.OK).show();
                return;
            }
        }
        try {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM user WHERE username=?");
            stm.setString(1,tm.getUsername());
            int i = stm.executeUpdate();
            loadAllUsers();
            if (i!=1){
                throw new RuntimeException("Something went wrong! Please try again!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void btnUpdateRoleOnAction(ActionEvent actionEvent) {
        disableControls(false);
        btnUpdateRole.setDisable(true);
        txtUsername.setDisable(true);
        btnCreateRole.setText("Update");
    }

    public void btnCreateRoleOnAction(ActionEvent actionEvent) {
        if (isValidated()){
            if (btnCreateRole.getText().equals("Update")){
                userTM sItem = tblUser.getSelectionModel().getSelectedItem();
                sItem.setName(txtName.getText());
                sItem.setUsername(txtUsername.getText());
                sItem.setPassword(txtPassword.getText());
                updateDatabase(sItem);
                clearFields();
                btnCreateRole.setText("Create Role");
                disableControls(true);
                tblUser.getSelectionModel().clearSelection();
                btnAddNewUser.requestFocus();
                loadAllUsers();

            }else {
                Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to add a new User?",ButtonType.NO,ButtonType.YES).showAndWait();
                if (buttonType.get().equals(ButtonType.YES)){
                    try {
                        Connection connection = DBConnection.getInstance().getConnection();
                        PreparedStatement stm = connection.prepareStatement("INSERT INTO user (username, name, password, role) VALUES (?,?,?,?)");
                        stm.setString(1,txtUsername.getText());
                        stm.setString(2,txtName.getText());
                        stm.setString(3,txtPassword.getText());
                        stm.setString(4,menuRole.getText());
                        int i = stm.executeUpdate();
                        if (i!=1){
                            throw new RuntimeException("Failed to initialize the database");
                        }
                        loadAllUsers();
                        clearFields();
                    } catch (SQLException e) {
                        new Alert(Alert.AlertType.ERROR,"Something went wrong! Please try again!",ButtonType.OK).show();
                        e.printStackTrace();
                    }
                }

            }
        }

    }

    private void updateDatabase(userTM sItem) {
        String username = sItem.getUsername();
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("UPDATE user SET name=?,password=?,role=? WHERE username=?");
            stm.setString(1,txtName.getText());
            stm.setString(2,txtPassword.getText());
            stm.setString(3,menuRole.getText());
            stm.setString(4,username);
            int update = stm.executeUpdate();
            if (update!=1){
                throw new RuntimeException("Something went wrong! Please try again!");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void btnAddNewUserOnAction(ActionEvent actionEvent) {
        clearFields();
        btnCreateRole.setText("Create Role");
        tblUser.getSelectionModel().clearSelection();
        disableControls(false);
        txtUsername.requestFocus();
        btnUpdateRole.setDisable(true);
    }

    private void clearFields() {
        txtUsername.clear();
        txtName.clear();
        txtPassword.clear();
        txtConfirmPassword.clear();
        menuRole.setText("Select Role");
    }

    public boolean isValidated(){
        String name = txtName.getText().trim();
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String confirmPassword = txtConfirmPassword.getText().trim();

        if (username.length() <4) {
            new Alert(Alert.AlertType.ERROR, "Username should be at least 4 characters long").show();
            txtUsername.selectAll();
            txtUsername.requestFocus();
            return false;
        } else if (!name.matches("[A-Za-z ]+")) {
            new Alert(Alert.AlertType.ERROR, "Please enter a valid name!").show();
            txtName.selectAll();
            txtName.requestFocus();
            return false;
        } else if (!username.matches("[A-Za-z0-9]+")) {
            new Alert(Alert.AlertType.ERROR, "Username can contain only characters and digits").show();
            txtUsername.selectAll();
            txtUsername.requestFocus();
            return false;
        } else if (password.length() < 4) {
            new Alert(Alert.AlertType.ERROR, "Password should be at least 4 characters long").show();
            txtPassword.selectAll();
            txtPassword.requestFocus();
            return false;
        } else if (!password.equals(confirmPassword)) {
            new Alert(Alert.AlertType.ERROR, "Password mismatch").show();
            txtConfirmPassword.selectAll();
            txtConfirmPassword.requestFocus();
            return false;
        }else if(menuRole.getText().equals("Select a Role")){
            new Alert(Alert.AlertType.ERROR,"Please select a user role",ButtonType.OK).show();
            menuRole.requestFocus();
            return false;
        }else {
            return true;
        }

    }

    public void MngOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)){
            ((Stage)btnAddNewUser.getScene().getWindow()).close();
        }
    }
}
