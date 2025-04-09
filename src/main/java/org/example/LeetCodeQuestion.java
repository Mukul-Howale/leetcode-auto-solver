package org.example;

public record LeetCodeQuestion(String title, String slug, String difficulty, double acRate) {

    public void print() {
        System.out.printf("🔹 %s (%s) | AC Rate: %.2f%%\n🔗 https://leetcode.com/problems/%s\n\n",
                title, difficulty, acRate, slug);
    }
}

