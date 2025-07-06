package View.main;

import Model.DatabaseManager;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.List;
import java.util.Vector;

/**
 * GUI class for viewing and browsing games stored in the database.
 * Provides filtering, searching, and detailed game viewing capabilities.
 */
public class GameViewer extends JFrame {
    private JTable gamesTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JTextArea gameDetailsArea;
    private JScrollPane detailsScrollPane;
    
    // Column names for the games table
    private static final String[] COLUMN_NAMES = {
        "ID", "Date", "Event", "White Player", "Black Player", "Result", "ECO"
    };
    
    public GameViewer() {
        setTitle("Chess Game Database Viewer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        initializeComponents();
        loadGames();
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        // Main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top panel for search and filter controls
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table and details
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        
        // Games table
        createGamesTable();
        JScrollPane tableScrollPane = new JScrollPane(gamesTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 400));
        centerPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Game details panel
        createGameDetailsPanel();
        centerPanel.add(detailsScrollPane, BorderLayout.EAST);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel for buttons
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Search field
        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(20);
        searchField.addActionListener(e -> performSearch());
        
        // Filter combo box
        JLabel filterLabel = new JLabel("Filter by:");
        filterCombo = new JComboBox<>(new String[]{"All Games", "White Wins", "Black Wins", "Draws"});
        filterCombo.addActionListener(e -> performSearch());
        
        // Search button
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        
        // Refresh button
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadGames());
        
