# Git Etiquette Guide

To ensure smooth collaboration, we've outlined the following Git etiquette guide. This guide will help in writing concise commit messages, managing git branches, pull request descriptions, and other best practices.

## 1. Commit Messages

Your commit message should provide clear context about the change. This allows others (and future you) to understand why a particular change was made.

Here are the guidelines to write concise and meaningful commit messages:

- **Use the Imperative Mood**: Commit messages should be written in the present tense and imperative mood ("Add feature" not "Added feature" or "Adds feature").
- **First Line Should Be Summary**: The first line of your commit message should be a brief summary of the changes, kept under 50 characters whenever possible.
- **Body**: Follow this summary line with a blank line, then a more detailed explanation is always helpful but not necessary. This is often useful when the commit has significant importance or complexity.
- **Consider Including the 'Why'**: If the commit resolves an issue or bug, mention the issue number and provide a brief explanation of how the changes fix the issue.

Here's a good commit message example:

```
Fix off-by-one error in loop (issue #123)

This commit fixes an off-by-one error in the list iteration function. 
This was causing an index out of bounds exception when processing the last item in the list. 
Tests have been updated to catch this in the future.
```

## 2. Branch Management

Git branches should be concise and focused on a single feature, fix, or improvement. Here are a few practices:

- **Branch Name**: The branch name should be descriptive of the change and follow a specific format such as `type/description`. Example: `feature/login-system`.
- **Please don't accept branch recommendation by Github UI**: The branch name recommendation by Github UI will in most cases be a very long branch name concatenated with the issue number, which often results in branch names like this one : 
`feature/Merge_yada_and_yada_yada_into_one_yada_yada_status-lol_#199021` 
- **Keep Branches Short-lived**: The longer a branch lives separate from the `main` or `master` branch, the higher the possibility of merge conflicts. Aim to merge branches as soon as the work is complete and has been tested.
- **Rebase Frequently**: If your branch lives longer, rebase it frequently on top of the main branch.

## 3. Pull Requests

Pull requests (PRs) are how your changes get incorporated into the project. Here are a few key points:

- **Link PR to an Issue**: Your PR should link to the issue it resolves. This helps track the resolution of bugs or addition of features.
- **PR Description**: The PR description should outline what changes have been made and why. This should be a summary of your commits.
- **Small and Focused PRs**: Aim to keep PRs small and focused. It's easier to understand and review a PR with a handful of related changes than one with multiple unrelated changes.

Example PR description:

```
## Description
This PR implements a login system (issue #456). It adds a login and logout button, as well as an authentication system. This will allow users to create an account and sign in.

## Changes
- Add login button component
- Add logout button component
- Add authentication API integration

## Issue Related
This PR closes #456
```

## 4. Other Best Practices

- **Stay Updated**: Keep your local copy up to date with the upstream regularly.
- **Test Before Pushing**: Always test your code and make sure all tests pass before you push.

Following these guidelines ensures a smoother collaboration and maintains a clean, manageable project history. Thanks for your contributions!

