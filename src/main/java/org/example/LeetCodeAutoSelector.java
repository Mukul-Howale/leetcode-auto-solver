package org.example;

import okhttp3.*;
import org.json.JSONObject;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class LeetCodeAutoSelector {
    private static final String GRAPHQL_URL = "https://leetcode.com/graphql";
    private static final OkHttpClient client = new OkHttpClient();
    private static final String SOLVED_FILE = "src/main/java/org/example/solved_problems.txt";

    public static LeetCodeQuestion autoSelect() throws Exception {
        Set<String> solved = loadSolvedProblems();
        LeetCodeQuestion selected = null;

        while (true) {
            LeetCodeQuestion candidate = getRandomProblem();
            if (!solved.contains(candidate.slug())) {
                selected = candidate;
                break;
            }
        }

        System.out.println("🎯 Selected unsolved problem:");
        selected.print();

        // Save it to solved file
        saveToSolvedFile(selected.slug());

        return selected;
    }

    private static LeetCodeQuestion getRandomProblem() throws IOException {
        String query = """
        query {
          randomQuestion {
            title
            titleSlug
            difficulty
            acRate
          }
        }""";

        // Retry logic
        int retries = 3;
        JSONObject q = null;

        while (retries-- > 0) {
            JSONObject data = runGraphQLQuery(query);
            q = data.optJSONObject("randomQuestion");

            if (q != null) break;

            System.out.println("⚠️ Retrying fetch random question...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        if (q == null) {
            throw new RuntimeException("❌ Failed to fetch randomQuestion after retries.");
        }

        return new LeetCodeQuestion(
                q.getString("title"),
                q.getString("titleSlug"),
                q.getString("difficulty"),
                q.getDouble("acRate")
        );
    }

    private static JSONObject runGraphQLQuery(String query) throws IOException {
        JSONObject body = new JSONObject();
        body.put("query", query);

        Request request = new Request.Builder()
                .url(GRAPHQL_URL)
                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .addHeader("Content-Type", "application/json")
                .addHeader("Referer", "https://leetcode.com/problemset/all/")
                .addHeader("Origin", "https://leetcode.com")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }

            if(response.body() == null) throw new NullPointerException("Response body is empty");
            System.out.println(response.body());
            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            // Safeguard: show error if `data` is missing
            if (!json.has("data")) {
                throw new RuntimeException("❌ GraphQL response missing 'data' field:\n" + responseBody);
            }

            return json.getJSONObject("data");
        }
    }

    private static Set<String> loadSolvedProblems(){
        File file = new File(SOLVED_FILE);

        Set<String> solved = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                solved.add(line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return solved;
    }

    private static void saveToSolvedFile(String slug) throws IOException {
        try (FileWriter writer = new FileWriter(SOLVED_FILE, true)) {
            writer.write(slug + "\n");
        }
    }
}
