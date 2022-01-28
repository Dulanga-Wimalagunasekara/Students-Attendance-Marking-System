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

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class RecordAttendanceFormController {
    public TextField txtStudentID;
    public ImageView imgProfile;
    public Button btnIn;
    public Button btnOut;
    public Label lblDate;
    public Label lblID;
    public Label lblName;
    public Label lblStatus;
    public Label lblStudentName;
    private PreparedStatement stm;

    public void initialize(){
        btnIn.setDisable(true);
        btnOut.setDisable(true);

        Timeline timeline = new Timeline(new KeyFrame(Duration.INDEFINITE, event -> {
            lblDate.setText(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %1$Tp",new Date()));
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        Connection connection = DBConnection.getInstance().getConnection();
        try {
            stm = connection.prepareStatement("SELECT * FROM student WHERE id=?");
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,"Failed to connect with the database! Please contact Dulanga.", ButtonType.OK).show();
            e.printStackTrace();
            ((Stage)btnOut.getScene().getWindow()).close();
        }


    }

    public void txtStudentID_OnAction(ActionEvent actionEvent) {
        btnIn.setDisable(true);
        btnOut.setDisable(true);
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
                btnIn.setDisable(false);
                btnOut.setDisable(false);
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

    public void btnIn_OnAction(ActionEvent actionEvent) {

    }

    public void btnOut_OnAction(ActionEvent actionEvent) {

    }
}
