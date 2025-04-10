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

        JSONObject data = runGraphQLQuery(query);
        JSONObject q = data.optJSONObject("randomQuestion");

        if (q == null) {
            throw new RuntimeException("❌ Failed to fetch randomQuestion: returned null.");
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
                .build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONObject(response.body().string()).getJSONObject("data");
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
