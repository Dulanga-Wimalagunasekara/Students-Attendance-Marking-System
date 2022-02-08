package controller;

import db.DBConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;
import security.SecurityContextHolder;


import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class RecordAttendanceFormController {
    public TextField txtStudentID;
    public ImageView imgProfile;
    public Label lblDate;
    public Label lblID;
    public Label lblName;
    public Label lblStatus;
    public Label lblStudentName;
    public ToggleButton btnIn;
    public ToggleButton btnOut;
    private PreparedStatement stm;

    public void initialize() {

        lblDate.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %1$Tp", new Date()));
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            lblDate.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %1$Tp", new Date()));
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            stm = connection.prepareStatement("SELECT * FROM student WHERE id=?");
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Failed to connect with the database!", ButtonType.OK).show();
            e.printStackTrace();
            ((Stage) btnOut.getScene().getWindow()).close();
        }

        txtStudentID.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!txtStudentID.getText().isEmpty()) {
                if (!btnIn.isSelected() && !btnOut.isSelected()) {
                    txtStudentID.clear();
                    new Alert(Alert.AlertType.WARNING, "Please Select an Option(IN/OUT) to Continue.").show();
                }
            }

        });

        btnIn.selectedProperty().addListener(observable -> {
            txtStudentID.selectAll();
            if (btnIn.isSelected()) {
                btnIn.setStyle("-fx-background-color: lightblue");
            } else {
                btnIn.setStyle("-fx-background-color: green");
            }
            txtStudentID.requestFocus();

        });

        btnOut.selectedProperty().addListener(observable -> {
            txtStudentID.selectAll();
            if (btnOut.isSelected()) {
                btnOut.setStyle("-fx-background-color: lightblue");
            } else {
                btnOut.setStyle("-fx-background-color: red");
            }
            txtStudentID.requestFocus();
        });

        updateLabels();

    }

    public void txtStudentID_OnAction(ActionEvent actionEvent) {

        lblStudentName.setText("Please enter/scan the student ID to proceed");
        imgProfile.setImage(new Image("/view/assets/qr-code.png"));

        if (txtStudentID.getText().trim().isEmpty()) {
            return;
        }

        try {
            stm.setString(1, txtStudentID.getText().trim());
            ResultSet rst = stm.executeQuery();

            if (rst.next()) {
                lblStudentName.setText(rst.getString("name").toUpperCase());
                InputStream is = rst.getBlob("picture").getBinaryStream();
                imgProfile.setImage(new Image(is));
                insertIntoDatabase(rst.getString("name"),rst.getString("id"),rst.getString("grade"));
                txtStudentID.selectAll();
            } else {
                new Alert(Alert.AlertType.ERROR, "Invalid Student ID, Try again!", ButtonType.OK).show();
                txtStudentID.selectAll();
                txtStudentID.requestFocus();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.WARNING, "Something went wrong. Please try again!", ButtonType.OK).show();
            txtStudentID.selectAll();
            txtStudentID.requestFocus();
        }
    }

    private void sendSMS(String id,LocalDateTime dateTime) {

        new Thread(() -> {
            Connection connection = DBConnection.getInstance().getConnection();
            String message;
            String contact;
            try {
                connection.setAutoCommit(false);
                PreparedStatement stm1 = connection.prepareStatement("SELECT status FROM attendance WHERE student_id=? ORDER BY id DESC LIMIT 1");
                stm1.setString(1,id);
                ResultSet rstAttendance = stm1.executeQuery();
                PreparedStatement stm2 = connection.prepareStatement("SELECT name,contact FROM student WHERE id=?");
                stm2.setString(1,id);
                ResultSet rstStudent = stm2.executeQuery();
                connection.commit();
                if (rstStudent.next() && rstAttendance.next()){
                    if (btnIn.isSelected()){
                        message=rstStudent.getString("name")+" is attending to the class. AT: "+dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+" Status: "+rstAttendance.getString("status");
                    }else{
                        message=rstStudent.getString("name")+" is leaving the class. AT: "+dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+" Status: "+rstAttendance.getString("status");
                    }
                    contact=rstStudent.getString("contact");
                }else {
                    throw new RuntimeException("Something went wrong. Please try again.");
                }

            } catch (Throwable e) {
                e.printStackTrace();
                rollbackConnection(connection);
                return;
            }finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(message);
            System.out.println(contact);

            String content ="{\n\"message\":\""+message+"\",\n\"phoneNumber\":\""+contact+"\"\n}";
            System.out.println(content);

            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, content);
            Request request = new Request.Builder()
                    .url("https://api.smshub.lk/api/v1/send/single")
                    .method("POST", body)
                    .addHeader("Authorization", "TOKEN HERE")
                    .addHeader("Content-Type", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                System.out.println(response);
            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.WARNING,"SMS Sending Failed!. Please try again",ButtonType.OK).show();
            }
        }).start();
    }

    private void rollbackConnection(Connection connection) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }

    private void insertIntoDatabase(String name, String id, String grade) throws SQLException {

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm2 = connection.prepareStatement("INSERT INTO attendance (date, status, student_id, username,name,grade) VALUES (?,?,?,?,?,?)");
            LocalDateTime dateTime = LocalDateTime.now();
            stm2.setObject(1,dateTime);
            stm2.setString(2, btnIn.isSelected() ? "IN" : "OUT");
            stm2.setString(3,id);
            stm2.setString(4,SecurityContextHolder.getPrincipal().getUsername());
            stm2.setString(5,name);
            stm2.setString(6,grade);

            int i = stm2.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("Failed to execute the statement");
            }
            updateLabels();
//            sendSMS(txtStudentID.getText(),dateTime);

        } catch (Throwable e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to initialize the database!", ButtonType.OK).show();
        }
    }

    private void updateLabels() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM attendance ORDER BY id DESC LIMIT 1");
            ResultSet rst = stm.executeQuery();
            if (rst.next()){
                PreparedStatement stm2 = connection.prepareStatement("SELECT name FROM student WHERE id=?");
                stm2.setString(1,rst.getString("student_id"));
                ResultSet rst2 = stm2.executeQuery();
                rst2.next();
                lblID.setText("ID: "+rst.getString("student_id"));
                lblName.setText("Name: "+rst2.getString("name"));
                Object date = rst.getObject("date");
                LocalDateTime dateTime= (LocalDateTime) date;
                lblStatus.setText(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) +" - "+rst.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void RecOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)){
            ((Stage)btnIn.getScene().getWindow()).close();
        }
    }
}
