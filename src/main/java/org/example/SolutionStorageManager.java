package org.example;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SolutionStorageManager {
    /**
     * Saves a solved problem to the tracking file and stores the solution code
     * @param question The LeetCode question that was solved
     * @param isAccepted Whether the solution was accepted by LeetCode
     */
    public static void storeSolution(LeetCodeQuestion question, boolean isAccepted) throws IOException {
        String solvedFile = "src/main/java/org/example/problems/solved_problems.txt";
        String unSolvedFile = "src/main/java/org/example/problems/unsolved_problems.txt";

        // Only add to solved problems list if accepted
        if (isAccepted) saveProblem(question, solvedFile);
        else saveProblem(question, unSolvedFile);
    }

    /**
     * Adds a problem to list depending if they are solved or not
     */
    private static void saveProblem(LeetCodeQuestion question, String path) throws IOException {
        // Create parent directories if needed
        Path path1 = Paths.get(path);
        Files.createDirectories(path1.getParent());

        // Format: title_slug,id,title,url,timestamp
        String entry = String.format("%s,%s,%s,%s,%s\n",
                question.title_slug(),
                question.id(),
                question.title(),
                question.url(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Append to file
        Files.write(
                path1,
                entry.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);

        System.out.println("Problem added to solved list: " + question.title());
    }

    /**
     * Checks if a problem has been solved before
     */
    public static boolean isProblemSolved(String titleSlug) {
        try {
            Path path = Paths.get("src/main/java/org/example/solved_problems.txt");
            if (!Files.exists(path)) {
                return false;
            }

            return Files.lines(path)
                    .map(line -> line.split(",")[0]) // Get the title_slug
                    .anyMatch(slug -> slug.equals(titleSlug));
        } catch (IOException e) {
            System.out.println("Error checking solved problems: " + e.getMessage());
            return false;
        }
    }
}