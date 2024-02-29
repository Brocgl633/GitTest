package IIT_550_ah2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IPServer {

    private static Pattern pattern;
    private static Matcher matcher;

    private static final String IP_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
            + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static boolean validate(final String ip) {
        pattern = Pattern.compile(IP_PATTERN);
        matcher = pattern.matcher(ip);
        return matcher.matches();
    }

}
