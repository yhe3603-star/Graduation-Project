# 代码精简与优化 Spec

## Why
项目代码存在冗余逻辑、重复代码、可读性问题，需要进行精简优化以提升代码质量和可维护性，同时严格保证所有功能、样式、交互完全不变。

## What Changes
- 精简前端 Vue 组件代码，合并重复逻辑
- 精简后端 Java 代码，清理冗余代码
- 优化代码结构，提升可读性
- 合并相似逻辑，减少代码行数

## Impact
- Affected specs: 前端所有 Vue 组件、后端所有 Java 类
- Affected code: 
  - 前端：`dong-medicine-frontend/src/` 下所有文件
  - 后端：`dong-medicine-backend/src/main/java/` 下所有文件

## ADDED Requirements

### Requirement: 前端代码精简
系统 SHALL 保持所有页面功能、样式、交互完全不变的前提下，精简 Vue 代码。

#### Scenario: Vue 组件精简
- **WHEN** 分析 Vue 组件代码
- **THEN** 合并重复的模板逻辑、简化 computed/methods、优化 import 语句
- **AND** 保证页面展示、样式、交互、功能完全不变

#### Scenario: 工具函数精简
- **WHEN** 分析 utils 目录下的工具函数
- **THEN** 合并相似函数、简化逻辑、清理未使用代码
- **AND** 保证所有调用方正常工作

### Requirement: 后端代码精简
系统 SHALL 保持所有接口返回、业务逻辑、功能完全不变的前提下，精简 Java 代码。

#### Scenario: Controller 精简
- **WHEN** 分析 Controller 层代码
- **THEN** 合并重复的参数校验逻辑、简化响应构建、优化注解使用
- **AND** 保证接口返回结果完全一致

#### Scenario: Service 精简
- **WHEN** 分析 Service 层代码
- **THEN** 合并重复的业务逻辑、简化条件判断、优化数据转换
- **AND** 保证业务逻辑完全不变

#### Scenario: Entity/DTO 精简
- **WHEN** 分析实体类和 DTO
- **THEN** 简化注解配置、合并相似字段、优化 Lombok 使用
- **AND** 保证数据结构完全不变

## Constraints
- **禁止**修改变量名、函数名、类名
- **禁止**修改页面样式、DOM 结构
- **禁止**修改功能逻辑、业务行为
- **禁止**删除任何功能模块
- **禁止**修改接口签名和返回格式
