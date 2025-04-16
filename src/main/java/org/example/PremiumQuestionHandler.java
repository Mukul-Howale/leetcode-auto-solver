package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;

public class PremiumQuestionHandler {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String LEETCODE_BASE = "https://leetcode.com";

    /**
     * Checks if a question is premium and if user has premium access
     * @param titleSlug The problem's title slug
     * @return true if the question is accessible (either free or premium with access)
     */
    public static boolean isQuestionAccessible(String titleSlug) throws IOException {
        // First check if question is premium
        boolean isPremium = isPremiumQuestion(titleSlug);

        if (!isPremium) {
            // Not a premium question, so it's accessible
            return true;
        }

        // It's a premium question, check if user has premium access
        return hasUserPremiumAccess();
    }

    private static boolean isPremiumQuestion(String titleSlug) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String session = dotenv.get("LEETCODE_SESSION");

        String graphqlUrl = LEETCODE_BASE + "/graphql";

        JSONObject variables = new JSONObject();
        variables.put("titleSlug", titleSlug);

        JSONObject queryJson = new JSONObject();
        queryJson.put("query", "query questionData($titleSlug: String!) {question(titleSlug: $titleSlug) {isPaidOnly}}");
        queryJson.put("variables", variables);

        RequestBody body = RequestBody.create(queryJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(graphqlUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", "LEETCODE_SESSION=" + session)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Failed to check premium status. Response: " + response.code());
                return false; // Assume not premium if check fails
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            return json.getJSONObject("data")
                    .getJSONObject("question")
                    .getBoolean("isPaidOnly");
        } catch (Exception e) {
            System.out.println("Error checking premium status: " + e.getMessage());
            return false; // Assume not premium if check fails
        }
    }

    private static boolean hasUserPremiumAccess() throws IOException {
        Dotenv dotenv = Dotenv.load();
        String session = dotenv.get("LEETCODE_SESSION");

        String url = LEETCODE_BASE + "/api/is_current_user_premium/";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Cookie", "LEETCODE_SESSION=" + session)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Failed to check premium access. Response: " + response.code());
                return false;
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            return json.optBoolean("is_premium", false);
        } catch (Exception e) {
            System.out.println("Error checking premium access: " + e.getMessage());
            return false;
        }
    }
}