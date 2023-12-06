import java.sql.*;
import java.util.Scanner;

public class console  {
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/mydatabase";
    private static final String USER = "your_username";
    private static final String PASSWORD = "your_password";

    private static final String ROLE_COUNSELOR = "COUNSELOR";
    private static final String ROLE_FACULTY = "FACULTY";
    private static final String ROLE_STUDENT = "STUDENT";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC Driver not found!");
            e.printStackTrace();
            return;
        }

        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            System.out.println("Connected to the database");

            // Authentication
            String role = authenticate(connection);
            if (role == null) {
                System.out.println("Authentication failed. Exiting the program.");
                return;
            }

            // Authorization
            if (ROLE_COUNSELOR.equals(role)) {
                counselorMenu(connection);
            } else if (ROLE_FACULTY.equals(role)) {
                facultyMenu(connection);
            } else if (ROLE_STUDENT.equals(role)) {
                System.out.println("You do not have access to the Student Management System. Exiting.");
            }

        } catch (SQLException e) {
            System.err.println("SQL Exception:");
            e.printStackTrace();
        }
    }

    private static String authenticate(Connection connection) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        String selectQuery = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("role");
                } else {
                    System.out.println("Authentication failed. Invalid username or password.");
                    return null;
                }
            }
        }
    }

    private static void counselorMenu(Connection connection) throws SQLException {
        while (true) {
            System.out.println("\nCounselor Menu");
            System.out.println("1. Add Student");
            System.out.println("2. View All Students");
            System.out.println("3. Update Student");
            System.out.println("4. Delete Student");
            System.out.println("5. Logout");
            System.out.print("Enter your choice: ");

            Scanner scanner = new Scanner(System.in);
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (choice) {
                case 1:
                    addStudent(connection);
                    break;
                case 2:
                    viewAllStudents(connection);
                    break;
                case 3:
                    updateStudent(connection);
                    break;
                case 4:
                    deleteStudent(connection);
                    break;
                case 5:
                    System.out.println("Logging out. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter a valid option.");
            }
        }
    }

    private static void facultyMenu(Connection connection) throws SQLException {
        viewAllStudents(connection);
    }

    private static void addStudent(Connection connection) throws SQLException {
        System.out.println("\nAdd Student");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter student name: ");
        String name = scanner.nextLine();
        System.out.print("Enter student roll number: ");
        int rollNumber = scanner.nextInt();

        String insertQuery = "INSERT INTO students (name, roll_number) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, name);
            preparedStatement.setInt(2, rollNumber);
            preparedStatement.executeUpdate();
        }

        System.out.println("Student added successfully!");
    }

    private static void viewAllStudents(Connection connection) throws SQLException {
        System.out.println("\nAll Students");
        String selectQuery = "SELECT * FROM students";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectQuery)) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                int rollNumber = resultSet.getInt("roll_number");
                System.out.println("ID: " + id + ", Name: " + name + ", Roll Number: " + rollNumber);
            }
        }
    }

    private static void updateStudent(Connection connection) throws SQLException {
        System.out.println("\nUpdate Student");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the ID of the student to update: ");
        int id = scanner.nextInt();

        String selectQuery = "SELECT * FROM students WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, id);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    System.out.print("Enter new name for the student: ");
                    scanner.nextLine(); // Consume the newline character
                    String newName = scanner.nextLine();

                    String updateQuery = "UPDATE students SET name = ? WHERE id = ?";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateQuery)) {
                        updateStatement.setString(1, newName);
                        updateStatement.setInt(2, id);
                        updateStatement.executeUpdate();
                    }

                    System.out.println("Student updated successfully!");
                } else {
                    System.out.println("Student not found with the given ID.");
                }
            }
        }
    }

    private static void deleteStudent(Connection connection) throws SQLException {
        System.out.println("\nDelete Student");
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the ID of the student to delete: ");
        int id = scanner.nextInt();

        String deleteQuery = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.setInt(1, id);
            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Student deleted successfully!");
            } else {
                System.out.println("Student not found with the given ID.");
            }
        }
    }
}
