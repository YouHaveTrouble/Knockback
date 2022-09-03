package me.youhavetrouble.knockback.util;

import java.util.ArrayList;
import java.util.Locale;

public class StringUtil {

    public static ArrayList<String> getCompletions(ArrayList<String> input, String currentArg) {
        input.removeIf(string -> !string.toLowerCase(Locale.ENGLISH).startsWith(currentArg.toLowerCase(Locale.ENGLISH)));
        return input;
    }

}
