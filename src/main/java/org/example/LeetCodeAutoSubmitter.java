package org.example;

import io.github.bonigarcia.wdm.WebDriverManager;
import io.github.cdimascio.dotenv.Dotenv;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.time.Duration;

public class LeetCodeAutoSubmitter {
    public static void submitCode(String slug, String javaCode) {
        Dotenv dotenv = Dotenv.load();
        String email = dotenv.get("LEETCODE_EMAIL");
        String password = dotenv.get("LEETCODE_PASSWORD");


        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Login
            driver.get("https://leetcode.com/accounts/login/");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("id_login"))).sendKeys(email);
            driver.findElement(By.id("id_password")).sendKeys(password);
            driver.findElement(By.id("signin_btn")).click();

            // Wait until login finishes and redirect to problem
            wait.until(ExpectedConditions.urlContains("leetcode.com"));

            // Navigate to problem
            String problemUrl = "https://leetcode.com/problems/" + slug;
            driver.get(problemUrl);

            // Select language (Java)
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#lang-select")));
            driver.findElement(By.cssSelector("div#lang-select")).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//span[text()='Java']"))).click();

            // Clear and paste AI-generated code
            WebElement codeArea = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div.view-lines")
            ));

            // Focus editor and paste
            Actions actions = new Actions(driver);
            actions.click(codeArea).perform();

            // Use clipboard-based paste to insert code
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(javaCode), null);
            actions.keyDown(Keys.CONTROL).sendKeys("v").keyUp(Keys.CONTROL).perform();

            // Click Submit
            WebElement submitBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(., 'Submit')]")
            ));
            submitBtn.click();

            System.out.println("✅ Code submitted successfully!");

        } catch (Exception e) {
            System.out.println("❌ Submission failed: " + e.getMessage());
        } finally {
            // driver.quit(); // Optional: close browser
        }
    }
}
