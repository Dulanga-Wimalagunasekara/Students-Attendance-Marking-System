package controller;

import db.DBConnection;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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
                Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_student_attendance", "root", "root");
                Platform.runLater(() -> {
                    LoadLoginForm(connection);
                });
            } catch (ClassNotFoundException e) {
                shutdownApp(e);
                e.printStackTrace();
            } catch (SQLException e) {
                if (e.getSQLState().equals("42000")) {
                    Platform.runLater(this::loadImportDBForm);

                } else {
                    shutdownApp(e);
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
            stage.setOnCloseRequest(Event::consume);

            stage.showAndWait();

            if (fileProperty.getValue()==null) {
                lblStatus.setText("Creating a new Database..");
                new Thread(() -> {
                    try {
                        sleep(500);
                        Platform.runLater(() -> {
                            lblStatus.setText("Loading DB Script");
                        });

                        InputStream is = this.getClass().getResourceAsStream("/assets/db-script.sql");
                        byte[] buffer = new byte[is.available()];
                        is.read(buffer);
                        String script= new String(buffer);
                        sleep(500);
                        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306?allowMultiQueries=true", "root", "root");
                        Platform.runLater(() -> {
                            lblStatus.setText("Execute Database Script...");
                        });
                        Statement stm = connection.createStatement();
                        connection.setAutoCommit(false);
                        stm.execute(script);
                        connection.setAutoCommit(true);
                        connection.close();
                        sleep(500);

                        connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dep8_student_attendance", "root", "root");
                        sleep(500);
                        DBConnection.getInstance().init(connection);

                        Platform.runLater(() -> {
                            lblStatus.setText("Obtaining a new DB Connection...");
                            loadCreateAdminForm();
                        });

                    } catch (IOException | SQLException e) {
                        dropDatabase();
                        shutdownApp(e);
                    }


                }).start();

            } else {
                /*Todo:Restore the backup and Handle the Exceptions*/
                System.out.println("Restoring...!");
//                LoadLoginForm(connection);
            }

        } catch (IOException e) {
            shutdownApp(e);
        }
    }

    public void LoadLoginForm(Connection connection){
        /* Let's store the connection first */
        DBConnection.getInstance().init(connection);

        /* Let's redirect to log in form */
        try{
            Stage stage = new Stage();
            AnchorPane root = FXMLLoader.load(this.getClass().getResource("/view/LoginForm.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Student Attendance System: Log In");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.sizeToScene();
            stage.show();

            /* Let's close the splash screen eventually */
            ((Stage)(lblStatus.getScene().getWindow())).close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadCreateAdminForm() {
        Stage stage = new Stage();
        Scene scene = null;
        try {
            scene = new Scene(FXMLLoader.load(getClass().getResource("/view/CreateAdminForm.fxml")));
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.setTitle("Student Attendance System : Create Admin");
            stage.setResizable(false);
            stage.sizeToScene();
            stage.show();
            ((Stage)lblStatus.getScene().getWindow()).close();
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

    public void shutdownApp(Throwable t){
        Platform.runLater(() -> {
            lblStatus.setText("Failed to Initialize the Connection");

        });

        sleep(2000);
        if (t!=null){
            t.printStackTrace();
        }
        System.exit(1);
    }

    public void dropDatabase(){
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306", "root", "root");
            Statement stm = connection.createStatement();
            stm.execute("DROP DATABASE IF EXISTS dep8_student_attendance");
            connection.close();
        } catch (SQLException e) {
            shutdownApp(e);
        }

    }
}

