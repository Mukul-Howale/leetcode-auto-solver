package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LeetCodeAutoSubmitter {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String LEETCODE_BASE = "https://leetcode.com";

    public static void submitCode(String titleSlug, String code, String language) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String csrfToken = dotenv.get("X_CSRF_TOKEN");
        String session = dotenv.get("LEETCODE_SESSION");

        // Get the question ID first
        String questionId = getQuestionId(titleSlug, session);

        String url = LEETCODE_BASE + "/problems/" + titleSlug + "/submit/";

        // Create proper JSON using JSONObject for safer escaping
        JSONObject payload = new JSONObject();
        payload.put("lang", language);
        payload.put("question_id", questionId);
        payload.put("typed_code", code);

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(payload.toString(), JSON);

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-csrftoken", csrfToken)
                .addHeader("Cookie", "LEETCODE_SESSION=" + session + "; csrftoken=" + csrfToken)
                .addHeader("Referer", LEETCODE_BASE + "/problems/" + titleSlug + "/")
                .addHeader("Origin", LEETCODE_BASE)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                System.out.println("❌ Failed response: " + responseBody);
                throw new IOException("❌ Submission failed: " + response);
            }

            System.out.println("✅ Submission successful");
            System.out.println(responseBody);
        }
    }

    private static String getQuestionId(String titleSlug, String sessionCookie) throws IOException {
        String graphqlUrl = LEETCODE_BASE + "/graphql";

        String query = String.format("{\"query\":\"query getQuestionDetail($titleSlug: String!) {question(titleSlug: $titleSlug) {questionId}}\",\"variables\":{\"titleSlug\":\"%s\"}}", titleSlug);

        RequestBody body = RequestBody.create(query, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(graphqlUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", "LEETCODE_SESSION=" + sessionCookie)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to get question ID: " + response);
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONObject("data")
                    .getJSONObject("question")
                    .getString("questionId");
        }
    }
}
