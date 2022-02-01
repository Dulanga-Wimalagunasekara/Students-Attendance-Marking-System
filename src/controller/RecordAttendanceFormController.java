package controller;

import db.DBConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import okhttp3.*;
import security.SecurityContextHolder;
import sun.net.www.http.HttpClient;


import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Connection;
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

    public void initialize(){

        lblDate.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %1$Tp",new Date()));
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            lblDate.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %1$Tp",new Date()));
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        Connection connection = DBConnection.getInstance().getConnection();

        try {
            stm = connection.prepareStatement("SELECT * FROM student WHERE id=?");
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to connect with the database!", ButtonType.OK).show();
            e.printStackTrace();
            ((Stage)btnOut.getScene().getWindow()).close();
        }

        txtStudentID.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!txtStudentID.getText().isEmpty()){
                if (!btnIn.isSelected() && !btnOut.isSelected()){
                    txtStudentID.clear();
                    new Alert(Alert.AlertType.WARNING,"Please Select an Option(IN/OUT) to Continue.").show();
                }
            }

        });

        btnIn.selectedProperty().addListener(observable -> {
            if (btnIn.isSelected()){
                btnIn.setStyle("-fx-background-color: lightblue");
            }else {
                btnIn.setStyle("-fx-background-color: green");
            }
            txtStudentID.requestFocus();

        });

        btnOut.selectedProperty().addListener(observable -> {
            if (btnOut.isSelected()){
                btnOut.setStyle("-fx-background-color: lightblue");
            }else {
                btnOut.setStyle("-fx-background-color: red");
            }
            txtStudentID.requestFocus();
        });

    }

    public void txtStudentID_OnAction(ActionEvent actionEvent) {

        lblStudentName.setText("Please enter/scan the student ID to proceed");
        imgProfile.setImage(new Image("/view/assets/qr-code.png"));

        if (txtStudentID.getText().trim().isEmpty()){
            return;
        }

        try {
            stm.setString(1, txtStudentID.getText().trim());
            ResultSet rst = stm.executeQuery();

            if (rst.next()){
                lblStudentName.setText(rst.getString("name").toUpperCase());
                InputStream is = rst.getBlob("picture").getBinaryStream();
                imgProfile.setImage(new Image(is));
                sendSMS(txtStudentID.getText());
                insertIntoDatabase();
                txtStudentID.selectAll();
            }else{
                new Alert(Alert.AlertType.ERROR, "Invalid Student ID, Try again!", ButtonType.OK).show();
                txtStudentID.selectAll();
                txtStudentID.requestFocus();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.WARNING, "Something went wrong. Please try again!",ButtonType.OK).show();
            txtStudentID.selectAll();
            txtStudentID.requestFocus();
        }
    }

    private void sendSMS(String id) {

        /*Todo: Edit the sms with student details*/
        new Thread(() -> {
            System.out.println("getting into the initialize method");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType,"{\n\"message\":\"Dulanga is attending to the class...!\",\n\"phoneNumber\":\"0712742787\"\n}");
            Request request = new Request.Builder()
                    .url("https://api.smshub.lk/api/v1/send/single")
                    .method("POST", body)
                    .addHeader("Authorization", "TOKEN IS HERE")
                    .addHeader("Content-Type", "application/json")
                    .build();
            try {
                Response response = client.newCall(request).execute();
            } catch (Throwable e) {
                new Alert(Alert.AlertType.ERROR,"Failed to send the sms. Please try again!",ButtonType.OK).show();
                e.printStackTrace();
            }
        }).start();
    }

    private void insertIntoDatabase() throws SQLException {
        Connection connection = DBConnection.getInstance().getConnection();
        try {
                connection.setAutoCommit(false);
                PreparedStatement stm2 = connection.prepareStatement("INSERT INTO attendance (date, status, student_id, username) " +
                        "VALUES (date=?,status=?,student_id=?,username=?)");
                Date date = new Date();
                stm2.setTime(1,new Time(date.getTime()));
                stm2.setString(2,btnIn.isSelected()? "IN":"OUT");
                System.out.println(txtStudentID.getText());
                stm2.setString(3,txtStudentID.getText());
                System.out.println(SecurityContextHolder.getPrincipal().getName());
                stm2.setString(4,SecurityContextHolder.getPrincipal().getName());
                int i = stm2.executeUpdate();
                connection.setAutoCommit(true);

                if (i!=1){
                    throw new RuntimeException("Failed to execute the statement");
                }

        } catch (Throwable e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR,"Failed to initialize the database!",ButtonType.OK).show();
            connection.rollback();
        }
    }

}
