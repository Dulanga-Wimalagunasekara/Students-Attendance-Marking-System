package controller;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import db.DBConnection;

import javafx.application.Platform;
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
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import view.TM.studentTM;

import javax.sql.rowset.serial.SerialBlob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

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
    public Button btnBackup;
    private ObservableList<studentTM> items;

    public void initialize() throws IOException {
        Path path = Paths.get(System.getProperty("user.home"),"Documents/QR_Codes");
        if (!Files.isDirectory(path)){
            Files.createDirectory(path);
        }
        choGrade.getItems().addAll("06", "07", "08", "09", "10", "11","12");
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
//            imageView.setRotate(90);
            return new ReadOnlyObjectWrapper<>(imageView);
        });

        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!tblStudents.getSelectionModel().isEmpty()){
                clearFields();
                tblStudents.getSelectionModel().clearSelection();
            }
            if (newValue != null) {
                filterList();
                txtSearch.requestFocus();
            } else {
                tblStudents.setItems(items);
            }
        });

        tblStudents.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue!=null){
                clearFields();
                disableControls(true);
                btnSaveStudent.setText("Update");
                btnSaveStudent.setDisable(false);
                txtContact.setText(newValue.getContact());
                txtContact.setDisable(false);
                txtName.setText(newValue.getName());
                txtName.setDisable(false);
                txtPicture.setText("[PICTURE]");
                txtPicture.setDisable(false);
                btnBrowse.setDisable(false);
                choGrade.setValue(String.valueOf(newValue.getGrade()));
                txtId.setText(newValue.getStId());
                btnDeleteStudent.setDisable(false);
                btnDeleteStudent.requestFocus();
            }else{
                btnSaveStudent.setText("Save Student");
                disableControls(true);
            }
        });

        tblStudents.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue==null){
                tblStudents.getSelectionModel().clearSelection();
            }
        });

    }

    private void filterList() {
        new Thread(() -> {
            FilteredList<studentTM> filteredList = new FilteredList<>(items);
            tblStudents.setItems(filteredList);
            String text = txtSearch.getText().toUpperCase(Locale.ROOT);

            filteredList.setPredicate(val -> val.getStId().toUpperCase(Locale.ROOT).contains(text) || String.valueOf(val.getGrade()).contains(text) ||
                    val.getName().toUpperCase(Locale.ROOT).contains(text) || val.getContact().toUpperCase(Locale.ROOT).contains(text));
        }).start();
    }

    private void loadAllStudents() {
        new Thread(() -> {
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
        }).start();
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

    public void btnDeleteStudent_OnAction(ActionEvent actionEvent){
            studentTM selectedItem = tblStudents.getSelectionModel().getSelectedItem();
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                connection.setAutoCommit(false);
                PreparedStatement stm1 = connection.prepareStatement("DELETE FROM attendance WHERE student_id=?");
                stm1.setString(1,selectedItem.getStId());
                PreparedStatement stm = connection.prepareStatement("DELETE FROM student WHERE id=?");
                stm.setString(1,selectedItem.getStId());
                Optional<ButtonType> buttonType = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete the student",ButtonType.YES,ButtonType.NO).showAndWait();
                if (buttonType.get().equals(ButtonType.YES)){
                    stm1.executeUpdate();
                    int i = stm.executeUpdate();
                    if (i!=1){
                        throw new RuntimeException("Something went wrong! Please try again!");
                    }else {
                        connection.commit();
                        new Alert(Alert.AlertType.CONFIRMATION,"Deleted Successfully!",ButtonType.OK).show();
                        loadAllStudents();
                    }
                }

            } catch (Throwable e) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                new Alert(Alert.AlertType.ERROR,"Something went wrong! Please try again!",ButtonType.OK).show();
                e.printStackTrace();
            }finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                tblStudents.getSelectionModel().clearSelection();
                clearFields();
                disableControls(true);
                btnNewStudent.setDisable(false);
            }
    }

    public void btnSaveStudent_OnAction(ActionEvent actionEvent) {
            if (btnSaveStudent.getText().equals("Save Student")){
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
                            showAltert(Alert.AlertType.CONFIRMATION,"Added Successfully",ButtonType.OK);
                        }

                        items.add(new studentTM(txtId.getText(), Integer.parseInt(choGrade.getValue()),
                                    txtName.getText(), bytes, txtContact.getText()));
                        tblStudents.refresh();
                        String content=txtId.getText();
                        Path path1 = Paths.get(System.getProperty("user.home"),"Documents/QR_Codes",txtId.getText().replace("/","_")+" - "+txtName.getText() + ".png");
                        String pathQr = path1.toString();
                        String charset="UTF-8";
                        Map<EncodeHintType, ErrorCorrectionLevel> hashMap = new HashMap<EncodeHintType, ErrorCorrectionLevel>();
                        hashMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
                        generateQr(content,pathQr,charset,hashMap,250,250);

                    } catch (SQLException | IOException e) {
                        e.printStackTrace();
                        showAltert(Alert.AlertType.ERROR,"Something went wrong! Please try again!",ButtonType.OK);
                    }
                        clearFields();
                        choGrade.setValue(value);
                        btnNewStudent.setDisable(false);
                        txtName.requestFocus();
                }
            }else {
                if (Isvalidated()){
                    try {
                        String sql;
                        byte[] bytes=null;
                        if (txtPicture.getText().equals("[PICTURE]")){
                            sql="UPDATE student SET name=?,contact=? WHERE id=?";
                        }else {
                            Path path = Paths.get(txtPicture.getText());
                            bytes = Files.readAllBytes(path);
                            sql="UPDATE student SET name=?,contact=?,picture=? WHERE id=?";
                        }
                        Connection connection = DBConnection.getInstance().getConnection();
                        PreparedStatement stm = connection.prepareStatement(sql);
                        stm.setString(1,txtName.getText());
                        stm.setString(2,txtContact.getText());
                        if (!txtPicture.getText().equals("[PICTURE]") && bytes!=null){
                            stm.setBlob(3,new SerialBlob(bytes));
                            stm.setString(4,txtId.getText());
                        }else {
                            stm.setString(3,txtId.getText());
                        }
                        int i = stm.executeUpdate();
                        if (i!=1){
                            throw new RuntimeException("Update Failed!");
                        }else {
                            showAltert(Alert.AlertType.CONFIRMATION,"Updated Successfully!",ButtonType.OK);
                        }
                    } catch (Throwable e) {
                        showAltert(Alert.AlertType.ERROR,"Update Failed! Please try again!",ButtonType.OK);
                        e.printStackTrace();
                    }finally {
                            tblStudents.getSelectionModel().clearSelection();
                            clearFields();
                            loadAllStudents();
                    }
                }
            }

    }

    private void showAltert(Alert.AlertType type,String Content,ButtonType btnType) {
            new Alert(type,Content,btnType).show();
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

    private File recentDirectory = new File(System.getProperty("user.home"),"Documents");

    public void btnBrowse_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images",
                "*.jpg", "*.png"));
        fileChooser.setInitialDirectory(recentDirectory);
        File file = fileChooser.showOpenDialog(btnNewStudent.getScene().getWindow());
        if (file!=null){
            recentDirectory = file.getParentFile();
            txtPicture.setText(file.getAbsolutePath());
        }
    }

    public void btnNewStudent_OnAction(ActionEvent actionEvent) {
        tblStudents.getSelectionModel().clearSelection();
        clearFields();
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

    public void generateQr(String data, String path, String charset, Map map, int h, int w){
        new Thread(() -> {
            try{
                BitMatrix matrix = new MultiFormatWriter().encode(new String(data.getBytes(charset), charset), BarcodeFormat.QR_CODE, w, h);
                MatrixToImageWriter.writeToFile(matrix, path.substring(path.lastIndexOf('.') + 1), new File(path));
            }catch (Throwable e){
                e.printStackTrace();
            }
        }).start();
    }

    public void btnBackupOnAction(ActionEvent actionEvent) {
            try {
                Connection connection = DBConnection.getInstance().getConnection();
                PreparedStatement stm = connection.prepareStatement("SELECT id,name,grade,contact FROM student");
                ResultSet rst = stm.executeQuery();

                XSSFWorkbook book = new XSSFWorkbook();
                XSSFSheet sheet = book.createSheet();
                XSSFRow row = sheet.createRow(0);

                row.createCell(0).setCellValue("ID");
                row.createCell(1).setCellValue("Name");
                row.createCell(2).setCellValue("Grade");
                row.createCell(3).setCellValue("Contact");
                int rowCount=1;
                while (rst.next()){
                    row=sheet.createRow(rowCount);
                    for (int i = 0; i < 3; i++) {
                        row.createCell(i).setCellValue(rst.getString(i+1));
                    }
                    row.createCell(3).setCellValue(rst.getInt("contact"));
                    rowCount++;
                }
                    FileChooser fileChooser = new FileChooser();
                    File file = fileChooser.showSaveDialog(btnNewStudent.getScene().getWindow());
                    if (file!=null){
                        Path path = Paths.get(file.getAbsolutePath()+".xlsx");
                        try {
                            OutputStream outputStream = Files.newOutputStream(path);
                            book.write(outputStream);
                            outputStream.close();
                            new Alert(Alert.AlertType.CONFIRMATION,"Backup Success!",ButtonType.OK).show();
                        } catch (IOException e) {
                            new Alert(Alert.AlertType.ERROR,"Something went wrong! Please try again!",ButtonType.OK).show();
                            e.printStackTrace();
                        }
                    }
            } catch (SQLException e) {
                e.printStackTrace();
            }

    }
}
