package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class LeetCodeAutoSubmitter {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String LEETCODE_BASE = "https://leetcode.com";

    public static String submitCode(String titleSlug, String code, String language) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String csrfToken = dotenv.get("X_CSRF_TOKEN");
        String session = dotenv.get("LEETCODE_SESSION");

        String url = LEETCODE_BASE + "/problems/" + titleSlug + "/submit/";

        JSONObject payload = new JSONObject();
        payload.put("titleSlug", titleSlug);
        payload.put("question_id", getQuestionId(titleSlug, session));
        payload.put("language", language);
        payload.put("typed_code", code);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Referer", LEETCODE_BASE + "/problems/" + titleSlug)
                .addHeader("Origin", LEETCODE_BASE)
                .addHeader("X-CSRFToken", csrfToken)
                .addHeader("Cookie", "csrftoken=" + csrfToken + "; LEETCODE_SESSION=" + session + ";")
                .post(RequestBody.create(payload.toString(), MediaType.get("application/json")))
                .build();


        OkHttpClient client = new OkHttpClient();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("❌ Submission failed: " + response);
            }
            return response.body().string();
        }
    }

    private static String getQuestionId(String titleSlug, String sessionCookie) throws IOException {
        String query = """
        {
          "query":"query getQuestionDetail($titleSlug: String!) {question(titleSlug: $titleSlug) { questionId }}",
          "variables":{"titleSlug":"%s"}
        }
        """.formatted(titleSlug);

        Request request = new Request.Builder()
                .url(LEETCODE_BASE + "/graphql")
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", "LEETCODE_SESSION=" + sessionCookie + ";")
                .post(RequestBody.create(query, MediaType.get("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            JSONObject json = new JSONObject(response.body().string());
            return json.getJSONObject("data")
                    .getJSONObject("question")
                    .getString("questionId");
        }
    }
}
