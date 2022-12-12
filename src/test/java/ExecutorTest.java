import Run.Executor;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class ExecutorTest {
    private final Executor executor;
    public ExecutorTest() {
        executor = new Executor();
    }

    @Test
    public void initialTest() throws IOException {
        System.out.println("Initial Test for Executor!");
        String result = executor.exec("mvn -v");
        Assert.assertTrue(result.length() > 0);
        Assert.assertTrue(result.contains("Maven"));
    }

    @Test
    public void showErrMsgTest() throws IOException {
        System.out.println("Test for showing error msg!");
        String cmd = "abcde";
        String result = executor.exec(cmd);//no such cmd
        System.out.println(result);
        Assert.assertTrue(result.contains("'" + cmd + "'"));
    }

    @Test
    public void showProjectCommitsTest() throws IOException {
        System.out.println("Test for showing project commits!");
        String[] cmdArr = {"cd ./univocity-parsers", "git log"};
        String result = executor.exec(cmdArr);
        System.out.println(result);
        Assert.assertTrue(result.contains("commit "));
        int num = result.length();
        System.out.println("result has " + num + " characters!");

        System.out.println("result has " + ((num - result.replaceAll("commit ", "").length()) / 7) + "'commit '!");
    }
}
