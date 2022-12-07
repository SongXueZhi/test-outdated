package Utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RepoUtils {
    private final Repository repository;
    private final Git git;
    private final CanonicalTreeParser parentTreeParser;
    private final CanonicalTreeParser childTreeParser;

    public RepoUtils(Repository repository) throws IOException {
        this.repository = repository;
        git = new Git(repository);
        parentTreeParser = new CanonicalTreeParser();
        childTreeParser = new CanonicalTreeParser();
    }

    public void close() {
        git.close();
    }

    @SuppressWarnings("unused")
    public void listTags() throws GitAPIException {
        List<Ref> tagRefList = git.tagList().call();
        for (var ref : tagRefList) {
            System.out.println("Tag: " + ref + " " + ref.getName() + " " + ref.getObjectId().getName() +
                    (ref.getPeeledObjectId() == null ? "" : ", peeled: " + ref.getPeeledObjectId()));
        }
    }

    //list all branches of this repository
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void showAllCommits() throws GitAPIException, IOException {
        Iterable<RevCommit> commits = git.log().all().call();
        int cnt = 0;
        for (RevCommit commit : commits) {
            System.out.println("commit: " + commit.getShortMessage());
            cnt++;
        }
        System.out.println(cnt + " commits in total");
    }

    //todo give DiffEntry out
    public List<RevCommit> showOnlyTestChangedCommits() throws IOException, GitAPIException {
        //get all commits
        Iterable<RevCommit> commits = git.log().all().call();
        int cnt = 0;
        int onlyTestChangedCnt = 0;
        List<RevCommit> commitList = new ArrayList<>();
        for (var commit : commits) {//reverse order
            if (commit.getParentCount() <= 0) {
                continue;
            }
            //todo may deal with merge commit
            RevCommit parentCommit = commit.getParent(0);
            List<DiffEntry> diffs = getDiffBetweenCommits(parentCommit, commit);

            System.out.println("--------------------");
            if (onlyTestChanged(diffs)) {
                commitList.add(commit);
                for (var entry : diffs) {
                    System.out.println(/*"old: " + entry.getOldPath() + ", new: " + entry.getNewPath() + */"Entry: " + entry);
                }
                onlyTestChangedCnt++;
            }
            cnt++;
        }
        System.out.println(cnt + " commits in total");
        System.out.println(onlyTestChangedCnt + " test commits");
        return commitList;
    }

    public List<DiffEntry> getDiffBetweenCommits(RevCommit parentCommit, RevCommit childCommit) throws IOException, GitAPIException {
        ObjectId parentId = parentCommit.getTree().getId();
        ObjectId childId = childCommit.getTree().getId();
        List<DiffEntry> diffs;
        try (ObjectReader reader = repository.newObjectReader()) {
            parentTreeParser.reset(reader, parentId);
            childTreeParser.reset(reader, childId);
            diffs = git.diff().setNewTree(childTreeParser).setOldTree(parentTreeParser).call();
        }
        return diffs;
    }

    //todo modify
    private static boolean onlyTestChanged(List<DiffEntry> diffs) {
        boolean onlyTestChanged = true;
        for (var entry : diffs) {
            if (!entry.toString().contains("src/test")) {
                onlyTestChanged = false;
            }
        }
        return onlyTestChanged && diffs.size() > 0;
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

    @SuppressWarnings("unused")
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

    private List<Edit> getEdit(DiffEntry entry) throws IOException {
        List<Edit> editList = new LinkedList<>();
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repository);
            FileHeader fileHeader = diffFormatter.toFileHeader(entry);
            List <? extends HunkHeader> hunkHeaders = fileHeader.getHunks();
            hunkHeaders.forEach(hunkHeader -> editList.addAll(hunkHeader.toEditList()));
        }
        return editList;
    }
}
