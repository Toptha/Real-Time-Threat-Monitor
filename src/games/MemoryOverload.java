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

import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class MemoryOverload {
    private final ObservableList<Integer> allocations = FXCollections.observableArrayList();
    private final Random rnd = new Random();
    private Timeline timeline;
    private static MemoryOverload instance;

    private ProgressBar memBar;
    private Label memLabel;
    private ListView<String> allocList;
    private Slider autoAllocRate;
    private ToggleButton autoToggle;
    private final int MAX_SAFE_MB = 500;

    public MemoryOverload() { instance = this; }

    public BorderPane createView() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("module-root");

        VBox left = new VBox(12);
        left.setPadding(new Insets(12));
        left.setPrefWidth(320);
        left.getStyleClass().add("control-column");

        Label title = new Label("Memory Overload");
        title.getStyleClass().add("module-title");

        Button btnAlloc = new Button("Allocate Chunk");
        Button btnFree = new Button("Free Random");
        Button btnClear = new Button("Clear All");
        HBox row1 = new HBox(8, btnAlloc, btnFree, btnClear);
        row1.setAlignment(Pos.CENTER_LEFT);

        autoToggle = new ToggleButton("Auto Alloc");
        autoAllocRate = new Slider(0, 20, 4);
        autoAllocRate.setShowTickMarks(true);
        autoAllocRate.setShowTickLabels(true);
        autoAllocRate.setMajorTickUnit(5);

        VBox autoBox = new VBox(6, new Label("Auto allocation (per tick)"), autoAllocRate, autoToggle);

        memBar = new ProgressBar(0);
        memBar.setPrefWidth(260);
        memLabel = new Label("Memory: 0 MB");

        allocList = new ListView<>();
        allocList.setPrefHeight(300);

        left.getChildren().addAll(title, row1, autoBox, new Separator(), memBar, memLabel, new Label("Allocations (size kb)"), allocList);
        root.setLeft(left);

        Canvas canvas = new Canvas(800, 600);
        StackPane center = new StackPane(canvas);
        center.getStyleClass().add("canvas-pane");
        root.setCenter(center);

        btnAlloc.setOnAction(e -> allocateOnce());
        btnFree.setOnAction(e -> freeRandom());
        btnClear.setOnAction(e -> { allocations.clear(); updateUI(); });

        timeline = new Timeline(new KeyFrame(Duration.millis(350), ev -> tick(canvas)));
        timeline.setCycleCount(Timeline.INDEFINITE);

        autoToggle.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) start(); else stop();
        });

        return root;
    }

    private void allocateOnce() {
        int sizeKb = 20 + rnd.nextInt(1024); 
        allocations.add(sizeKb);
        updateUI();
    }

    private void freeRandom() {
        if (!allocations.isEmpty()) allocations.remove(rnd.nextInt(allocations.size()));
        updateUI();
    }

    private void tick(Canvas canvas) {
        int toAlloc = (int)Math.round(autoAllocRate.getValue());
        for (int i=0;i<toAlloc;i++) {
            if (autoToggle.isSelected()) allocateOnce();
        }

        draw(canvas);
    }

    private void draw(Canvas canvas) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFill(Color.web("#071021"));
        g.fillRect(0,0,w,h);

        int cap = Math.min(allocations.size(), 120);
        double baseX = 40, baseY = 80;
        for (int i = 0; i < cap; i++) {
            int sizeKb = allocations.get(i);
            double rw = Math.min(140, 20 + sizeKb / 8.0);
            double rh = 12;
            double x = baseX + (i % 8) * 150;
            double y = baseY + (i / 8) * 20;
            g.setFill(i % 2 == 0 ? Color.web("#2ecc71") : Color.web("#27ae60"));
            g.fillRoundRect(x, y, rw, rh, 6, 6);
        }

        long totalKb = allocations.stream().mapToLong(Integer::longValue).sum();
        double totalMb = totalKb / 1024.0;
        double ratio = Math.min(1.0, totalMb / MAX_SAFE_MB);

        g.setFill(Color.web("#222"));
        g.fillRoundRect(40, h - 100, w - 120, 28, 10, 10);
        g.setFill(ratio < 0.7 ? Color.web("#1abc9c") : ratio < 0.95 ? Color.web("#f39c12") : Color.web("#e74c3c"));
        g.fillRoundRect(40, h - 100, (w - 120) * ratio, 28, 10, 10);

        g.setFill(Color.WHITE);
        g.fillText(String.format("Total Allocated: %.2f MB", totalMb), 40, h - 110);

        updateUI();
    }

    private void updateUI() {
        // update progress bar and label and listview using streams
        long totalKb = allocations.stream().mapToLong(Integer::longValue).sum();
        double totalMb = totalKb / 1024.0;
        memBar.setProgress(Math.min(1.0, totalMb / MAX_SAFE_MB));
        memLabel.setText(String.format("Memory: %.2f MB / %d MB", totalMb, MAX_SAFE_MB));

        var topSizes = allocations.stream()
                .sorted(Comparator.reverseOrder())
                .limit(200)
                .map(sz -> String.valueOf(sz))
                .collect(Collectors.toList());
        allocList.getItems().setAll(topSizes);
    }

    // controls for external start/stop from main
    public void start() { if (timeline != null) timeline.play(); }
    public void stop() { if (timeline != null) timeline.stop(); }
    public static void startSimulationStatic() { if (instance != null) instance.start(); }
    public static void stopSimulationStatic() { if (instance != null) instance.stop(); }
}
