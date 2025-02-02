package DataBase;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Scanner;
public class constants {
    public static String currentUser;
    public static double balance;
    public static double current_acc_num;
    private static final String jdbc = "jdbc:sqlite:The_Bank.db";
    private static final String sql_user = "root";
    private static final String sql_pass = "root";

    public void sql_login() {
        try {
            Connection C = DriverManager.getConnection(jdbc);
            System.out.println("Connection Sucessfull");
        } catch (Exception e) {
            System.out.println("Connection Not Sucessfull");
            e.printStackTrace();
        }
    }

    public void insert_user(String name, String username, String password) {
        try {
            if (isUsernameTaken(username)) {
                JOptionPane.showMessageDialog(null, "Error: Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Connection con = DriverManager.getConnection(jdbc);
            long accountNumber = generateUniqueAccountNumber(con);
            if (accountNumber == -1) {
                JOptionPane.showMessageDialog(null, "Error: Unable to generate account number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Insert user into 'users' table with account number
            String insertUserQuery = "INSERT INTO users (Name,usersname, password, account_number) VALUES (?, ?, ?,?)";
            PreparedStatement insertUserStatement = con.prepareStatement(insertUserQuery);
            insertUserStatement.setString(1, name);
            insertUserStatement.setString(2, username);
            insertUserStatement.setString(3, password);
            insertUserStatement.setLong(4, accountNumber);
            String welcomeMessage = "Welcome " + name + "\nThis is your Account number: " + accountNumber + "\n Remember it !!";
            JOptionPane.showMessageDialog(null, welcomeMessage, "Welcome", JOptionPane.INFORMATION_MESSAGE);
            int rowsInserted = insertUserStatement.executeUpdate();


            // Create table for the user
//            if (rowsInserted > 0) {
//                String createTableQuery = "CREATE TABLE " + username + "_table (`id` INT NOT NULL AUTO_INCREMENT,balance DECIMAL(10, 2), deposit DECIMAL(10, 2), withdrawal DECIMAL(10, 2), PRIMARY KEY (`id`))";
//                PreparedStatement createTableStatement = con.prepareStatement(createTableQuery);
//                createTableStatement.executeUpdate();

            // Insert account number into the user's table
//                    String insertAccountNumberQuery = "INSERT INTO " + username + "_table (account_number) VALUES (?)";
//                    PreparedStatement insertAccountNumberStatement = con.prepareStatement(insertAccountNumberQuery);
//                    insertAccountNumberStatement.setLong(1, accountNumber);
//                    insertAccountNumberStatement.executeUpdate();


//                String filename = username + ".txt";
//                File userFile = new File(filename);
//                if (userFile.createNewFile()) {
//                    System.out.println("File created: " + userFile.getName());
//                } else {
//                    System.out.println("File already exists.");
//                }
//
//                System.out.println("User created successfully. Account number: " + accountNumber);
//            }
            con.close(); // Close the connection when done
        } catch (Exception e) {
            System.out.println("Error inserting user.");
            e.printStackTrace();
        }
    }

    private boolean isUsernameTaken(String username) {
        try {
            Connection connection = DriverManager.getConnection(jdbc);
            String query = "SELECT * FROM users WHERE usersname = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // Returns true if username is already taken
        } catch (Exception e) {
            System.out.println("Error checking username.");
            e.printStackTrace();
            return false; // Return false in case of an error
        }
    }

    private long generateUniqueAccountNumber(Connection connection) {
        Random random = new Random();
        long accountNumber;
        try {
            // Generate a random number between 1000000 and 9999999 (7-digit)
            accountNumber = random.nextInt(9000000) + 1000000;

            // Check if the generated account number already exists in 'users' table
            String query = "SELECT * FROM users WHERE account_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, accountNumber);
            ResultSet resultSet = statement.executeQuery();

            // If the account number already exists, generate a new one
            while (resultSet.next()) {
                accountNumber = random.nextInt(9000000) + 1000000;
                statement.setLong(1, accountNumber);
                resultSet = statement.executeQuery();
            }
            return accountNumber;
        } catch (Exception e) {
            System.out.println("Error generating account number.");
            e.printStackTrace();
            return -1;
        }
    }

    public boolean validateUser(String username, String password) {
        boolean isValid = false;
        try {
            Connection connection = DriverManager.getConnection(jdbc);
            String query = "SELECT * FROM users WHERE usersname = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            currentUser = username;
            if (resultSet.next()) {
                isValid = true;
                current_acc_num = resultSet.getDouble("account_number");
            }
            connection.close(); // Close the connection when done
        } catch (Exception e) {
            System.out.println("Error validating user.");
            e.printStackTrace();
        }
        return isValid;
    }

    public static void deposit(double amount, JLabel balanceLabel) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbc);
            connection.setAutoCommit(false); // Disable auto-commit mode

            if (currentUser == null || currentUser.isEmpty()) {
                System.out.println("Error: currentUser is null or empty.");
                // Handle the error or return from the method
                return;
            }

            String query = "INSERT INTO Transation(account_number, deposit, withdraw,transaction_date) VALUES (?, ?, 0,?)";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, current_acc_num); // Set the account number
            statement.setDouble(2, amount); // Set the deposit amount
            statement.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            int rowsAffected = statement.executeUpdate(); // Execute the insert statement
            if (rowsAffected > 0) {
                System.out.println("Deposit successful.");
            } else {
                System.out.println("Deposit failed.");
            }

            String query1 = "SELECT SUM(deposit) AS total_deposit, SUM(withdraw) AS total_withdrawal FROM Transation WHERE account_number = ?";
            PreparedStatement statement1 = connection.prepareStatement(query1);
            statement1.setDouble(1, current_acc_num); // Set the account number parameter
            ResultSet rs = statement1.executeQuery(); // Execute the prepared statement

            if (rs.next()) {
                double totalDeposit = rs.getDouble("total_deposit");
                double totalWithdrawal = rs.getDouble("total_withdrawal");

                balance = totalDeposit - totalWithdrawal;
                System.out.println("Balance: " + balance);
            }
            double updatedBalance = getCurrentBalance(connection);
            balanceLabel.setText("Balance: $" + updatedBalance);

            connection.commit(); // Commit the transaction
        } catch (SQLException e) {
            System.out.println("An error occurred while processing the deposit.");
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.rollback(); // Rollback the transaction if an error occurs
                } catch (SQLException rollbackException) {
                    rollbackException.printStackTrace();
                }
            }
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true); // Re-enable auto-commit mode
                    connection.close(); // Close the connection
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }


    public static void withdraw(double amount, JLabel balanceLabel) {
        Scanner scanner = new Scanner(System.in);
        Connection connection = null;
        int maxRetries = 5; // Maximum number of retries
        int retryDelay = 1000; // Delay in milliseconds between retries

        try {
            int retries = 0;
            while (retries < maxRetries) {
                try {
                    connection = DriverManager.getConnection(jdbc);
                    connection.setAutoCommit(false); // Start a transaction

                    if (currentUser == null || currentUser.isEmpty()) {
                        System.out.println("Error: currentUser is null or empty.");
                        // Handle the error or return from the method
                        return;
                    }

                    // Check balance before withdrawal
                    double currentBalance = getCurrentBalance(connection);
                    System.out.println("Current Balance: " + currentBalance);

                    // Check if balance is sufficient
                    if (amount > currentBalance) {
                        System.out.println("Insufficient balance.");
                        return;
                    }

                    // Proceed with withdrawal if balance is sufficient
                    String query = "INSERT INTO Transation(account_number, deposit, withdraw,transaction_date) VALUES (?, 0, ?,?)";
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.setDouble(1, current_acc_num);
                    statement.setDouble(2, amount);
                    statement.setString(3, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    int rowsAffected = statement.executeUpdate(); // Use executeUpdate() instead of executeQuery()
                    if (rowsAffected > 0) {
                        System.out.println("Withdrawal successful.");
                    } else {
                        System.out.println("Withdrawal failed.");
                    }

                    // Update balance after withdrawal
                    double updatedBalance = getCurrentBalance(connection);
                    System.out.println("Updated Balance: " + updatedBalance);

                    balanceLabel.setText("Balance: $" + updatedBalance); // Update balance label

                    connection.commit(); // Commit the transaction if all operations succeed
                    break; // Exit the retry loop if successful
                } catch (SQLException e) {
                    System.out.println("An error occurred while processing the withdrawal. Retrying...");
                    e.printStackTrace();
                    retries++;
                    Thread.sleep(retryDelay); // Wait before retrying
                } finally {
                    if (connection != null) {
                        try {
                            connection.rollback(); // Rollback the transaction if an error occurs
                        } catch (SQLException rollbackException) {
                            rollbackException.printStackTrace();
                        } finally {
                            try {
                                connection.close(); // Close the connection
                            } catch (SQLException closeException) {
                                closeException.printStackTrace();
                            }
                        }
                    }
                }
            }

            if (retries == maxRetries) {
                System.out.println("Maximum retries reached. Withdrawal failed.");
            }
        } catch (InterruptedException e) {
            System.out.println("Thread interrupted while waiting to retry withdrawal.");
            e.printStackTrace();
        }
    }



    // Helper method to get the current balance from the database
    public static double getCurrentBalance(Connection connection) throws SQLException {
        String query = "SELECT SUM(deposit) AS total_deposit, SUM(withdraw) AS total_withdrawal FROM Transation where account_number = " + current_acc_num;
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            double totalDeposit = rs.getDouble("total_deposit");
            double totalWithdrawal = rs.getDouble("total_withdrawal");
            return totalDeposit - totalWithdrawal;
        } else {
            throw new SQLException("No data found.");
        }
    }

    public static void Checkbalance() {

        String query = "SELECT SUM(deposit) AS total_deposit, SUM(withdraw) AS total_withdrawal FROM Transation where account_number = " + current_acc_num;

        try (Connection con = DriverManager.getConnection(jdbc);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                double totalDeposit = rs.getDouble("total_deposit");
                double totalWithdrawal = rs.getDouble("total_withdrawal");

                double balance = totalDeposit - totalWithdrawal;
                System.out.println("Total deposit: " + totalDeposit);
                System.out.println("Total withdrawal: " + totalWithdrawal);
                System.out.println("Balance: " + balance);
            } else {
                System.out.println("No data found.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public static void transactionLog() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbc);

            if (currentUser == null || currentUser.isEmpty()) {
                System.out.println("Error: currentUser is null or empty.");
                // Handle the error or return from the method
                return;
            }

            String query = "SELECT transaction_date, deposit, withdraw FROM Transation WHERE account_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, current_acc_num); // Set the account number parameter
            ResultSet resultSet = statement.executeQuery(); // Execute the prepared statement

            System.out.println("Transaction Log for Account Number: " + current_acc_num);
            System.out.println("------------------------------------------------------------");
            System.out.printf("%-20s %-15s %-15s%n", "Date & Time", "Deposit", "Withdrawal");
            System.out.println("------------------------------------------------------------");
            while (resultSet.next()) {
                String transactionDate = resultSet.getString("transaction_date");
                double deposit = resultSet.getDouble("deposit");
                double withdrawal = resultSet.getDouble("withdraw");
                System.out.printf("%-20s %-15.2f %-15.2f%n", transactionDate, deposit, withdrawal);
            }
            System.out.println("------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("An error occurred while fetching the transaction log.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close(); // Close the connection
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }
    public String getTransactionLog() {
        StringBuilder log = new StringBuilder();
        Connection connection = null;
        try {
            String jdbc = "jdbc:sqlite:The_Bank.db";
            connection = DriverManager.getConnection(jdbc);


            if (currentUser == null || currentUser.isEmpty()) {
                log.append("Error: currentUser is null or empty.");
                // Handle the error or return from the method
                return log.toString();
            }

            String query = "SELECT deposit, withdraw, transaction_date FROM Transation WHERE account_number = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setDouble(1, current_acc_num); // Set the account number parameter
            ResultSet resultSet = statement.executeQuery(); // Execute the prepared statement

            log.append("Transaction Log for Account Number: ").append(current_acc_num).append("\n");
            log.append("----------------------------------------------------\n");
            log.append(String.format("%-15s %-15s %-20s\n", "Deposit", "Withdrawal", "Transaction Time"));
            log.append("----------------------------------------------------\n");
            while (resultSet.next()) {
                double deposit = resultSet.getDouble("deposit");
                double withdrawal = resultSet.getDouble("withdraw");
                String transactionTime = resultSet.getString("transaction_date");
                log.append(String.format("%-15.2f %-15.2f %-20s\n", deposit, withdrawal, transactionTime));
            }
            log.append("----------------------------------------------------\n");

        } catch (SQLException e) {
            log.append("An error occurred while fetching the transaction log.");
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close(); // Close the connection
                } catch (SQLException closeException) {
                    closeException.printStackTrace();
                }
            }
        }

        return log.toString();
    }


}







