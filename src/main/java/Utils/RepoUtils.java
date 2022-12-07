package Utils;

import Entity.CommitDiff;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.ArrayList;
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

    public List<CommitDiff> getOnlyTestChangedCommits() throws IOException, GitAPIException {
        //get all commits
        Iterable<RevCommit> commits = git.log().all().call();
        int cnt = 0;
        int onlyTestChangedCnt = 0;
        List<CommitDiff> commitDiffList = new ArrayList<>();
        for (var commit : commits) {//reverse order
            if (commit.getParentCount() <= 0) {//no father(the closest commit)
                continue;
            }
            //todo may deal with merge commit
            RevCommit parentCommit = commit.getParent(0);
            List<DiffEntry> diffs = getDiffBetweenCommits(parentCommit, commit);

            System.out.println("--------------------");
            if (onlyTestChanged(diffs)) {
                commitDiffList.add(new CommitDiff(commit, diffs));
                for (var entry : diffs) {
                    System.out.println(/*"old: " + entry.getOldPath() + ", new: " + entry.getNewPath() + */"Entry: " + entry);
                }
                onlyTestChangedCnt++;
            }
            cnt++;
        }
        System.out.println(cnt + " commits in total");
        System.out.println(onlyTestChangedCnt + " test commits");
        return commitDiffList;
    }

    private List<DiffEntry> getDiffBetweenCommits(RevCommit parentCommit, RevCommit childCommit) throws IOException, GitAPIException {
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

    public void showExisting(List<CommitDiff> commitDiffList) throws IOException {
        for (var commitDiff : commitDiffList) {
            for (var entry : commitDiff.getDiffEntriesFromParent()) {
                if (entry.getChangeType() != DiffEntry.ChangeType.MODIFY) {//filter out adding/deleting/copying/renaming a file
                    continue;
                }
                if (!entry.toString().contains(".java")) {//filter out non-java files
                    continue;
                }
                List<Edit> edits = getEdits(entry);
                //todo: parse edits
            }
            //todo: deal with commits whose all entries are filtered out
        }
    }

    //used for filter commits
    //all changed files(java or normal files) in each entry are in the test directory will pass this function
    private static boolean onlyTestChanged(List<DiffEntry> diffs) {
        boolean onlyTestChanged = true;
        for (var entry : diffs) {
            if (!entry.toString().contains("src/test")) {
                onlyTestChanged = false;
                break;
            }
        }
        return onlyTestChanged && diffs.size() > 0;
    }

    private List<Edit> getEdits(DiffEntry entry) throws IOException {
        List<Edit> editList = new ArrayList<>();
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(repository);
            FileHeader fileHeader = diffFormatter.toFileHeader(entry);
            List <? extends HunkHeader> hunkHeaders = fileHeader.getHunks();
            hunkHeaders.forEach(hunkHeader -> editList.addAll(hunkHeader.toEditList()));
        }
        return editList;
    }
}
