# 组合式API函数目录说明

## 文件夹结构

本目录包含项目中所有的Vue 3组合式API函数，用于封装可复用的逻辑。

```
composables/
├── useAdminData.js        # 管理后台数据处理
├── useDebounce.js         # 防抖函数
├── useErrorHandler.js     # 错误处理
├── useFavorite.js         # 收藏功能
├── useFormDialog.js       # 表单对话框
├── useInteraction.js      # 交互功能
├── useMedia.js            # 媒体处理
├── usePersonalCenter.js   # 个人中心
├── usePlantGame.js        # 植物游戏
├── useQuiz.js             # 答题功能
├── useUpdateLog.js        # 更新日志
└── index.js               # 统一导出
```

## 详细说明

### 1. useAdminData.js

**功能**：管理后台数据处理逻辑

**主要方法**：
- `getAdminData`：获取管理后台数据
- `createAdminItem`：创建管理后台条目
- `updateAdminItem`：更新管理后台条目
- `deleteAdminItem`：删除管理后台条目
- `handleAdminSearch`：处理管理后台搜索

**使用场景**：管理后台页面的数据操作

**使用示例**：
```javascript
import { useAdminData } from '@/composables/useAdminData'

const { 
  loading, 
  dataList, 
  total, 
  getAdminData, 
  handleDelete 
} = useAdminData('plants')
```

### 2. useDebounce.js

**功能**：防抖函数，用于优化搜索、输入等频繁触发的操作

**主要方法**：
- `useDebounce`：创建防抖函数

**参数**：
- `value`：需要防抖的值
- `delay`：防抖延迟时间（毫秒）

**使用示例**：
```javascript
import { useDebounce } from '@/composables/useDebounce'

const searchKeyword = ref('')
const debouncedKeyword = useDebounce(searchKeyword, 300)

// 当debouncedKeyword变化时执行搜索
watch(debouncedKeyword, (newValue) => {
  if (newValue) {
    searchData(newValue)
  }
})
```

### 3. useErrorHandler.js

**功能**：统一错误处理

**主要方法**：
- `useErrorHandler`：创建错误处理器
- `handleApiError`：处理API错误
- `showErrorToast`：显示错误提示

**使用场景**：处理API请求错误、表单验证错误等

### 4. useFavorite.js

**功能**：收藏功能逻辑

**主要方法**：
- `toggleFavorite`：切换收藏状态
- `isFavorited`：检查是否已收藏
- `getUserFavorites`：获取用户收藏列表

**使用场景**：药材、知识、传承人等内容的收藏功能

### 5. useFormDialog.js

**功能**：表单对话框逻辑

**主要方法**：
- `openDialog`：打开对话框
- `closeDialog`：关闭对话框
- `resetForm`：重置表单
- `validateForm`：验证表单

**使用场景**：管理后台的添加/编辑表单

### 6. useInteraction.js

**功能**：用户交互功能逻辑

**主要方法**：
- `handleComment`：处理评论
- `handleLike`：处理点赞
- `handleShare`：处理分享

**使用场景**：文章、药材等内容的交互功能

### 7. useMedia.js

**功能**：媒体文件处理逻辑

**主要方法**：
- `uploadImage`：上传图片
- `uploadVideo`：上传视频
- `uploadDocument`：上传文档
- `previewMedia`：预览媒体文件

**使用场景**：管理后台的媒体文件上传

### 8. usePersonalCenter.js

**功能**：个人中心相关逻辑

**主要方法**：
- `getUserInfo`：获取用户信息
- `updateUserInfo`：更新用户信息
- `changePassword`：修改密码
- `getUserActivities`：获取用户活动记录

**使用场景**：个人中心页面

### 9. usePlantGame.js

**功能**：植物识别游戏逻辑

**主要方法**：
- `startGame`：开始游戏
- `submitAnswer`：提交答案
- `getGameResult`：获取游戏结果
- `getLeaderboard`：获取排行榜

**使用场景**：植物识别游戏页面

### 10. useQuiz.js

**功能**：答题功能逻辑

**主要方法**：
- `startQuiz`：开始答题
- `submitAnswer`：提交答案
- `getQuizResult`：获取答题结果
- `getQuizHistory`：获取答题历史

**使用场景**：答题页面

### 11. useUpdateLog.js

**功能**：更新日志相关逻辑

**主要方法**：
- `getUpdateLogs`：获取更新日志
- `formatLogDate`：格式化日志日期

**使用场景**：更新日志展示

### 12. index.js

**功能**：统一导出所有组合式API函数

**使用方法**：
```javascript
import { useDebounce, useFavorite, useMedia } from '@/composables'
```

## 开发规范

1. **命名规范**：函数名使用 `use` 前缀，驼峰命名法
2. **返回值**：返回响应式数据和方法的对象
3. **依赖管理**：明确函数依赖，避免循环依赖
4. **文档说明**：为每个函数添加详细的注释说明
5. **错误处理**：包含适当的错误处理逻辑

## 使用建议

- 组合式API函数应该专注于单一功能
- 避免在组合式函数中直接操作DOM
- 合理使用 `ref`、`reactive`、`computed` 等响应式API
- 对于异步操作，使用 `async/await` 或 `Promise`

---

**最后更新时间**：2026年3月23日