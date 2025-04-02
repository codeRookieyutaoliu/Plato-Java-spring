# Plato-Java-spring Git工作流规范

## 分支管理

我们采用简化版的Git Flow工作流，主要包含以下分支类型：

1. **main** - 主分支，保存正式发布的版本
2. **develop** - 开发分支，最新的开发进度
3. **feature/xxx** - 功能分支，用于开发新功能
4. **bugfix/xxx** - 修复分支，用于修复bug
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

## 发布流程

1. 从`develop`分支创建发布分支：`git checkout -b release/x.y.z develop`
2. 进行发布准备工作，如版本号修改、文档更新等
3. 完成后合并到`main`和`develop`分支：
   ```
   git checkout main
   git merge --no-ff release/x.y.z
   git tag -a vx.y.z -m "Release version x.y.z"
   
   git checkout develop
   git merge --no-ff release/x.y.z
   ```
4. 删除发布分支：`git branch -d release/x.y.z`
5. 推送到远程仓库：`git push --tags origin main develop`

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