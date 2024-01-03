import java.io.IOException;
import java.sql.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Database {
	private Connection connection;
    private TravelData travelData;

    public Database(TravelData travelData, String propertiesFilePath) throws SQLException, IOException {
        DatabaseConfig config = new DatabaseConfig(propertiesFilePath);
        this.connection = DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        
        createDatabaseIfNotExists("travel");

        this.connection.close();

        this.connection = DriverManager.getConnection(config.getUrl() + "/travel", config.getUser(), config.getPassword());
        this.travelData = travelData;

        createDb();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                dropAllTables();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    private void dropAllTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS offers");
        }
    }
    private void createDatabaseIfNotExists(String dbName) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SHOW DATABASES LIKE '" + dbName + "'");
            if (!resultSet.next()) {
                statement.executeUpdate("CREATE DATABASE " + dbName);
            }
        }
    }

    public void createDb() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createTableSql = "CREATE TABLE IF NOT EXISTS offers (\n" +
                    "        id INT AUTO_INCREMENT PRIMARY KEY, \n" +
                    "        location VARCHAR(255), \n" +
                    "        country VARCHAR(255), \n" +
                    "        departure_date DATE, \n" +
                    "        return_date DATE, \n" +
                    "        place VARCHAR(255), \n" +
                    "        price DECIMAL(10, 2), \n" +
                    "        currency VARCHAR(10),\n" +
                    "        UNIQUE (location, country, departure_date, return_date)\n" +
                    "    )";
            statement.execute(createTableSql);

            ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM offers");
            if (rs.next() && rs.getInt(1) > 0) {
            }

            String insertSql = "INSERT IGNORE INTO offers \n" +
                    "    (location, country, departure_date, return_date, place, price, currency) \n" +
                    "    VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(insertSql);

            for (TravelData.TravelOffer offer : travelData.getOffers()) {
                preparedStatement.setString(1, offer.getLocale());
                preparedStatement.setString(2, offer.getCountry());
                preparedStatement.setDate(3, new java.sql.Date(offer.getDepartureDate().getTime()));
                preparedStatement.setDate(4, new java.sql.Date(offer.getReturnDate().getTime()));
                preparedStatement.setString(5, offer.getPlace());
                preparedStatement.setBigDecimal(6, offer.getPrice());
                preparedStatement.setString(7, offer.getCurrency());
                preparedStatement.executeUpdate();
            }
        }
    }

    public void showGui() throws SQLException {
        JFrame frame = new JFrame("Travel Offers");
        String[] columnNames = {"Location", "Country", "Departure Date", "Return Date", "Place", "Price", "Currency"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT * FROM offers");
        while (rs.next()) {
            model.addRow(new Object[]{rs.getString("location"), rs.getString("country"), rs.getDate("departure_date"), rs.getDate("return_date"), rs.getString("place"), rs.getBigDecimal("price"), rs.getString("currency")});
        }

        JTable table = new JTable(model);
        frame.add(new JScrollPane(table));
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
