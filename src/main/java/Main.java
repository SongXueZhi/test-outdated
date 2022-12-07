import Helper.RepoHelper;
import Utils.RepoUtils;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.HunkHeader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    public static final String GIT_PATH = "." + File.separator + "univocity-parsers" + File.separator + ".git";
    public static void main(String[] args) throws IOException, GitAPIException {

        try (Repository repository = RepoHelper.openJGitRepository(GIT_PATH)) {
            RepoUtils repoUtil = new RepoUtils(repository);
            System.out.println(repoUtil.showOnlyTestChangedCommits().size());
            repoUtil.close();
        }

    }
}
