package com.demo.llmstreaming.service;

import com.alibaba.fastjson2.JSONObject;
import com.demo.llmstreaming.vo.StreamResponseVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 流式服务
 */
@Slf4j
@Service
public class StreamService {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${llm.api.url:https://qianfan.baidubce.com/v2/chat/completions}")
    private String chatUrl;

    @Value("${llm.api.key:YOUR_API_KEY}")
    private String apiKey;

    @Value("${llm.api.model:ernie-speed-pro-128k}")
    private String model;

    @Value("${llm.api.timeout:60000}")
    private long timeout;
    
    private final OkHttpClient okHttpClient;

    public StreamService() {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 大模型流式响应
     */
    public void streamLLMResponse(String prompt, int maxTokens, HttpServletResponse response) throws IOException {
        streamLLMResponse(prompt, maxTokens, response, false);
    }

     /**
      * 大模型流式响应（支持字符级控制）
      */
     public void streamLLMResponse(String prompt, int maxTokens, HttpServletResponse response, boolean charLevel) throws IOException {
         log.info("开始大模型流式响应，提示词：{}，字符级输出：{}", prompt, charLevel);
         
        // 构建请求体
        String requestBody = buildRequestBody(prompt, maxTokens);
         
         // 构建请求头
         Map<String, String> headers = buildHeaders();
         
         // 设置响应头
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
         response.setHeader("Cache-Control", "no-cache");
         response.setHeader("Connection", "keep-alive");
         
         PrintWriter writer = response.getWriter();
         String dataId = UUID.randomUUID().toString();
         
         // 创建消息回调
         MsgCallback msgCallback = new MsgCallback() {
             private volatile boolean interrupted = false;
             
             @Override
             public void msgCallback(String message, StringBuffer lines, boolean isSuccess, boolean isDone) {
                 try {
                     if (StringUtils.isNotBlank(message)) {
                         if (charLevel) {
                             // 字符级处理：将消息拆分成单个字符
                             for (int i = 0; i < message.length(); i++) {
                                 char ch = message.charAt(i);
                                 String charStr = String.valueOf(ch);
                                 
                                 StreamResponseVO data = StreamResponseVO.createData(
                                     dataId, "LLM_RESPONSE", charStr, null);
                                 data.setFinished(false);
                                 
                                 String jsonData = objectMapper.writeValueAsString(data);
                                 writer.write(jsonData + "\n");
                                 writer.flush();
                                 
                                 // 添加小延迟，让字符级输出更明显
                                 try {
                                     Thread.sleep(50); // 50ms延迟
                                 } catch (InterruptedException e) {
                                     Thread.currentThread().interrupt();
                                     interrupted = true;
                                     break;
                                 }
                             }
                         } else {
                             // 正常处理：发送整条消息
                             StreamResponseVO data = StreamResponseVO.createData(
                                 dataId, "LLM_RESPONSE", message, null);
                             data.setFinished(isDone);
                             
                             String jsonData = objectMapper.writeValueAsString(data);
                             writer.write(jsonData + "\n");
                             writer.flush();
                         }
                     }
                     
                     if (isDone) {
                         // 发送结束标记
                         StreamResponseVO endData = StreamResponseVO.createEnd(dataId);
                         String jsonData = objectMapper.writeValueAsString(endData);
                         writer.write(jsonData + "\n");
                         writer.flush();
                         
                         log.info("大模型流式响应完成");
                     }
                 } catch (Exception e) {
                     log.error("发送大模型响应失败", e);
                     interrupted = true;
                 }
             }
             
             @Override
             public boolean isInterrupted() {
                 return interrupted;
             }
         };
         
         // 执行流式请求，传入字符级控制参数
         executeStreamRequest(requestBody, headers, msgCallback, charLevel);
     }

    /**
     * 执行流式请求
     */
    private void executeStreamRequest(String requestBody, Map<String, String> headers, MsgCallback msgCallback, boolean charLevel) {
        Response response = streamPost(chatUrl, requestBody, headers);
        if (response != null) {
            BufferedReader reader = new BufferedReader(response.body().charStream());
            try {
                boolean isSuccess = true;
                boolean isDone = false;
                String line = null;
                StringBuffer lines = new StringBuffer();
                
                    while((line = reader.readLine()) != null) {
                        if (line.isEmpty()) {
                            // 跳过空串
                            continue;
                        }

                        if (msgCallback.isInterrupted()) {
                            isDone = true;
                            msgCallback.msgCallback("", lines, false, true);
                            break;
                        }
    
                    // 数据处理
                    JSONObject jsonObject = streamJsonMsgValidate(line, charLevel);
                    isSuccess = isSuccess && jsonObject.getBoolean("result");
                    int code = jsonObject.getInteger("code");
                    String message = "";
                    
                    if (code == 0 || code == 1) {
                        // code == 0 正常数据
                        // code == 1 不以data:打头的数据，是一串json
                        message = jsonObject.getString("message");
                        lines.append(message);
                    }

                    if (isSuccess) {
                        // 此处只发送成功数据，失败数据等到循环结束发送
                        if (StringUtils.isNotBlank(message)) {
                            // 过滤空串
                            if(StringUtils.isNotBlank(message)){
                                msgCallback.msgCallback(message, lines, isSuccess, false);
                            }
                        }
                    }
                }
                
                if (!isDone) {
                    // 被打断，或者失败数据
                    // 循环中止，仍没有Done
                    if (isJSONString(lines.toString())) {
                        // 最后一条数据为json，直接发送
                        msgCallback.msgCallback("应答失败", new StringBuffer("[LLM response failed] " + lines.toString()), false, true);
                    } else {
                        msgCallback.msgCallback("", lines, isSuccess, true);
                    }
                }
            } catch (Exception e) {
                log.error("[LLM read error] {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                msgCallback.msgCallback("链路超时", new StringBuffer("[LLM read error] " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage())), false, true);
            } finally {
                try {
                    reader.close();
                    response.close();
                } catch (IOException e) {
                    log.error("关闭资源失败", e);
                }
            }
        } else {
            msgCallback.msgCallback("请求超时", new StringBuffer("[LLM request failed] response is null"), false, true);
        }
    }

    /**
     * HTTP流式POST请求
     */
    public Response streamPost(String url, String requestBody, Map<String, String> headers) {
        Response response = null;
        Request.Builder builder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(requestBody, MediaType.get("application/json")));
        
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        
        Request request = builder.build();
        Call call = okHttpClient.newCall(request);
        try {
            response = call.execute();
        } catch (IOException e) {
            if (response != null) {
                response.close();
            }
            log.error("Http stream post error: {}", e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }
        return response;
    }

    /**
     * 构建请求体
     */
    private String buildRequestBody(String prompt, int maxTokens) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("stream", true);
        requestBody.put("max_output_tokens", maxTokens);
        requestBody.put("temperature", 0.95);
        requestBody.put("top_p", 0.7);
        requestBody.put("penalty_score", 1.0);
        
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", new Object[]{message});
        
        return requestBody.toJSONString();
    }

    /**
     * 构建请求头
     */
    private Map<String, String> buildHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "Bearer " + apiKey);
        headers.put("Accept", "text/event-stream");
        return headers;
    }

