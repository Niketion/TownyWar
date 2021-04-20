package io.github.niketion.townywar.utils;

import com.google.common.collect.Lists;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class StringUtils {

    public static String c(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String[] c(List<String> list) {
        List<String> colorized = Lists.newArrayList();
        for (String string : list) {
            colorized.add(c(string));
        }

        return colorized.toArray(new String[0]);
    }
}
