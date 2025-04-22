package Y1SHUI.sdk;

import Y1SHUI.sdk.domain.model.model.ChatCompletionRequest;
import Y1SHUI.sdk.domain.model.model.ChatCompletionSyncResponse;
import Y1SHUI.sdk.domain.model.model.Message;
import Y1SHUI.sdk.domain.model.model.Model;
import Y1SHUI.sdk.type.utils.BearerTokenUtils;
import Y1SHUI.sdk.type.utils.WXAccessTokenUtils;
import com.alibaba.fastjson2.JSON;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class OpenAiCodeReview {

    public static void main(String[] args) throws  Exception{
        System.out.println("openai 代码评审，测试执行");

        String token = System.getenv("CUSTOM_TOKEN");
        if (null == token || token.isEmpty()) {
            throw new RuntimeException("token is null");
        }

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

        //3.写入评审日志
        String logUrl = writeLog(token,log);
        System.out.println("writeLog: "+ logUrl);

        //4.push
        pushUrl(logUrl);
        System.out.println("pushLog: "+logUrl);
    }

    private static void pushUrl(String logUrl){
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println(accessToken);

        Message message = new Message();
        message.put("project","small-pay");
        System.out.println("Message Url：" + logUrl);
        message.put("review",logUrl);
        message.setUrl(logUrl);
        message.setTemplate_id("PM9esxw384uCGn27JZrDJZcv-J3aKYeOrbUuDJEfUk8");

        String url = String.format("https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=%s", accessToken);
        sendPostRequest(url, JSON.toJSONString(message));
    }

    private static void sendPostRequest(String urlString, String jsonBody) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (Scanner scanner = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name())) {
                String response = scanner.useDelimiter("\\A").next();
                System.out.println(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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


        ChatCompletionRequest chatCompletionRequest = new ChatCompletionRequest();
        chatCompletionRequest.setModel(Model.GLM_4_FLASH.getCode());
        chatCompletionRequest.setMessages(new ArrayList<ChatCompletionRequest.Prompt>() {
            private static final long serialVersionUID = -7988151926241837899L;
            {
                add(new ChatCompletionRequest.Prompt("user","你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码如下:"));
                add(new ChatCompletionRequest.Prompt("user",diffCode));
            }
        });
        /*
         * 向 HTTP 连接（connection）的输出流（OutputStream）写入 JSON 数据，通常用于 POST/PUT 请求的请求体发送。
         * 转化成byte是因为网络是面向字节流的
         * */
        try(OutputStream os = httpURLConnection.getOutputStream()){
            byte[] input = JSON.toJSONString(chatCompletionRequest).getBytes(StandardCharsets.UTF_8);
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

    private static String writeLog(String token,String log) throws Exception{
        //克隆远程仓库到本地
        Git git = Git.cloneRepository()
                .setURI("https://github.com/liyishui2003/openai-code-review-log.git")
                .setDirectory(new File("repo"))
                .setCredentialsProvider(new UsernamePasswordCredentialsProvider("token",""))
                .call();

        //在本地新建文件夹
        String dateFolderName = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        File dateFolder = new  File("repo/" + dateFolderName);
        if (!dateFolder.exists()) {
            dateFolder.mkdirs();
            System.out.println("[DEBUG] 创建文件夹: " + dateFolder.getAbsolutePath()); // 打印路径
        } else {
            System.out.println("[DEBUG] 文件夹已存在: " + dateFolder.getAbsolutePath());
        }

        String fileName = generateRandomString(12) + ".md";
        File newFile = new File(dateFolder,fileName);
        try (FileWriter writer = new FileWriter(newFile)) {
            writer.write(log);
            System.out.println("[DEBUG] 文件已写入: " + newFile.getAbsolutePath()); // 打印文件路径
        }


        git.add().addFilepattern(dateFolderName + "/" + fileName).call();
        git.commit().setMessage("Add new file via GitHub Actions").call();
        git.push().setCredentialsProvider(new UsernamePasswordCredentialsProvider(token, "")).call();

        // 打印部分 token（前4位 + 后4位）用于调试
        System.out.println("[DEBUG] Token (masked): "
                + (token != null ? token.substring(0, 4) + "****" + token.substring(token.length() - 4) : "null"));

        // 检查远程分支是否有最新提交（绕过 PushResult）
        String branchName = git.getRepository().getBranch();
        String latestCommit = git.getRepository().findRef("HEAD").getObjectId().getName();

        ProcessBuilder checkRemote = new ProcessBuilder("git", "ls-remote", "origin", branchName);
        Process process = checkRemote.start();
        String output = new BufferedReader(new InputStreamReader(process.getInputStream()))
                .lines().collect(Collectors.joining("\n"));

        //论 code_token 和 github_token 的区别
        if (output.contains(latestCommit)) {
            System.out.println("✅ 推送成功，远程分支已更新！");
        } else {
            System.out.println("❌ 推送失败，远程分支无变更！");
        }
        return "https://github.com/liyishui2003/openai-code-review-log/blob/main/" + dateFolderName +"/" + fileName;

    }

    private static String generateRandomString(int length){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for(int i = 0;i < length; i++){
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
