package moe.xetanai.rubix.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FilterUtils {
    private static final Logger logger = LogManager.getLogger(FilterUtils.class.getName());
    private static List<String> filteredWords = new ArrayList<>();

    private FilterUtils(){}

    public static String filter(String target) {
        String result = target;

        for(String word : filteredWords) {
            result = target.replace(result, "\\*\\*\\*\\*");
        }

        return result;
    }

    public static void processFilteredWords() {
        // TODO: Do this properly! Prevent message sending when writing to the list.
        logger.info("Processing filteredwords.json. Message sending in this time may fail.");
        List<String> fallback = filteredWords;

        try {
            StringBuilder configContents = new StringBuilder();
            Stream<String> stream = Files.lines(Paths.get("filteredwords.json"), StandardCharsets.UTF_8);
            stream.forEach(line -> configContents.append(line).append("\n"));
            stream.close();

            JSONArray array = new JSONArray(new JSONTokener(configContents.toString()));
            List<String> list = new ArrayList<>();

            for (int i = 0; i < array.length(); i++) {
                list.add(array.getString(i));
            }

            filteredWords = list;
            logger.info("Finished processing filtered words. These should be effective immediately.");
        } catch (IOException | JSONException err) {
            logger.error("Failed to process filteredwords.json. Reverting changes.", err);
            filteredWords = fallback;
        }
    }
}
