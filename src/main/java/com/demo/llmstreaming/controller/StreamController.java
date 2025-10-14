package com.demo.llmstreaming.controller;

import com.demo.llmstreaming.service.StreamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 流式响应控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/stream")
@Api(tags = "流式响应管理")
public class StreamController {

    @Autowired
    private StreamService streamService;

    /**
     * 大模型流式响应接口
     */
    @GetMapping(value = "/llm", produces = "application/json")
    @ApiOperation(value = "大模型流式响应", notes = "使用OkHttp调用大模型API进行流式响应，支持字符级输出")
    public void streamLLMResponse(
            @ApiParam(value = "提示词", required = true) @RequestParam String prompt,
            @ApiParam(value = "是否字符级输出", required = false) @RequestParam(defaultValue = "true") boolean charLevel,
            @ApiParam(value = "最大token数", required = false) @RequestParam(defaultValue = "500") int maxTokens,
            HttpServletResponse response) throws IOException {
        
        log.info("开始大模型流式响应，提示词：{}，字符级输出：{}，最大token数：{}", prompt, charLevel, maxTokens);
        streamService.streamLLMResponse(prompt, maxTokens, response, charLevel);
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    @ApiOperation(value = "健康检查", notes = "检查服务是否正常运行")
    public String health() {
        return "OK";
    }
}

