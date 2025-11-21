package app;

import games.MemoryOverload;
import games.DDoSSimulator;
import games.StackOverflowSim;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Game extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("Cyber Threats Simulator — Main");

        TabPane tabs = new TabPane();

        Tab memTab = new Tab("Memory Overload", new MemoryOverload().createView());
        memTab.setClosable(false);

        Tab ddosTab = new Tab("DDoS Simulator", new DDoSSimulator().createView());
        ddosTab.setClosable(false);

        Tab stackTab = new Tab("Stack Overflow", new StackOverflowSim().createView());
        stackTab.setClosable(false);

        tabs.getTabs().addAll(memTab, ddosTab, stackTab);

        BorderPane root = new BorderPane(tabs);

        HBox bottom = new HBox(12);
        bottom.setStyle("-fx-padding:10; -fx-alignment:center;");
        Button startAll = new Button("Start All");
        Button stopAll = new Button("Stop All");
        startAll.setOnAction(e -> {
            MemoryOverload.startSimulationStatic();
            DDoSSimulator.startSimulationStatic();
            StackOverflowSim.startSimulationStatic();
        });
        stopAll.setOnAction(e -> {
            MemoryOverload.stopSimulationStatic();
            DDoSSimulator.stopSimulationStatic();
            StackOverflowSim.stopSimulationStatic();
        });
        bottom.getChildren().addAll(startAll, stopAll);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 1200, 760);
        try { scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm()); } catch (Exception ex) { /* ignore */ }

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
