# Plato-Java-spring

Plato项目的Java实现版本，基于Spring框架。

## 项目结构

```
Plato-Java-spring/
├── plato-common/     - 通用工具类和模型
├── plato-client/     - 客户端SDK和命令行工具
├── plato-gateway/    - 网关服务，负责长连接管理
├── plato-ipconf/     - IP配置服务
└── plato-state/      - 状态管理服务
```

## 项目状态

当前项目开发进度：

- **plato-client**: 大体完成
- **plato-ipconf**: 大体完成
- **plato-framework**: 正在用于重构common模块
- **plato-gateway**: 计划完全重写
- **plato-state**: 未开工

## 开发指南

1. 克隆仓库：
   ```bash
   git clone https://github.com/codeRookieyutaoliu/Plato-Java-spring.git
   ```

2. 查看Git工作流文档：
   ```bash
   cat git-workflow.md
   ```

3. 按照Git工作流文档创建功能分支并开始开发。

详细的开发流程和规范请参考 [git-workflow.md](./git-workflow.md)。

## 开发规范

### 版本管理规范

我们使用[语义化版本 2.0.0](https://semver.org/lang/zh-CN/)进行版本标识，格式为：主版本号.次版本号.修订号

- 主版本号：当你做了不兼容的API修改
- 次版本号：当你做了向下兼容的功能性新增
- 修订号：当你做了向下兼容的问题修正

### Git分支规范

详见 [CONTRIBUTING.md](CONTRIBUTING.md)

### 代码风格规范

- 使用Checkstyle进行代码风格检查
- 遵循Google Java Style指南
- 每个类和方法都应有完整的中文注释
- 变量和方法命名采用驼峰命名法
- 常量使用全大写，下划线分隔
- 最大行长度为120字符

### CI/CD流程

- 使用GitHub Actions进行持续集成和部署
- 每次推送到开发分支会自动构建和单元测试
- 提交到主分支或发布分支时会自动部署到相应环境
- 代码覆盖率至少要达到60%

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+

### 构建项目

```bash
mvn clean package
```

### 运行测试

```bash
mvn test
```

### 启动服务

```bash
# 启动特定模块
cd plato-gateway
mvn spring-boot:run
```

## 贡献指南

请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 了解详细的贡献流程。 