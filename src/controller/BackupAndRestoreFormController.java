package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

public class BackupAndRestoreFormController {
    public Button btnBackup;
    public Button btnRestore;

    public void btnBackup_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose backup location");
        fileChooser.setInitialFileName(LocalDate.now() + "-sas-back");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Backup files (*.dep8bak)", "*.dep8back"));
        File file = fileChooser.showSaveDialog(btnBackup.getScene().getWindow());

        if (file != null) {
            ProcessBuilder mysqlDumpProcessBuilder = new ProcessBuilder("mysqldump",
                    "-h", "localhost",
                    "--port", "3306",
                    "-u", "root",
                    "-proot",
                    "--add-drop-database",
                    "--databases", "dep8_student_attendance");

            mysqlDumpProcessBuilder.redirectOutput(System.getProperty("os.name").equalsIgnoreCase("windows") ? file : new File(file.getAbsolutePath() + ".dep8back"));
            try {
                Process mysqlDump = mysqlDumpProcessBuilder.start();
                int exitCode = mysqlDump.waitFor();

                if (exitCode == 0) {
                    new Alert(Alert.AlertType.INFORMATION, "Backup process succeeded",ButtonType.OK).show();
                } else {
                    new Alert(Alert.AlertType.ERROR, "Backup process failed, try again!",ButtonType.OK).show();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnRestore_OnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Students' Backup","*.dep8back"));
        File file = fileChooser.showOpenDialog(btnBackup.getScene().getWindow());
        ProcessBuilder processBuilder = new ProcessBuilder("mysql", "-h", "localhost", "-u", "root", "-proot");
        processBuilder.redirectInput(file);
        try {
            Process start = processBuilder.start();
            int i = start.waitFor();
            if (i==0){
                new Alert(Alert.AlertType.CONFIRMATION,"Successfully Restored!",ButtonType.OK).show();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }
}
