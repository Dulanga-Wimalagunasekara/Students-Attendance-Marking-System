package controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.stage.Stage;


import java.time.LocalDate;

public class FilterFormController {

    public DatePicker startDate;
    public DatePicker endDate;
    public SimpleObjectProperty<LocalDate> dateStart;
    public SimpleObjectProperty<LocalDate> dateEnd;


    public void initialize(){

    }

    public void initDates(SimpleObjectProperty<LocalDate> dateStart,SimpleObjectProperty<LocalDate> dateEnd){
        this.dateStart=dateStart;
        this.dateEnd=dateEnd;
    }

    public void btnOKOnAction(ActionEvent actionEvent) {
        this.dateStart.setValue(startDate.getValue());
        this.dateEnd.setValue(endDate.getValue());
        if (dateStart.get()==null || dateEnd.getValue() == null){
            new Alert(Alert.AlertType.WARNING,"Please select both start and end dates!", ButtonType.OK).show();
            return;
        }else {
            ((Stage)startDate.getScene().getWindow()).close();
        }
    }
}
