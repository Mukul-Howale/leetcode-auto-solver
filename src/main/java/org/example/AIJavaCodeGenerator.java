package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIJavaCodeGenerator {
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    public static String generateCode(String title, String link) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("GEMINI_API_KEY");
        OkHttpClient client = new OkHttpClient();

        JSONObject requestBody = new JSONObject()
                .put("contents", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("parts", new org.json.JSONArray()
                                        .put(new JSONObject()
                                                .put("text", "Write clean Java code for the following LeetCode problem:\n"
                                                        + "Title: " + title + "\n"
                                                        + "Link: " + link + "\n"
                                                        + "Include inline comments for clarity. Return ONLY the solution code without any markdown formatting or additional text."
                                                        + "Before writing the solution please check the current description, examples and constraints.")
                                        )
                                )
                        )
                );

        Request request = new Request.Builder()
                .url(GEMINI_URL + "?key=" + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            JSONObject json = new JSONObject(body);
            String generatedText = json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");

            // Clean up the response by extracting just the Java code
            return extractJavaCode(generatedText);
        }
    }

    /**
     * Extracts Java code from the generated text, removing any markdown code blocks
     */
    private static String extractJavaCode(String text) {
        // First check if the text is wrapped in markdown code blocks
        Pattern codeBlockPattern = Pattern.compile("```(?:java)?\\s*([\\s\\S]*?)\\s*```");
        Matcher matcher = codeBlockPattern.matcher(text);

        if (matcher.find()) {
            // If it's in a code block, extract just the code
            return matcher.group(1).trim();
        } else {
            // If it's not in a code block format, return the original text
            // but check for any other Markdown formatting
            return text.trim();
        }
    }
}