package org.example;

public class Main {
    public static void main(String[] args){
        try {
            // Load solved problems from file
            LeetCodeQuestion selected = LeetCodeAutoSelector.autoSelect();

            // Generate code
            String solution = AIJavaCodeGenerator.generateCode(
                    selected.title_slug(),
                    selected.url()
            );

            // Submit to LeetCode
            LeetCodeAutoSubmitter.submitCode(selected.title_slug(), solution);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}