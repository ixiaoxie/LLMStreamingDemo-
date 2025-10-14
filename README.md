# 🚀 LLM Streaming Demo - 大模型流式响应处理

[![Java](https://img.shields.io/badge/Java-8+-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.x-green.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Stars](https://img.shields.io/github/stars/yourusername/llm-streaming-demo.svg)](https://github.com/yourusername/llm-streaming-demo/stargazers)

## 📖 项目简介

一个基于Spring Boot的大模型流式响应处理demo，支持字符级输出效果，提供完整的API集成方案和实时测试界面。该项目展示了如何实现流式数据处理、字符级输出效果，以及如何集成第三方大模型API。

## ✨ 核心特性

- 🔄 **流式数据处理**: 支持实时流式响应，无需等待完整响应
- 📝 **字符级输出**: 每个字符独立发送，实现打字机效果
- 🔌 **多API支持**: 支持百度千帆、OpenAI、阿里云等多种大模型API
- 🎨 **实时测试界面**: 提供Web界面进行实时测试
- 📊 **完善错误处理**: 包含完整的异常处理和降级方案
- ⚡ **高性能**: 基于OkHttp，支持长连接和流式响应

## 🛠️ 技术栈

- **后端**: Spring Boot 2.x, OkHttp 4.x, Fastjson2
- **前端**: HTML5, JavaScript, CSS3
- **构建工具**: Maven 3.6+
- **运行环境**: JDK 8+

## 📦 快速开始

### 环境要求

- JDK 8 或更高版本
- Maven 3.6 或更高版本
- 有效的大模型API密钥

### 安装步骤

1. **克隆项目**
```bash
git clone https://github.com/yourusername/llm-streaming-demo.git
cd llm-streaming-demo
```

2. **配置API密钥**
```yaml
# 在 application.yml 中配置
llm:
  api:
    key: your-api-key-here
    url: https://api.example.com
    model: gpt-3.5-turbo
```

3. **启动应用**
```bash
# 使用Maven启动
mvn spring-boot:run

# 或使用启动脚本
chmod +x scripts/start.sh
./scripts/start.sh
```

4. **访问测试页面**
打开浏览器访问: `http://localhost:8080/stream-test.html`

## 🚀 使用说明

### API接口

```http
GET /api/stream/llm?prompt=你的问题&charLevel=true&maxTokens=500
```

**参数说明:**
- `prompt`: 提示词（必填）
- `charLevel`: 是否字符级输出（可选，默认true）
- `maxTokens`: 最大token数（可选，默认500）

**响应格式:**
```json
{"dataId":"uuid","dataType":"LLM_RESPONSE","content":"你","finished":false}
{"dataId":"uuid","dataType":"LLM_RESPONSE","content":"好","finished":false}
{"dataId":"uuid","dataType":"LLM_RESPONSE","content":"，","finished":false}
```

### 测试页面功能

- 实时输入提示词
- 查看字符级流式输出
- 监控响应状态
- 错误信息显示

## 🔧 配置说明

### 支持的API

#### 1. 百度千帆
```yaml
llm:
  api:
    key: your-baidu-api-key
    url: https://qianfan.baidubce.com/v2/chat/completions
    model: ernie-speed-pro-128k
```

#### 2. OpenAI
```yaml
llm:
  api:
    key: your-openai-api-key
    url: https://api.openai.com/v1/chat/completions
    model: gpt-3.5-turbo
```

#### 3. 阿里云
```yaml
llm:
  api:
    key: your-aliyun-api-key
    url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    model: qwen-turbo
```

### 环境变量配置

```bash
export LLM_API_KEY=your-api-key
export LLM_API_URL=https://api.example.com
export LLM_MODEL=gpt-3.5-turbo
```

## 📊 项目结构

```
llm-streaming-demo/
├── src/main/java/com/demo/llmstreaming/
│   ├── controller/           # 控制器层
│   │   └── StreamController.java
│   ├── service/             # 服务层
│   │   ├── StreamService.java
│   │   └── MsgCallback.java
│   ├── vo/                  # 数据传输对象
│   │   └── StreamResponseVO.java
│   ├── config/              # 配置类
│   │   └── StreamConfiguration.java
│   └── LlmStreamingApplication.java  # 主启动类
├── src/main/resources/
│   ├── static/              # 静态资源
│   │   └── stream-test.html
│   ├── application.yml      # 配置文件
│   └── application.properties
├── scripts/                 # 测试脚本
│   ├── start.sh
│   ├── test-api.sh
│   ├── test-char-level.sh
│   └── verify-api-key.sh
├── docs/                    # 文档
│   ├── API_INTEGRATION_GUIDE.md
│   └── DEPLOYMENT_GUIDE.md
├── pom.xml                  # Maven配置
└── README.md               # 项目说明
```

## 🎯 核心实现

### 流式数据处理流程

```
API响应 → 解析JSON → 字符级拆分 → 逐字符输出
```

### 字符级输出实现

```java
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
    
    // 添加延迟，实现打字机效果
    Thread.sleep(50);
}
```

## 🧪 测试

### 使用测试脚本

```bash
# 基础API测试
./scripts/test-api.sh

# 字符级输出测试
./scripts/test-char-level.sh

# API密钥验证
./scripts/verify-api-key.sh
```

### 使用curl测试

```bash
curl -X GET "http://localhost:8080/api/stream/llm?prompt=你好，请介绍一下自己" \
  -H "Accept: application/json" \
  --no-buffer
```

## 🚨 注意事项

1. **API密钥安全**: 请妥善保管API密钥，不要提交到代码仓库
2. **网络连接**: 确保网络连接稳定，支持长连接
3. **资源管理**: 及时关闭HTTP连接，避免资源泄漏
4. **错误处理**: 注意处理API错误和网络异常

## 📈 性能优化

### 连接池配置
```yaml
llm:
  api:
    timeout: 60000
    max-connections: 100
    keep-alive: 30000
```

### 线程池配置
```yaml
spring:
  task:
    execution:
      pool:
        core-size: 10
        max-size: 50
        queue-capacity: 200
```

## 🐳 Docker部署

### 构建镜像
```bash
mvn clean package -DskipTests
docker build -t llm-streaming-demo:1.0.0 .
```

### 运行容器
```bash
docker run -d \
  --name llm-streaming-demo \
  -p 8080:8080 \
  -e LLM_API_KEY=your-api-key \
  llm-streaming-demo:1.0.0
```

## ☸️ Kubernetes部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: llm-streaming-demo
spec:
  replicas: 3
  selector:
    matchLabels:
      app: llm-streaming-demo
  template:
    metadata:
      labels:
        app: llm-streaming-demo
    spec:
      containers:
      - name: llm-streaming-demo
        image: llm-streaming-demo:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: LLM_API_KEY
          value: "your-api-key"
```

## 🤝 贡献

欢迎提交Issue和Pull Request！

### 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## ⭐ 如果这个项目对你有帮助，请给个Star！

## 📞 联系方式

- 作者: Your Name
- 邮箱: your.email@example.com
- GitHub: [@yourusername](https://github.com/yourusername)

## 🙏 致谢

感谢所有为这个项目做出贡献的开发者！

---

**⭐ 如果这个项目对你有帮助，请给个Star支持一下！**

