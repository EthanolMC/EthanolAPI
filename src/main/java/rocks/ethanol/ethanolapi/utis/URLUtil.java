package rocks.ethanol.ethanolapi.utis;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class URLUtil {

    public static String urlEncode(final char c) {
        return String.format("%%%s", Integer.toHexString(c));
    }

    public static String urlEncode(final String string) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (final char c : string.toCharArray()) {
            stringBuilder.append(URLUtil.urlEncode(c));
        }
        return stringBuilder.toString();
    }

    public static Map<String, String> extractQuery(final String query) {
        return Arrays.stream(query.split("&")).map(entry -> entry.split("=")).collect(Collectors.toMap(entry -> entry[0], entry -> entry.length > 1 ? entry[1] : ""));
    }

}
