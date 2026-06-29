# Forking Eclipse Papyrus-Web on GitHub — Setup & Maintenance

This repository (`papyrus-web-plus-plus`) is a downstream fork of the Eclipse
**Papyrus-Web** project, whose canonical home is GitLab:

- **Upstream (read-only source):** <https://gitlab.eclipse.org/eclipse/papyrus/org.eclipse.papyrus-web> — default branch `master`
- **This fork (where we work):** GitHub — default branch `main`

Because the upstream lives on GitLab, GitHub's *Fork* button can't reach it. Git
is distributed, though — a "remote" is just a URL — so we **fetch from GitLab and
push to GitHub** via two remotes, using a *pristine-mirror* layout that keeps
upstream code and our own changes from getting tangled.

## Branch layout

| Branch            | Tracks              | Purpose                                                  |
| ----------------- | ------------------- | -------------------------------------------------------- |
| `main`            | `origin` (GitHub)   | Our changes on top of the full upstream history. We push here. |
| `upstream-master` | `upstream` (GitLab) | Exact mirror of upstream `master`. **Never edited.**     |

`git diff upstream-master..main` therefore always shows *exactly* our delta from upstream.

---

## Part A — What was done to create this fork

Run once, from a clone of the (near-empty) GitHub repo:

```bash
# 1. Add the GitLab upstream as a read-only remote + safety + conflict memory
git remote add upstream https://gitlab.eclipse.org/eclipse/papyrus/org.eclipse.papyrus-web.git
git remote set-url --push upstream DISABLE     # block accidental pushes to Eclipse
git config rerere.enabled true                 # auto-replay conflict resolutions

# 2. Download upstream's full history (nothing changes locally yet)
git fetch upstream --tags

# 3. Create the pristine mirror branch
git branch upstream-master upstream/master

# 4. Adopt upstream's real history as the base of main
git checkout main
git reset --hard upstream/master

# 5. Publish to the GitHub fork
git push --force-with-lease origin main        # replaces the throwaway initial commit
git push origin upstream-master                # publish mirror (no -u: keep it tracking GitLab)
```

**Result:** `main` carries the complete upstream history (~2,221 files in the
working tree, ~466 commits), the empty repo's initial commit is gone, and each
branch tracks the right remote.

---

## Part B — Reproduce from scratch (any GitLab → GitHub fork)

Starting from **an empty repo**:

1. **Create an empty repo** on your host (GitHub here). Initialising it with a
   README is fine — step 5 replaces it.
2. **Clone it** and enter it:
   ```bash
   git clone <YOUR_FORK_URL>
   cd <repo>
   ```
3. **Find the upstream's default branch**, then wire it up (it may be `master`
   *or* `main` — don't assume):
   ```bash
   git ls-remote --symref <UPSTREAM_URL> HEAD      # prints "ref: refs/heads/<branch>"
   git remote add upstream <UPSTREAM_URL>
   git remote set-url --push upstream DISABLE
   git config rerere.enabled true
   git fetch upstream --tags
   ```
4. **Create the mirror + adopt history** (swap `master` for the branch from step 3
   if different):
   ```bash
   git branch upstream-master upstream/master
   git checkout main
   git reset --hard upstream/master
   ```
5. **Publish:**
   ```bash
   git push --force-with-lease origin main
   git push origin upstream-master
   ```

> ⚠️ The force-push in step 5 **rewrites the fork's history**. It is safe here
> only because the repo is brand-new and nobody has cloned it. Never force-push a
> branch other people depend on.

---

## Keeping the fork up to date

Whenever you want upstream's new features, run this from `main`:

```bash
git fetch upstream                              # 1. latest from GitLab
git branch -f upstream-master upstream/master   # 2. advance the pristine mirror
git merge upstream-master                       # 3. integrate (the only place conflicts appear)
git push origin main                            # 4. publish your fork
git push origin upstream-master                 # 5. (optional) mirror the pristine branch too
```

Optional one-command alias:

```bash
git config alias.sync-upstream '!git fetch upstream && git branch -f upstream-master upstream/master && git merge upstream-master'
# usage:  git sync-upstream   # then push when the merge is clean
```

---

## The one rule that makes this work

**Don't flatten history.** Merges stay cheap only because `main` shares a real
common ancestor with upstream. If you ever bootstrap a fork by *copying files*
into a fresh `git init` / "Initial commit", you sever that ancestor and every
future sync degrades into resolving the whole codebase by hand. Always import
upstream by **fetching its history** (as above), never by copying a snapshot.

---

## Handy commands

```bash
git diff upstream-master..main      # exactly your customizations
git log  upstream-master..main      # your commits only
git checkout 2026.05.0              # build against a tagged upstream release
git push origin --tags              # mirror upstream's release tags to your fork
```
