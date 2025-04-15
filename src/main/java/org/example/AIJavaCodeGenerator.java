package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

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
                                                        + "Include inline comments for clarity.")
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
            return json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text");
        }
    }
}
