package ru.rian.riamessenger.utils;

/**
 * Created by ASUS-PC on 19.07.2015.
 */
public class RiaTextUtils {
    /*The method capitalizes russian words
     */
    public static String capFirst(String text) {

        if (text == null) return null;
        text = text.toLowerCase().trim();

        String result = "";

        if (text.length() > 0) {

            int indices[] = {0, 1};

            text = text.substring(indices[0], indices[1]).toUpperCase() + text.substring(1);

            if (text.contains(" ")) {

                for (String word : text.split(" ")) {

                    word = word.trim();
                    if (word.length() > 0) {
                        if (word.startsWith("(")) {
                            indices[0] = 1;
                            indices[1] = 2;
                            result += " (";
                        } else {
                            indices[0] = 0;
                            indices[1] = 1;
                            result += " ";
                        }
                        result += word.substring(indices[0], indices[1]).toUpperCase() + word.substring(indices[1]);
                    }
                }
            } else result = text;
        }

        return result.trim();
    }
}
