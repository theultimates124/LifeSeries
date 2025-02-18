package net.mat0u5.lifeseries.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtils {
    private static HashMap<List<String>, List<String>> emotes = new HashMap<List<String>, List<String>>();

    public static void setEmotes() {
        emotes.put(List.of("skull"),List.of("☠"));
        emotes.put(List.of("smile"),List.of("☺"));
        emotes.put(List.of("frown"),List.of("☹"));
        emotes.put(List.of("heart"),List.of("❤"));
        emotes.put(List.of("copyright"),List.of("©"));
        emotes.put(List.of("trademark","tm"),List.of("™"));
    }

    public static String replaceEmotes(String input) {
        for (Map.Entry<List<String>, List<String>> entry : emotes.entrySet()) {
            if (entry.getValue().size()==0) continue;
            String emoteValue = entry.getValue().get(0);
            for (String emote : entry.getKey()) {
                String emoteCode = ":" + emote + ":";
                input = replaceCaseInsensitive(input, emoteCode, emoteValue);
            }
            if (!input.contains(":")) return input;
        }
        return input;
    }

    public static String replaceCaseInsensitive(String input, String replaceWhat, String replaceWith) {
        Pattern pattern = Pattern.compile(replaceWhat, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(input);
        String result = matcher.replaceAll(replaceWith);
        return result;
    }

    public static String toRomanNumeral(int num) {
        String[] romanNumerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (num > 0 && num <= romanNumerals.length) ? romanNumerals[num - 1] : String.valueOf(num);
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
