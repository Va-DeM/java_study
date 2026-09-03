import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) {

        hasPassedTime();

        System.out.println(getPeriodFromJavaBirthday());

    }

    public static String getPeriodFromJavaBirthday() {
        LocalDate birthdayJava = LocalDate.of(1995, 5, 23);
        Period period = birthdayJava.until(LocalDate.now());
        return period.getYears() + " years, " +
                period.getMonths() + " months, " +
                period.getDays() + " days";
    }

    public static void hasPassedTime() {
        LocalDate birthdayJava = LocalDate.of(1995, 5, 23);
        LocalDate today = LocalDate.now();
        long diffYears = birthdayJava.until(today, ChronoUnit.YEARS);
        long diffMonth = birthdayJava.until(today, ChronoUnit.MONTHS) % 12;
        long diffDays = birthdayJava.until(today, ChronoUnit.DAYS) % 29;

        System.out.println(diffYears + " years, " + diffMonth + " months, " + diffDays + " days");
    }
}
