package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class LeetCodeSolutionVerifier {
    private static final OkHttpClient client = new OkHttpClient();
    private static final String LEETCODE_BASE = "https://leetcode.com";
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * Submits and verifies a solution, retrying up to MAX_RETRY_COUNT times if it fails
     * @return true if the solution was accepted
     */
    public static boolean submitAndVerifySolution(String titleSlug, String code, String language) throws IOException, InterruptedException {
        for (int attempt = 1; attempt <= MAX_RETRY_COUNT; attempt++) {
            System.out.println("\nAttempt " + attempt + " of " + MAX_RETRY_COUNT);

            // Submit the solution
            String submissionId = submitSolution(titleSlug, code, language);
            if (submissionId == null) {
                System.out.println("Submission failed, cannot verify");
                continue;
            }

            // Check the submission result
            boolean isAccepted = checkSubmissionResult(submissionId);

            if (isAccepted) {
                System.out.println("✅ Solution accepted!");
                return true;
            } else {
                System.out.println("❌ Solution incorrect, retrying...");

                if (attempt < MAX_RETRY_COUNT) {
                    // Generate a new solution for the next attempt
                    code = AIJavaCodeGenerator.generateCode(
                            titleSlug,
                            LEETCODE_BASE + "/problems/" + titleSlug + "/"
                    );
                    System.out.println("Generated new solution for retry");
                }
            }
        }

        System.out.println("Failed after " + MAX_RETRY_COUNT + " attempts");
        return false;
    }

    private static String submitSolution(String titleSlug, String code, String language) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String csrfToken = dotenv.get("X_CSRF_TOKEN");
        String session = dotenv.get("LEETCODE_SESSION");

        // Get the question ID
        String questionId = getQuestionId(titleSlug, session);
        if (questionId == null || questionId.isEmpty()) {
            return null;
        }

        String url = LEETCODE_BASE + "/problems/" + titleSlug + "/submit/";

        JSONObject payload = new JSONObject();
        payload.put("lang", language);
        payload.put("question_id", questionId);
        payload.put("typed_code", code);
        RequestBody body = RequestBody.create(payload.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("x-csrftoken", csrfToken)
                .addHeader("Cookie", "LEETCODE_SESSION=" + session + "; csrftoken=" + csrfToken)
                .addHeader("Referer", LEETCODE_BASE + "/problems/" + titleSlug + "/")
                .addHeader("Origin", LEETCODE_BASE)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("❌ Submission failed: " + response);
                return null;
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            if (json.has("submission_id")) {
                // Convert the submission_id to string, regardless of whether it's an integer or string
                String submissionId = String.valueOf(json.get("submission_id"));
                System.out.println("Submission ID: " + submissionId);
                return submissionId;
            } else {
                System.out.println("No submission ID in response");
                return null;
            }
        }
    }

    private static boolean checkSubmissionResult(String submissionId) throws IOException, InterruptedException {
        Dotenv dotenv = Dotenv.load();
        String session = dotenv.get("LEETCODE_SESSION");

        String url = LEETCODE_BASE + "/submissions/detail/" + submissionId + "/check/";

        // Poll the submission status until it's done
        int maxAttempts = 10;
        for (int i = 0; i < maxAttempts; i++) {
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Cookie", "LEETCODE_SESSION=" + session)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("Failed to check submission: " + response);
                    return false;
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                String state = json.getString("state");

                if ("SUCCESS".equals(state)) {
                    // Submission checked, check the status
                    String status = json.getString("status_msg");
                    System.out.println("Submission status: " + status);

                    // Print additional info if available
                    if (json.has("runtime")) {
                        System.out.println("Runtime: " + json.getString("runtime"));
                    }
                    if (json.has("memory")) {
                        System.out.println("Memory usage: " + json.get("memory"));
                    }

                    // Return true if "Accepted"
                    return "Accepted".equals(status);
                } else if ("PENDING".equals(state) || "STARTED".equals(state)) {
                    // Still running, wait and check again
                    System.out.println("Checking submission status... (" + state + ")");
                    TimeUnit.SECONDS.sleep(2);
                } else {
                    // Something went wrong
                    System.out.println("Unexpected submission state: " + state);
                    return false;
                }
            }
        }

        System.out.println("Timed out while checking submission");
        return false;
    }

    private static String getQuestionId(String titleSlug, String sessionCookie) throws IOException {
        String graphqlUrl = LEETCODE_BASE + "/graphql";

        JSONObject variables = new JSONObject();
        variables.put("titleSlug", titleSlug);

        JSONObject queryJson = new JSONObject();
        queryJson.put("query", "query getQuestionDetail($titleSlug: String!) {question(titleSlug: $titleSlug) {questionId}}");
        queryJson.put("variables", variables);

        RequestBody body = RequestBody.create(queryJson.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(graphqlUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", "LEETCODE_SESSION=" + sessionCookie)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Failed to get question ID: " + response);
                return null;
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            return json.getJSONObject("data")
                    .getJSONObject("question")
                    .getString("questionId");
        }
    }
}