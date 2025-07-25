import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class RegistrationForm extends JFrame {
    private JTextField nameField, contactField;
    private JRadioButton maleRadio, femaleRadio;
    private JComboBox<String> dayCombo, monthCombo, yearCombo;
    private JTextArea addressArea;
    private JCheckBox termsCheckBox;
    private JButton submitButton, resetButton;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    public RegistrationForm() {
        setTitle("Registration Form");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // === Form Panel ===
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        nameField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);

        // Contact
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Contact:"), gbc);
        contactField = new JTextField(20);
        gbc.gridx = 1;
        formPanel.add(contactField, gbc);

        // Gender
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Gender:"), gbc);
        maleRadio = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);
        JPanel genderPanel = new JPanel();
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        gbc.gridx = 1;
        formPanel.add(genderPanel, gbc);

        // DOB
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("DOB:"), gbc);
        String[] days = new String[31];
        for (int i = 1; i <= 31; i++) days[i - 1] = String.valueOf(i);
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                           "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] years = new String[35];
        for (int i = 0; i < 35; i++) years[i] = String.valueOf(1990 + i);
        dayCombo = new JComboBox<>(days);
        monthCombo = new JComboBox<>(months);
        yearCombo = new JComboBox<>(years);
        JPanel dobPanel = new JPanel();
        dobPanel.add(dayCombo);
        dobPanel.add(monthCombo);
        dobPanel.add(yearCombo);
        gbc.gridx = 1;
        formPanel.add(dobPanel, gbc);

        // Address
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Address:"), gbc);
        addressArea = new JTextArea(3, 20);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(addressArea), gbc);

        // Terms
        termsCheckBox = new JCheckBox("Accept Terms and Conditions.");
        gbc.gridx = 1; gbc.gridy = 5;
        formPanel.add(termsCheckBox, gbc);

        // Buttons
        submitButton = new JButton("Submit");
        resetButton = new JButton("Reset");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(resetButton);
        gbc.gridx = 1; gbc.gridy = 6;
        formPanel.add(buttonPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // === Table Panel ===
        String[] columnNames = {"Name", "Contact", "Gender", "DOB", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0);
        dataTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(dataTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // === Submit Button Action ===
        submitButton.addActionListener(e -> {
            if (!termsCheckBox.isSelected()) {
                JOptionPane.showMessageDialog(this, "Please accept the terms.");
                return;
            }

            String name = nameField.getText().trim();
            String contact = contactField.getText().trim();
            String gender = maleRadio.isSelected() ? "Male" : (femaleRadio.isSelected() ? "Female" : "");
            String dob = dayCombo.getSelectedItem() + "-" + monthCombo.getSelectedItem() + "-" + yearCombo.getSelectedItem();
            String address = addressArea.getText().trim();

            if (name.isEmpty() || contact.isEmpty() || gender.isEmpty() || address.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            // Add to table
            tableModel.addRow(new Object[]{name, contact, gender, dob, address});

            // Save to DB
            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/registration", "root", "");
                String sql = "INSERT INTO users (id, name, contact, gender, dob, address) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pst = conn.prepareStatement(sql);
                int id = (int) (Math.random() * 10000); // random 4-digit ID
                pst.setInt(1, id);
                pst.setString(2, name);
                pst.setString(3, contact);
                pst.setString(4, gender);
                pst.setString(5, dob);
                pst.setString(6, address);
                pst.executeUpdate();
                conn.close();
                JOptionPane.showMessageDialog(this, "User saved to database.");
                loadTableData(); // Refresh table
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
        });

        // === Reset Button Action ===
        resetButton.addActionListener(e -> {
            nameField.setText("");
            contactField.setText("");
            genderGroup.clearSelection();
            dayCombo.setSelectedIndex(0);
            monthCombo.setSelectedIndex(0);
            yearCombo.setSelectedIndex(0);
            addressArea.setText("");
            termsCheckBox.setSelected(false);
        });

        // Load table data on start
        loadTableData();
    }

    private void loadTableData() {
        try {
            tableModel.setRowCount(0); // Clear table
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/registration", "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name, contact, gender, dob, address FROM users");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("contact"),
                        rs.getString("gender"),
                        rs.getString("dob"),
                        rs.getString("address")
                });
            }

            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationForm().setVisible(true));
    }
}
