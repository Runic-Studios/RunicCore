package com.runicrealms.plugin.utilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by KissOfFate
 * Date: 6/28/2019
 * Time: 11:12 PM
 */
public class FilterUtil {

    private static List<String> swearWords = new ArrayList<>();

    public static void loadFromFile(File file) {
        try(Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
            stream.forEach(swearWords::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasFilterWord(String string) {
        for(String swear : swearWords) {
            if(string.contains(swear)) {
                return true;
            }
        }
        return false;
    }
}
