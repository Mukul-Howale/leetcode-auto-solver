package org.example;

import okhttp3.*;
import org.json.JSONObject;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class LeetCodeAutoSelector {
    private static final String GRAPHQL_URL = "https://leetcode-api-pied.vercel.app/random";
    private static final OkHttpClient client = new OkHttpClient();
    private static final String SOLVED_FILE = "src/main/java/org/example/solved_problems_test.txt";

    public static LeetCodeQuestion autoSelect() throws Exception {
        Set<String> solved = loadSolvedProblems();
        LeetCodeQuestion selected = null;

        while (true) {
            LeetCodeQuestion candidate = getRandomProblem();
            if (!solved.contains(candidate.title())) {
                selected = candidate;
                break;
            }
        }

        // Save it to solved file
        saveToSolvedFile(selected.title());

        return selected;
    }

    private static LeetCodeQuestion getRandomProblem() throws IOException {
        Request request = new Request.Builder()
                .url(GRAPHQL_URL)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code: " + response);
            }

            if(response.body() == null) throw new NullPointerException("Response body is empty");
            System.out.println(response.body());
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
