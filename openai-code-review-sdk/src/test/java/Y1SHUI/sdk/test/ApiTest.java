package Y1SHUI.sdk.test;

import Y1SHUI.sdk.domain.model.model.ChatCompletionRequest;
import Y1SHUI.sdk.domain.model.model.ChatCompletionSyncResponse;
import Y1SHUI.sdk.type.utils.BearerTokenUtils;
import Y1SHUI.sdk.type.utils.WXAccessTokenUtils;
import com.alibaba.fastjson2.JSON;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ApiTest {
    public static void main(String[] args) {
        String apikey = "8512a089bd914ba48808631878703ceb.t71ryeCCI2jdUWeg";
        String token = BearerTokenUtils.getToken(apikey);
        System.out.println(token);
    }

    @Test
    public void test_http() throws IOException {
        String apikey = "8512a089bd914ba48808631878703ceb.t71ryeCCI2jdUWeg";
        String token = BearerTokenUtils.getToken(apikey);

        URL url = new URL("https://open.bigmodel.cn/api/paas/v4/chat/completions");
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Authorization","Bearer "+token);
        httpURLConnection.setRequestProperty("Content-Type","application/json");
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
        httpURLConnection.setDoOutput(true);

        String code = "1+1";

        String jsonInpuString = "{"
                + "\"model\":\"glm-4-flash\","
                + "\"messages\": ["
                + "    {"
                + "        \"role\": \"user\","
                + "        \"content\": \"你是一个高级编程架构师，精通各类场景方案、架构设计和编程语言请，请您根据git diff记录，对代码做出评审。代码为: " + code + "\""
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
    }

    @Test
    public void  test_wx(){
        String accessToken = WXAccessTokenUtils.getAccessToken();
        System.out.println(accessToken);

        Message message = new Message();
        message.put("project","small-pay");
        message.put("review","feat:新加功能");

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

    public static  class Message{

        private  String touser = "o515c6uit3jIVXLB_c0KeKj1UQBk";
        private  String template_id = "PM9esxw384uCGn27JZrDJZcv-J3aKYeOrbUuDJEfUk8";
        private String url = "https://github.com/liyishui2003/openai-code-review-log/blob/main/2025-04-21/iTy46nCLbVyJ.md";
        private Map<String, Map<String,String>> data = new HashMap<>();

        public void put(String key,String value){
            data.put(key,new HashMap<String,String>(){
                {put("value",value);}
            });
        }

        public String getTouser() {
            return touser;
        }

        public void setTouser(String touser) {
            this.touser = touser;
        }

        public String getTemplate_id() {
            return template_id;
        }

        public void setTemplate_id(String template_id) {
            this.template_id = template_id;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Map<String, Map<String, String>> getData() {
            return data;
        }

        public void setData(Map<String, Map<String, String>> data) {
            this.data = data;
        }

    }
}
