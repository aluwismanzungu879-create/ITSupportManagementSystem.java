import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ITSupportManagementSystem {

    private static final String DATABASE_FILE = "support_database.csv";

    public static void main(String[] args) {
        createDatabaseFile();
        showLoginWindow();
    }

    // Creates the support database file if it does not exist
    private static void createDatabaseFile() {
        File file = new File(DATABASE_FILE);

        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("TicketID,UserID,Problem,Priority,Status");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Unable to create database: " + e.getMessage());
            }
        }
    }

    // Login window
    private static void showLoginWindow() {
        JFrame frame = new JFrame("IT Support Management System - Login");
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel userLabel = new JLabel("User ID:");
        JTextField userField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton clearButton = new JButton("Clear");

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(loginButton);
        panel.add(clearButton);

        frame.add(panel);
        frame.setVisible(true);

        loginButton.addActionListener(e -> {
            String userID = userField.getText().trim();
            String password = new String(passwordField.getPassword());

            // Demonstration login details
            if (userID.equals("admin") && password.equals("admin123")) {
                frame.dispose();
                showDashboard(userID);
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Invalid user ID or password.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        clearButton.addActionListener(e -> {
            userField.setText("");
            passwordField.setText("");
        });
    }

    // Main dashboard
    private static void showDashboard(String userID) {
        JFrame frame = new JFrame("IT Support Dashboard");
        frame.setSize(700, 450);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        JLabel heading = new JLabel("Welcome, " + userID,
                SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 20));

        JButton submitButton = new JButton("Submit Support Ticket");
        JButton viewButton = new JButton("View Support Database");
        JButton logoutButton = new JButton("Logout");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(viewButton);
        buttonPanel.add(logoutButton);

        frame.setLayout(new BorderLayout(10, 10));
        frame.add(heading, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        submitButton.addActionListener(e -> showSubmitTicketWindow(userID));
        viewButton.addActionListener(e -> showDatabaseWindow());
        logoutButton.addActionListener(e -> {
            frame.dispose();
            showLoginWindow();
        });
    }

    // Submit support ticket window
    private static void showSubmitTicketWindow(String userID) {
        JFrame frame = new JFrame("Submit Support Ticket");
        frame.setSize(450, 350);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel problemLabel = new JLabel("Problem Description:");
        JTextField problemField = new JTextField();

        JLabel priorityLabel = new JLabel("Priority:");
        JComboBox<String> priorityBox =
                new JComboBox<>(new String[]{"Low", "Medium", "High"});

        JLabel statusLabel = new JLabel("Status:");
        JLabel statusValue = new JLabel("Open");

        JButton submitButton = new JButton("Save Ticket");
        JButton cancelButton = new JButton("Cancel");

        panel.add(problemLabel);
        panel.add(problemField);
        panel.add(priorityLabel);
        panel.add(priorityBox);
        panel.add(statusLabel);
        panel.add(statusValue);
        panel.add(new JLabel());
        panel.add(new JLabel());
        panel.add(submitButton);
        panel.add(cancelButton);

        frame.add(panel);
        frame.setVisible(true);

        submitButton.addActionListener(e -> {
            String problem = problemField.getText().trim();
            String priority = priorityBox.getSelectedItem().toString();

            if (problem.isEmpty()) {
                JOptionPane.showMessageDialog(frame,
                        "Please enter a problem description.");
                return;
            }

            int ticketID = getNextTicketID();

            try (PrintWriter writer = new PrintWriter(
                    new FileWriter(DATABASE_FILE, true))) {

                // Replace commas to keep the CSV structure valid
                problem = problem.replace(",", " ");

                writer.println(ticketID + "," + userID + "," + problem
                        + "," + priority + ",Open");

                JOptionPane.showMessageDialog(frame,
                        "Support ticket saved successfully.\nTicket ID: "
                                + ticketID);

                frame.dispose();

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame,
                        "Error saving ticket: " + ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> frame.dispose());
    }

    // Finds the next ticket ID
    private static int getNextTicketID() {
        int nextID = 1;

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(DATABASE_FILE))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TicketID")) {
                    continue;
                }

                String[] data = line.split(",", -1);

                if (data.length > 0) {
                    try {
                        int currentID = Integer.parseInt(data[0]);
                        if (currentID >= nextID) {
                            nextID = currentID + 1;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error reading database: " + e.getMessage());
        }

        return nextID;
    }

    // Displays the support database
    private static void showDatabaseWindow() {
        JFrame frame = new JFrame("Support Database");
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);

        String[] columns = {
                "Ticket ID", "User ID", "Problem", "Priority", "Status"
        };

        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);

        loadDatabase(model);

        JButton updateButton = new JButton("Update Selected Ticket");
        JButton refreshButton = new JButton("Refresh");
        JButton closeButton = new JButton("Close");

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(updateButton);
        bottomPanel.add(refreshButton);
        bottomPanel.add(closeButton);

        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(table), BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        updateButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(frame,
                        "Please select a ticket first.");
                return;
            }

            String[] statuses = {"Open", "In Progress", "Resolved"};
            String newStatus = (String) JOptionPane.showInputDialog(
                    frame,
                    "Select new status:",
                    "Update Ticket",
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    statuses,
                    table.getValueAt(selectedRow, 4)
            );

            if (newStatus != null) {
                int ticketID = Integer.parseInt(
                        table.getValueAt(selectedRow, 0).toString());

                updateTicketStatus(ticketID, newStatus);
                model.setValueAt(newStatus, selectedRow, 4);

                JOptionPane.showMessageDialog(frame,
                        "Ticket status updated.");
            }
        });

        refreshButton.addActionListener(e -> {
            model.setRowCount(0);
            loadDatabase(model);
        });

        closeButton.addActionListener(e -> frame.dispose());
    }

    // Loads database records into the table
    private static void loadDatabase(DefaultTableModel model) {
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(DATABASE_FILE))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] data = line.split(",", -1);

                if (data.length == 5) {
                    model.addRow(data);
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error loading database: " + e.getMessage());
        }
    }

    // Updates a ticket status in the CSV database
    private static void updateTicketStatus(int ticketID, String newStatus) {
        List<String> records = new ArrayList<>();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(DATABASE_FILE))) {

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith(ticketID + ",")) {
                    String[] data = line.split(",", -1);

                    if (data.length == 5) {
                        data[4] = newStatus;
                        line = String.join(",", data);
                    }
                }

                records.add(line);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error reading ticket: " + e.getMessage());
            return;
        }

        try (PrintWriter writer =
                     new PrintWriter(new FileWriter(DATABASE_FILE))) {

            for (String record : records) {
                writer.println(record);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error updating ticket: " + e.getMessage());
        }
    }
}
