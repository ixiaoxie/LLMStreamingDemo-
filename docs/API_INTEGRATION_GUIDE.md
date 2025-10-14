# API集成指南

## 概述

本指南介绍如何配置和集成不同的大模型API服务商，包括百度千帆、OpenAI、阿里云等。

## 支持的API服务商

### 1. 百度千帆

#### 配置信息
```yaml
llm:
  api:
    key: your-baidu-api-key
    url: https://qianfan.baidubce.com/v2/chat/completions
    model: ernie-speed-pro-128k
```

#### 获取API密钥
1. 访问 [百度智能云千帆平台](https://qianfan.cloud.baidu.com/)
2. 注册并登录账号
3. 创建应用获取API Key和Secret Key
4. 在控制台获取访问令牌

#### 支持的模型
- `ernie-speed-pro-128k` - 快速响应模型
- `ernie-3.5-8k` - 标准模型
- `ernie-4.0-8k` - 最新模型

#### 响应格式
```json
{
  "id": "chatcmpl-xxx",
  "object": "chat.completion.chunk",
  "created": 1234567890,
  "result": "你好",
  "is_end": false,
  "is_truncated": false,
  "need_clear_history": false,
  "usage": {
    "prompt_tokens": 10,
    "completion_tokens": 5,
    "total_tokens": 15
  }
}
```

### 2. OpenAI

#### 配置信息
```yaml
llm:
  api:
    key: your-openai-api-key
    url: https://api.openai.com/v1/chat/completions
    model: gpt-3.5-turbo
```

#### 获取API密钥
1. 访问 [OpenAI官网](https://platform.openai.com/)
2. 注册并登录账号
3. 在API Keys页面创建新的API密钥
4. 复制密钥用于配置

#### 支持的模型
- `gpt-3.5-turbo` - 标准模型
- `gpt-4` - 高级模型
- `gpt-4-turbo` - 快速模型

#### 响应格式
```json
{
  "id": "chatcmpl-xxx",
  "object": "chat.completion.chunk",
  "created": 1234567890,
  "model": "gpt-3.5-turbo",
  "choices": [
    {
      "index": 0,
      "delta": {
        "content": "你好"
      },
      "finish_reason": null
    }
  ]
}
```

### 3. 阿里云

#### 配置信息
```yaml
llm:
  api:
    key: your-aliyun-api-key
    url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    model: qwen-turbo
```

#### 获取API密钥
1. 访问 [阿里云DashScope](https://dashscope.aliyun.com/)
2. 注册并登录账号
3. 在API密钥管理页面创建密钥
4. 复制API Key用于配置

#### 支持的模型
- `qwen-turbo` - 快速模型
- `qwen-plus` - 标准模型
- `qwen-max` - 高级模型

#### 响应格式
```json
{
  "output": {
    "text": "你好",
    "finish_reason": "null"
  },
  "usage": {
    "input_tokens": 10,
    "output_tokens": 5,
    "total_tokens": 15
  },
  "request_id": "xxx"
}
```

## 配置方式

### 方式一：配置文件

修改 `src/main/resources/application.yml`：

```yaml
llm:
  api:
    key: your-actual-api-key
    url: https://api.example.com/v1/chat/completions
    model: your-model-name
    timeout: 60000
```

### 方式二：环境变量

```bash
export LLM_API_KEY=your-actual-api-key
export LLM_API_URL=https://api.example.com/v1/chat/completions
export LLM_MODEL=your-model-name
export LLM_API_TIMEOUT=60000
```

### 方式三：启动参数

```bash
java -jar app.jar \
  --llm.api.key=your-actual-api-key \
  --llm.api.url=https://api.example.com/v1/chat/completions \
  --llm.api.model=your-model-name
```

## 请求参数

### 通用参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | String | 是 | 提示词 |
| charLevel | Boolean | 否 | 是否字符级输出，默认true |
| maxTokens | Integer | 否 | 最大token数，默认500 |

### 示例请求

```bash
curl -X GET "http://localhost:8080/api/stream/llm?prompt=你好&charLevel=true&maxTokens=100"
```

## 响应格式

### 流式响应

每个响应都是一个JSON对象，包含以下字段：

```json
{
  "dataId": "uuid",
  "dataType": "LLM_RESPONSE",
  "content": "你",
  "finished": false,
  "timestamp": "2024-01-01T12:00:00.000",
  "sequence": 1
}
```

### 响应类型

- `LLM_RESPONSE` - 正常响应内容
- `END` - 响应结束标记
- `ERROR` - 错误信息

### 结束标记

```json
{
  "dataId": "uuid",
  "dataType": "END",
  "finished": true,
  "timestamp": "2024-01-01T12:00:00.000"
}
```

### 错误响应

```json
{
  "dataId": "uuid",
  "dataType": "ERROR",
  "errorMessage": "API错误: 无效的API密钥",
  "finished": true,
  "timestamp": "2024-01-01T12:00:00.000"
}
```

## 错误处理

### 常见错误

1. **401 Unauthorized**
   - 原因：API密钥无效
   - 解决：检查API密钥是否正确

2. **403 Forbidden**
   - 原因：API密钥权限不足
   - 解决：检查API密钥权限设置

3. **429 Too Many Requests**
   - 原因：请求频率过高
   - 解决：降低请求频率或升级API套餐

4. **500 Internal Server Error**
   - 原因：API服务内部错误
   - 解决：稍后重试或联系服务商

### 错误处理策略

1. **重试机制**：自动重试失败的请求
2. **降级处理**：API不可用时返回默认响应
3. **错误日志**：记录详细错误信息用于排查
4. **用户提示**：友好的错误提示信息

## 性能优化

### 连接池配置

```yaml
llm:
  api:
    timeout: 60000
    max-connections: 100
    keep-alive: 30000
```

### 缓存策略

```yaml
llm:
  cache:
    enabled: true
    ttl: 3600
    max-size: 1000
```

### 并发控制

```yaml
llm:
  concurrency:
    max-requests: 10
    queue-size: 100
```

## 监控和日志

### 日志配置

```yaml
logging:
  level:
    com.demo.llmstreaming: DEBUG
    okhttp3: DEBUG
```

### 监控指标

- 请求成功率
- 响应时间
- 错误率
- Token使用量

## 安全建议

1. **API密钥保护**
   - 不要将API密钥提交到代码仓库
   - 使用环境变量或密钥管理服务
   - 定期轮换API密钥

2. **网络安全**
   - 使用HTTPS协议
   - 配置防火墙规则
   - 限制访问IP范围

3. **数据安全**
   - 敏感数据加密传输
   - 不在日志中记录敏感信息
   - 定期清理临时数据

## 故障排除

### 常见问题

1. **连接超时**
   ```bash
   # 检查网络连接
   ping api.example.com
   
   # 检查防火墙设置
   telnet api.example.com 443
   ```

2. **响应格式错误**
   ```bash
   # 检查API文档
   curl -v https://api.example.com/v1/chat/completions
   ```

3. **认证失败**
   ```bash
   # 验证API密钥
   curl -H "Authorization: Bearer your-api-key" https://api.example.com/v1/models
   ```

### 调试工具

1. **API测试脚本**
   ```bash
   ./scripts/verify-api-key.sh
   ```

2. **流式响应测试**
   ```bash
   ./scripts/test-char-level.sh
   ```

3. **性能测试**
   ```bash
   ./scripts/test-api.sh
   ```

## 更新日志

### v1.0.0
- 支持百度千帆API
- 支持OpenAI API
- 支持阿里云API
- 实现流式响应处理
- 实现字符级输出效果

