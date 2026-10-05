package rebelmythik.antiVillagerLag.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorCode {
    private static final Pattern HEX = Pattern.compile("#[a-fA-F0-9]{6}");

    public String cm(String input) {
        Matcher matcher = HEX.matcher(input);
        StringBuilder replaced = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(replaced, Matcher.quoteReplacement(toSectionHex(matcher.group())));
        }
        matcher.appendTail(replaced);
        return ChatColor.translateAlternateColorCodes('&', replaced.toString());
    }

    private static String toSectionHex(String hex) {
        StringBuilder legacy = new StringBuilder("§x");
        String digits = hex.substring(1).toUpperCase();
        for (int i = 0; i < digits.length(); i++) {
            legacy.append('§').append(digits.charAt(i));
        }
        return legacy.toString();
    }
}
