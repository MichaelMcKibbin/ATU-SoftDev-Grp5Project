# macOS Development Environment Setup Guide
## For ATU-SoftDev-Grp5Project

A complete, beginner-friendly guide for setting up your Mac to work on the team project. This guide covers everything from basic command-line navigation to committing code to GitHub.

---

## Prerequisites

- **Terminal App:** You should have Terminal pre-installed on your Mac
- **Administrator Access:** You'll need your Mac password for some steps
- **GitHub Account:** Create one at https://github.com if you don't have one
- **Internet Connection:** Required for downloads and GitHub access

---

## IMPORTANT: Before You Start

**Please read the project's own setup documentation first:**

1. **Getting Started Guide:** https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/blob/main/docs/getting-started.md

This document may have project-specific setup steps that are important for your team's particular setup.

---

## TL;DR steps:

#### 1. Set up directories and clone
cd ~/Documents/ATU/SoftwareDevelopment
git clone https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project.git

#### 2. Get latest code
git pull origin main

#### 3. Create your branch
git checkout -b feature/my-task

#### 4. Make changes in your IDE...

#### 5. Save your work
git add .
git commit -m "Describe what you did"

#### 6. Upload to GitHub
git push -u origin feature/my-task

#### 7. Create PR on GitHub website
https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/pulls

---

# Tutorial

## Part 1: Installing Developer Tools

### Step 1: Check Your Shell

Before we start, let's identify which shell your Mac uses. Open Terminal (Command + Space, type "Terminal", press Enter).

Run this command:
```bash
echo $SHELL
```

**What you'll see:**
- If output is `/bin/zsh` → You're using **zsh** (modern Macs, Catalina 10.15+)
- If output is `/bin/bash` → You're using **bash** (older Macs)

**Remember this for later when updating PATH.**

---

### Step 2: Install Homebrew (Package Manager)

Homebrew is a tool that makes installing software on Mac much easier.

**Check if you already have Homebrew:**
```bash
brew --version
```

**Expected output:** (if already installed)
```
Homebrew 3.x.x
```

If you see a version number, skip to Step 3.

**If not installed, paste this command:**
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

Follow any on-screen prompts. You'll likely need to enter your Mac password.

**After installation, update Homebrew:**
```bash
brew update
```

---

### Step 3: Install Java 21

Java is required to compile and run your project code.

**Install Java 21:**
```bash
brew install openjdk@21
```

**Update your PATH and JAVA_HOME** (This tells your Mac where to find Java):

**Choose the option that matches your setup:**

#### Option A: Apple Silicon Macs (M1, M2, M3 chips) + zsh
Most likely scenario for new Macs.

```bash
echo 'export PATH="/opt/homebrew/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/opt/homebrew/opt/openjdk@21"' >> ~/.zshrc
source ~/.zshrc
```

#### Option B: Intel Macs + zsh
```bash
echo 'export PATH="/usr/local/opt/openjdk@21/bin:$PATH"' >> ~/.zshrc
echo 'export JAVA_HOME="/usr/local/opt/openjdk@21"' >> ~/.zshrc
source ~/.zshrc
```

#### Option C: bash users (older macOS)
```bash
echo 'export PATH="/usr/local/opt/openjdk@21/bin:$PATH"' >> ~/.bash_profile
echo 'export JAVA_HOME="/usr/local/opt/openjdk@21"' >> ~/.bash_profile
source ~/.bash_profile
```

**Verify Java is installed:**
```bash
java -version
javac -version
```

**Expected output:**
```
openjdk version "21.0.x"
OpenJDK Runtime Environment (build 21.0.x+...)
OpenJDK 64-Bit Server VM (build 21.0.x+...)
```

---

### Step 4: Install Maven

Maven helps manage your project's code and dependencies.

**Install Maven:**
```bash
brew install maven
```

**Verify Maven is installed:**
```bash
mvn -version
```

**Expected output:**
```
Apache Maven 3.x.x
Maven home: /opt/homebrew/Cellar/maven/3.x.x/libexec
Java version: 21.0.x, vendor: Homebrew
```

