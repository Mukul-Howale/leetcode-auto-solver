package org.example;

public class Main {
    public static void main(String[] args){
        try {
            // Load solved problems from file
            LeetCodeQuestion selected = LeetCodeAutoSelector.autoSelect();

            // Generate code
            String solution = AIJavaCodeGenerator.generateCode(
                    selected.title(),
                    "https://leetcode.com/problems/" + selected.slug()
            );

            // Submit to LeetCode
            LeetCodeAutoSubmitter.submitCode(selected.slug(), solution);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}