import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.nio.file.Paths;

public class AppInitializer extends Application {

    public static void main(String[] args) {
//        System.out.println(Paths.get(System.getProperty("user.home"), "documents"));
        launch(args);
    }

    @Override

    public void start(Stage primaryStage) throws IOException {

        AnchorPane load = FXMLLoader.load(getClass().getResource("view/SplashScreenForm.fxml"));
        Scene scene = new Scene(load);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.centerOnScreen();
        primaryStage.sizeToScene();
        primaryStage.show();

    }
}
