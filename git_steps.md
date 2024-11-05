# Steps for contributing
### 1. Download the repository
```bash
git clone git@github.com:noahwagner04/Space_Battles.git
cd Space_Battles
```
### 2. Make a feature branch (do step 4 first)
```bash
git branch feature-branch
git switch feature-branch
```
### 3. Add and commit your changes
```bash
git add .
git commit -m "Describe your change"
```
### 4. Update the master branch
```bash
git switch master
git pull
```
### 5. Rebase feature branch
```bash
git switch feature-branch
git rebase master
```
### 6. Resolve any conflicts, then continue rebasing
```bash
git add .
git rebase --continue
```
### 7. Add commits to fix things if necessary
```bash
git add .
git commit -m "Describe your change"
```
### 8. Squash all commits on the feature branch into one
```bash
# In the editor that opens, keep pick for the first commit 
# and change the other lines from pick to squash (or s).
git rebase -i master
```
### 9. Fast-forward merge the squashed branch into master
```bash
git switch master
git merge feature-branch
```
### 10. Push changes to remote
```bash
git push
```
