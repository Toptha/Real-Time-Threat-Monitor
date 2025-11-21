package games;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Random;

public class StackOverflowSim {
    private final ObservableList<String> frames = FXCollections.observableArrayList();
    private Timeline timeline;
    private static StackOverflowSim instance;
    private final Random rnd = new Random();

    private Label framesLabel;
    private Slider pushRate;
    private ToggleButton auto;

    public StackOverflowSim() { instance = this; }

    public BorderPane createView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("module-root");

        VBox left = new VBox(10);
        left.setPadding(new Insets(12));
        left.setPrefWidth(320);
        left.getStyleClass().add("control-column");

        Label title = new Label("Stack Growth Simulator");
        title.getStyleClass().add("module-title");

        Button push = new Button("Push Frame");
        Button pop = new Button("Pop Frame");
        Button clear = new Button("Clear");
        HBox row = new HBox(8, push, pop, clear);

        pushRate = new Slider(0, 10, 2);
        pushRate.setShowTickMarks(true);
        pushRate.setShowTickLabels(true);
        auto = new ToggleButton("Auto Push");

        framesLabel = new Label("Frames: 0");

        left.getChildren().addAll(title, row, new Label("Auto push rate"), pushRate, auto, new Separator(), framesLabel);
        root.setLeft(left);

        Canvas canvas = new Canvas(400, 600);
        root.setCenter(new StackPane(canvas));

        push.setOnAction(e -> pushFrame());
        pop.setOnAction(e -> popFrame());
        clear.setOnAction(e -> { frames.clear(); });

        auto.selectedProperty().addListener((obs, o, n) -> {
            if (n) start(); else stop();
        });

        timeline = new Timeline(new KeyFrame(Duration.millis(350), ev -> tick(canvas)));
        timeline.setCycleCount(Timeline.INDEFINITE);

        return root;
    }

    private void pushFrame() {
        frames.add("frame#" + frames.size());
        framesLabel.setText("Frames: " + frames.size());
    }

    private void popFrame() {
        if (!frames.isEmpty()) frames.remove(frames.size()-1);
        framesLabel.setText("Frames: " + frames.size());
    }

    private void tick(Canvas canvas) {
        if (auto.isSelected()) {
            int amount = (int)Math.round(pushRate.getValue());
            for (int i=0;i<amount;i++) pushFrame();
        }
        draw(canvas);
    }

    private void draw(Canvas c) {
        GraphicsContext g = c.getGraphicsContext2D();
        double w = c.getWidth(), h = c.getHeight();
        g.setFill(Color.web("#02040a"));
        g.fillRect(0,0,w,h);

        // draw stack frames bottom-up
        int maxDraw = Math.min(frames.size(), 24);
        double fw = w - 80;
        double fh = 28;
        for (int i=0;i<maxDraw;i++) {
            int idx = i;
            double y = h - (i+1) * (fh + 8) - 20;
            // color intensity increases with depth
            double t = Math.min(1.0, (frames.size() / 40.0));
            Color frameColor = Color.hsb(160 - idx*3, 0.8, 0.6 + 0.4 * t);
            g.setFill(frameColor);
            g.fillRoundRect(40, y, fw, fh, 6, 6);
            g.setFill(Color.BLACK);
            g.fillText(frames.get(frames.size() - 1 - idx), 52, y + fh - 8);
        }

        // overflow warning if too many frames
        if (frames.size() > 40) {
            g.setFill(Color.web("#ff4d4d"));
            g.fillText("STACK OVERFLOW IMMINENT!", 40, 40);
        }
        framesLabel.setText("Frames: " + frames.size());
    }

    public void start() { if (timeline != null) timeline.play(); }
    public void stop() { if (timeline != null) timeline.stop(); }
    public static void startSimulationStatic() { if (instance != null) instance.start(); }
    public static void stopSimulationStatic() { if (instance != null) instance.stop(); }
}
