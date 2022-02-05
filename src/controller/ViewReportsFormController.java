package controller;

import db.DBConnection;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import view.TM.attendanceTM;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ViewReportsFormController {
    public TableView<attendanceTM> tblAttendance;
    public TextField txtSearch;
    public ChoiceBox<String> choBox;
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
            if (!txtSearch.getText().isEmpty()){
                searchProperty(items);
            }else {
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
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM attendance");
            ResultSet rst = stm.executeQuery();
            while (rst.next()) {
                itemsList.add(new attendanceTM(rst.getInt("id"), rst.getObject("date"),
                        rst.getString("name"), rst.getInt("grade"), rst.getString("status"),
                        rst.getString("student_id"), rst.getString("username")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
