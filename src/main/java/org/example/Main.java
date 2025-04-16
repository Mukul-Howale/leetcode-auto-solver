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

            // Print the first few lines of the solution for verification
            System.out.println("\nGenerated solution (first 5 lines):");
            String[] lines = solution.split("\n", 6);
            for (int i = 0; i < Math.min(5, lines.length); i++) {
                System.out.println(lines[i]);
            }
            if (lines.length > 5) {
                System.out.println("... (full solution has " + lines.length + " lines)");
            }

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