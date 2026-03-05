---
description: "Check Skinner status: active worktrees, Ralph branches, recent logs, and pending merges. Usage: /skinner-status [workspace]"
---

## User Input

```text
$ARGUMENTS
```

## Goal

Show the current status of Skinner enforcement: active worktrees, Ralph branches, session logs, and pending merges.

## Instructions

1. **Show active worktrees**:
   ```bash
   cd /c/Users/rafael.giovannini/Documents/GitHub/AI-Brain && git worktree list
   ```

2. **Show Ralph branches** (unmerged):
   ```bash
   git branch --list "ralph/*" --no-merged
   ```

3. **Show recent session logs** (if workspace provided, filter by it):
   - If workspace argument given:
     ```bash
     ls -lt .skinner/logs/<workspace>/ | head -5
     ```
   - Otherwise:
     ```bash
     find .skinner/logs/ -name "session-*.log" -type f | sort -r | head -10
     ```

4. **For each active ralph worktree**, show:
   - Branch name
   - Commit count ahead of current branch: `git log HEAD..<branch> --oneline`
   - Whether it has uncommitted changes

5. **Show last log tail** (most recent session):
   - Read the last 30 lines of the most recent log file

6. **Suggest actions**:
   - If unmerged ralph branches exist: "Run `git merge <branch>` to incorporate Ralph's work"
   - If worktrees exist with no new commits: "Run `git worktree remove <path> --force` to clean up"
   - If worktrees have uncommitted changes: "Review and commit or discard changes"

## Examples

```
/skinner-status
/skinner-status ghostfit
```
