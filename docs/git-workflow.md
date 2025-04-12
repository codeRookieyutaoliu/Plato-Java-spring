# Plato-Java-spring Git工作流规范

## 分支管理

我们采用简化版的Git Flow工作流，主要包含以下分支类型：

1. **main** - 主分支，保存正式发布的版本
2. **develop** - 开发分支，最新的开发进度
3. **feature/xxx** - 功能分支，用于开发新功能
4. **bugfix/xxx** - 修复分支，用于修复开发环境中的bug
5. **release/xxx** - 发布分支，用于准备发布

## 当前分支结构

- **main** - 主分支
- **develop** - 开发分支
- **feature/state** - state模块开发分支
- **feature/gateway** - gateway模块开发分支

## 当前项目状态

- **plato-client**: 大体完成
- **plato-ipconf**: 大体完成
- **plato-framework**: 正在用于重构common模块
- **plato-gateway**: 计划完全重写
- **plato-state**: 未开工

## 开发流程

1. 从`develop`分支创建功能分支：`git checkout -b feature/xxx develop`
2. 在功能分支上进行开发并提交：`git commit -m "feat: xxx"`
3. 当功能开发完成后，合并回`develop`分支：
   ```
   git checkout develop
   git merge --no-ff feature/xxx
   git push origin develop
   ```
4. 删除功能分支：`git branch -d feature/xxx`

## 代码审查要求

- 所有功能分支合并到开发分支前必须通过代码审查
- 至少需要1名团队成员批准
- 代码审查检查点：
  - 代码质量与规范
  - 单元测试覆盖率
  - 安全性问题
  - 性能考量

## 提交规范

我们使用Angular提交规范，提交消息格式如下：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 类型（Type）

- **feat**: 新功能
- **fix**: 修复bug
- **docs**: 文档更新
- **style**: 代码风格调整，不影响功能
- **refactor**: 代码重构，不新增功能也不修复bug
- **perf**: 性能优化
- **test**: 添加或修改测试用例
- **chore**: 构建过程或辅助工具的变动

### 范围（Scope）

指定提交影响的范围，例如：client、ipconf、gateway、state等

### 主题（Subject）

简短描述，不超过50个字符

### 提交示例
```
feat(用户模块): 添加用户注册功能

实现了用户注册API及相关服务层逻辑
增加了用户注册表单验证

Closes #123
```

## 语义化版本管理

本项目采用[语义化版本 2.0.0](https://semver.org/lang/zh-CN/)进行版本控制。版本格式为：X.Y.Z（主版本号.次版本号.修订号）

版本号递增规则如下：

1. 主版本号（X）：当做了不兼容的API修改时递增
2. 次版本号（Y）：当做了向下兼容的功能性新增时递增
3. 修订号（Z）：当做了向下兼容的问题修正时递增

## 版本号管理

每个模块的版本号在各自的`pom.xml`文件中定义：

```xml
<version>X.Y.Z</version>
```

父项目的版本号变更时，子模块的版本号需要同步更新。

## 命名规则

- 发布分支：`release/vX.Y.Z`
- Git标签：`vX.Y.Z`

## 发布流程

1. 从`develop`分支创建发布分支：`git checkout -b release/vX.Y.Z develop`
2. 进行发布准备工作，如版本号修改、文档更新等
3. 在`release`分支上进行版本相关修改和最后的测试
4. 测试通过后合并到`main`和`develop`分支：
   ```
   git checkout main
   git merge --no-ff release/vX.Y.Z
   git tag -a vX.Y.Z -m "Release version X.Y.Z"
   
   git checkout develop
   git merge --no-ff release/vX.Y.Z
   ```
5. 删除发布分支：`git branch -d release/vX.Y.Z`
6. 推送到远程仓库：`git push --tags origin main develop`

## CHANGELOG维护

所有版本变更必须记录在CHANGELOG.md文件中，格式如下：

```markdown
# 更新日志

## [X.Y.Z] - YYYY-MM-DD

### 新增
- 新增功能点1
- 新增功能点2

### 变更
- 变更内容1
- 变更内容2

### 修复
- 修复问题1
- 修复问题2

### 移除
- 移除功能1
- 移除功能2
```

## 版本依赖管理

1. 第三方依赖版本统一在父`pom.xml`的`<dependencyManagement>`部分定义
2. 子模块间依赖时，必须指定版本号（使用`${project.version}`）
3. 发布新版本前，需要检查并更新所有过期的依赖

## Git配置建议

```bash
# 设置用户信息
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 设置默认编辑器
git config --global core.editor "vim"

# 设置彩色输出
git config --global color.ui true

# 设置自动推送当前分支
git config --global push.default current
``` 