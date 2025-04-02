# 贡献指南

## Git 分支策略

- **主分支**：`main`/`master` - 生产环境代码
- **开发分支**：`develop` - 开发环境代码
- **功能分支**：`feature/*` - 新功能开发
- **修复分支**：`bugfix/*` - 修复开发环境bug
- **热修复分支**：`hotfix/*` - 修复生产环境bug
- **发布分支**：`release/*` - 版本发布准备

## 分支工作流

1. 从`develop`分支创建新的功能分支
   ```bash
   git checkout develop
   git pull
   git checkout -b feature/user-registration
   ```

2. 在功能分支上进行开发并提交
3. 完成功能后，通过Pull Request合并到`develop`分支
4. 发布前创建`release`分支
5. 从`release`分支合并到`main`/`master`分支进行生产发布

## 提交规范

提交信息格式要求：
```
<类型>(<范围>): <描述>

[可选正文]

[可选脚注]
```

### 类型
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构（不改变功能）
- `perf`: 性能优化
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

### 提交示例
```
feat(用户模块): 添加用户注册功能

实现了用户注册API及相关服务层逻辑
增加了用户注册表单验证

Closes #123
```

## 代码审查要求

- 所有功能分支合并到开发分支前必须通过代码审查
- 至少需要1名团队成员批准
- 代码审查检查点：
  - 代码质量与规范
  - 单元测试覆盖率
  - 安全性问题
  - 性能考量 