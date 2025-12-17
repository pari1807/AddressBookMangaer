package addressbook;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Main GUI Class for Address Book Management System
 * Provides the complete user interface using Java Swing
 */
public class AddressBookGUI extends JFrame {
    private DatabaseManager dbManager;
    private ContactTableModel tableModel;
    private JTable contactTable;
    private JTextField searchField;
    private JTextField nameField, phoneField, emailField, addressField, notesField;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JButton exportButton, importButton, backupButton, themeButton;
    private JLabel statusLabel;
    private Contact selectedContact;
    private boolean isDarkMode = false;
    
    // Color themes
    private final Color LIGHT_BG = new Color(248, 249, 250);
    private final Color DARK_BG = new Color(33, 37, 41);
    private final Color LIGHT_PANEL = Color.WHITE;
    private final Color DARK_PANEL = new Color(52, 58, 64);
    private final Color LIGHT_TEXT = Color.BLACK;
    private final Color DARK_TEXT = Color.WHITE;
    private final Color ACCENT_COLOR = new Color(0, 123, 255);
    
    public AddressBookGUI() {
        dbManager = DatabaseManager.getInstance();
        dbManager.initializeDatabase();
        dbManager.insertSampleData(); // Add sample data
        
        initializeComponents();
        setupLayout();
        loadContacts();
        applyTheme();
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setTitle("Address Book Management System v2.0");
        setVisible(true);
    }
    
