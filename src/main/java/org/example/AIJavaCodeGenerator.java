package org.example;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AIJavaCodeGenerator {
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final String LEETCODE_BASE = "https://leetcode.com";
    private static final OkHttpClient client = new OkHttpClient();

    public static String generateCode(String title, String link) throws IOException {
        // First, fetch the problem details
        String problemDetails = fetchProblemDetails(title);

        Dotenv dotenv = Dotenv.load();
        String apiKey = dotenv.get("GEMINI_API_KEY");

        // Create a more detailed prompt with problem description
        String prompt = "Write clean Java code for the following LeetCode problem:\n\n" +
                "Title: " + title + "\n" +
                "Link: " + link + "\n\n" +
                "Problem details:\n" + problemDetails + "\n\n" +
                "Include inline comments explaining your approach and any key steps. " +
                "Return ONLY the solution code without any markdown formatting or additional text.";

        JSONObject requestBody = new JSONObject()
                .put("contents", new org.json.JSONArray()
                        .put(new JSONObject()
                                .put("parts", new org.json.JSONArray()
                                        .put(new JSONObject()
                                                .put("text", prompt)
                                        )
                                )
                        )
                );

        Request request = new Request.Builder()
                .url(GEMINI_URL + "?key=" + apiKey)
                .post(RequestBody.create(requestBody.toString(), MediaType.get("application/json")))
                .addHeader("Content-Type", "application/json")
                .build();

        System.out.println("Sending request to Gemini API with problem details...");

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
     * Fetches problem description, examples, and constraints from LeetCode
     */
    private static String fetchProblemDetails(String titleSlug) throws IOException {
        Dotenv dotenv = Dotenv.load();
        String session = dotenv.get("LEETCODE_SESSION");

        String graphqlUrl = LEETCODE_BASE + "/graphql";

        String query = String.format(
                "{\"query\":\"query getQuestionDetail($titleSlug: String!) " +
                        "{question(titleSlug: $titleSlug) {content exampleTestcases notes difficulty}}\",\"variables\":{\"titleSlug\":\"%s\"}}",
                titleSlug);

        RequestBody body = RequestBody.create(query, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(graphqlUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Cookie", "LEETCODE_SESSION=" + session)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.out.println("Failed to fetch problem details: " + response);
                return "No problem details available";
            }

            String responseBody = response.body().string();
            JSONObject json = new JSONObject(responseBody);

            JSONObject question = json.getJSONObject("data").getJSONObject("question");

            StringBuilder details = new StringBuilder();

            // Add content (problem description - this comes in HTML format from LeetCode API)
            String content = question.getString("content");
            // Simple HTML to text conversion to clean up the description
            content = content.replaceAll("<[^>]*>", ""); // Remove HTML tags
            content = content.replaceAll("&nbsp;", " "); // Replace HTML entities
            content = content.replaceAll("&lt;", "<");
            content = content.replaceAll("&gt;", ">");
            content = content.replaceAll("&amp;", "&");
            details.append("# Description:\n").append(content).append("\n\n");

            // Add examples
            String examples = question.getString("exampleTestcases");
            details.append("# Examples:\n").append(examples).append("\n\n");

            // Add notes (constraints are usually part of notes)
            if (!question.isNull("notes") && !question.getString("notes").isEmpty()) {
                String notes = question.getString("notes");
                notes = notes.replaceAll("<[^>]*>", ""); // Remove HTML tags
                details.append("# Constraints/Notes:\n").append(notes).append("\n\n");
            }

            // Add difficulty
            details.append("# Difficulty: ").append(question.getString("difficulty"));

            return details.toString();
        } catch (Exception e) {
            System.out.println("Error fetching problem details: " + e.getMessage());
            return "Problem details unavailable due to error: " + e.getMessage();
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