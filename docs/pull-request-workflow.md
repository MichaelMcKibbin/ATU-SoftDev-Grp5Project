<!--
# Pull Request Workflow
# Prepared by @MichaelMcKibbin
# 23 Oct 2025
-->
# Pull Request → Review → Merge Workflow

This guide explains how to safely merge new code from a **feature branch** into the protected `main` branch through a **Pull Request (PR)**, **code review**, and **automated tests**.

---

## 1️⃣ Push your feature branch

After committing your changes locally in IntelliJ or the terminal:

```git push -u origin feature/<your-branch-name>```

This creates your branch on GitHub.
You’ll see a message in your browser:

**Compare & pull request**

Click the green button to start a Pull Request (PR).

## 2️⃣ Open a Pull Request (PR)
The comparison maight look a little like this:

```base: main  ←  compare: feature/parser-fsm```

Add a clear title and description:

```feat: implement CsvParser FSM```

**Description example:**

Adds the finite-state machine parser for CSV input, which handles quoted/unquoted fields and newline variants, and throws ParseException for malformed rows.

Link any related issues (optional):

Closes #12
Assign at least one **Reviewer** (e.g. @michaelmckibbin or another teammate).

Click **Create pull request**.

## 3️⃣ Wait for CI Tests (Automated Build)
GitHub Actions will automatically run Maven tests (mvn test).

You’ll see build status below your PR title:

- 🟡 Checks in progress
- ✅ All checks have passed (ready to merge)
- ❌ Checks failed (you’ll need to fix code/tests)

Click **Details** to view build logs.

## 4️⃣ Review Process (Reviewer’s Role)
Each reviewer should:

1. Open the PR → **Files changed** tab.

2. Read the code for:
   - Correctness and clarity
   - Consistent naming, indentation, and comments
   - Adequate test coverage

Use the **+** icon beside code lines to comment or suggest changes.

When satisfied, click:
- **Review changes → Approve**, or
- **Request changes** if something needs updating
    
## 5️⃣ Developer Makes Updates (if requested)
If changes are requested:

Continue working on the same branch in IntelliJ.

Commit and push updates:

Add a commit message such as: "fix: handle multiline quoted fields"

The Pull Request automatically updates on GitHub — no need to create a new one.

Reviewers can recheck and approve.

## 6️⃣ Merge the Pull Request
Once:

✔️ All reviews are approved

✔️ All CI checks pass

✔️ There are no merge conflicts

You’ll see the green Merge pull request button.

### Choose Squash and merge (recommended)
This combines all commits from your feature branch into one clean commit on main.

Click **Merge pull request → Squash and merge**

Add a concise summary message, e.g.:

feat: add CsvParser FSM core implementation

**Click Confirm merge.**

## 7️⃣ Clean Up the Branch
After the merge is complete, and you're satisfied that everything is as intended, 
GitHub offers:

“Delete branch”

✅ Click it — this keeps the repo tidy.


Then on your local machine:

git checkout main
git pull origin main
git branch -d feature/parser-fsm

## 8️⃣ Summary – Developer View
|Step	|Action	|Tool|
|:-	|:-	|:-|
|0	|Pull latest	|Terminal|
|1	|Push feature branch	|IntelliJ / Terminal|
|2	|Create Pull Request	|GitHub|
|3	|Wait for CI tests	|GitHub Actions|
|4	|Get review & feedback	|GitHub|
|5	|Fix and push updates	|IntelliJ|
|6	|Squash and merge after approval	|GitHub|
|7	|Delete branch & pull main	|GitHub / Terminal|

👥 9️⃣ Summary – Reviewer View

|Step	|Action	|
|:-	|:-	|
|1	|Open PR → “Files changed” tab|
|2	|Add comments or suggestions|
|3	|Approve or request changes|
|4	|Verify that CI tests pass|
|5	|Confirm merge (if authorised)|


## 1️⃣0️⃣ Example Workflow (Visual)
```
(feature/parser-fsm)
     │
     ├── Commit A  feat: start CsvParser
     ├── Commit B  fix: escape quote handling
     └── Commit C  test: add quoted field test
        ↓
Push → Open PR → Review + CI → Approve → Merge (Squash)
        ↓
(main)
     └── feat: implement CsvParser FSM
```
     
## ⚠️ Common Mistakes

|Problem	| Fix                      |
|:-    |:-------------------------|
|❌ Commit message is too long	| Use a short, descriptive title and description |
|❌ Committed directly to main	| Always create a feature branch first (git checkout -b feature/...) |
|❌ Didn’t pull latest before starting	| Run git pull origin main before new work |
|❌ Merged without review	| Add a reviewer and wait for approval |
|❌ Ignored failing tests	| Fix issues before merging|
|❌ Left old branches in repo	| Delete after merge (git branch -d)|


## Tips
Keep PRs small and focused (200–300 lines max).

Always include or update unit tests in your PR.

Use clear commit messages (feat:, fix:, test:, docs:).

Respond politely to reviewer feedback — reviews are about code quality, not criticism.

Use Squash and Merge to keep main history clean.

<!--
📸 (For future use...) Screenshot Placeholders
[Screenshot 1] – Creating a PR on GitHub

[Screenshot 2] – Reviewer “Files changed” view

[Screenshot 3] – Approve / Request changes buttons

[Screenshot 4] – Squash and merge confirmation

Add these later.
-->

Repository URL:
https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project

