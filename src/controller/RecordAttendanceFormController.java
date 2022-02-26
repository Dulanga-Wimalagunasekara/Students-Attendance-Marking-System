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
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import security.SecurityContextHolder;


import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.sql.*;
import java.sql.Connection;
import java.time.LocalDate;
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
    public ToggleButton btnAutoMode;
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
                if (!btnIn.isSelected() && !btnOut.isSelected() && !btnAutoMode.isSelected()) {
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

        btnAutoMode.selectedProperty().addListener(observable -> {
            txtStudentID.selectAll();
            if (btnAutoMode.isSelected()) {
                btnAutoMode.setStyle("-fx-background-color: lightblue");
            } else {
                btnAutoMode.setStyle("-fx-background-color:  #5291ff");
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
                insertIntoDatabase(rst.getString("name"), rst.getString("id"), rst.getString("grade"));
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

    private void sendSMS(String id, LocalDateTime dateTime) {

        new Thread(() -> {
            Connection connection = DBConnection.getInstance().getConnection();
            String message;
            String contact;
            try {
                connection.setAutoCommit(false);
                PreparedStatement stm1 = connection.prepareStatement("SELECT status FROM attendance WHERE student_id=? ORDER BY id DESC LIMIT 1");
                stm1.setString(1, id);
                ResultSet rstAttendance = stm1.executeQuery();
                PreparedStatement stm2 = connection.prepareStatement("SELECT name,contact FROM student WHERE id=?");
                stm2.setString(1, id);
                ResultSet rstStudent = stm2.executeQuery();
                connection.commit();
                if (rstStudent.next() && rstAttendance.next()) {
                    if (rstAttendance.getString("status").equals("IN")) {
                        message = rstStudent.getString("name") + " is attending to the Maths class. AT: " + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " Status: " + rstAttendance.getString("status");
                    } else {
                        message = rstStudent.getString("name") + " is leaving the Maths class. AT: " + dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " Status: " + rstAttendance.getString("status");
                    }
                    contact = rstStudent.getString("contact");
                } else {
                    throw new RuntimeException("Something went wrong. Please try again.");
                }

            } catch (Throwable e) {
                e.printStackTrace();
                rollbackConnection(connection);
                return;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            String content = "{\n\"message\":\"" + message + "\",\n\"phoneNumber\":\"" + contact + "\"\n}";

            try {
                URL url = new URL("https://api.smshub.lk/api/v2/send/single");
                HttpURLConnection connection1 = (HttpURLConnection) url.openConnection();
                connection1.setRequestMethod("POST");
                connection1.setRequestProperty("Content-Type","application/json");
                connection1.setRequestProperty("Authorization","Token Here");

                connection1.setDoOutput(true);
                connection1.getOutputStream().write(content.getBytes());
                connection1.getOutputStream().close();
                int response = connection1.getResponseCode();
                System.out.println(response);
            } catch (IOException e) {
                e.printStackTrace();
                new Alert(Alert.AlertType.WARNING, "SMS Sending Failed!. Please try again", ButtonType.OK).show();
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
            String status = checkStatus(id);
            PreparedStatement stm2 = connection.prepareStatement("INSERT INTO attendance (date, status, student_id, username,name,grade) VALUES (?,?,?,?,?,?)");
            LocalDateTime dateTime = LocalDateTime.now();
            stm2.setObject(1, dateTime);
            stm2.setString(2, status);
            stm2.setString(3, id);
            stm2.setString(4, SecurityContextHolder.getPrincipal().getUsername());
            stm2.setString(5, name);
            stm2.setString(6, grade);

            int i = stm2.executeUpdate();
            if (i != 1) {
                throw new RuntimeException("Failed to execute the statement");
            }
            updateLabels();
            sendSMS(txtStudentID.getText(),dateTime);
            Media media = new Media(this.getClass().getResource("/assets/Barcode-scanner-beep-sound.mp3").toURI().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(1);
            mediaPlayer.play();
        } catch (Throwable e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to initialize the database!", ButtonType.OK).show();
        }
    }

    private String checkStatus(String id) {
        String format = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (btnIn.isSelected()) {
            return "IN";
        } else if (btnOut.isSelected()) {
            return "OUT";
        } else {
            Connection connection = DBConnection.getInstance().getConnection();
            try {
                PreparedStatement stm = connection.prepareStatement("SELECT status FROM attendance WHERE student_id=? AND date BETWEEN ? AND ? ORDER BY id DESC LIMIT 1");
                stm.setString(1, id);
                stm.setString(2, format + " 00:00:00");
                stm.setString(3, format + " 23:59:59");
                ResultSet resultSet = stm.executeQuery();
                if (resultSet.next()) {
                    if (resultSet.getString("status").equals("IN")) {
                        return "OUT";
                    } else {
                        return "IN";
                    }
                } else {
                    return "IN";
                }
            } catch (Throwable e) {
                new Alert(Alert.AlertType.ERROR, "Something went wrong with Auto Mode!", ButtonType.OK).show();
                e.printStackTrace();
                return null;
            }
        }
    }

    private void updateLabels() {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
            PreparedStatement stm = connection.prepareStatement("SELECT * FROM attendance ORDER BY id DESC LIMIT 1");
            ResultSet rst = stm.executeQuery();
            if (rst.next()) {
                PreparedStatement stm2 = connection.prepareStatement("SELECT name FROM student WHERE id=?");
                stm2.setString(1, rst.getString("student_id"));
                ResultSet rst2 = stm2.executeQuery();
                rst2.next();
                lblID.setText("ID: " + rst.getString("student_id"));
                lblName.setText("Name: " + rst2.getString("name"));
                Object date = rst.getObject("date");
                LocalDateTime dateTime = (LocalDateTime) date;
                lblStatus.setText(dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - " + rst.getString("status"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void RecOnKeyPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ESCAPE)) {
            ((Stage) btnIn.getScene().getWindow()).close();
        }
    }
}
