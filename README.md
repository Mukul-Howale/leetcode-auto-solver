# LeetCode Auto Solver

Solve LeetCode questions automatically! 🚀

This project automates solving LeetCode problems by fetching random unsolved problems and generating AI-powered solutions without any human intervention.
It takes random questions from leetcode and passes it to gemini api.
The solution generated is then submitted to leetcode.

## Requirement
1. Build Tool - Maven
2. Create .env file at the root of the project
   ```
   e.g. D:\leetcode-auto-solver\.env
   ```
3. Add the following variables to the .env file
   ```
   X_CSRF_TOKEN
   LEETCODE_SESSION
   GEMINI_API_KEY
   ```
4. Create a folder and add .txt files
   ```
   e.g.
   src/main/java/org/example/problems/solved_problems.txt
   src/main/java/org/example/problems/unsolved_problems.txt
   ```

## Getting Started

1. **Clone the repository:**
   ```
   git clone https://github.com/Mukul-Howale/leetcode-auto-solver.git
   cd leetcode-auto-solver
   ```

2. **Build the project:**
   ```
   mvn clean install
   ```

3. **Run the project:**
   ```
   mvn exec:java -Dexec.mainClass="org.example.Main"
   ```

## TO:DO

1. Sending question details in the prompt for better solution response.
2. Using different GPT api for failed solution.
3. Retrying all unsolved questions.
4. Option to submit a solution for a particular question.
5. Option to submit particular number of questions. (Right now it runs on loop until stoped manually)
6. Maybe a UI (I don't know, maybe!!)

Note : I made this project because I was bored. Try to solve the questions youself!!

Happy Coding!!
