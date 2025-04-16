package org.example;

public class Main {
    public static void main(String[] args) {
        try {
            // Load solved problems from file
            LeetCodeQuestion selected = LeetCodeAutoSelector.autoSelect();
            System.out.println("Selected problem: " + selected.title());
            System.out.println("URL: " + selected.url());

            // Generate code
            System.out.println("\nGenerating solution with AI...");
            String solution = AIJavaCodeGenerator.generateCode(
                    selected.title(),
                    selected.url()
            );

            // Confirm submission
            System.out.println("\nSubmitting solution to LeetCode...");

            // Submit to LeetCode
            LeetCodeAutoSubmitter.submitCode(selected.title_slug(), solution, "java");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}