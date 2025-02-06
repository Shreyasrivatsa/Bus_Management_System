package mysql;

import java.sql.*;
import java.util.Scanner;

public class BusManagementSystem {
    private static final String URL = "jdbc:mysql://localhost:3306/bus_management";
    private static final String USER = " ";  // Change to your MySQL username
    private static final String PASSWORD = " ";  // Change to your MySQL password

    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n=== Bus Management System ===");
            System.out.println("1. Add a New Bus");
            System.out.println("2. View All Buses");
            System.out.println("3. Book Seats");
            System.out.println("4. Exit");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    addBus();
                    break;
                case 2:
                    viewBuses();
                    break;
                case 3:
                    bookSeats();
                    break;
                case 4:
                    System.out.println("Exiting the system...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Invalid choice! Try again.");
            }
        }
    }

    private static void addBus() {
        scanner.nextLine(); // Consume newline left-over
        System.out.print("Enter Bus Name: ");
        String busName = scanner.nextLine();
        System.out.print("Enter Source: ");
        String source = scanner.nextLine();
        System.out.print("Enter Destination: ");
        String destination = scanner.nextLine();
        System.out.print("Enter Total Seats: ");
        int totalSeats = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO buses (bus_name, source, destination, total_seats) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, busName);
            stmt.setString(2, source);
            stmt.setString(3, destination);
            stmt.setInt(4, totalSeats);
            stmt.executeUpdate();
            System.out.println("Bus added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewBuses() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM buses")) {

            System.out.println("\nAvailable Buses:");
            System.out.printf("%-5s %-15s %-15s %-15s %-10s %-10s\n", "ID", "Bus Name", "Source", "Destination", "Total Seats", "Booked");
            System.out.println("----------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("%-5d %-15s %-15s %-15s %-10d %-10d\n",
                        rs.getInt("bus_id"), rs.getString("bus_name"), rs.getString("source"),
                        rs.getString("destination"), rs.getInt("total_seats"), rs.getInt("booked_seats"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void bookSeats() {
        System.out.print("Enter Bus ID: ");
        int busId = scanner.nextInt();
        System.out.print("Enter number of seats to book: ");
        int seatsToBook = scanner.nextInt();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement psCheck = conn.prepareStatement("SELECT total_seats, booked_seats FROM buses WHERE bus_id = ?");
             PreparedStatement psUpdate = conn.prepareStatement("UPDATE buses SET booked_seats = booked_seats + ? WHERE bus_id = ?")) {

            psCheck.setInt(1, busId);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                int totalSeats = rs.getInt("total_seats");
                int bookedSeats = rs.getInt("booked_seats");
                int availableSeats = totalSeats - bookedSeats;

                if (seatsToBook <= availableSeats) {
                    psUpdate.setInt(1, seatsToBook);
                    psUpdate.setInt(2, busId);
                    psUpdate.executeUpdate();
                    System.out.println("Successfully booked " + seatsToBook + " seats.");
                } else {
                    System.out.println("Only " + availableSeats + " seats available. Booking failed.");
                }
            } else {
                System.out.println("Bus ID not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
