package com.demo.llmstreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * LLM流式响应应用主启动类
 */
@SpringBootApplication
public class LlmStreamingApplication {

    public static void main(String[] args) {
        SpringApplication.run(LlmStreamingApplication.class, args);
        System.out.println("🚀 LLM Streaming Demo 启动成功！");
        System.out.println("📖 访问地址: http://localhost:8080/stream-test.html");
        System.out.println("🔧 API接口: http://localhost:8080/api/stream/llm?prompt=你的问题");
    }
}