    /**
     * 流式JSON消息验证（增强字符级处理）
     */
    private JSONObject streamJsonMsgValidate(String line, boolean charLevel) {
        JSONObject result = new JSONObject();
        result.put("result", true);
        result.put("code", 0);
        result.put("message", "");
        
        try {
            if (line.startsWith("data: ")) {
                String jsonStr = line.substring(6);
                if ("[DONE]".equals(jsonStr)) {
                    result.put("code", -1);
                    return result;
                }
                
                JSONObject jsonObject = JSONObject.parseObject(jsonStr);
                
                // 检查是否有错误
                if (jsonObject.containsKey("error")) {
                    JSONObject error = jsonObject.getJSONObject("error");
                    String errorCode = error.getString("code");
                    String errorMessage = error.getString("message");
                    log.error("API返回错误: code={}, message={}", errorCode, errorMessage);
                    result.put("result", false);
                    result.put("code", -2);
                    result.put("message", "API错误: " + errorMessage);
                    return result;
                }
                
                // 百度千帆API响应格式解析
                String content = "";
                if (jsonObject.containsKey("result")) {
                    content = jsonObject.getString("result");
                } else if (jsonObject.containsKey("choices") && jsonObject.getJSONArray("choices").size() > 0) {
                    // 兼容OpenAI格式
                    JSONObject choice = jsonObject.getJSONArray("choices").getJSONObject(0);
                    if (choice.containsKey("delta") && choice.getJSONObject("delta").containsKey("content")) {
                        content = choice.getJSONObject("delta").getString("content");
                    }
                }
                
                if (StringUtils.isNotBlank(content)) {
                    result.put("message", content);
                }
            } else {
                // 不以data:打头的数据，是一串json
                result.put("code", 1);
                result.put("message", line);
                
                // 检查是否是错误响应
                try {
                    JSONObject jsonObject = JSONObject.parseObject(line);
                    if (jsonObject.containsKey("error")) {
                        JSONObject error = jsonObject.getJSONObject("error");
                        String errorCode = error.getString("code");
                        String errorMessage = error.getString("message");
                        log.error("API返回错误: code={}, message={}", errorCode, errorMessage);
                        result.put("result", false);
                        result.put("code", -2);
                        result.put("message", "API错误: " + errorMessage);
                    }
                } catch (Exception e) {
                    // 不是JSON格式，忽略
                }
            }
        } catch (Exception e) {
            log.error("解析流式JSON失败: {}", e.getMessage());
            result.put("result", false);
            result.put("message", "解析失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 判断是否为JSON字符串
     */
    private boolean isJSONString(String str) {
        try {
            JSONObject.parseObject(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}