        panel.add(searchLabel);
        panel.add(searchField);
        panel.add(filterLabel);
        panel.add(filterCombo);
        panel.add(searchButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    private void createGamesTable() {
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        gamesTable = new JTable(tableModel);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesTable.getTableHeader().setReorderingAllowed(false);
        
        // Add mouse listener for row selection
        gamesTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    showGameDetails();
                }
            }
        });
        
        // Set column widths
        gamesTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        gamesTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Date
        gamesTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Event
        gamesTable.getColumnModel().getColumn(3).setPreferredWidth(120); // White
        gamesTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Black
        gamesTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Result
        gamesTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // ECO
    }
    
    private void createGameDetailsPanel() {
        gameDetailsArea = new JTextArea();
        gameDetailsArea.setEditable(false);
        gameDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        gameDetailsArea.setLineWrap(true);
        gameDetailsArea.setWrapStyleWord(true);
        
        detailsScrollPane = new JScrollPane(gameDetailsArea);
        detailsScrollPane.setPreferredSize(new Dimension(350, 400));
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Game Details"));
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        
        JButton exportButton = new JButton("Export Selected Game");
        exportButton.addActionListener(e -> exportSelectedGame());
        
        JButton deleteButton = new JButton("Delete Selected Game");
        deleteButton.addActionListener(e -> deleteSelectedGame());
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        panel.add(exportButton);
        panel.add(deleteButton);
        panel.add(closeButton);
        
        return panel;
    }
    
    private void loadGames() {
        tableModel.setRowCount(0);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:games.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("""
                 SELECT id, event, site, date, white_player, black_player, result, eco, moves
                 FROM games 
                 ORDER BY date DESC, created_at DESC
                 """)) {
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("date"));
                row.add(rs.getString("event"));
                row.add(rs.getString("white_player"));
                row.add(rs.getString("black_player"));
                row.add(rs.getString("result"));
                row.add(rs.getString("eco"));
                
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading games: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        String filterType = (String) filterCombo.getSelectedItem();
        
        tableModel.setRowCount(0);
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, event, site, date, white_player, black_player, result, eco, moves ");
        sql.append("FROM games WHERE 1=1 ");
        
        // Add search conditions
        if (!searchTerm.isEmpty()) {
            sql.append("AND (white_player LIKE ? OR black_player LIKE ? OR event LIKE ?) ");
        }
        
        // Add filter conditions
        if (filterType != null) {
            switch (filterType) {
                case "White Wins":
                    sql.append("AND result = '1-0' ");
                    break;
                case "Black Wins":
                    sql.append("AND result = '0-1' ");
                    break;
                case "Draws":
                    sql.append("AND result = '1/2-1/2' ");
                    break;
            }
        }
        
        sql.append("ORDER BY date DESC, created_at DESC");
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:games.db");
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (!searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("date"));
                row.add(rs.getString("event"));
                row.add(rs.getString("white_player"));
                row.add(rs.getString("black_player"));
                row.add(rs.getString("result"));
                row.add(rs.getString("eco"));
                
                tableModel.addRow(row);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching games: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void showGameDetails() {
        int selectedRow = gamesTable.getSelectedRow();
        if (selectedRow == -1) {
            gameDetailsArea.setText("No game selected");
            return;
        }
        
        int gameId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:games.db");
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM games WHERE id = ?")) {
            
            pstmt.setInt(1, gameId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Game ID: ").append(rs.getInt("id")).append("\n\n");
                details.append("[Event \"").append(rs.getString("event")).append("\"]\n");
                details.append("[Site \"").append(rs.getString("site")).append("\"]\n");
                details.append("[Date \"").append(rs.getString("date")).append("\"]\n");
                details.append("[Round \"1\"]\n");
                details.append("[White \"").append(rs.getString("white_player")).append("\"]\n");
                details.append("[Black \"").append(rs.getString("black_player")).append("\"]\n");
                details.append("[Result \"").append(rs.getString("result")).append("\"]\n");
                details.append("[WhiteElo \"\"]\n");
                details.append("[BlackElo \"\"]\n");
                details.append("[ECO \"").append(rs.getString("eco")).append("\"]\n\n");
                details.append(rs.getString("moves"));
                
                gameDetailsArea.setText(details.toString());
            }
            
        } catch (SQLException e) {
            gameDetailsArea.setText("Error loading game details: " + e.getMessage());
        }
    }
    
    private void exportSelectedGame() {
        int selectedRow = gamesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a game to export", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int gameId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Game");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "PGN Files (*.pgn)", "pgn"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:games.db");
                 PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT * FROM games WHERE id = ?")) {
                
                pstmt.setInt(1, gameId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    StringBuilder pgnContent = new StringBuilder();
                    
                    // Build PGN header
                    pgnContent.append("[Event \"").append(rs.getString("event") != null ? rs.getString("event") : "?").append("\"]\n");
                    pgnContent.append("[Site \"").append(rs.getString("site") != null ? rs.getString("site") : "?").append("\"]\n");
                    pgnContent.append("[Date \"").append(rs.getString("date") != null ? rs.getString("date") : "????.??.??").append("\"]\n");
                    pgnContent.append("[Round \"1\"]\n");
                    pgnContent.append("[White \"").append(rs.getString("white_player") != null ? rs.getString("white_player") : "?").append("\"]\n");
                    pgnContent.append("[Black \"").append(rs.getString("black_player") != null ? rs.getString("black_player") : "?").append("\"]\n");
                    pgnContent.append("[Result \"").append(rs.getString("result") != null ? rs.getString("result") : "*").append("\"]\n");
                    pgnContent.append("[WhiteElo \"\"]\n");
                    pgnContent.append("[BlackElo \"\"]\n");
                    pgnContent.append("[ECO \"").append(rs.getString("eco") != null ? rs.getString("eco") : "").append("\"]\n\n");
                    
                    // Add moves
                    String moves = rs.getString("moves");
                    if (moves != null && !moves.trim().isEmpty()) {
                        pgnContent.append(moves);
                    } else {
                        pgnContent.append("*");
                    }
                    
                    // Ensure the file has .pgn extension
                    java.io.File selectedFile = fileChooser.getSelectedFile();
                    String filePath = selectedFile.getAbsolutePath();
                    if (!filePath.toLowerCase().endsWith(".pgn")) {
                        selectedFile = new java.io.File(filePath + ".pgn");
                    }
                    
                    // Write the file
                    java.nio.file.Files.write(
                        selectedFile.toPath(),
                        pgnContent.toString().getBytes("UTF-8")
                    );
                    
                    JOptionPane.showMessageDialog(this, 
                        "Game exported successfully to:\n" + selectedFile.getAbsolutePath(), 
                        "Export Complete", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Game not found in database", 
                        "Export Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception e) {
                e.printStackTrace(); // Print stack trace for debugging
                JOptionPane.showMessageDialog(this, 
                    "Error exporting game: " + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void deleteSelectedGame() {
        int selectedRow = gamesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a game to delete", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int gameId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete this game? This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:games.db");
                 PreparedStatement pstmt = conn.prepareStatement(
                     "DELETE FROM games WHERE id = ?")) {
                
                pstmt.setInt(1, gameId);
                int affected = pstmt.executeUpdate();
                
                if (affected > 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Game deleted successfully!", 
                        "Delete Complete", 
                        JOptionPane.INFORMATION_MESSAGE);
                    loadGames(); // Refresh the table
                    gameDetailsArea.setText(""); // Clear details
                }
                
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting game: " + e.getMessage(), 
                    "Delete Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Main method for testing the GameViewer independently.
     */
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new GameViewer();
        });
    }
} 