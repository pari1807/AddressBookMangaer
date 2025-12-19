package addressbook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Main Entry Point with Login Authentication
 * Handles user authentication before accessing the Address Book
 */
public class Main extends JFrame {
    private DatabaseManager dbManager;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton, exitButton;
    private JLabel statusLabel;
    
    public Main() {
        dbManager = DatabaseManager.getInstance();
        dbManager.initializeDatabase();
        
        initializeLoginGUI();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setTitle("Address Book - Login");
        setResizable(false);
        setVisible(true);
    }
    
    private void initializeLoginGUI() {
        setLayout(new BorderLayout());
        
        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(0, 133, 255));
        JLabel titleLabel = new JLabel("Address Book Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        
        // Login form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        formPanel.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Username field
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(usernameField, gbc);
        
        // Password field
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(passwordField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        
        exitButton = new JButton("Exit");
        exitButton.setBackground(new Color(220, 53, 69));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        // Status label
        statusLabel = new JLabel("Please enter your credentials");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(new Color(108, 117, 125));
        
        // Add action listeners
        loginButton.addActionListener(new LoginActionListener());
        exitButton.addActionListener(e -> System.exit(0));
        
        // Add Enter key support
        getRootPane().setDefaultButton(loginButton);
        
        // Layout
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(buttonPanel, gbc);
        
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
        
        // Set default credentials hint
        usernameField.setText("admin");
        passwordField.setText("admin123");
    }
    
    private class LoginActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            
            if (username.isEmpty() || password.isEmpty()) {
                showStatus("Please enter both username and password", Color.RED);
                return;
            }
            
            // Disable login button during authentication
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
            
            // Create a SwingWorker for background authentication
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() {
                    return dbManager.authenticateUser(username, password);
                }
                
                @Override
                protected void done() {
                    try {
                        boolean authenticated = get();
                        if (authenticated) {
                            showStatus("Login successful! Loading Address Book...", Color.GREEN);
                            
                            // Delay to show success message
                            Timer timer = new Timer(1000, evt -> {
                                dispose(); // Close login window
                                new AddressBookGUI(); // Open main application
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            showStatus("Invalid username or password", Color.RED);
                            passwordField.setText("");
                            passwordField.requestFocus();
                        }
                    } catch (Exception ex) {
                        showStatus("Authentication error: " + ex.getMessage(), Color.RED);
                    } finally {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                    }
                }
            };
            
            worker.execute();
        }
    }
    
    private void showStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
        
        // Reset status after 3 seconds
        Timer timer = new Timer(3000, e -> {
            statusLabel.setText("Please enter your credentials");
            statusLabel.setForeground(new Color(108, 117, 125));
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create and show login window
        SwingUtilities.invokeLater(() -> {
            new Main();
        });
    }
}