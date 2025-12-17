package addressbook;

import javax.swing.table.AbstractTableModel;
import java.util.List;
import java.util.ArrayList;

/**
 * Custom Table Model for Contact JTable
 * Provides data binding between Contact objects and JTable
 */
public class ContactTableModel extends AbstractTableModel {
    private final String[] columnNames = {
        "ID", "Name", "Phone", "Email", "Address", "Notes", "Created", "Updated"
    };
    private List<Contact> contacts;
    
    public ContactTableModel() {
        this.contacts = new ArrayList<>();
    }
    
    public ContactTableModel(List<Contact> contacts) {
        this.contacts = contacts != null ? contacts : new ArrayList<>();
    }
    
    @Override
    public int getRowCount() {
        return contacts.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class;
            default: return String.class;
        }
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false; // Make table read-only
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= contacts.size()) {
            return null;
        }
        
        Contact contact = contacts.get(rowIndex);
        switch (columnIndex) {
            case 0: return contact.getId();
            case 1: return contact.getName();
            case 2: return contact.getPhone();
            case 3: return contact.getEmail();
            case 4: return truncateText(contact.getAddress(), 30);
            case 5: return truncateText(contact.getNotes(), 25);
            case 6: return contact.getFormattedCreatedAt();
            case 7: return contact.getFormattedUpdatedAt();
            default: return null;
        }
    }
    
    // Utility method to truncate long text
    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    // Add contact to model
    public void addContact(Contact contact) {
        contacts.add(contact);
        int row = contacts.size() - 1;
        fireTableRowsInserted(row, row);
    }
    
    // Remove contact from model
    public void removeContact(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < contacts.size()) {
            contacts.remove(rowIndex);
            fireTableRowsDeleted(rowIndex, rowIndex);
        }
    }
    
    // Update contact in model
    public void updateContact(int rowIndex, Contact contact) {
        if (rowIndex >= 0 && rowIndex < contacts.size()) {
            contacts.set(rowIndex, contact);
            fireTableRowsUpdated(rowIndex, rowIndex);
        }
    }
    
    // Get contact at specific row
    public Contact getContactAt(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < contacts.size()) {
            return contacts.get(rowIndex);
        }
        return null;
    }
    
    // Clear all contacts
    public void clearContacts() {
        int size = contacts.size();
        if (size > 0) {
            contacts.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }
    
    // Set new contacts list
    public void setContacts(List<Contact> newContacts) {
        this.contacts = newContacts != null ? newContacts : new ArrayList<>();
        fireTableDataChanged();
    }
    
    // Get all contacts
    public List<Contact> getAllContacts() {
        return new ArrayList<>(contacts);
    }
    
    // Get contact count
    public int getContactCount() {
        return contacts.size();
    }
    
    // Find contact by ID
    public int findContactById(int id) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }
    
    // Filter contacts by search term
    public void filterContacts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return;
        }
        
        List<Contact> filteredContacts = new ArrayList<>();
        String lowerSearchTerm = searchTerm.toLowerCase();
        
        for (Contact contact : contacts) {
            if (contact.getName().toLowerCase().contains(lowerSearchTerm) ||
                contact.getPhone().toLowerCase().contains(lowerSearchTerm) ||
                contact.getEmail().toLowerCase().contains(lowerSearchTerm) ||
                contact.getAddress().toLowerCase().contains(lowerSearchTerm) ||
                contact.getNotes().toLowerCase().contains(lowerSearchTerm)) {
                filteredContacts.add(contact);
            }
        }
        
        setContacts(filteredContacts);
    }
    
    // Sort contacts by column
    public void sortByColumn(int column, boolean ascending) {
        contacts.sort((c1, c2) -> {
            int result = 0;
            switch (column) {
                case 0: result = Integer.compare(c1.getId(), c2.getId()); break;
                case 1: result = c1.getName().compareToIgnoreCase(c2.getName()); break;
                case 2: result = c1.getPhone().compareToIgnoreCase(c2.getPhone()); break;
                case 3: result = c1.getEmail().compareToIgnoreCase(c2.getEmail()); break;
                case 4: result = c1.getAddress().compareToIgnoreCase(c2.getAddress()); break;
                case 5: result = c1.getNotes().compareToIgnoreCase(c2.getNotes()); break;
                case 6: result = c1.getCreatedAt().compareTo(c2.getCreatedAt()); break;
                case 7: result = c1.getUpdatedAt().compareTo(c2.getUpdatedAt()); break;
            }
            return ascending ? result : -result;
        });
        fireTableDataChanged();
    }
}
