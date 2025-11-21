package cti;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

public class SimulatorSwing {
    private static class NodePanel {
        final String nodeId;
        final JPanel panel;
        final JProgressBar cpuBar;
        final JProgressBar memBar;
        final JTextArea miniLog;

        volatile int cpu = 10;
        volatile int mem = 20;
        Timer decayTimer;

        NodePanel(String nodeId) {
            this.nodeId = nodeId;
            panel = new JPanel(new BorderLayout(6,6));
            panel.setBorder(BorderFactory.createTitledBorder(nodeId));

            cpuBar = new JProgressBar(0, 100);
            cpuBar.setStringPainted(true);
            cpuBar.setValue(cpu);
            memBar = new JProgressBar(0, 100);
            memBar.setStringPainted(true);
            memBar.setValue(mem);

            JPanel bars = new JPanel(new GridLayout(2,1,4,4));
            bars.add(makeLabeled("CPU", cpuBar));
            bars.add(makeLabeled("MEM", memBar));

            miniLog = new JTextArea(4, 20);
            miniLog.setEditable(false);
            miniLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
            JScrollPane sp = new JScrollPane(miniLog);

            panel.add(bars, BorderLayout.CENTER);
            panel.add(sp, BorderLayout.SOUTH);
        }

        private JPanel makeLabeled(String label, JComponent comp) {
            JPanel p = new JPanel(new BorderLayout(4,4));
            p.add(new JLabel(label), BorderLayout.WEST);
            p.add(comp, BorderLayout.CENTER);
            return p;
        }

        void appendLog(String line) {
            SwingUtilities.invokeLater(() -> {
                miniLog.append(line + "\n");
                miniLog.setCaretPosition(miniLog.getDocument().getLength());
            });
        }

        void applyAttack(Attacker.AttackType type, int intensity) {
            double impact = intensity / 10.0;

            if (decayTimer != null) decayTimer.stop();

            switch (type) {
                case CPU_OVERLOAD:
                    simulateIncrease("CPU overload", (int) Math.round(30 + impact * 60), (int)(3000 + intensity * 200));
                    break;
                case MEM_FILL:
                    simulateIncrease("Memory fill", (int) Math.round(25 + impact * 70), (int)(3500 + intensity * 250));
                    break;
                case PORT_FLOOD:
                    simulateIncrease("Port flood (spikes CPU)", (int) Math.round(15 + impact * 50), (int)(2000 + intensity * 150));
                    break;
                case ANOMALY_SPIKE:
                    simulateIncrease("Anomaly spike (transient)", (int) Math.round(10 + impact * 40), (int)(1500 + intensity * 120));
                    break;
            }
        }

        private void simulateIncrease(String label, int peakIncrease, int durationMs) {
            appendLog("[attack] " + label + " -> peak+" + peakIncrease + "% for " + durationMs + "ms");
            int stepsUp = 8;
            int stepMs = Math.max(50, durationMs / (stepsUp + 4));
            int initialCpu = cpu;
            int initialMem = mem;
            int targetCpu = Math.min(100, initialCpu + peakIncrease);
            int targetMem = Math.min(100, initialMem + peakIncrease/2);

            Timer upTimer = new Timer(stepMs, null);
            final int[] step = {0};
            upTimer.addActionListener(e -> {
                step[0]++;
                double frac = Math.min(1.0, (double) step[0] / stepsUp);
                cpu = initialCpu + (int) Math.round((targetCpu - initialCpu) * frac);
                mem = initialMem + (int) Math.round((targetMem - initialMem) * frac);
                cpuBar.setValue(cpu);
                cpuBar.setString(cpu + "%");
                memBar.setValue(mem);
                memBar.setString(mem + "%");
                if (step[0] >= stepsUp) {
                    ((Timer)e.getSource()).stop();
                    scheduleDecay(durationMs, initialCpu, initialMem);
                }
            });
            upTimer.setRepeats(true);
            upTimer.start();
        }

        private void scheduleDecay(int holdMs, int initialCpu, int initialMem) {
            int hold = Math.max(200, holdMs / 6);

            // Use a single-shot Swing Timer as a non-blocking wait; this avoids sleeping on EDT.
            Timer holdTimer = new Timer(hold, null);
            holdTimer.setRepeats(false);
            holdTimer.addActionListener(ev -> {
                SwingUtilities.invokeLater(() -> {
                    int decaySteps = 12;
                    int decayStepMs = Math.max(50, (holdMs + 600) / decaySteps);
                    final int startCpu = cpu;
                    final int startMem = mem;
                    decayTimer = new Timer(decayStepMs, null);
                    final int[] s = {0};
                    decayTimer.addActionListener(e -> {
                        s[0]++;
                        double frac = Math.min(1.0, (double) s[0] / decaySteps);
                        cpu = startCpu - (int) Math.round((startCpu - initialCpu) * frac);
                        mem = startMem - (int) Math.round((startMem - initialMem) * frac);
                        cpuBar.setValue(cpu); cpuBar.setString(cpu + "%");
                        memBar.setValue(mem); memBar.setString(mem + "%");
                        if (s[0] >= decaySteps) {
                            ((Timer)e.getSource()).stop();
                            appendLog("[recovered] CPU=" + cpu + "% MEM=" + mem + "%");
                        }
                    });
                    decayTimer.setRepeats(true);
                    decayTimer.start();
                });
            });
            holdTimer.start();
        }
    }

