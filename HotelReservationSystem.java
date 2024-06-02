import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class HotelReservationSystem {

    static final String DB_URL = "jdbc:mysql://localhost:3306/NGOK";
    static final String USER = "root";
    static final String PASS = "Akucantik123.";

    public static void main(String[] args) {
        // Membuat database dan table
        createDatabaseAndTables();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame();
            }
        });
    }

    private static void createDatabaseAndTables() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", USER, PASS);
             Statement stmt = conn.createStatement()) {
            
            // Membuat database
            String sql = "CREATE DATABASE IF NOT EXISTS NGOK";
            stmt.executeUpdate(sql);

            sql = "USE NGOK";
            stmt.executeUpdate(sql);

            // Membuat tabel "room"
            sql = "CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_number INT PRIMARY KEY, " +
                    "room_type VARCHAR(50) NOT NULL)";
            stmt.executeUpdate(sql);

            // Membuat tabel "reservation"
            sql = "CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "guest_name VARCHAR(100) NOT NULL, " +
                    "contact_info VARCHAR(100), " +
                    "room_number INT, " +
                    "check_in_date DATE, " +
                    "check_out_date DATE, " +
                    "FOREIGN KEY (room_number) REFERENCES rooms(room_number))";
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class LoginFrame extends JFrame {

    private JTextField userField;
    private JPasswordField passField;

    public LoginFrame() {
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.PINK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("                        Username"), gbc);

        gbc.gridy++;
        userField = new JTextField(25); // Lebar input username
        panel.add(userField, gbc);

        gbc.gridy++;
        panel.add(new JLabel("                        Password"), gbc);

        gbc.gridy++;
        passField = new JPasswordField(25); // Lebar input password
        panel.add(passField, gbc);

        gbc.gridy++;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton loginButton = new JButton("Login");
        panel.add(loginButton, gbc);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = userField.getText();
                String password = new String(passField.getPassword());
                if (authenticate(username, password)) {
                    new DashboardFrame(username);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials");
                }
            }
        });

        add(panel);
        pack();
        setVisible(true);
    }

    private boolean authenticate(String username, String password) {
        return username.equals("admin") && password.equals("777");
    }
}

class DashboardFrame extends JFrame {

    private String username;
    public DashboardFrame(String username) {
        this.username = username;
        setTitle("Dashboard");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JLabel welcomeLabel = new JLabel("WELCOME TO HANOY HILLS RESORT");
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton roomButton = new JButton("Manage Rooms");
        roomButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RoomManagementFrame();
            }
        });

        JButton reservationButton = new JButton("Manage Reservations");
        reservationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ReservationManagementFrame();
            }
        });

        JPanel panel = new JPanel(new GridLayout(3, 1));
        panel.setBackground(Color.PINK);
        panel.add(welcomeLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.setBackground(Color.PINK);
        buttonPanel.add(roomButton);
        buttonPanel.add(reservationButton);

        panel.add(buttonPanel);
        add(panel);

        setVisible(true);
    }
}

class RoomManagementFrame extends JFrame {

    private DefaultTableModel roomTableModel;
    private JTable roomTable;

