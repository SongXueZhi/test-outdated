import Helper.RepoHelper;
import Utils.RepoUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, GitAPIException {
        try (Repository repository = RepoHelper.openJGitRepository("./univocity-parsers/.git")) {
            RepoUtils repoUtil = new RepoUtils(repository);
            repoUtil.showOnlyTestChangedCommits();
//            repoUtil.showAllCommits();
            repoUtil.close();
        }
    }
}
