import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.stream.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class TravelData {
    private ConcurrentLinkedQueue<TravelOffer> offers = new ConcurrentLinkedQueue<>();

    public TravelData(File dataDir) throws IOException {
        Files.walk(dataDir.toPath())
                .filter(Files::isRegularFile)
                .toList()
                .parallelStream()
                .forEach(this::parseFile);
    }

    private void parseFile(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                TravelOffer offer = TravelOffer.parse(line);
                if (offer != null) {
                    offers.add(offer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<TravelOffer> getOffers() {
        return new ArrayList<>(offers);
    }
    

    public static class TravelOffer {
    	private String locale;
        private String country;
        private Date departureDate;
        private Date returnDate;
        private String place;
        private BigDecimal price;
        private String currency;
        
        
        public String getLocalizedDescription(Locale locale, SimpleDateFormat sdf) {
            return String.format(locale, "Country: %s, Departure: %s, Return: %s, Place: %s, Price: %s %s",
                this.country, 
                sdf.format(this.departureDate),
                sdf.format(this.returnDate),
                this.place, 
                this.price.toString(), 
                this.currency);
        }
        public static TravelOffer parse(String line) {
            String[] parts = line.split("\t");
            if (parts.length < 7) return null;

            TravelOffer offer = new TravelOffer();
            offer.locale = parts[0];
            offer.country = parts[1];
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                offer.departureDate = sdf.parse(parts[2]);
                offer.returnDate = sdf.parse(parts[3]);
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
            offer.place = parts[4];
            String priceString = parts[5].replace(",", "").replace(".", "");
            offer.price = new BigDecimal(priceString).setScale(2, RoundingMode.HALF_UP);
            offer.currency = parts[6];

            return offer;
        }
        public String getLocale() {
            return locale;
        }

        public String getCountry() {
            return country;
        }

        public Date getDepartureDate() {
            return departureDate;
        }

        public Date getReturnDate() {
            return returnDate;
        }

        public String getPlace() {
            return place;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public String getCurrency() {
            return currency;
        }
    }
}


