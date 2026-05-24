package com.talha.quiz.projectsem2.service;

import com.talha.quiz.projectsem2.model.User;
import com.talha.quiz.projectsem2.util.SessionManager;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {
    private static final DatabaseService database = DatabaseService.getInstance();


    public static void registerUser(String username, String password, String email) {
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        try ( PreparedStatement statement = database.getConnection().prepareStatement(
                database.getUserQuery("user.register")
        )) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, email);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Registration failed", e);
        }
    }

    public static boolean login(String username, String password) {
        try (PreparedStatement statement = database.getConnection().prepareStatement(
                database.getUserQuery("user.login"))) {

            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (!resultSet.next())
                    return false;

                String passwordHash = resultSet.getString("password_hash");

                if (BCrypt.checkpw(password, passwordHash)) {
                    User user = new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("password_hash"),
                            resultSet.getString("email")
                    );
                    SessionManager.loginUser(user);
                    return true;
                } else {
                    return false;
                }
            }
        } catch(SQLException e) {
            throw new RuntimeException("Authentication failed", e);
        }
    }
}