---

### Step 5: Install Git

Git is the version control system you'll use to save and share your code.

**Install Git:**
```bash
brew install git
```

**Verify Git is installed:**
```bash
git --version
```

**Expected output:**
```
git version 2.x.x
```

---

### Step 6: Configure Git with Your Information

Git needs to know who you are when you save your work.

**Set your name:**
```bash
git config --global user.name "Your Firstname Lastname"
```

**Example:**
```bash
git config --global user.name "John Smith"
```

**Set your email (use your GitHub email):**
```bash
git config --global user.email "your-email@student.atu.ie"
```

**Set default branch name:**
```bash
git config --global init.defaultBranch main
```

**Enable Credential Helper** (Saves your token so you don't have to type it every time):
```bash
git config --global credential.helper osxkeychain
```

**Verify your configuration:**
```bash
git config --global --list
```

**Expected output:**
```
user.name=John Smith
user.email=your-email@student.atu.ie
init.defaultBranch=main
credential.helper=osxkeychain
```

---

### Step 7: Generate GitHub Personal Access Token

GitHub requires a special token (like a password) for you to push code. Regular passwords no longer work.

**Steps:**

1. Go to: https://github.com/settings/tokens

2. Click **"Generate new token"** → **"Generate new token (classic)"**

3. Configure the token:
   - **Token name:** Enter something like "macOS Dev Setup"
   - **Expiration:** Select "90 days" (or "No expiration" if you prefer)
   - **Scopes:** Check these boxes:
     - `repo` (Full control of private repositories)
     - `workflow` (Update GitHub Action workflows)

4. Scroll down and click **"Generate token"**

5. **IMPORTANT:** Copy the token immediately and save it in a safe place (Notes app, password manager, etc.). You won't see it again!

**Note:** When you push code to GitHub later, use this token as your "password".

---

## Part 2: Understanding the Command Line (CLI)

Before cloning your project, let's learn a few basic commands you'll use frequently.

### Basic Commands Explained

| Command | What it does | Example |
|---------|------------|---------|
| `pwd` | Shows current directory (where you are) | `pwd` |
| `ls` | Lists files/folders in current directory | `ls` |
| `mkdir` | Creates a new folder | `mkdir newfolder` |
| `cd` | Changes directory (moves to a folder) | `cd newfolder` |
| `cd ..` | Goes up one level (to parent folder) | `cd ..` |
| `cd ~` | Goes to home directory | `cd ~` |

### Practice: Navigate Your Mac

**See where you are right now:**
```bash
pwd
```

**Expected output:**
```
/Users/Username
```

**Go to your home directory:**
```bash
cd ~
```

**List what's in your home directory:**
```bash
ls
```

**Expected output:**
```
Applications    Desktop        Documents      Downloads      Library        Movies         Music          Pictures       Public
```

**Go to Documents:**
```bash
cd Documents
```

**Check you're in the right place:**
```bash
pwd
```

**Expected output:**
```
/Users/YourUsername/Documents
```

---

## Part 3: Setting Up Your Project Directory

Now let's create a folder structure for your project.

### Step 1: Create the Directory Structure

We're going to create: `~/Documents/ATU/SoftwareDevelopment/`

**Navigate to Documents:**
```bash
cd ~/Documents
```

**Create the ATU folder:**
```bash
mkdir ATU
```

**Move into the ATU folder:**
```bash
cd ATU
```

**Create the SoftwareDevelopment folder:**
```bash
mkdir SoftwareDevelopment
```

**Move into the SoftwareDevelopment folder:**
```bash
cd SoftwareDevelopment
```

**Verify you're in the right place:**
```bash
pwd
```

**Expected output:**
```
/Users/YourUsername/Documents/ATU/SoftwareDevelopment
```

---

### Step 2: Clone the Team Repository

Now you'll download the project code from GitHub.

**Clone the repository (download the project):**
```bash
git clone https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project.git
```

This will create a folder called `ATU-SoftDev-Grp5Project`. Move into it:
```bash
cd ATU-SoftDev-Grp5Project
```

**See what's inside the project:**
```bash
ls
```

**Expected output:**
```
README.md       docs            pom.xml         src             target
```

**Verify you're in the project directory:**
```bash
pwd
```

**Expected output:**
```
/Users/YourUsername/Documents/ATU/SoftwareDevelopment/ATU-SoftDev-Grp5Project
```

---

## Part 4: Understanding Git Workflow

### What is a Branch?

Think of a branch as a separate copy of the project where **you** can work without affecting others' code. Once your work is complete and reviewed, it gets merged back into the main project.

**Important Rule:** Never work directly on the `main` branch. Always create your own branch.

### Example:
- Main branch = Official project code
- Your branch = Your personal workspace for a feature
- Pull Request = Your request to merge your work back into Main

---

## Part 5: Git Workflow - Step by Step

### Step A: Make Sure You Have the Latest Code

Before starting new work, get the latest version from GitHub.

**Switch to the main branch:**
```bash
git checkout main
```

**Get the latest code from GitHub:**
```bash
git pull origin main
```

**Expected output:**
```
Already up to date.
```
(Or it will show updates if others have pushed code)

---

### Step B: Create Your Own Branch

Create a new branch for your task. Use a name that describes what you're doing.

**Create and switch to a new branch:**
```bash
git checkout -b feature/your-task-name
```

**Examples:**
```bash
git checkout -b feat/feature-a
git checkout -b feat/feature-b
git checkout -b test/feature-c
git checkout -b docs/class-something
```

**Check which branch you're on:**
```bash
git branch
```

**Expected output:**
```
  main
* feature/your-task-name
```

(The `*` shows you're on the feature branch)

---

### Step C: Write Your Code

Now you can write code, create files, or make changes to existing files.

Use your IDE (VS Code, IntelliJ, etc.) to make your changes in this directory.

---

### Step D: Check What Changed

Before saving, see what files you've modified.

**See the status of your work:**
```bash
git status
```

**Expected output:**
```
On branch feature/user-login
Changes not staged for commit:
  modified:   src/User.java
  modified:   src/Database.java
Untracked files:
  new file:   src/Login.java
```

**Explanation:**
- **modified:** Files you changed
- **Untracked:** New files Git doesn't know about yet

---

### Step E: Stage Your Changes (Prepare to Save)

Tell Git which files you want to save.

**Add all changed files:**
```bash
git add .
```

The dot (`.`) means "add everything I changed."

**Or add specific files:**
```bash
git add src/User.java src/Login.java
```

**Check what's staged:**
```bash
git status
```

**Expected output (files should be green):**
```
Changes to be committed:
  modified:   src/User.java
  new file:   src/Login.java
```

---

### Step F: Save Your Changes (Commit)

Save your changes with a message explaining what you did.

**Commit your changes:**
```bash
git commit -m "Add user login feature"
```

**Expected output:**
```
[feature/user-login a1b2c3d] Add user login feature
 2 files changed, 45 insertions(+)
 create mode 100644 src/Login.java
```

**Good commit messages:**
- ✅ "Add user login validation"
- ✅ "Fix password encryption bug"
- ✅ "Update database connection string"
- ✅ "Refactor authentication module"

**Bad commit messages:**
- ❌ "fix stuff"
- ❌ "changes"
- ❌ "asdfgh"
- ❌ "wip" (Work In Progress - be specific!)

**Check your commit:**
```bash
git log --oneline
```

**Expected output:**
```
a1b2c3d Add user login feature
7f8e9d0 Add database schema
5c6b2a1 Initial commit
```

---

### Step G: Upload Your Branch to GitHub (Push)

Send your saved changes to GitHub so others can see them.

**First time pushing this branch:**
```bash
git push -u origin feature/your-task-name
```

**Example:**
```bash
git push -u origin feature/user-login
```

**Expected output:**
```
Enumerating objects: 5, done.
Counting objects: 100% (5/5), done.
Delta compression using up to 8 threads
To github.com:MichaelMcKibbin/ATU-SoftDev-Grp5Project.git
 * [new branch]      feature/user-login -> feature/user-login
Branch 'feature/user-login' set up to track 'origin/feature/user-login'.
```

**Subsequent pushes on the same branch:**
```bash
git push
```

**Authentication:**
- **Username:** Your GitHub username
- **Password:** Your Personal Access Token (from Part 1, Step 7)

Since we set up osxkeychain in Part 1, Step 6, you should only have to enter this once. It will be saved for future pushes.

---

## IMPORTANT: Follow the Team's PR Workflow

Before creating your pull request, **read the team's official PR workflow:**

https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/blob/main/docs/pull-request-workflow.md

This document explains your team's specific requirements for:
- PR naming conventions
- Code review process
- Testing requirements before PR
- Merge policies
- Any additional checks or procedures

---

## Part 6: Create a Pull Request (PR)

A Pull Request is how you ask to merge your work into the main project.

### Steps:

**1. Go to the GitHub repository:**
```
https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project
```

**2. Look for the yellow banner**

After you push, you should see a yellow banner saying:
```
feature/your-task-name had recent pushes
```

Click the green **"Compare & pull request"** button.

**3. If you don't see the banner:**
- Click **"Pull requests"** tab at the top
- Click **"New pull request"** button
- Select your branch from the "compare" dropdown

**4. Fill in the PR details:**

**Title:** Brief description
- Example: "Add user login feature"

**Description:** Explain:
- What changes you made
- Why you made them
- How to test it

Example:
```
## What changed
- Added login form to User.java
- Added password validation
- Updated database schema

## How to test
- Run the application
- Click "Login"
- Try logging in with test account (username: test, password: test123)

## Related issue
Closes #15
```

**5. Click "Create pull request"**

**6. Wait for review**

A teammate will review your code. They might:
- Approve it (ready to merge)
- Request changes (you need to fix something)

**7. If changes are requested:**
- Make the changes locally on your branch
- Run: `git add .`
- Run: `git commit -m "Address review feedback"`
- Run: `git push`
- The PR automatically updates with your new changes

**8. Once approved, click "Merge pull request"**

Your code is now part of the main project!

---

### After Merging: Clean Up Your Branch

**Once your PR is merged, delete your branch to keep the repository clean:**

**Delete the remote branch on GitHub:**
```bash
git push origin --delete feature/your-task-name
```

**Delete the local branch on your Mac:**
```bash
git branch -d feature/your-task-name
```

**Switch back to main:**
```bash
git checkout main
git pull origin main
```

Now you're ready to create a new branch for your next task!

---

## Quick Reference: Git Commands Cheat Sheet

### Essential Daily Commands

```bash
# Check which branch you're on
git branch

# Switch to main branch
git checkout main

# Get latest code from GitHub
git pull origin main

# Create a new branch
git checkout -b feature/task-name

# Check what you changed
git status

# See differences in a file
git diff src/User.java

# Stage all changes
git add .

# Stage specific files
git add src/User.java src/Login.java

# Save your changes
git commit -m "Describe what you did"

# Upload to GitHub
git push

# View commit history
git log --oneline

# View who changed what
git blame src/User.java
```

### When Something Goes Wrong

```bash
# Undo changes to a specific file (WARNING: loses those changes)
git checkout -- src/User.java

# Discard all uncommitted local changes (WARNING: loses all changes)
git reset --hard HEAD

# Get latest from GitHub (if your code is behind)
git pull origin main

# See which branches exist on GitHub
git branch -r

# Delete a local branch
git branch -d feature/old-feature

# Undo your last commit (but keep the changes)
git reset --soft HEAD~1

# See what you're about to push
git log -p origin/main..HEAD
```

---

## Troubleshooting

### Problem: "command not found: git" or "command not found: java"

**Solution:**
```bash
source ~/.zshrc
```

(or `source ~/.bash_profile` if you're using bash)

Then close and reopen Terminal.

---

### Problem: "fatal: Authentication failed"

**Solution:**
- You entered your GitHub password instead of your Personal Access Token
- Generate a token: https://github.com/settings/tokens
- Use that token as your "password"
- Make sure you selected the right scopes (`repo` and `workflow`)

---

### Problem: "Your branch and 'origin/main' have diverged"

**Solution:**
You and a teammate both changed the same files. Ask your team lead for help with merge conflicts. This is normal in team development!

---

### Problem: "fatal: not a git repository"

**Solution:**
Make sure you're in the project directory:
```bash
cd ~/Documents/ATU/SoftwareDevelopment/ATU-SoftDev-Grp5Project
```

---

### Problem: Can't push changes / "Permission denied"

**Solution:**
Make sure you committed your work first:
```bash
git status
git add .
git commit -m "Your message"
git push
```

---

### Problem: "Please enter your GitHub credentials" keeps appearing

**Solution:**
This means your credentials weren't saved. Set up the credential helper:
```bash
git config --global credential.helper osxkeychain
```

Then try pushing again. It will ask once, then save for future use.

---

### Problem: Accidentally committed to main branch instead of feature branch

**Solution:**
Don't panic! Your team lead can help fix this. For now:
```bash
git log --oneline
```

Share the commit hash with your team lead. They can help revert it.

---

## Summary: Your First Day Workflow

```bash
# 1. Set up directories and clone
cd ~/Documents/ATU/SoftwareDevelopment
git clone https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project.git
cd ATU-SoftDev-Grp5Project

# 2. Get latest code
git pull origin main

# 3. Create your branch
git checkout -b feature/my-task

# 4. Make changes in your IDE
# (Edit files, write code, create new files, etc.)

# 5. Check what changed
git status

# 6. Stage and save your work
git add .
git commit -m "Describe what you did"

# 7. Upload to GitHub
git push -u origin feature/my-task

# 8. Create PR on GitHub website
# Go to repository → Click "Compare & pull request" → Fill in details → Submit

# 9. Wait for review and merge
# Team reviews your code, approves, and merges

# 10. Clean up
git checkout main
git pull origin main
git branch -d feature/my-task
```

---

## Final Verification Checklist

Before you start coding, verify everything is working:

- [ ] **Java installed:** `java -version` shows 21.0.x
- [ ] **Maven installed:** `mvn -version` shows Apache Maven 3.x.x
- [ ] **Git installed:** `git --version` shows git 2.x.x
- [ ] **Git configured:** `git config --global --list` shows your name and email
- [ ] **GitHub token generated** and saved somewhere safe
- [ ] **Project cloned:** You can see the files in `/Documents/ATU/SoftwareDevelopment/ATU-SoftDev-Grp5Project/`
- [ ] **Can create branch:** `git checkout -b feature/test-branch` works
- [ ] **Read getting-started.md:** You've checked the project-specific setup
- [ ] **Read pull-request-workflow.md:** You understand the team's PR process

**If any checkmarks are missing**, go back to that section and run the command again.

---

## Important Reminders

- **Always create a new branch** – Never work on `main`  
- **Pull before you start** – Get latest code: `git pull origin main`  
- **Commit frequently** – Don't wait until the end of the day  
- **Write clear messages** – "Add login feature" not "stuff"  
- **Push to GitHub** – Otherwise your teammates can't see your work  
- **Create a PR** – Don't merge directly to main
- **Get a review** – Someone should check your code before merging  
- **Check team docs first** – Read getting-started.md and pull-request-workflow.md  
- **Ask for help** – We are learning together.

---

## Additional Resources

- **Team Getting Started Guide:** https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/blob/main/docs/getting-started.md
- **Team PR Workflow:** https://github.com/MichaelMcKibbin/ATU-SoftDev-Grp5Project/blob/main/docs/pull-request-workflow.md
- **Official Git Documentation:** https://git-scm.com/doc
- **GitHub Guides:** https://guides.github.com
- **Java Documentation:** https://docs.oracle.com/en/java/javase/21/
- **Maven Documentation:** https://maven.apache.org/guides/
- **Homebrew:** https://brew.sh

---

**Last Updated:** November 2025  
**For:** ATU-SoftDev-Grp5Project Team  

**Questions?** Ask your team lead, check the `/docs` folder in the repository, or create an issue on GitHub.