
package app;

import util.DBConnection;
import util.PasswordUtil;

import java.io.Console;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class CreateAdmin {

    public static void main(String[] args) {

        Console console = System.console();
        String password;

        if (console != null) {
            char[] chars = console.readPassword(
                    "Enter a new admin password: ");
            if (chars == null) {
                System.out.println("Password input cancelled.");
                return;
            }
            password = new String(chars);
        } else {
            try (Scanner scanner = new Scanner(System.in)) {
                System.out.print("Enter a new admin password: ");
                password = scanner.nextLine();
            }
        }

        if (password.length() < 8) {
            System.out.println(
                    "Password must contain at least 8 characters.");
            return;
        }

        String email = "admin@hospital.com";

        try {
            String hashedPassword =
                    PasswordUtil.hashPassword(password);

            try (Connection con =
                         DBConnection.getInstance().getConnection()) {

                con.setAutoCommit(false);

                try {
                    // Check whether this admin email already exists.
                    String checkSql =
                            "SELECT id FROM users WHERE email = ?";

                    try (PreparedStatement ps =
                                 con.prepareStatement(checkSql)) {
                        ps.setString(1, email);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                throw new SQLException(
                                        "admin@hospital.com already exists. "
                                        + "No new account was created.");
                            }
                        }
                    }

                    // Create the login account.
                    String userSql =
                            "INSERT INTO users "
                            + "(firstname, lastname, email, phonenumber, "
                            + "password, role) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";

                    int adminId;

                    try (PreparedStatement ps = con.prepareStatement(
                            userSql, Statement.RETURN_GENERATED_KEYS)) {

                        ps.setString(1, "Hospital");
                        ps.setString(2, "Admin");
                        ps.setString(3, email);
                        ps.setNull(4, java.sql.Types.VARCHAR);
                        ps.setString(5, hashedPassword);
                        ps.setString(6, "Admin");

                        ps.executeUpdate();

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) {
                                throw new SQLException(
                                        "Could not generate admin ID.");
                            }
                            adminId = rs.getInt(1);
                        }
                    }

                    // Register the same ID in the admins table.
                    String adminSql =
                            "INSERT INTO admins (id, role) VALUES (?, ?)";

                    try (PreparedStatement ps =
                                 con.prepareStatement(adminSql)) {
                        ps.setInt(1, adminId);
                        ps.setString(2, "ADMIN");
                        ps.executeUpdate();
                    }

                    con.commit();

                    System.out.println(
                            "Admin account created successfully!");
                    System.out.println("Admin ID: " + adminId);
                    System.out.println("Login email: " + email);
                    System.out.println(
                            "Keep your password private.");

                } catch (SQLException | RuntimeException ex) {
                    con.rollback();
                    throw ex;
                } finally {
                    con.setAutoCommit(true);
                }
            }

        } catch (SQLException | GeneralSecurityException ex) {
            System.out.println(
                    "Admin creation failed: " + ex.getMessage());
        }
    }
}
