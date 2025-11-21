import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TestF extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("TestFX");
        stage.setScene(new Scene(new Label("JavaFX Running! (dGPU)"), 320, 160));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
