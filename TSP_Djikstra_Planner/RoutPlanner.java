import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class RoutPlanner extends JFrame {
    private final int NODE_COUNT = 7;
    private JTextField[][] distanceFields;
    private JCheckBox[] locationCheckBoxes;
    private JButton computeButton;
    private JTextArea resultArea;
    private String[] locationNames;
    private double[][] graph;

    public RoutPlanner() {
        super("Delivery Planner - Dijkstra + TSP");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        locationNames = new String[NODE_COUNT];
        graph = new double[NODE_COUNT][NODE_COUNT];

        JPanel topPanel = new JPanel(new GridLayout(NODE_COUNT + 1, NODE_COUNT + 1));
        distanceFields = new JTextField[NODE_COUNT][NODE_COUNT];

        topPanel.add(new JLabel("")); // Empty top-left corner
        for (int i = 0; i < NODE_COUNT; i++) {
            JTextField tf = new JTextField("Loc" + (i + 1));
            topPanel.add(tf);
            locationNames[i] = tf.getText();
            int index = i;
            tf.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    locationNames[index] = tf.getText();
                    updateCheckboxLabels();
                }
            });
        }

        for (int i = 0; i < NODE_COUNT; i++) {
            JTextField tf = new JTextField("Loc" + (i + 1));
            topPanel.add(tf);
            locationNames[i] = tf.getText();
            int index = i;
            tf.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e) {
                    locationNames[index] = tf.getText();
                    updateCheckboxLabels();
                }
            });

            for (int j = 0; j < NODE_COUNT; j++) {
                JTextField distField = new JTextField("0");
                distanceFields[i][j] = distField;
                topPanel.add(distField);
            }
        }

        add(topPanel, BorderLayout.NORTH);

        JPanel middlePanel = new JPanel();
        middlePanel.setLayout(new FlowLayout());
        locationCheckBoxes = new JCheckBox[NODE_COUNT];
        for (int i = 0; i < NODE_COUNT; i++) {
            locationCheckBoxes[i] = new JCheckBox(locationNames[i]);
            middlePanel.add(locationCheckBoxes[i]);
        }
        add(middlePanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        computeButton = new JButton("Compute Optimal Path");
        bottomPanel.add(computeButton, BorderLayout.NORTH);

        resultArea = new JTextArea(10, 50);
        resultArea.setEditable(false);
        bottomPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        computeButton.addActionListener(e -> computeOptimalPath());

        setVisible(true);
    }

    private void updateCheckboxLabels() {
        for (int i = 0; i < NODE_COUNT; i++) {
            locationCheckBoxes[i].setText(locationNames[i]);
        }
    }

    private void computeOptimalPath() {
        // Read graph
        for (int i = 0; i < NODE_COUNT; i++) {
            for (int j = 0; j < NODE_COUNT; j++) {
                try {
                    graph[i][j] = Double.parseDouble(distanceFields[i][j].getText());
                    if (i == j) graph[i][j] = 0;
                } catch (NumberFormatException ex) {
                    graph[i][j] = Double.POSITIVE_INFINITY;
                }
            }
        }

        // Get selected locations
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < NODE_COUNT; i++) {
            if (locationCheckBoxes[i].isSelected()) selected.add(i);
        }

        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least 1 location!");
            return;
        }

        // Generate all permutations
        List<List<Integer>> permutations = new ArrayList<>();
        permute(selected, 0, permutations);

        double minDist = Double.POSITIVE_INFINITY;
        List<Integer> bestPath = null;

        for (List<Integer> perm : permutations) {
            double dist = 0;
            int prev = 0; // starting from 0 (Depot)
            boolean valid = true;
            for (int loc : perm) {
                if (graph[prev][loc] == Double.POSITIVE_INFINITY) {
                    valid = false;
                    break;
                }
                dist += graph[prev][loc];
                prev = loc;
            }
            if (valid && graph[prev][0] != Double.POSITIVE_INFINITY) dist += graph[prev][0];
            else valid = false;
            if (valid && dist < minDist) {
                minDist = dist;
                bestPath = new ArrayList<>(perm);
            }
        }

        if (bestPath != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("Optimal Path:\n0(Depot) -> ");
            for (int loc : bestPath) {
                sb.append(loc).append("(").append(locationNames[loc]).append(") -> ");
            }
            sb.append("0(Depot)\nTotal Distance: ").append(minDist);
            resultArea.setText(sb.toString());
        } else {
            resultArea.setText("No valid path found.");
        }
    }

    private void permute(List<Integer> arr, int k, List<List<Integer>> result) {
        if (k == arr.size()) {
            result.add(new ArrayList<>(arr));
        } else {
            for (int i = k; i < arr.size(); i++) {
                Collections.swap(arr, i, k);
                permute(arr, k + 1, result);
                Collections.swap(arr, k, i);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RoutPlanner::new);
    }
}

