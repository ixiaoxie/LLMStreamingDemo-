# 🚀 快速开始指南

## 项目简介

这是一个基于Spring Boot的大模型流式响应处理demo，支持字符级输出效果，可以集成多种大模型API。

## 快速启动

### 1. 环境准备
- JDK 8+
- Maven 3.6+
- 有效的大模型API密钥

### 2. 配置API密钥

**方式一：修改配置文件**
```yaml
# 编辑 src/main/resources/application.yml
llm:
  api:
    key: your-actual-api-key  # 替换为您的API密钥
    url: https://api.example.com  # 替换为API地址
    model: your-model-name  # 替换为模型名称
```

**方式二：设置环境变量**
```bash
export LLM_API_KEY=your-actual-api-key
export LLM_API_URL=https://api.example.com
export LLM_MODEL=your-model-name
```

### 3. 启动应用

```bash
# 编译并启动
mvn spring-boot:run

# 或使用启动脚本
chmod +x scripts/start.sh
./scripts/start.sh
```

### 4. 访问测试页面

打开浏览器访问：`http://localhost:8080/stream-test.html`

## 支持的API服务商

### 百度千帆
```yaml
llm:
  api:
    key: your-baidu-api-key
    url: https://qianfan.baidubce.com/v2/chat/completions
    model: ernie-speed-pro-128k
```

### OpenAI
```yaml
llm:
  api:
    key: your-openai-api-key
    url: https://api.openai.com/v1/chat/completions
    model: gpt-3.5-turbo
```

### 阿里云
```yaml
llm:
  api:
    key: your-aliyun-api-key
    url: https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation
    model: qwen-turbo
```

## API接口

### 流式响应接口
```http
GET /api/stream/llm?prompt=你的问题&charLevel=true&maxTokens=500
```

### 健康检查接口
```http
GET /api/stream/health
```

## 测试脚本

```bash
# 基础API测试
./scripts/test-api.sh

# 字符级输出测试
./scripts/test-char-level.sh

# API密钥验证
./scripts/verify-api-key.sh
```

## 常见问题

### 1. 编译失败
- 检查JDK版本是否为8+
- 检查Maven版本是否为3.6+
- 检查网络连接是否正常

### 2. 启动失败
- 检查端口8080是否被占用
- 检查API密钥配置是否正确
- 查看日志文件：`logs/llm-streaming-demo.log`

### 3. API调用失败
- 检查API密钥是否有效
- 检查网络连接是否正常
- 检查API地址和模型名称是否正确

## 项目结构

```
llm-streaming-demo/
├── src/main/java/com/demo/llmstreaming/  # 源代码
├── src/main/resources/                   # 配置文件和静态资源
├── scripts/                              # 测试脚本
├── docs/                                 # 文档
├── pom.xml                               # Maven配置
└── README.md                             # 项目说明
```

## 更多信息

- 详细文档：[README.md](README.md)
- API集成指南：[docs/API_INTEGRATION_GUIDE.md](docs/API_INTEGRATION_GUIDE.md)
- 部署指南：[docs/DEPLOYMENT_GUIDE.md](docs/DEPLOYMENT_GUIDE.md)

## 技术支持

如果遇到问题，请：
1. 查看日志文件
2. 运行测试脚本
3. 检查配置文件
4. 参考文档说明

