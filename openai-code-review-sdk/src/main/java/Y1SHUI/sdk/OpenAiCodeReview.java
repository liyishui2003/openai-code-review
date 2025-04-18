package Y1SHUI.sdk;

import Y1SHUI.sdk.domain.model.model.ChatCompletionSyncResponse;
import Y1SHUI.sdk.type.utils.BearerTokenUtils;
import com.alibaba.fastjson2.JSON;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

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

        //2.代码评审
        String log = codeReview(diffCode.toString());
        System.out.println("code review: "+ log);
    }

    private static String codeReview(String diffCode) throws IOException {

        String apikey = "8512a089bd914ba48808631878703ceb.t71ryeCCI2jdUWeg";
        String token = BearerTokenUtils.getToken(apikey);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Authorization","Bearer "+token);
        httpURLConnection.setRequestProperty("Content-Type","application/json");
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpURLConnection.setDoOutput(true);

        String jsonInpuString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + diffCode + "\""
                + "    }"
                + "]"
                + "}";

        /*
         * 向 HTTP 连接（connection）的输出流（OutputStream）写入 JSON 数据，通常用于 POST/PUT 请求的请求体发送。
         * 转化成byte是因为网络是面向字节流的
         * */
        try(OutputStream os = httpURLConnection.getOutputStream()){
            byte[] input = jsonInpuString.getBytes(StandardCharsets.UTF_8);
            os.write(input);
        }

        int responseCode = httpURLConnection.getResponseCode();
        System.out.println(responseCode);

        /*
          获取原始网络数据	InputStream	读取字节流
          字节 → 字符转换	InputStreamReader 将字节流按一定的编码规则换成字符
          高效读取文本	BufferedReader	缓冲数据，支持逐行读取
        */
        BufferedReader in = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
        String inputLine;

        StringBuilder content = new StringBuilder();
        while( (inputLine = in.readLine()) != null ){
            content.append(inputLine);
        }

        in.close();
        httpURLConnection.disconnect();
        System.out.println(content);

        ChatCompletionSyncResponse response = JSON.parseObject(content.toString(), ChatCompletionSyncResponse.class);
        System.out.println(response.getChoices().get(0).getMessage().getContent());
        return response.getChoices().get(0).getMessage().getContent();
    }
}
