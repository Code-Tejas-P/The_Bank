package net.codejava;

import DataBase.constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BankingAppGUI extends JFrame {
    private constants c = new constants();

    public BankingAppGUI() {
        setTitle("Banking Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLocationRelativeTo(null); // Center the window

        openLoginPage(); // Open the login page
    }

    private void openLoginPage() {
        JFrame loginFrame = new JFrame("Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(300, 200);
        loginFrame.setLocationRelativeTo(null); // Center the window

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String jdbc = "jdbc:sqlite:The_Bank.db";
                Connection connection = null;
                try {
                    connection = DriverManager.getConnection(jdbc);
                    String username = usernameField.getText();
                    String password = new String(passwordField.getPassword());
                    if (c.validateUser(username, password)) {
                        openMainMenu(username, c.current_acc_num, c.getCurrentBalance(connection));
                        connection.close();
                        loginFrame.dispose(); // Close the login frame
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid username or password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });

        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = JOptionPane.showInputDialog(null, "Enter your name:");
                String username = JOptionPane.showInputDialog(null, "Choose a username:");
                String password = JOptionPane.showInputDialog(null, "Choose a password:");
                c.insert_user(name, username, password);
            }
        });

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        loginFrame.add(panel);
        loginFrame.setVisible(true);
    }

    private void openMainMenu(String username, double accountNumber, double balance) {
        JFrame mainMenuFrame = new JFrame("Main Menu");
        mainMenuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainMenuFrame.setSize(500, 650);
        mainMenuFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(7, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel userInfoLabel = new JLabel("Welcome, " + username + "!");
        userInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 30));

        JLabel accountNumberLabel = new JLabel("Account Number: " + accountNumber);
        accountNumberLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel balanceLabel = new JLabel("Balance: $" + balance);
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton depositButton = new JButton("Deposit");
        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openDepositWindow(balanceLabel); // Pass balance label to update
            }
        });

        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openWithdrawalWindow(balanceLabel); // Pass balance label to update
            }
        });

        JButton transactionLogButton = new JButton("Transaction Log");
        transactionLogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openTransactionLogWindow();
            }
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainMenuFrame.dispose(); // Close the main menu frame
                openLoginPage(); // Open the login page
            }
        });

        panel.add(userInfoLabel);
        panel.add(accountNumberLabel);
        panel.add(balanceLabel);
        panel.add(depositButton);
        panel.add(withdrawButton);
        panel.add(transactionLogButton);
        panel.add(logoutButton); // Add logout button

        mainMenuFrame.add(panel);
        mainMenuFrame.setVisible(true);
    }

    private void openDepositWindow(JLabel balanceLabel) {
        JFrame depositFrame = new JFrame("Deposit");
        depositFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        depositFrame.setSize(400, 150);
        depositFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField amountField = new JTextField();
        JButton depositButton = new JButton("Deposit");

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    c.deposit(amount,balanceLabel);
                    JOptionPane.showMessageDialog(null, "Deposit successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    depositFrame.dispose(); // Close the deposit window after successful deposit
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(new JLabel("Enter amount to deposit:"));
        panel.add(amountField);
        panel.add(depositButton);

        depositFrame.add(panel);
        depositFrame.setVisible(true);
    }

    private void openWithdrawalWindow(JLabel balanceLabel) {
        JFrame withdrawalFrame = new JFrame("Withdrawal");
        withdrawalFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        withdrawalFrame.setSize(400, 150);
        withdrawalFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField amountField = new JTextField();
        JButton withdrawalButton = new JButton("Withdraw");

        withdrawalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    c.withdraw(amount, balanceLabel);
                    JOptionPane.showMessageDialog(null, "Withdrawal successful.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    withdrawalFrame.dispose(); // Close the withdrawal window after successful withdrawal
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        panel.add(new JLabel("Enter amount to withdraw:"));
        panel.add(amountField);
        panel.add(withdrawalButton);

        withdrawalFrame.add(panel);
        withdrawalFrame.setVisible(true);
    }

    private void openTransactionLogWindow() {
        JFrame logFrame = new JFrame("Transaction Log");
        logFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        logFrame.setSize(400, 300);
        logFrame.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logTextArea);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logFrame.dispose();
            }
        });

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.SOUTH);

        logFrame.add(panel);
        logFrame.setVisible(true);

        // Populate the text area with the transaction log
        logTextArea.setText(c.getTransactionLog());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BankingAppGUI();
            }
        });
    }
}
