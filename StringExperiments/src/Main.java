import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        String text = "A23E-8M28-76AF-2L65-24A3";
        String regex = "[A-F0-9]{4}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        matcher.find();
        matcher.find();
        matcher.find();
        System.out.println(matcher.group());
    }
}
