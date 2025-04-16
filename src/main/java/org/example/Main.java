package org.example;

public class Main {
    public static void main(String[] args){
        try {
            // Load solved problems from file
            LeetCodeQuestion selected = LeetCodeAutoSelector.autoSelect();
            System.out.println(selected.url());

            // Generate code
            String solution = AIJavaCodeGenerator.generateCode(
                    selected.title_slug(),
                    selected.url()
            );
            System.out.println(solution);

            // Submit to LeetCode
            LeetCodeAutoSubmitter.submitCode(selected.title_slug(), solution, "java");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}