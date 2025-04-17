import javax.swing.*;
import java.sql.*;
import java.util.*;

class Edge {
    String to;
    double riskScore;

    Edge(String to, double riskScore) {
        this.to = to;
        this.riskScore = riskScore;
    }
}

class Graph {
    Map<String, List<Edge>> adjList = new HashMap<>();

    public void addEdge(String from, String to, double riskScore) {
        adjList.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, riskScore));
        adjList.computeIfAbsent(to, k -> new ArrayList<>()).add(new Edge(from, riskScore));
    }
    

    public void loadFromDB(Connection conn) {
        String query = "SELECT n1.NodeName AS source, n2.NodeName AS destination, " +
                       "(e.CSS + e.SLF + e.PPI + e.PPS) AS risk_score " +
                       "FROM edges e " +
                       "JOIN nodes n1 ON e.FromNode = n1.NodeID " +
                       "JOIN nodes n2 ON e.ToNode = n2.NodeID";

        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String from = rs.getString("source");
                String to = rs.getString("destination");
                double riskScore = rs.getDouble("risk_score");
                addEdge(from, to, riskScore);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Result dijkstra(String source, String destination) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        for (String node : adjList.keySet()) {
            dist.put(node, Double.MAX_VALUE);
        }
        dist.put(source, 0.0);
        pq.add(source);

        while (!pq.isEmpty()) {
            String current = pq.poll();

            if (current.equals(destination)) break;

            for (Edge edge : adjList.getOrDefault(current, new ArrayList<>())) {
                double newDist = dist.get(current) + edge.riskScore;

                if (newDist < dist.getOrDefault(edge.to, Double.MAX_VALUE)) {
                    dist.put(edge.to, newDist);
                    prev.put(edge.to, current);
                    pq.add(edge.to);
                }
            }
        }

        List<String> path = new ArrayList<>();
        String current = destination;
        while (current != null) {
            path.add(0, current);
            current = prev.get(current);
        }

        int totalRiskScore = (int) Math.round(dist.getOrDefault(destination, Double.MAX_VALUE));
        return new Result(path, totalRiskScore);
    }
}

class Result {
    List<String> path;
    int riskscoreInt;

    Result(List<String> path, int riskscoreInt) {
        this.path = path;
        this.riskscoreInt = riskscoreInt;
    }

    @Override
    public String toString() {
        return "Safest Path: " + String.join(" -> ", path) + " -> END\nTotal Risk Score: " + riskscoreInt;
    }
}

public class PathfinderApp {
    private static Graph graph = new Graph();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Pathfinder App");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextField sourceField = new JTextField(10);
        JTextField destinationField = new JTextField(10);
        JButton findButton = new JButton("Find Safest Path");
        JTextArea resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Source Node:"));
        panel.add(sourceField);
        panel.add(new JLabel("Destination Node:"));
        panel.add(destinationField);
        panel.add(findButton);
        panel.add(new JScrollPane(resultArea));

        frame.add(panel);
        frame.setVisible(true);

        try {
            // Load JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Replace with your own DB credentials
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/pathfinder_db", // Update this!
                "root",                            // Update this!
                "root"                             // Update this!
            );

            graph.loadFromDB(conn);

        } catch (Exception ex) {
            ex.printStackTrace();
            resultArea.setText("Failed to connect to database.");
        }

        findButton.addActionListener(e -> {
            String source = sourceField.getText().trim();
            String destination = destinationField.getText().trim();
            if (!source.isEmpty() && !destination.isEmpty()) {
                Result result = graph.dijkstra(source, destination);
                resultArea.setText(result.toString());
            } else {
                resultArea.setText("Please enter both source and destination nodes.");
            }
        });
    }
}
