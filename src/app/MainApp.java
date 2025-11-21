package app;

import cti.SimulatorSwing;
import cti.Attacker;
import cti.SystemStatsReader;

import java.util.Scanner;

public class MainApp {

    private static final int DEFAULT_NODE_COUNT = 6;
    private static final int MIN_NODES = 1;
    private static final int MAX_NODES = 64;

    public static void main(String[] args) {
        int defaultNodes = DEFAULT_NODE_COUNT;

        if (args != null && args.length > 0) {
            try {
                int argNodes = Integer.parseInt(args[0]);
                if (argNodes >= MIN_NODES && argNodes <= MAX_NODES) {
                    defaultNodes = argNodes;
                } else {
                    System.out.printf("CLI node count out of bounds (%d..%d). Using %d.%n",
                            MIN_NODES, MAX_NODES, defaultNodes);
                }
            } catch (NumberFormatException ignored) {
                System.out.println("Invalid CLI arg for node count. Using default: " + defaultNodes);
            }
        }

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                printMenu(defaultNodes);
                System.out.print("> ");
                String choice = sc.nextLine().trim();

                switch (choice.toLowerCase()) {
                    case "q":
                    case "quit":
                    case "exit":
                        System.out.println("Exiting. Ride safe out there.");
                        running = false;
                        break;

                    case "1":
                        try {
                            SystemStatsReader.printStats();
                        } catch (Throwable t) {
                            System.err.println("Error while reading system stats: " + t.getMessage());
                            t.printStackTrace(System.err);
                        }
                        break;

                    case "2":
                        int n = promptInt(sc, "How many attackers to generate? (default 5): ", 1, 1000, 5);
                        System.out.println("Generating " + n + " attackers:");
                        for (int i = 0; i < n; i++) {
                            System.out.println(Attacker.random());
                        }
                        break;

                    case "3":
                        int nodes = promptInt(sc,
                                String.format("Node count for simulator (min %d, max %d) [default %d]: ",
                                        MIN_NODES, MAX_NODES, defaultNodes),
                                MIN_NODES, MAX_NODES, defaultNodes);
                        System.out.println("Launching Swing simulator with " + nodes + " nodes. Close the window to continue.");
                        launchSimulator(nodes);
                        break;

                    case "4":
                        defaultNodes = promptInt(sc,
                                String.format("Set default node count (%d..%d) [current %d]: ", MIN_NODES, MAX_NODES, defaultNodes),
                                MIN_NODES, MAX_NODES, defaultNodes);
                        System.out.println("Default node count set to " + defaultNodes);
                        break;

                    case "h":
                    case "help":
                        printExtendedHelp();
                        break;

                    default:
                        System.out.println("Unknown option. Type 'h' for help or choose a valid option.");
                }
            }
        } catch (Exception e) {
            System.err.println("Fatal error in main loop: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }

    private static void printMenu(int defaultNodes) {
        System.out.println("\n=== CTI Demo Console ===");
        System.out.println("Choose demo to run (type the number, 'h' for help, 'q' to quit):");
        System.out.println("1 - System stats");
        System.out.println("2 - Attacker console demo");
        System.out.println("3 - Swing simulator (UI) [launches with node count prompt]");
        System.out.println("4 - Set default simulator node count (current: " + defaultNodes + ")");
        System.out.println("h - Help / usage");
        System.out.println("q - Quit");
    }

    private static void printExtendedHelp() {
        System.out.println("\nHelp:");
        System.out.println(" - Option 1 prints system stats via SystemStatsReader.printStats()");
        System.out.println(" - Option 2 prints some random attackers (Attacker.random())");
        System.out.println(" - Option 3 launches the Swing simulator (SimulatorSwing). You'll be asked for node count.");
        System.out.println(" - Option 4 changes the default node count used for quick launches.");
        System.out.println("Notes:");
        System.out.println(" - Valid node count is between " + MIN_NODES + " and " + MAX_NODES + ".");
        System.out.println(" - You can also pass an initial node count as the first CLI argument when starting the app.");
    }

    private static int promptInt(Scanner sc, String prompt, int min, int max, int defaultVal) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine().trim();
            if (line.isEmpty()) return defaultVal;
            try {
                int v = Integer.parseInt(line);
                if (v < min || v > max) {
                    System.out.printf("Value out of range (%d..%d). Try again.%n", min, max);
                    continue;
                }
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Not a valid integer. Try again.");
            }
        }
    }

    private static void launchSimulator(int nodes) {
        try {
            SimulatorSwing sim = new SimulatorSwing(nodes);
            sim.showUI();
        } catch (Throwable t) {
            System.err.println("Failed to launch simulator: " + t.getMessage());
            t.printStackTrace(System.err);
        }
    }
}
