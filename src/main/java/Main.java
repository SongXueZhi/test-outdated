import Entity.CommitDiff;
import Helper.RepoHelper;
import Utils.RepoUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static final String GIT_PATH = "." + File.separator + "univocity-parsers" + File.separator + ".git";

    public static void main(String[] args) throws IOException, GitAPIException {
        try (Repository repository = RepoHelper.openJGitRepository(GIT_PATH)) {
            RepoUtils repoUtil = new RepoUtils(repository);
            List<CommitDiff> commitDiffList = repoUtil.getOnlyTestChangedCommits();
            repoUtil.showExisting(commitDiffList);
            repoUtil.close();
        }

    }
}
