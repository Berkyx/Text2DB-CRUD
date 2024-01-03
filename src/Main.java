import java.io.*;

public class Main {
    public static void main(String[] args) {
        try {
            File dataDir = new File("data");
            TravelData travelData = new TravelData(dataDir);
            Database db = new Database(travelData, "config.properties");

            db.createDb();
            db.showGui();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
