package Admin;

import java.sql.*;

public class admin {

        public static void main(String[]args)
        {
            String jdbc ="jdbc:sqlite:The_Bank.db";
            try {
                Connection c = DriverManager.getConnection(jdbc);
                String sql = "Select * from users;";
                Statement s = c.createStatement();
               ResultSet r = s.executeQuery(sql);
                System.out.println("Welcome Admin");
               while(r.next()) {
                   String name = r.getString("name");
                   String num = r.getString("usersname");
                   String pass = r.getString("password");
                   String type = r.getString("account_number");
                   System.out.println(name + " " + num + " " + pass +" "+type);
                   boolean isValid=validateUser("ttt", "111");
                   if (isValid) {
                       System.out.println("Login successful!");
                   }
                   else {
                       System.out.println("Incorrect username or password.");
                   }
               }
            }
             catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    public static boolean validateUser(String username, String password)
    {
        String jdbc ="jdbc:sqlite:The_Bank.db";
        boolean isValid = false;
        try {
            Connection connection = DriverManager.getConnection(jdbc);
            String query = "SELECT * FROM users WHERE usersname = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                isValid = true;

            }
            connection.close(); // Close the connection when done
        } catch (Exception e) {
            System.out.println("Error validating user.");
            e.printStackTrace();
        }
        return isValid;
    }

    }