    public RoomManagementFrame() {
        setTitle("Room Management");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        roomTableModel = new DefaultTableModel(new Object[]{"Room Number", "Room Type"}, 0);
        roomTable = new JTable(roomTableModel);
        customizeTableHeader(roomTable);
        centerTableData(roomTable);
        loadRooms();

        JScrollPane scrollPane = new JScrollPane(roomTable);
        JButton addButton = new JButton("Add Room");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomNumber = JOptionPane.showInputDialog("Enter room number:");
                String roomType = JOptionPane.showInputDialog("Enter room type:");
                if (roomNumber != null && !roomNumber.trim().isEmpty() &&
                    roomType != null && !roomType.trim().isEmpty()) {
                    addRoom(Integer.parseInt(roomNumber), roomType);
                    loadRooms();
                }
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.PINK);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.SOUTH);
        add(panel);

        setVisible(true);
    }

    private void customizeTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.PINK);
        header.setForeground(Color.BLACK);

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBackground(Color.PINK);
        renderer.setForeground(Color.BLACK);
    }

    private void centerTableData(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
    }

    private void loadRooms() {
        roomTableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(HotelReservationSystem.DB_URL, HotelReservationSystem.USER, HotelReservationSystem.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms")) {

            while (rs.next()) {
                int roomNumber = rs.getInt("room_number");
                String roomType = rs.getString("room_type");
                roomTableModel.addRow(new Object[]{roomNumber, roomType});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addRoom(int roomNumber, String roomType) {
        try (Connection conn = DriverManager.getConnection(HotelReservationSystem.DB_URL, HotelReservationSystem.USER, HotelReservationSystem.PASS);
             Statement stmt = conn.createStatement()) {

            String sql = "INSERT INTO rooms (room_number, room_type) VALUES (" + roomNumber + ", '" + roomType + "')";
            stmt.executeUpdate(sql);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class ReservationManagementFrame extends JFrame {

    private DefaultTableModel reservationTableModel;
    private JTable reservationTable;

    public ReservationManagementFrame() {
        setTitle("Reservation Management");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        reservationTableModel = new DefaultTableModel(new Object[]{"Guest Name", "Contact", "Room Number", "Check-in Date", "Check-out Date"}, 0);
        reservationTable = new JTable(reservationTableModel);
        customizeTableHeader(reservationTable);
        centerTableData(reservationTable);
        loadReservations();

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        JButton addButton = new JButton("Add Reservation");

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addReservationDialog();
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.PINK);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
        buttonPanel.add(addButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        setVisible(true);
    }

    private void loadReservations() {
        reservationTableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(HotelReservationSystem.DB_URL, HotelReservationSystem.USER, HotelReservationSystem.PASS);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM reservations")) {

            while (rs.next()) {
                String guestName = rs.getString("guest_name");
                String contactInfo = rs.getString("contact_info");
                int roomNumber = rs.getInt("room_number");
                Date checkInDate = rs.getDate("check_in_date");
                Date checkOutDate = rs.getDate("check_out_date");
                reservationTableModel.addRow(new Object[]{guestName, contactInfo, roomNumber, checkInDate, checkOutDate});
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addReservationDialog() {
        String guestName = JOptionPane.showInputDialog("Enter guest name:");
        String contactInfo = JOptionPane.showInputDialog("Enter contact information:");
        String roomNumber = JOptionPane.showInputDialog("Enter room number:");
        String checkInDate = JOptionPane.showInputDialog("Enter check-in date (YYYY-MM-DD):");
        String checkOutDate = JOptionPane.showInputDialog("Enter check-out date (YYYY-MM-DD):");

        if (guestName != null && !guestName.trim().isEmpty() &&
            contactInfo != null && !contactInfo.trim().isEmpty() &&
            roomNumber != null && !roomNumber.trim().isEmpty() &&
            checkInDate != null && !checkInDate.trim().isEmpty() &&
            checkOutDate != null && !checkOutDate.trim().isEmpty()) {

            addReservation(guestName, contactInfo, Integer.parseInt(roomNumber), Date.valueOf(checkInDate), Date.valueOf(checkOutDate));
            loadReservations();
        }
    }

    private void addReservation(String guestName, String contactInfo, int roomNumber, Date checkInDate, Date checkOutDate) {
        try (Connection conn = DriverManager.getConnection(HotelReservationSystem.DB_URL, HotelReservationSystem.USER, HotelReservationSystem.PASS);
             PreparedStatement pstmt = conn.prepareStatement("INSERT INTO reservations (guest_name, contact_info, room_number, check_in_date, check_out_date) VALUES (?, ?, ?, ?, ?)")) {

            pstmt.setString(1, guestName);
            pstmt.setString(2, contactInfo);
            pstmt.setInt(3, roomNumber);
            pstmt.setDate(4, checkInDate);
            pstmt.setDate(5, checkOutDate);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void customizeTableHeader(JTable table) {
        JTableHeader header = table.getTableHeader();
        header.setBackground(Color.PINK);
        header.setForeground(Color.BLACK);

        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        renderer.setBackground(Color.PINK);
        renderer.setForeground(Color.BLACK);
    }

    private void centerTableData(JTable table) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
    }
}
