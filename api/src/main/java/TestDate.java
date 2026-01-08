
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TestDate {
    public static void main(String[] args) {
        try {
            String orderDateStr = "12月22日";
            String timeSplit = "11:00-12:00";
            
            // Simulation of DateFormatUtil.parseChineseDate
            int currentYear = LocalDate.now().getYear();
            System.out.println("Current Year: " + currentYear);
            LocalDate orderDate = LocalDate.parse(currentYear + "年" + orderDateStr,
                    DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
            System.out.println("Parsed Order Date: " + orderDate);

            String startTimeStr = timeSplit.split("-")[0];
            String[] parts = startTimeStr.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            LocalDateTime appointmentTime = LocalDateTime.of(orderDate, LocalTime.of(hour, minute));
            System.out.println("Appointment Time: " + appointmentTime);

            LocalDateTime now = LocalDateTime.now();
            System.out.println("Now: " + now);
            LocalDateTime limit = now.plusHours(2);
            System.out.println("Limit (Now + 2h): " + limit);

            boolean isAfter = limit.isAfter(appointmentTime);
            System.out.println("Is Limit After Appointment? " + isAfter);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
