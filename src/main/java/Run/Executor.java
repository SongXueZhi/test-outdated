package Run;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Executor {
    ProcessBuilder processBuilder;

    public Executor() {
        processBuilder = new ProcessBuilder();
    }

    public String exec(String[] cmdArr) throws IOException {
        int len = cmdArr.length;
        if (len == 1) return exec(cmdArr[0]);
        if (len == 0) return "";

        StringBuilder builder = new StringBuilder();
        builder.append(cmdArr[0]);
        for (int i = 1; i < len; i++) {
            builder.append("&&").append(cmdArr[i]);
        }
        return exec(builder.toString());
    }

    public String exec(String cmd) throws IOException {
        StringBuilder builder = new StringBuilder();
        processBuilder.redirectErrorStream(true);
        //todo may support multi-system
        processBuilder.command("cmd.exe", "/c", cmd);
        Process process = processBuilder.start();
        //todo may modify charset
        try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        finally {
            process.destroy();
        }
        return builder.toString();
    }
}
