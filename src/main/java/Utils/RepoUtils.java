package Utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.util.List;

public class RepoUtils {
    private final Repository repository;
    private final Git git;

    public RepoUtils(Repository repository) throws IOException {
        this.repository = repository;
        git = new Git(repository);
//        System.out.println("Having repo: " + repository.getDirectory());
//        Ref head = repository.exactRef("refs/heads/master");
//        System.out.println("Ref of refs/heads/master: " + head);
    }

    public void close() {
        git.close();
    }

    public void listTags() throws GitAPIException, IOException {
        List<Ref> tagRefList = git.tagList().call();
        for (var ref : tagRefList) {
            System.out.println("Tag: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName() +
                    (ref.getPeeledObjectId() == null ? "" : ", peeled: " + ref.getPeeledObjectId()));
        }

    }

    //list all branches of this repository
    public void listBranches() throws GitAPIException {
        List<Ref> brhRefList = git.branchList().call();
        for (var ref : brhRefList) {
            System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
        System.out.println("Now including remote branches:");
        brhRefList = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
        for (var ref : brhRefList) {
            System.out.println("Branch: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName());
        }
    }

    public void showAllCommits() throws GitAPIException, IOException {
        Iterable<RevCommit> commits = git.log().all().call();
        int cnt = 0;
        for (RevCommit commit : commits) {
            System.out.println("commit: " + commit.getShortMessage());
            cnt++;
        }
        System.out.println(cnt + " commits in total");
    }

    public void showOnlyTestChangedCommits() throws IOException, GitAPIException {
        Iterable<RevCommit> commits = git.log().all().call();
        int cnt = 0;
        RevCommit lastCommit = null;
        CanonicalTreeParser lastParser = new CanonicalTreeParser();
        CanonicalTreeParser newParser = new CanonicalTreeParser();
        int count = 0;
        boolean onlyTestChanged = true;
        try (ObjectReader reader = repository.newObjectReader()) {
            for (var commit : commits) {
                List<DiffEntry> diffs;
                newParser.reset(reader, commit.getTree());
                if (lastCommit == null) {
                    diffs = git.diff().setNewTree(newParser).setOldTree(null).call();
                } else {
                    lastParser.reset(reader, lastCommit.getTree());
                    diffs = git.diff().setNewTree(newParser).setOldTree(lastParser).call();
                }

                for (var entry : diffs) {
//                    System.out.println("entry: " + entry);
                    if (!entry.toString().contains("src/test/")) {
                        onlyTestChanged = false;
                    }
                }
                System.out.println("--------------------");
                if (onlyTestChanged && diffs.size() > 0) {
                    for (var entry : diffs) {
                        System.out.println(/*"old: " + entry.getOldPath() + ", new: " + entry.getNewPath() + */"Entry: " + entry);
                    }
                    count++;
                }

                cnt++;
                lastCommit = commit;
            }
            System.out.println(cnt + " commits in total");
            System.out.println(count + " test commits");
        }
    }

    @SuppressWarnings("unused")
    //just used for testing showing changes
    public void showChangedFileBetweenCommits() throws IOException, GitAPIException {
        ObjectId oldHead = repository.resolve("HEAD^^^^{tree}");
        ObjectId head = repository.resolve("HEAD^{tree}");
        System.out.println("Printing diff between tree: " + oldHead + " and " + head);
        try (ObjectReader reader = repository.newObjectReader()) {
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
            for (var entry : diffs) {
                System.out.println("old: " + entry.getOldPath() + ", new: " + entry.getNewPath() + ", entry: " + entry);
            }
        }
    }

    public void showStatus() throws GitAPIException {
        Status status = git.status().call();
        System.out.println("Added: " + status.getAdded());
        System.out.println("Changed: " + status.getChanged());
        System.out.println("Conflicting: " + status.getConflicting());
        System.out.println("ConflictingStageState: " + status.getConflictingStageState());
        System.out.println("IgnoredNotInIndex: " + status.getIgnoredNotInIndex());
        System.out.println("Missing: " + status.getMissing());
        System.out.println("Modified: " + status.getModified());
        System.out.println("Removed: " + status.getRemoved());
        System.out.println("Untracked: " + status.getUntracked());
        System.out.println("UntrackedFolders: " + status.getUntrackedFolders());
    }
}