    private void initializeComponents() {
        // Initialize table and model
        tableModel = new ContactTableModel();
        contactTable = new JTable(tableModel);
        
        // Configure table
        contactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactTable.setRowHeight(30);
        contactTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = contactTable.getSelectedRow();
                if (selectedRow >= 0) {
                    selectedContact = tableModel.getContactAt(selectedRow);
                    populateFields();
                }
            }
        });
        
        // Add double-click listener for editing
        contactTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = contactTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        selectedContact = tableModel.getContactAt(row);
                        populateFields();
                    }
                }
            }
        });
        
        // Configure table header
        JTableHeader header = contactTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 12));
        header.setReorderingAllowed(false);
        
        // Add table sorter
        TableRowSorter<ContactTableModel> sorter = new TableRowSorter<>(tableModel);
        contactTable.setRowSorter(sorter);
        
        // Initialize form fields
        nameField = new JTextField(20);
        phoneField = new JTextField(20);
        emailField = new JTextField(20);
        addressField = new JTextField(20);
        notesField = new JTextField(20);
        
        // Initialize search field with proper listener
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { performSearch(); }
        });
        
        // Initialize buttons
        addButton = new JButton("Add Contact");
        updateButton = new JButton("Update Contact");
        deleteButton = new JButton("Delete Contact");
        clearButton = new JButton("Clear Fields");
        exportButton = new JButton("Export CSV");
        importButton = new JButton("Import CSV");
        backupButton = new JButton("Backup DB");
        themeButton = new JButton("🌙 Dark Mode");
        
        // Button listeners
        addButton.addActionListener(e -> addContact());
        updateButton.addActionListener(e -> updateContact());
        deleteButton.addActionListener(e -> deleteContact());
        clearButton.addActionListener(e -> clearFields());
        exportButton.addActionListener(e -> exportToCSV());
        importButton.addActionListener(e -> importFromCSV());
        backupButton.addActionListener(e -> backupDatabase());
        themeButton.addActionListener(e -> toggleTheme());
        
        // Status label
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        // Initially disable update and delete buttons
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel - Search and theme toggle
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(themeButton);
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Contact Information"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Add form fields
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        formPanel.add(phoneField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        formPanel.add(emailField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1;
        formPanel.add(addressField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        formPanel.add(notesField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(importButton);
        buttonPanel.add(backupButton);
        
        // Left panel - Form and buttons
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(formPanel, BorderLayout.CENTER);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Table panel
        JScrollPane scrollPane = new JScrollPane(contactTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Contacts"));
        
        // Main layout
        add(topPanel, BorderLayout.NORTH);
        add(leftPanel, BorderLayout.WEST);
        add(scrollPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);
    }
    
    private void loadContacts() {
        List<Contact> contacts = dbManager.getAllContacts();
        tableModel.setContacts(contacts);
        updateStatus("Loaded " + contacts.size() + " contacts");
    }
    
    private void performSearch() {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadContacts();
        } else {
            List<Contact> contacts = dbManager.searchContacts(searchTerm);
            tableModel.setContacts(contacts);
            updateStatus("Found " + contacts.size() + " contacts matching '" + searchTerm + "'");
        }
    }
    
    private void addContact() {
        if (!validateFields()) return;
        
        Contact contact = new Contact(
            nameField.getText().trim(),
            phoneField.getText().trim(),
            emailField.getText().trim(),
            addressField.getText().trim(),
            notesField.getText().trim()
        );
        
        if (dbManager.insertContact(contact)) {
            loadContacts();
            clearFields();
            updateStatus("Contact added successfully!");
        } else {
            showError("Failed to add contact. Please check if email already exists.");
        }
    }
    
    private void updateContact() {
        if (selectedContact == null) {
            showError("Please select a contact to update.");
            return;
        }
        
        if (!validateFields()) return;
        
        selectedContact.setName(nameField.getText().trim());
        selectedContact.setPhone(phoneField.getText().trim());
        selectedContact.setEmail(emailField.getText().trim());
        selectedContact.setAddress(addressField.getText().trim());
        selectedContact.setNotes(notesField.getText().trim());
        
        if (dbManager.updateContact(selectedContact)) {
            loadContacts();
            clearFields();
            updateStatus("Contact updated successfully!");
        } else {
            showError("Failed to update contact.");
        }
    }
    
    private void deleteContact() {
        if (selectedContact == null) {
            showError("Please select a contact to delete.");
            return;
        }
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this contact?\n" + selectedContact.getName(),
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            if (dbManager.deleteContact(selectedContact.getId())) {
                loadContacts();
                clearFields();
                updateStatus("Contact deleted successfully!");
            } else {
                showError("Failed to delete contact.");
            }
        }
    }
    
    private void clearFields() {
        nameField.setText("");
        phoneField.setText("");
        emailField.setText("");
        addressField.setText("");
        notesField.setText("");
        selectedContact = null;
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        contactTable.clearSelection();
    }
    
    private void populateFields() {
        if (selectedContact != null) {
            nameField.setText(selectedContact.getName());
            phoneField.setText(selectedContact.getPhone());
            emailField.setText(selectedContact.getEmail());
            addressField.setText(selectedContact.getAddress());
            notesField.setText(selectedContact.getNotes());
            updateButton.setEnabled(true);
            deleteButton.setEnabled(true);
        }
    }
    
    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            showError("Name is required.");
            nameField.requestFocus();
            return false;
        }
        
        if (!Contact.isValidName(nameField.getText().trim())) {
            showError("Name must be at least 2 characters long.");
            nameField.requestFocus();
            return false;
        }
        
        if (!Contact.isValidPhone(phoneField.getText().trim())) {
            showError("Please enter a valid phone number (10-15 digits).");
            phoneField.requestFocus();
            return false;
        }
        
        if (!Contact.isValidEmail(emailField.getText().trim())) {
            showError("Please enter a valid email address.");
            emailField.requestFocus();
            return false;
        }
        
        if (!Contact.isValidAddress(addressField.getText().trim())) {
            showError("Address must be at least 5 characters long.");
            addressField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.setSelectedFile(new File("contacts_backup.csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                FileWriter writer = new FileWriter(file);
                
                // Write header
                writer.write("Name,Phone,Email,Address,Notes,Created,Updated\n");
                
                // Write data
                List<Contact> contacts = tableModel.getAllContacts();
                for (Contact contact : contacts) {
                    writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        contact.getName(),
                        contact.getPhone(),
                        contact.getEmail(),
                        contact.getAddress(),
                        contact.getNotes(),
                        contact.getFormattedCreatedAt(),
                        contact.getFormattedUpdatedAt()
                    ));
                }
                
                writer.close();
                updateStatus("Contacts exported to " + file.getName());
            } catch (IOException e) {
                showError("Failed to export contacts: " + e.getMessage());
            }
        }
    }
    
    private void importFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            // Implementation for CSV import would go here
            updateStatus("CSV import feature - Implementation needed");
        }
    }
    
    private void backupDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("SQL Files", "sql"));
        fileChooser.setSelectedFile(new File("addressbook_backup.sql"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String backupPath = fileChooser.getSelectedFile().getAbsolutePath();
            if (dbManager.backupDatabase(backupPath)) {
                updateStatus("Database backed up successfully!");
            } else {
                showError("Failed to backup database.");
            }
        }
    }
    
    private void toggleTheme() {
        isDarkMode = !isDarkMode;
        applyTheme();
        themeButton.setText(isDarkMode ? "☀️ Light Mode" : "🌙 Dark Mode");
    }
    
    private void applyTheme() {
        Color bgColor = isDarkMode ? DARK_BG : LIGHT_BG;
        Color panelColor = isDarkMode ? DARK_PANEL : LIGHT_PANEL;
        Color textColor = isDarkMode ? DARK_TEXT : LIGHT_TEXT;
        
        // Apply to main window
        getContentPane().setBackground(bgColor);
        
        // Apply to all components
        applyThemeToComponent(this, bgColor, panelColor, textColor);
        
        // Special handling for table
        contactTable.setBackground(panelColor);
        contactTable.setForeground(textColor);
        contactTable.getTableHeader().setBackground(ACCENT_COLOR);
        contactTable.getTableHeader().setForeground(Color.WHITE);
        
        repaint();
    }
    
    private void applyThemeToComponent(Container container, Color bgColor, Color panelColor, Color textColor) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel) {
                component.setBackground(panelColor);
                applyThemeToComponent((Container) component, bgColor, panelColor, textColor);
            } else if (component instanceof JTextField) {
                component.setBackground(panelColor);
                component.setForeground(textColor);
            } else if (component instanceof JLabel) {
                component.setForeground(textColor);
            } else if (component instanceof JButton) {
                component.setBackground(ACCENT_COLOR);
                component.setForeground(Color.WHITE);
            }
            
            if (component instanceof Container) {
                applyThemeToComponent((Container) component, bgColor, panelColor, textColor);
            }
        }
    }
    
    private void updateStatus(String message) {
        statusLabel.setText(message);
        Timer timer = new Timer(3000, e -> statusLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new AddressBookGUI());
    }
}