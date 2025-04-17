import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class PathfinderUI {
    private JFrame mainFrame;
    private Graph graph;
    private Connection conn;
    private Map<String, JPanel> panels = new HashMap<>();
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel;

    public PathfinderUI() {
        // Initialize database connection
        String url = "jdbc:mysql://localhost:3306/pathfinder_db";
        String user = "root";
        String password = "root";
        
        try {
            conn = DriverManager.getConnection(url, user, password);
            graph = new Graph();
            graph.loadFromDB(conn);
            initializeUI();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database connection failed: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        mainFrame = new JFrame("SheSafe");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(900, 650);
        mainFrame.setLocationRelativeTo(null);

        // Create main panel with card layout
        mainPanel = new JPanel(cardLayout);
        
        // Create and add all panels
        createMenuPanel();
        createFindPathPanel();
        createEmergencyPanel();
        createRatingPanel();
        createTopLocationsPanel();
        
        mainFrame.add(mainPanel);
        
        // Show menu panel first
        cardLayout.show(mainPanel, "menu");
        mainFrame.setVisible(true);
    }

    private void createMenuPanel() {
        JPanel menuPanel = new JPanel(new BorderLayout());
        menuPanel.setBackground(new Color(240, 245, 250));
        
        // Header
        JLabel header = new JLabel("SheSafe", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 28));
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 40, 0));
        header.setForeground(new Color(0, 82, 165));
        menuPanel.add(header, BorderLayout.NORTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 10, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 100, 50, 100));
        buttonPanel.setBackground(new Color(240, 245, 250));
        
        // Style for all buttons
        Font buttonFont = new Font("Segoe UI", Font.PLAIN, 18);
        Color buttonColor = new Color(255, 182, 193);
        Color buttonTextColor = Color.BLACK;
       
        // Find Path Button
        JButton findPathBtn = createStyledButton("Find Safest Path", buttonFont, buttonColor, buttonTextColor);
        findPathBtn.addActionListener(e -> cardLayout.show(mainPanel, "findPath"));
        
    
        
        // Rate Location Button
        JButton rateBtn = createStyledButton("Rate Location Safety", buttonFont, buttonColor, buttonTextColor);
        rateBtn.addActionListener(e -> cardLayout.show(mainPanel, "rating"));
        
        // Top Locations Button
        JButton topLocationsBtn = createStyledButton("View Top Safest Locations", buttonFont, buttonColor, buttonTextColor);
        topLocationsBtn.addActionListener(e -> {
            displayTopSafestLocations();
            cardLayout.show(mainPanel, "topLocations");
        });

            // Emergency Button
            JButton emergencyBtn = createStyledButton("Emergency Contacts", buttonFont, buttonColor, buttonTextColor);
            emergencyBtn.addActionListener(e -> cardLayout.show(mainPanel, "emergency"));
        
        buttonPanel.add(findPathBtn);
        buttonPanel.add(rateBtn);
        buttonPanel.add(topLocationsBtn);
        buttonPanel.add(emergencyBtn);
        
        menuPanel.add(buttonPanel, BorderLayout.CENTER);
        
        // Footer
        JLabel footer = new JLabel("Stay Safe - © 2023 Pathfinder", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        footer.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        footer.setForeground(Color.GRAY);
        menuPanel.add(footer, BorderLayout.SOUTH);
        
        panels.put("menu", menuPanel);
        mainPanel.add(menuPanel, "menu");
    }

    private JButton createStyledButton(String text, Font font, Color bgColor, Color textColor) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }

    private void createFindPathPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Back button
        JButton backBtn = new JButton("← Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        panel.add(backBtn, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel title = new JLabel("Find Safest Path");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(title, gbc);
        
        // Source input
        gbc.gridy++;
        gbc.gridwidth = 1;
        contentPanel.add(new JLabel("Source Location:"), gbc);
        
        gbc.gridx++;
        JTextField sourceField = new JTextField(25);
        contentPanel.add(sourceField, gbc);
        
        // Destination input
        gbc.gridy++;
        gbc.gridx = 0;
        contentPanel.add(new JLabel("Destination Location:"), gbc);
        
        gbc.gridx++;
        JTextField destField = new JTextField(25);
        contentPanel.add(destField, gbc);
        
        // Find button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton findBtn = new JButton("Find Path");
        contentPanel.add(findBtn, gbc);
        
        // Results area
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        resultsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        contentPanel.add(scrollPane, gbc);
        
        // Add action listener to find button
        findBtn.addActionListener(e -> {
            String source = sourceField.getText();
            String destination = destField.getText();
            
            if (source.isEmpty() || destination.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter both source and destination locations", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                Result result = graph.dijkstra(source, destination);
                resultsArea.setText(result.toString());
            } catch (Exception ex) {
                resultsArea.setText("Error finding path: " + ex.getMessage());
            }
        });
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        panels.put("findPath", panel);
        mainPanel.add(panel, "findPath");
    }
    

    private void createEmergencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Back button
        JButton backBtn = new JButton("← Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        panel.add(backBtn, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel title = new JLabel("Emergency Contacts");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(title, gbc);
        
        // Location input
        gbc.gridy++;
        gbc.gridwidth = 1;
        contentPanel.add(new JLabel("Your Current Location:"), gbc);
        
        gbc.gridx++;
        JTextField locationField = new JTextField(25);
        contentPanel.add(locationField, gbc);
        
        // Find button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton findBtn = new JButton("Find Emergency Contacts");
        findBtn.addActionListener(e -> {
            String location = locationField.getText();
            
            if (location.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please enter your location", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            fetchEmergencyContacts(location);
        });
        contentPanel.add(findBtn, gbc);
        
        // Results area
        gbc.gridy++;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        contentPanel.add(scrollPane, gbc);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        panels.put("emergency", panel);
        mainPanel.add(panel, "emergency");
    }

    private void fetchEmergencyContacts(String locationName) {
        JPanel emergencyPanel = (JPanel) panels.get("emergency");
        JTextArea resultsArea = (JTextArea) ((JScrollPane) ((JPanel) emergencyPanel.getComponent(1)).getComponent(5)).getViewport().getView();
        
        String query = "SELECT ec.contact_name, ec.phone_number " +
                     "FROM emergencycontacts ec " +
                     "JOIN nodes n ON ec.node_id = n.NodeID " +
                     "WHERE n.NodeName = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, locationName);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("--- Emergency Contacts Near ").append(locationName).append(" ---\n\n");
            
            boolean found = false;
            while (rs.next()) {
                String name = rs.getString("contact_name");
                String phone = rs.getString("phone_number");
                sb.append("Name: ").append(name).append("\n");
                sb.append("Phone: ").append(phone).append("\n\n");
                found = true;
            }

            if (!found) {
                sb.append("No emergency contacts found for this location.");
            }
            
            resultsArea.setText(sb.toString());
        } catch (SQLException e) {
            resultsArea.setText("Error fetching emergency contacts: " + e.getMessage());
        }
    }

    private void createRatingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Back button
        JButton backBtn = new JButton("← Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        panel.add(backBtn, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel title = new JLabel("Rate Location Safety");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        contentPanel.add(title, gbc);
        
        // User ID input
        gbc.gridy++;
        gbc.gridwidth = 1;
        contentPanel.add(new JLabel("Your User ID:"), gbc);
        
        gbc.gridx++;
        JTextField userIdField = new JTextField(25);
        contentPanel.add(userIdField, gbc);
        
        // Location input
        gbc.gridy++;
        gbc.gridx = 0;
        contentPanel.add(new JLabel("Location Name:"), gbc);
        
        gbc.gridx++;
        JTextField locationField = new JTextField(25);
        contentPanel.add(locationField, gbc);
        
        // Rating selection
        gbc.gridy++;
        gbc.gridx = 0;
        contentPanel.add(new JLabel("Safety Rating:"), gbc);
        
        gbc.gridx++;
        JComboBox<String> ratingCombo = new JComboBox<>(new String[]{"Safe", "Neutral", "Unsafe"});
        contentPanel.add(ratingCombo, gbc);
        
        // Submit button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton submitBtn = new JButton("Submit Rating");
        submitBtn.addActionListener(e -> {
            String userId = userIdField.getText();
            String location = locationField.getText();
            String rating = (String) ratingCombo.getSelectedItem();
            
            if (userId.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "Please fill in all fields", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            rateLocation(userId, location, rating);
        });
        contentPanel.add(submitBtn, gbc);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        panels.put("rating", panel);
        mainPanel.add(panel, "rating");
    }

    private void rateLocation(String userId, String location, String rating) {
        try {
            // Get NodeID from name
            String nodeQuery = "SELECT NodeID FROM nodes WHERE NodeName = ?";
            try (PreparedStatement nodeStmt = conn.prepareStatement(nodeQuery)) {
                nodeStmt.setString(1, location);
                ResultSet rs = nodeStmt.executeQuery();

                if (rs.next()) {
                    int nodeId = rs.getInt("NodeID");

                    String insertQuery = "INSERT INTO userratings (node_id, user_id, safety_rating) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, nodeId);
                        insertStmt.setString(2, userId);
                        insertStmt.setString(3, rating);
                        insertStmt.executeUpdate();
                        JOptionPane.showMessageDialog(mainFrame, "Rating submitted successfully!", 
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "Location not found", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(mainFrame, "Error submitting rating: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createTopLocationsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Back button
        JButton backBtn = new JButton("← Back to Menu");
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "menu"));
        panel.add(backBtn, BorderLayout.NORTH);
        
        // Main content
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        JLabel title = new JLabel("Top 5 Safest Locations", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        contentPanel.add(title, BorderLayout.NORTH);
        
        JTextArea resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        panels.put("topLocations", panel);
        mainPanel.add(panel, "topLocations");
    }

    private void displayTopSafestLocations() {
        JPanel topLocationsPanel = (JPanel) panels.get("topLocations");
        JTextArea resultsArea = (JTextArea) ((JScrollPane) ((JPanel) topLocationsPanel.getComponent(1)).getComponent(1)).getViewport().getView();
        
        String query = "SELECT n.NodeName, " +
                     "AVG(CASE safety_rating " +
                     "     WHEN 'Safe' THEN 3 " +
                     "     WHEN 'Neutral' THEN 2 " +
                     "     WHEN 'Unsafe' THEN 1 END) AS avg_rating " +
                     "FROM userratings ur " +
                     "JOIN nodes n ON ur.node_id = n.NodeID " +
                     "GROUP BY n.NodeName " +
                     "ORDER BY avg_rating DESC " +
                     "LIMIT 5";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Top 5 Safest Locations Based on User Ratings:\n\n");
            
            while (rs.next()) {
                String name = rs.getString("NodeName");
                double avg = rs.getDouble("avg_rating");
                sb.append(String.format("• %s (Safety Score: %.2f)\n", name, avg));
            }
            
            resultsArea.setText(sb.toString());
        } catch (SQLException e) {
            resultsArea.setText("Error fetching top locations: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PathfinderUI());
    }
} 