package org.example;

public class Main {
    public static void main(String[] args) {
        while(true){
            try {
                // Select an unsolved problem
                LeetCodeQuestion selected = null;
                boolean isAccessible = false;

                // Keep trying until we find an accessible problem
                while (selected == null || !isAccessible) {
                    selected = LeetCodeAutoSelector.getRandomProblem();

                    // Skip if already solved
                    if (SolutionStorageManager.isProblemSolved(selected.title_slug())) {
                        System.out.println("Problem already solved: " + selected.title());
                        selected = null;
                        continue;
                    }

                    // Check if problem is premium and accessible
                    isAccessible = PremiumQuestionHandler.isQuestionAccessible(selected.title_slug());
                    if (!isAccessible) {
                        System.out.println("Premium problem not accessible: " + selected.title());
                    }
                }

                System.out.println("Selected problem: " + selected.title());
                System.out.println("URL: " + selected.url());

                // Generate code
                System.out.println("\nGenerating solution with AI...");
                String solution = AIJavaCodeGenerator.generateCode(
                        selected.title(),
                        selected.url()
                );

                // Submit and verify solution (with retries)
                System.out.println("\nSubmitting solution to LeetCode...");
                boolean isAccepted = LeetCodeSolutionVerifier.submitAndVerifySolution(
                        selected.title_slug(),
                        solution,
                        "java"
                );

                // Store the solution regardless of outcome
                SolutionStorageManager.storeSolution(selected, isAccepted);

                if (isAccepted) {
                    System.out.println("\n🎉 Problem solved successfully!");
                } else {
                    System.out.println("\n😞 Failed to solve problem after multiple attempts");
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}