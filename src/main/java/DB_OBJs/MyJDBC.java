package DB_OBJs;

/*  JDBC class Responsible for connecting the database and the Application */


import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;

public class MyJDBC {
    // database configuration
    private static final String DB_URL = "jdbc:mariadb://127.0.0.1:3306/banker_db";
    private static final String DB_USERNAME = "Banker";
    private static final String DB_PASSWORD = "Banker123";

    // if valid return an object with the user's information
    public static User validateLogin(String username, String password){
        try{
            // establish a connection to the database using configurations
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            // create sql query
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ? AND password = ?"
            );

            // replace the ? with values
            // parameter index referring to the iteration of ? so 1 is the first ? and 2 is the second ?
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            //execute a query and state into a result set
            ResultSet resultSet = preparedStatement.executeQuery();

            /*
            * next() returns true or false
            * true - query returned data and result set now points to the first row
            * false - query returned no data and result set equals null
            */
            if(resultSet.next()){
                // get id
                int userId = resultSet.getInt("id");
                // get current balance
                BigDecimal currentBalance = resultSet.getBigDecimal("current_balance");
                //return user object
                return new User(userId, username, password, currentBalance);
            }

        } catch(SQLException e) {
            e.printStackTrace();
        }
        // not valid user
        return null;
    }

    // register new user to the database
    // true - register success
    // false - register fails
    public static boolean register(String username, String password){
        try{
            // first we will need to check if the username has already been taken
            if(!checkUser(username)) {
                Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

                PreparedStatement preparedStatement = connection.prepareStatement(
                        "INSERT INTO users(username, password, current_balance) VALUES(?, ?, ?)"
                );

                preparedStatement.setString(1, username);
                preparedStatement.setString(2, password);
                preparedStatement.setString(3, String.valueOf(new BigDecimal(0)));

                preparedStatement.executeQuery();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // check if username already exists in the database
    // true - user exists
    // false - user doesn't exist
    private static boolean checkUser(String username) {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );

            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            // this means that the query returned no data meaning that the username is available
            if(!resultSet.next()) return false;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    // true - transfer was a success
    // false - transfer was a failure
    public static boolean transfer(User user, String transferredUsername, float transferAmount){
        try{
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement queryUser = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?"
            );
            queryUser.setString(1, transferredUsername);
            ResultSet resultSet = queryUser.executeQuery();

            while (resultSet.next()) {
                // perform transfer
                User transferredUser = new User(
                        resultSet.getInt("Id"),
                        transferredUsername,
                        resultSet.getString("password"),
                        resultSet.getBigDecimal("current_balance")
                );

                // create transaction
                Transaction transferTransaction = new Transaction(
                        user.getId(),
                        "Transfer",
                        new BigDecimal(transferAmount),
                        null
                );

                Transaction recievedTransaction = new Transaction(
                        transferredUser.getId(),
                        "Transfer",
                        new BigDecimal(transferAmount),
                        null
                );

                // update transfer user
                transferredUser.setCurrentBalance(user.getCurrentBalance().add(BigDecimal.valueOf(transferAmount)));
                updateCurrentBalance(transferredUser);

                // update user current balance
                user.setCurrentBalance(transferredUser.getCurrentBalance().subtract(BigDecimal.valueOf(transferAmount)));
                updateCurrentBalance(user);

                // add these transactions to the database
                addTransactionToDatabase(transferTransaction);
                addTransactionToDatabase(recievedTransaction);
            }

            return true;

        } catch (SQLException   e) {
            e.printStackTrace();
        }
        return false;
    }

    // true - update to db was a success
    // false - update to the db was a failure

    public static boolean addTransactionToDatabase(Transaction transaction){
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement insertTransaction = connection.prepareStatement(
                    "INSERT transactions(used_id, transaction_type, transaction_amount, transaction_date) " +
                            "VALUES(?, ?, ?, NOW())"
            );
            // NOW() will put in the current date

            insertTransaction.setInt(1, transaction.getUserId());
            insertTransaction.setString(2, transaction.getTransactionType());
            insertTransaction.setBigDecimal(3, transaction.getTransactionAmount());

            return true;

        } catch (SQLException e) {
            e.printStackTrace();

        }
        return false;
    }

    public static boolean updateCurrentBalance(User user){
        try{
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement updateBalance = connection.prepareStatement(
                    "UPDATE users SET current_balance = ?  WHERE id = ?"
            );

            updateBalance.setBigDecimal(1, user.getCurrentBalance());
            updateBalance.setInt(2, user.getId());

            updateBalance.executeUpdate();

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // get all transactions (used for past transaction)
    public static ArrayList<Transaction> getPastTransaction(User user) {
        ArrayList<Transaction> pastTransactions = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);

            PreparedStatement selectAllTransaction = connection.prepareStatement(
                    "SELECT * FROM transaction WHERE user_id = ?"
            );

            selectAllTransaction.setInt(1, user.getId());

            ResultSet resultSet = selectAllTransaction.executeQuery();

            // iterate throughout the  results (if any)
            while(resultSet.next()) {
                // create transaction obj
                Transaction transaction = new Transaction(
                  user.getId(),
                  resultSet.getString("transaction_type"),
                  resultSet.getBigDecimal("transaction_amount"),
                        resultSet.getDate("transaction_date")
                );

                // store into array list
                pastTransactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pastTransactions;
    }
}
