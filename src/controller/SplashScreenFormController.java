package controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class SplashScreenFormController {
    public Label lblStatus;
    private SimpleObjectProperty<File> fileProperty = new SimpleObjectProperty<>();

    public void initialize() {
        establishConnection();


    }

    public void establishConnection() {
        lblStatus.setText("Establishing Connection....");
        new Thread(() -> {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_students", "root", "root");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                if (e.getSQLState().equals("42000")) {
                    Platform.runLater(this::loadImportDBForm);

                } else {

                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void loadImportDBForm() {

        try {

            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/ImportDBForm.fxml"));
            AnchorPane load = fxmlLoader.load();
            ImportDBFormController controller = fxmlLoader.getController();
            controller.initFileProperty(fileProperty);

            Stage stage = new Stage();
            Scene scene = new Scene(load);
            stage.setScene(scene);
            stage.sizeToScene();
            stage.setTitle("Student Attendance System: First Time Boot");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(lblStatus.getScene().getWindow());
            stage.centerOnScreen();
            stage.showAndWait();

            if (fileProperty.getValue()==null) {
                lblStatus.setText("Creating a new Database..");
                new Thread(() -> {
                    try {
                        sleep(100);
                        Platform.runLater(() -> {
                            lblStatus.setText("Loading DB Script");
                        });

                        InputStream is = this.getClass().getResourceAsStream("assets/db-script.sql");
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);
                        String script= new String(buffer);
                        sleep(100);
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306?allowMultiQueries=true", "root", "root");
                        Platform.runLater(() -> {
                            lblStatus.setText("Execute Database Script...");
                        });
                        Statement stm = connection.createStatement();
                        stm.execute(script);
                        connection.close();
                        sleep(100);

                        Platform.runLater(() -> {
                            lblStatus.setText("Obtaining a new DB Connection...");
                        });
                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_students", "root", "root");
                        sleep(100);

                    } catch (IOException | SQLException e) {
                        e.printStackTrace();
                    }


                }).start();

            } else {
                /*Todo:Restore the backup*/
                System.out.println("Restoring...!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sleep(long mills){
        try {
            Thread.sleep(mills);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}


