--------------------------------------------------------------------------------
:page/title Git Subtree for Multiple Directories
:tech-blog/published #time/ldt "2018-05-03T12:00"
:tech-blog/tags [:tag/git]
:tech-blog/description

How to split Git repos.
--------------------------------------------------------------------------------
:section/kind :centered
:section/theme :dark1
:section/title Git Subtree for Multiple Directories
:section/body

Your git repository has grown over time, and it's time for parts of it to become
its own separate repo. Depending on how much you want to extract, you have two
options: [`git filter-branch`](https://git-scm.com/docs/git-filter-branch) or
`git subtree split`.

--------------------------------------------------------------------------------
:section/title Extracting a Single Directory
:section/body

If you want to extract a single directory as a new Git repository, you're in
luck: `git subtree split` was made specifically for you. You achieve this in two
steps: first create a branch in the parent repository that only contains commits
pertaining to the directory of choice, then push/pull those into a new, blank
repository:

```sh
cd big-repo
git subtree split --prefix dir-to-extract -b selective-history
```

You now have a new branch where `dir-to-extract` is the root directory, and the
only commits are those from the original repository that pertains to files in
this directory.

**NB!** The resulting branch will only contain commits _literally_ committing to
`dir-to-extract/*`. If you've moved files into this directory, the history prior
to the move will be lost, and even renames will not be carried over by
`git subtree`.

To make a new repository out of this, do the following:

```sh
mkdir slim-repo
cd slim-repo
git init
git pull ../big-repo selective-history:master
```

Voila! If you want to skip the local copy and just push the repo directly to
some upstream, like Github, the whole operation can be performed like so:

```sh
cd big-repo
git subtree split --prefix dir-to-extract -b selective-history
git push git@github.com:cjohansen/my-new-slim-repo.git selective-history:master
```

## Extracting Multiple Directories

What if you wanted to extract not a single directory, but multiple ones? As far
as I can tell, `git subtree` cannot help you. But
[`git filter-branch`](https://git-scm.com/docs/git-filter-branch) can.
`filter-branch` can remove and/or rewrite commits in your repository on various
criteria.

To keep only history related to two or more directories, we can use
`--tree-filter` in combination with `find`. This way, we will effectively walk
through each and every commit in the repository, removing files that are not the
ones we want, and recreate the commit. This will likely reduce in quite a few
empty commits, and those can be removed with `--prune-empty`:

```sh
git clone git@github.com:cjohansen/some-fat-repo.git
cd some-fat-repo
git filter-branch \
    --tree-filter 'find . ! \( -path "./terraform*" -o \
                               -path "./packer*" -o \
                               -path "./.git*" -o \
                               -path "." \) \
                        -exec rm -fr {} +' \
    --prune-empty \
    HEAD
```

This might take some time. When it's done, you'll have a slimmed down repo with
only the files in the `terraform` and `packer` directories, as well as only
commits pertaining to those directories.