    private final JFrame frame;
    private final JPanel nodesGrid;
    private final JComboBox<String> nodeSelect;
    private final JComboBox<Attacker.AttackType> attackSelect;
    private final JSlider intensitySlider;
    private final JButton startBtn;
    private final JToggleButton autoToggle;
    private final JTextArea mainLog;
    private final List<NodePanel> nodes = new ArrayList<>();
    private Timer autoTimer;

    public SimulatorSwing(int nodeCount) {
        frame = new JFrame("CTI Attack Simulator - Nodes");
        nodesGrid = new JPanel(new GridLayout(0, Math.min(4, nodeCount), 8, 8));
        nodeSelect = new JComboBox<>();
        attackSelect = new JComboBox<>(Attacker.AttackType.values());
        intensitySlider = new JSlider(1, 10, 6);
        startBtn = new JButton("Start Attack");
        autoToggle = new JToggleButton("Auto: OFF");
        mainLog = new JTextArea(8, 60);
        mainLog.setEditable(false);
        mainLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        for (int i = 1; i <= nodeCount; i++) {
            String id = "node-" + i;
            NodePanel np = new NodePanel(id);
            nodes.add(np);
            nodesGrid.add(np.panel);
            nodeSelect.addItem(id);
        }

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        controls.add(new JLabel("Target Node:"));
        controls.add(nodeSelect);
        controls.add(new JLabel("Attack:"));
        controls.add(attackSelect);
        controls.add(new JLabel("Intensity:"));
        intensitySlider.setMajorTickSpacing(1);
        intensitySlider.setPaintTicks(true);
        intensitySlider.setPaintLabels(true);
        controls.add(intensitySlider);
        controls.add(startBtn);
        controls.add(autoToggle);

        JPanel main = new JPanel(new BorderLayout(8,8));
        main.add(nodesGrid, BorderLayout.CENTER);
        main.add(controls, BorderLayout.NORTH);
        main.add(new JScrollPane(mainLog), BorderLayout.SOUTH);

        frame.getContentPane().add(main);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        startBtn.addActionListener(this::onStartAttack);
        autoToggle.addActionListener(this::onAutoToggle);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) {
                stopAuto();
            }
        });
    }

    private void onStartAttack(ActionEvent e) {
        String nodeId = (String) nodeSelect.getSelectedItem();
        Attacker.AttackType type = (Attacker.AttackType) attackSelect.getSelectedItem();
        int intensity = intensitySlider.getValue();
        NodePanel np = findNode(nodeId);
        if (np == null) return;

        Attacker<String> attacker = new Attacker<>("manual-"+System.currentTimeMillis(), "0.0.0.0", type, intensity);
        log("Launching attack " + attacker);
        np.appendLog("Received attack: " + attacker);
        np.applyAttack(type, intensity);

        if (intensity >= 9) {
            log("*** CRITICAL ATTACK ON " + nodeId + " ***");
        }
    }

    private NodePanel findNode(String id) {
        for (NodePanel n: nodes) if (n.nodeId.equals(id)) return n;
        return null;
    }

    private void log(String line) {
        SwingUtilities.invokeLater(() -> {
            mainLog.append(line + "\n");
            mainLog.setCaretPosition(mainLog.getDocument().getLength());
        });
    }

    private void onAutoToggle(ActionEvent e) {
        if (autoToggle.isSelected()) {
            autoToggle.setText("Auto: ON");
            startAuto();
        } else {
            autoToggle.setText("Auto: OFF");
            stopAuto();
        }
    }

    private void startAuto() {
        if (autoTimer != null && autoTimer.isRunning()) return;
        Random r = new Random();
        autoTimer = new Timer(900, ev -> {
            NodePanel np = nodes.get(r.nextInt(nodes.size()));
            Attacker.AttackType t = Attacker.AttackType.values()[r.nextInt(Attacker.AttackType.values().length)];
            int intensity = 2 + r.nextInt(9);
            Attacker<String> autoA = new Attacker<>("auto-" + Math.abs(r.nextInt() % 10000), "10.0.0." + r.nextInt(255), t, intensity);
            log("Auto: " + autoA + " -> target " + np.nodeId);
            np.appendLog("Auto-attack: " + autoA);
            np.applyAttack(t, intensity);
        });
        autoTimer.setInitialDelay(0);
        autoTimer.start();
    }

    private void stopAuto() {
        if (autoTimer != null) {
            autoTimer.stop();
            autoTimer = null;
        }
    }

    public void showUI() {
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }
}
