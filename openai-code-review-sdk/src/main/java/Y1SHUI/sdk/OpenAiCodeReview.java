package Y1SHUI.sdk;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class OpenAiCodeReview {

    public static void main(String[] args) throws  Exception{
        System.out.println("测试执行");

        /*
         * 这里之所以还需要运行检出命令是因为github action做的是把代码拷贝到工作区
         * 并不能直接得到代码的前后差异
         * */
        // 1. 代码检出
        ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "HEAD~1", "HEAD");
        processBuilder.directory(new File("."));

        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        StringBuilder diffCode = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            diffCode.append(line);
        }

        //这里waitFor表示会等待外部命令(也就是git diff HEAD~1 HEAD)调用完成
        int exitCode = process.waitFor();
        System.out.println("Exited with code:" + exitCode);

        System.out.println("评审代码：" + diffCode.toString());
    }
}
