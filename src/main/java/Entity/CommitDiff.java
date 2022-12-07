package Entity;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

/*
* This class is used for storing a commit with its relevant diffEntry
*/
public class CommitDiff {
    private final RevCommit currentCommit;
    private final List<DiffEntry> diffEntriesFromParent;

    public CommitDiff(RevCommit currentCommit, List<DiffEntry> diffEntriesFromParent) {
        this.currentCommit = currentCommit;
        this.diffEntriesFromParent = diffEntriesFromParent;
    }

    public List<DiffEntry> getDiffEntriesFromParent() {
        return diffEntriesFromParent;
    }

    public RevCommit getCurrentCommit() {
        return currentCommit;
    }
}
