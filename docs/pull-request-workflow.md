<!--
# Pull Request Workflow
# Prepared by @MichaelMcKibbin
# 23 Oct 2025 / Updated 16 Nov 2025
-->
# Pull Request → Review → Merge Workflow

This guide explains how to safely merge new code from a **feature branch** into the protected `main` branch through a **Pull Request (PR)**, **code review**, and **automated tests**.

---

## 1️⃣ Update Your Branch Before Opening PR ⚠️ IMPORTANT

**Before pushing**, ensure your branch has the latest changes from main:

```bash
# Fetch latest changes
git fetch origin

# Check if main has moved ahead  
git log HEAD..origin/main --oneline

# If there are new commits, update your branch
git rebase origin/main
# OR
git merge origin/main

# Resolve any conflicts if they appear
# Run tests to ensure everything still works
```

**Why this matters:** If you skip this step, your PR may delete other people's merged work!

## 2️⃣ Push your feature branch

After updating and testing:

```git push -u origin feature/<your-branch-name>```

This creates your branch on GitHub.
You’ll see a message in your browser:

**Compare & pull request**

Click the green button to start a Pull Request (PR).

## 3️⃣ Open a Pull Request (PR)
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

## 4️⃣ Wait for CI Tests (Automated Build)
GitHub Actions will automatically run Maven tests (mvn test).

You’ll see build status below your PR title:

- 🟡 Checks in progress
- ✅ All checks have passed (ready to merge)
- ❌ Checks failed (you’ll need to fix code/tests)

Click **Details** to view build logs.

## 5️⃣ Review Process (Reviewer’s Role)
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

---

## 🚨 Preventing Merge Conflicts

### Before Opening a PR
**Always update your branch with latest main:**

It will help avoid overriding or deleting content submitted in other PRs/commits.


```bash
git fetch origin
git rebase origin/main  # or: git merge origin/main
git push -u origin feature/my-branch
```

### Before Merging a PR
**Check if main has changed since you opened the PR:**

On GitHub, look for:
- ⚠️ "This branch is X commits behind main" → **Update first!**
- ❌ "This branch has conflicts" → **Must resolve!**

**If main has changed, update your branch:**

```bash
git checkout feature/my-branch
git fetch origin
git rebase origin/main  # or: git merge origin/main
git push --force-with-lease  # if rebased
# OR
git push  # if merged
```

### Team Coordination tips
- ⏱️ Keep branches short-lived
- 👀 Review PRs fast as possible
- 💬 Communicate what you're working on Whatsapp, Github for help
- 🔄 Update your branch daily if work takes multiple days


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

