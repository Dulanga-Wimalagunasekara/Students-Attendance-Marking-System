package controller;

import db.DBConnection;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import view.TM.studentTM;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Locale;
import java.util.Optional;

public class AddStudentsFormController {

    public TextField txtId;
    public ChoiceBox<String> choGrade;
    public TextField txtName;
    public TextField txtPicture;
    public Button btnBrowse;
    public TextField txtContact;
    public Button btnSaveStudent;
    public TableView<studentTM> tblStudents;
    public Button btnDeleteStudent;
    public Button btnNewStudent;
    public TextField txtSearch;
    private ObservableList<studentTM> items;

    public void initialize() {

        choGrade.getItems().addAll("06", "07", "08", "09", "10", "11");
        disableControls(true);
        btnNewStudent.requestFocus();

        choGrade.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                generateId(newValue);
            }
        });

        loadAllStudents();

        tblStudents.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("stId"));
        tblStudents.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblStudents.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("grade"));
        tblStudents.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("contact"));
        TableColumn<studentTM, ImageView> col = (TableColumn<studentTM, ImageView>) tblStudents.getColumns().get(4);

        col.setCellValueFactory(param -> {
            byte[] img = param.getValue().getImg();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(img);
            ImageView imageView = new ImageView(new Image(byteArrayInputStream));
            imageView.setFitHeight(75);
            imageView.setFitWidth(75);
            return new ReadOnlyObjectWrapper<>(imageView);
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                filterList();
            } else {
                tblStudents.setItems(items);
            }
        });

        tblStudents.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnDeleteStudent.setDisable(false);
            btnDeleteStudent.requestFocus();
        });

    }

    private void filterList() {
        FilteredList<studentTM> filteredList = new FilteredList<>(items);
        tblStudents.setItems(filteredList);
        String text = txtSearch.getText().toUpperCase(Locale.ROOT);

        filteredList.setPredicate(val -> val.getStId().toUpperCase(Locale.ROOT).contains(text) || String.valueOf(val.getGrade()).contains(text) ||
                val.getName().toUpperCase(Locale.ROOT).contains(text) || val.getContact().toUpperCase(Locale.ROOT).contains(text));
    }

    private void loadAllStudents() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            items = FXCollections.observableArrayList();
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM student");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                Blob picture = rst.getBlob("picture");
                byte[] bytes = picture.getBytes(1, (int) picture.length());
                items.add(new studentTM(rst.getString("id"), rst.getInt("grade"),
                        rst.getString("name"), bytes, rst.getString("contact")));
            }
            tblStudents.setItems(items);
            txtSearch.clear();
            System.gc();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void generateId(String value) {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT id FROM student WHERE grade=? ORDER BY id DESC LIMIT 1");
            stm.setInt(1, Integer.parseInt(value));
            ResultSet rst = stm.executeQuery();
            StringBuilder stb = new StringBuilder();
            if (rst.next()) {
                String[] ids = rst.getString("id").split("/");
                String id = ids[ids.length - 1];
                stb.append("2022/");
                stb.append(value);
                stb.append("/");
                stb.append(Integer.parseInt(id) + 1);
                txtId.setText(stb.toString());
            } else {
                stb.append("2022/");
                stb.append(value);
                stb.append("/");
                stb.append(1001);
                txtId.setText(stb.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void disableControls(boolean val) {
        txtId.setDisable(val);
        choGrade.setDisable(val);
        txtName.setDisable(val);
        txtPicture.setDisable(val);
        txtContact.setDisable(val);
        btnSaveStudent.setDisable(val);
        btnDeleteStudent.setDisable(val);
        btnBrowse.setDisable(val);
    }

    public void btnDeleteStudent_OnAction(ActionEvent actionEvent) {
        studentTM selectedItem = tblStudents.getSelectionModel().getSelectedItem();
        Connection connection = DBConnection.getInstance().getConnection();
        ObservableList<studentTM> items = tblStudents.getItems();
        try {
            PreparedStatement stm = connection.prepareStatement("DELETE FROM student WHERE id=?");
            stm.setString(1,selectedItem.getStId());
            Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the student",ButtonType.YES,ButtonType.NO).showAndWait();
            if (buttonType.get().equals(ButtonType.YES)){
                int i = stm.executeUpdate();
                if (i!=1){
                    throw new RuntimeException("Something went wrong! Please try again!");
                }else {
                    new Alert(Alert.AlertType.CONFIRMATION,"Deleted Successfully!",ButtonType.OK).show();
                    loadAllStudents();
                }
            }

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Something went wrong! Please try again!",ButtonType.OK).show();
            e.printStackTrace();
        }
        tblStudents.getSelectionModel().clearSelection();
        btnDeleteStudent.setDisable(true);
    }

    public void btnSaveStudent_OnAction(ActionEvent actionEvent) {
        if (Isvalidated()) {
            String value = choGrade.getSelectionModel().getSelectedItem();
            Connection connection = DBConnection.getInstance().getConnection();
            Path path = Paths.get(txtPicture.getText());
            try {
                byte[] bytes = Files.readAllBytes(path);
                PreparedStatement stm = connection.prepareStatement("INSERT INTO student (id, name, picture,contact,grade) VALUES (?,?,?,?,?)");
                stm.setString(1, txtId.getText());
                stm.setString(2, txtName.getText());
                stm.setBlob(3, new SerialBlob(bytes));
                stm.setString(4, txtContact.getText());
                stm.setInt(5, Integer.parseInt(value));
                int i = stm.executeUpdate();
                if (i == 1) {
                    new Alert(Alert.AlertType.CONFIRMATION, "Added Successfully!", ButtonType.OK).show();
                }

                items.add(new studentTM(txtId.getText(), Integer.parseInt(choGrade.getValue()),
                        txtName.getText(), bytes, txtContact.getText()));
                tblStudents.refresh();

            } catch (SQLException | IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Something went wrong! Please try again!").show();
            }
            clearFields();
            choGrade.setValue(value);
            btnNewStudent.setDisable(false);
            txtName.requestFocus();

        }

    }

    private void clearFields() {
        choGrade.getSelectionModel().clearSelection();
        txtId.clear();
        txtName.clear();
        txtPicture.clear();
        txtContact.clear();
    }

    private boolean Isvalidated() {
        if (txtId.getText().isEmpty()) {
            return false;
        } else if (choGrade.getSelectionModel().getSelectedItem().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please select a grade!", ButtonType.OK).show();
            choGrade.requestFocus();
            return false;
        } else if (txtName.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please Enter a valid name!", ButtonType.OK).show();
            txtName.requestFocus();
            return false;
        } else if (txtPicture.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please select an image!", ButtonType.OK).show();
            btnBrowse.requestFocus();
            return false;
        } else if (!txtContact.getText().matches("\\d{10}") || txtContact.getText().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please Enter a valid contact number!", ButtonType.OK).show();
            txtContact.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    public void btnBrowse_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images",
                "*.jpg", "*.png"));
        File file = fileChooser.showOpenDialog(btnNewStudent.getScene().getWindow());
        txtPicture.setText(file.getAbsolutePath());
    }

    public void btnNewStudent_OnAction(ActionEvent actionEvent) {
        disableControls(false);
        btnDeleteStudent.setDisable(true);
        btnNewStudent.setDisable(true);
        choGrade.requestFocus();
        txtSearch.clear();
    }

    public void AddNewOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            ((Stage) btnNewStudent.getScene().getWindow()).close();
        }
    }
}
