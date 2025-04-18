package org.example;

import okhttp3.*;
import org.json.JSONObject;

import java.io.*;


public class LeetCodeAutoSelector {
    private static final String GRAPHQL_URL = "https://leetcode-api-pied.vercel.app/random";
    private static final OkHttpClient client = new OkHttpClient();

    public static LeetCodeQuestion getRandomProblem() throws IOException {
        Request request = new Request.Builder()
                .url(GRAPHQL_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }

            if(response.body() == null) throw new NullPointerException("Response body is empty");
            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);

            return new LeetCodeQuestion(
                    jsonObject.getString("id"),
                    jsonObject.getString("frontend_id"),
                    jsonObject.getString("title"),
                    jsonObject.getString("title_slug"),
                    jsonObject.getString("url")
            );
        }
    }
}
