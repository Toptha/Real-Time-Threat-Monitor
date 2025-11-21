package games;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DDoSSimulator {
    private Timeline timeline;
    private static DDoSSimulator instance;
    private final Random rnd = new Random();

    private final List<RequestParticle> particles = new ArrayList<>();
    private final List<BreachBlock> breaches = new ArrayList<>();

    private Label queuedLabel;
    private Slider burstSlider;
    private ToggleButton autoToggle;

    public DDoSSimulator() { instance = this; }

    public BorderPane createView() {
        BorderPane root = new BorderPane();
        VBox left = new VBox(10);
        left.setPadding(new Insets(12));
        left.setPrefWidth(320);

        Label title = new Label("DDoS Simulator");

        Button btnBurst = new Button("Send Burst");
        Button btnClear = new Button("Clear All");
        Button btnStart = new Button("Start");
        Button btnStop = new Button("Stop");
        HBox row = new HBox(8, btnBurst, btnClear);
        row.setAlignment(Pos.CENTER_LEFT);

        burstSlider = new Slider(20, 800, 120);
        burstSlider.setShowTickLabels(true);
        burstSlider.setShowTickMarks(true);
        autoToggle = new ToggleButton("Auto Flood");

        queuedLabel = new Label("Queued: 0");
        Label breachesLabel = new Label("Breaches: 0");

        left.getChildren().addAll(title, row, new Label("Burst size"), burstSlider, autoToggle,
                new Separator(), queuedLabel, breachesLabel);
        root.setLeft(left);

        Canvas canvas = new Canvas(800, 600);
        root.setCenter(new StackPane(canvas));

        btnBurst.setOnAction(e -> emitBurst((int) burstSlider.getValue()));     // 1-arg lambda
        btnClear.setOnAction(e -> { particles.clear(); breaches.clear(); updateTelemetry(breachesLabel); }); // block lambda

        btnStart.setOnAction(e -> start());   // 1-arg lambda
        btnStop.setOnAction(e -> stop());     // 1-arg lambda

        timeline = new Timeline(new KeyFrame(Duration.millis(33), ev -> tick(canvas, breachesLabel))); // 1-arg lambda
        timeline.setCycleCount(Animation.INDEFINITE);

        autoToggle.selectedProperty().addListener((obs, oldV, newV) -> { if (newV) start(); }); // 3-arg lambda + block lambda

        return root;
    }

    private void emitBurst(int size) {
        for (int i = 0; i < size; i++) {
            double x = rnd.nextDouble() * 780 + 10;
            double y = -10;
            double vx = (390 - x) / 120 + (rnd.nextDouble() - 0.5) * 0.6;
            double vy = 1.0 + rnd.nextDouble() * 2.0;
            particles.add(new RequestParticle(x, y, vx, vy, 1 + rnd.nextDouble()*2));
        }
        updateTelemetry(null);
    }

    private void tick(Canvas canvas, Label breachesLabel) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double w = canvas.getWidth(), h = canvas.getHeight();
        g.setFill(Color.web("#020617"));
        g.fillRect(0,0,w,h);

        if (autoToggle != null && autoToggle.isSelected() && rnd.nextDouble() < 0.18)
            emitBurst((int)(burstSlider.getValue()*0.15));

        Iterator<RequestParticle> it = particles.iterator();
        while (it.hasNext()) {
            RequestParticle p = it.next();
            p.update();

            double serverTop = h - 90;
            double serverLeft = w/2 - 120;
            double serverRight = w/2 + 120;
            double serverBottom = h - 20;

            if (p.y >= serverTop && p.x >= serverLeft && p.x <= serverRight) {
                double bx = Math.max(serverLeft + 8, Math.min(serverRight - 8, p.x + (rnd.nextDouble()-0.5)*20));
                double by = serverTop + 8 + rnd.nextDouble() * (serverBottom - serverTop - 16);
                breaches.add(new BreachBlock(bx, by));
                it.remove();
                continue;
            }

            if (p.y > h + 30) {
                it.remove();
            }
        }

        double sx = w/2 - 120;
        double sy = h - 90;
        g.setFill(Color.web("#1f2937"));
        g.fillRoundRect(sx, sy, 240, 70, 12, 12);

        for (int i = 0; i < breaches.size(); i++) {
            BreachBlock b = breaches.get(i);
            double ageFactor = Math.min(1.0, (i + 1) / 40.0);
            Color c = Color.color(1.0, 0.2 + 0.6*ageFactor, 0.2);
            g.setFill(c);
            g.fillRect(b.x - 6, b.y - 6, 12, 12);
            g.setStroke(Color.color(1.0, 0.6, 0.6, 0.25));
            g.strokeRect(b.x - 6, b.y - 6, 12, 12);
        }

        g.setFill(Color.WHITE);
        g.fillText("SERVER", sx + 90, sy + 35);

        int breachCount = breaches.size();
        double breachRatio = Math.min(1.0, breachCount / 120.0);

        double hbW = 200;
        double hbX = w/2 - hbW/2;
        double hbY = sy - 18;
        g.setFill(Color.web("#222"));
        g.fillRoundRect(hbX, hbY, hbW, 10, 6, 6);

        Color hbColor = breachRatio < 0.6 ? Color.web("#1abc9c") :
                        breachRatio < 0.9 ? Color.web("#f39c12") :
                        Color.web("#e74c3c");

        g.setFill(hbColor);
        g.fillRoundRect(hbX, hbY, hbW * breachRatio, 10, 6, 6);
        g.setFill(Color.WHITE);
        g.fillText(String.format("Breach: %d", breachCount), hbX + 6, hbY + 8);

        for (RequestParticle p : particles) {
            g.setGlobalAlpha(Math.max(0.35, Math.min(1.0, p.size/2.5)));
            g.setFill(Color.hsb(200 + p.size*30, 0.8, 1.0));
            g.fillOval(p.x, p.y, 6 + p.size*3, 6 + p.size*3);
        }
        g.setGlobalAlpha(1.0);

        int queued = particles.size();
        for (int i = 0; i < Math.min(240, queued); i++) {
            int col = i % 30;
            int row = i / 30;
            double x = 20 + col * 8;
            double y = 20 + row * 8;
            g.setFill(col % 2 == 0 ? Color.web("#60a5fa") : Color.web("#a78bfa"));
            g.fillRect(x, y, 6, 6);
        }

        int capacity = 12;
        for (int i = 0; i < capacity && !particles.isEmpty(); i++) particles.remove(0);

        updateTelemetry(breachesLabel);
    }

    private void updateTelemetry(Label breachesLabel) {
        if (queuedLabel != null) queuedLabel.setText("Queued: " + particles.size());
        if (breachesLabel != null) breachesLabel.setText("Breaches: " + breaches.size());
    }

    public void start() { if (timeline != null) timeline.play(); }
    public void stop() { if (timeline != null) timeline.stop(); }

    public static void startSimulationStatic() { if (instance != null) instance.start(); }
    public static void stopSimulationStatic()  { if (instance != null) instance.stop(); }

    private static class RequestParticle {
        double x,y,vx,vy,size;
        RequestParticle(double x, double y, double vx, double vy, double size) {
            this.x=x; this.y=y; this.vx=vx; this.vy=vy; this.size=size;
        }
        void update() {
            x += vx; y += vy; vy += 0.02;
            vx += (Math.random()-0.5)*0.04;
        }
    }

    private static class BreachBlock {
        double x, y;
        BreachBlock(double x, double y) { this.x = x; this.y = y; }
    }
}
