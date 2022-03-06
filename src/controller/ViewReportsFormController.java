package controller;

import db.DBConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import view.TM.attendanceTM;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class ViewReportsFormController {
    public TableView<attendanceTM> tblAttendance;
    public TextField txtSearch;
    public ChoiceBox<String> choBox;
    public Button btnFilter;
    public Button btnBackup;
    ObservableList<attendanceTM> items;

    public void initialize() {
        tblAttendance.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("order"));
        tblAttendance.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
        tblAttendance.getColumns().get(2).setCellValueFactory(new PropertyValueFactory<>("name"));
        tblAttendance.getColumns().get(3).setCellValueFactory(new PropertyValueFactory<>("grade"));
        tblAttendance.getColumns().get(4).setCellValueFactory(new PropertyValueFactory<>("status"));
        tblAttendance.getColumns().get(5).setCellValueFactory(new PropertyValueFactory<>("studentId"));
        tblAttendance.getColumns().get(6).setCellValueFactory(new PropertyValueFactory<>("operator"));

        loadAllAttendance();

        items = tblAttendance.getItems();
        txtSearch.textProperty().addListener(observable -> {
            if (!txtSearch.getText().isEmpty()) {
                searchProperty(items);
            } else {
                tblAttendance.setItems(items);
                tblAttendance.refresh();
            }

        });
        choBox.getItems().addAll("Date", "Name", "Grade", "Status", "Student ID", "Operator", "All");
        choBox.setValue("All");

    }

    private void searchProperty(ObservableList<attendanceTM> items) {
        FilteredList<attendanceTM> filteredList = new FilteredList<attendanceTM>(items);
        tblAttendance.setItems(filteredList);
        String s = txtSearch.getText().toUpperCase();
        switch (choBox.getValue()) {
            case "Date":
                filteredList.setPredicate(val -> val.getDate().toString().contains(s));
                break;

            case "Name":
                filteredList.setPredicate(val -> val.getName().toUpperCase().contains(s));
                break;

            case "Grade":
                filteredList.setPredicate(val -> val.getGrade() == Integer.parseInt(s));
                break;

            case "Status":
                filteredList.setPredicate(val -> val.getStatus().toUpperCase().contains(s));
                break;

            case "Student ID":
                filteredList.setPredicate(val -> val.getStudentId().toUpperCase().contains(s));
                break;

            case "Operator":
                filteredList.setPredicate(val -> val.getOperator().toUpperCase().contains(s));
                break;

            case "All":
                filteredList.setPredicate(val -> (val.getDate().toString().contains(s) |
                        val.getName().toUpperCase().contains(s) | String.valueOf(val.getGrade()).toUpperCase().contains(s) |
                        val.getStatus().toUpperCase().contains(s) | val.getStudentId().toUpperCase().contains(s) |
                        val.getOperator().toUpperCase().contains(s)));
                break;

        }

    }

    private void loadAllAttendance() {
        ObservableList<attendanceTM> itemsList = tblAttendance.getItems();
        itemsList.clear();
        Connection connection = DBConnection.getInstance().getConnection();

        try {
            String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM attendance WHERE date BETWEEN ? AND ? ORDER BY date DESC");
            stm.setString(1, format + " 00:00:00");
            stm.setString(2, format + " 23:59:59");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                itemsList.add(new attendanceTM(rst.getInt("id"), (LocalDateTime) rst.getObject("date"),
                        rst.getString("name"), rst.getInt("grade"), rst.getString("status"),
                        rst.getString("student_id"), rst.getString("username")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void btnFilterOnAction(ActionEvent actionEvent) throws IOException {
        SimpleObjectProperty<LocalDate> dateStart = new SimpleObjectProperty<>();
        SimpleObjectProperty<LocalDate> dateEnd = new SimpleObjectProperty<>();

        txtSearch.clear();
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/filterForm.fxml"));
        AnchorPane load = fxmlLoader.load();
        stage.setScene(new Scene(load));
        FilterFormController controller = fxmlLoader.getController();
        controller.initDates(dateStart, dateEnd);
        stage.setResizable(false);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.showAndWait();

        if (dateStart.getValue() != null & dateEnd.getValue() != null) {
            selectFilters(dateStart.getValue(), dateEnd.getValue());
        }

    }

    private void selectFilters(LocalDate dateStart, LocalDate dateEnd){
        ObservableList<attendanceTM> tblItems = this.items;
        tblItems.clear();
        String formatStart = dateStart.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String formatEnd = dateEnd.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM attendance WHERE date BETWEEN ? AND ? ORDER BY date DESC ");
            stm.setString(1,formatStart+" 00:00:00");
            stm.setString(2,formatEnd+" 23:59:59");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                tblItems.add(new attendanceTM(rst.getInt("id"), (LocalDateTime) rst.getObject("date"),
                        rst.getString("name"), rst.getInt("grade"), rst.getString("status"),
                        rst.getString("student_id"), rst.getString("username")));
            }

        } catch (Throwable e) {
            new Alert(Alert.AlertType.ERROR,"Something went wrong! Please try again!").show();
            e.printStackTrace();
        }
    }

    public void btnBackupOnAction(ActionEvent actionEvent) {
        ObservableList<attendanceTM> tblItems =tblAttendance.getItems();

        XSSFWorkbook book = new XSSFWorkbook();
        XSSFSheet sheet = book.createSheet();

        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i <tblAttendance.getColumns().size(); i++) {
            row.createCell(i).setCellValue(tblAttendance.getColumns().get(i).getText());
        }
        for (int i = 0; i <tblItems.size(); i++) {
            row=sheet.createRow(i+1);
            for (int j = 0; j < tblAttendance.getColumns().size(); j++) {
                row.createCell(j).setCellValue(tblAttendance.getColumns().get(j).getCellData(i).toString());
            }
        }

        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(btnFilter.getScene().getWindow());
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
    }

    public void viewRepbtnOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)){
            ((Stage)btnFilter.getScene().getWindow()).close();
        }
    }
}
