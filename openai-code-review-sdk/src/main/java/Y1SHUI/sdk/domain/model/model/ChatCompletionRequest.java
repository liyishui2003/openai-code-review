package Y1SHUI.sdk.domain.model.model;

import java.util.List;

/*
* 请求体封装类
* 将业务参数转换为符合 API 要求的 JSON 结构
* 方便直接拼装成request对象
* */
public class ChatCompletionRequest {

    private String model = Model.GLM_4_FLASH.getCode();
    private List<Prompt> messages;

    public static class Prompt {
        private String role;
        private String content;

        public Prompt() {
        }

        public Prompt(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<Prompt> getMessages() {
        return messages;
    }

    public void setMessages(List<Prompt> messages) {
        this.messages = messages;
    }
}
