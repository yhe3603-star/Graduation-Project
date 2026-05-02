# 组合式函数目录 (composables/)

Composable（组合式函数）是 Vue 3 Composition API 中封装和复用**有状态逻辑**的标准方式。每个 composable 以 `use` 开头，返回包含响应式数据（`ref`/`computed`）和操作方法（普通函数）的对象。

> **与 utils 的区别：** utils 是无状态的纯函数（相同输入严格返回相同输出），composables 包含 Vue 响应式状态和组件生命周期。

---

## 全部 14 个 Composable

| 文件 | 导出函数 | 用途 |
|------|---------|------|
| `useAdminData.js` | `useAdminSection` 等 | 管理后台各模块的数据获取与分页管理 |
| `useCompare.js` | `useCompare` | 药材对比功能（localStorage 持久化，最多 3 项） |
| `useDebounce.js` | `useDebounceFn`、`useDebounce` | 防抖函数（延迟执行） |
| `useErrorHandler.js` | `useErrorHandler` | 全局异步错误处理与展示 |
| `useFavorite.js` | `useFavorite` | 收藏功能（增删查、计数更新、浏览量递增） |
| `useFileUpload.js` | `useFileUpload` | 文件上传通用逻辑（图片/视频/文档） |
| `useFormDialog.js` | `useFormDialog` | CRUD 表单对话框（含更新日志管理） |
| `useInteraction.js` | `useCountdown`、`useComments`、`usePagination`、`useFilter`、`useStats` | 交互功能工具集 |
| `useMedia.js` | `useDocumentPreview`、`useMediaTabs`、`useDocumentList`、`useMediaDisplay`、`useFileInfo` | 媒体显示通用逻辑 |
| `usePersonalCenter.js` | `usePersonalCenter` | 个人中心全部功能（收藏、记录、评论、设置） |
| `usePlantGame.js` | `usePlantGame` | 植物识别游戏逻辑 |
| `useQuiz.js` | `useQuiz` | 趣味答题逻辑 |
| `useUpdateLog.js` | `useUpdateLog`、`useUpdateLogDisplay` | 更新日志增删改查 |
| `useVisualData.js` | `useVisualData` | 数据可视化数据获取 |

---

## useFavorite(type) -- 收藏功能

**文件：** `useFavorite.js`

通用的收藏功能 composable，被所有需要收藏的模块复用。

**参数：** `type` (String) -- 收藏目标类型，可选 `'plant'` | `'knowledge'` | `'inheritor'` | `'resource'` | `'qa'`

**返回值：**

| 属性/方法 | 类型 | 说明 |
|-----------|------|------|
| `favorites` | Ref<Array> | 当前用户的全部收藏列表 |
| `items` | Ref<Array> | 关联的数据列表（用于更新收藏计数） |
| `isLoggedIn` | ComputedRef<Boolean> | 是否已登录 |
| `isFavorited(id)` | Function | 判断指定 id 是否已收藏 |
| `loadFavorites()` | Function | 从服务端加载收藏列表 |
| `toggleFavorite(id, isFav)` | Function | 切换收藏状态，未登录弹出提示，返回 Boolean |
| `updateItemCount(id, delta)` | Function | 本地更新收藏计数（+1/-1），避免刷新列表 |
| `incrementViewCount(id)` | Function | 增加浏览量（POST 请求，失败不报错） |

**使用示例：**

```js
import { useFavorite } from '@/composables'

const { favorites, isFavorited, toggleFavorite, loadFavorites } = useFavorite('plant')

// 在卡片点击时
const toggleFavoriteCard = async (item) => {
  await toggleFavorite(item.id, isFavorited(item.id))
}
```

**设计要点：**
- 未登录时收藏操作会提示"请先登录"并返回 false
- 收藏成功/取消时通过 `updateItemCount` 本地更新计数，避免额外 API 请求
- 浏览量递增失败只打 debug 日志，不影响用户体验

---

## useQuiz(request, isLoggedIn) -- 趣味答题

**文件：** `useQuiz.js`

**参数：**
- `request` -- Axios 实例
- `isLoggedIn` -- ComputedRef<Boolean>（从 userStore 获取）

**核心逻辑：**

1. **难度配置：** 三个难度等级
   - easy（初级）：10 题，每题 10 分，限时 5 分钟
   - medium（中级）：20 题，每题 15 分，限时 5 分钟
   - hard（高级）：30 题，每题 20 分，限时 5 分钟

2. **开始答题：** `startNewQuiz()` 从 `/quiz/questions` 获取题目（传入 count 和 scorePerQuestion 参数），重置答题状态，启动倒计时

3. **答题导航：** `nextQuestion()` / `prevQuestion()` 在当前题目索引范围内切换

4. **提交答卷：** `submitQuiz(isAutoSubmit)` 提交答案数组（包含 `questionId` 和 `answer`），获取成绩（score + correct）

5. **超时自动提交：** 通过 `watch(isExpired)` 监听，倒计时归零时自动调用 `autoSubmit()`

6. **分享功能：** 通过 `navigator.share`（移动端）或 `navigator.clipboard.writeText`（桌面端）分享成绩

7. **历史记录：** 登录用户提交后可获取答题历史，`bestScore` computed 计算最高分

---

## usePlantGame(request, isLoggedIn) -- 植物识别游戏

**文件：** `usePlantGame.js`

**核心逻辑：**

1. **难度与选项数：**
   - easy：3 个选项，每题基础分 10
   - medium：4 个选项，每题基础分 15
   - hard：5 个选项，每题基础分 20

2. **加载植物数据：** `loadPlants()` 从 `/plants/random?limit=50` 获取 50 个随机植物

3. **开始游戏：** `startGame()` 重置分数/连击/计数，启动 3 分钟倒计时

4. **选项生成：** 从植物池中随机选 1 个正确植物 + N-1 个干扰项，随机排序

5. **答题与连击加分：**
   - 答对：`streak++`，得分 = 基础分 + min(streak-1, 5) * 2（最多额外 +10）
   - 答错：`streak = 0`，得分为 0
   - 答后 1 秒（正确）或 1.5 秒（错误）自动切换到下一题

6. **超时自动结束：** `watch(isExpired)` 监听，时间到调用 `autoEndGame()`

7. **保存成绩：** 登录用户且分数 > 0 时 POST 到 `/plant-game/submit`

8. **清理定时器：** `onUnmounted` 中清除倒计时和 nextPlant 定时器

---

## useInteraction.js -- 交互功能工具集

**文件：** `useInteraction.js`

导出 4 个通用 composable：

### useCountdown(durationMinutes)

倒计时器，用于答题和游戏限时。

**参数：** `durationMinutes` (Number, default: 3) -- 倒计时分钟数

**返回值：**
| 属性 | 类型 | 说明 |
|------|------|------|
| `formattedTime` | ComputedRef<String> | `MM:SS` 格式时间 |
| `isRunning` | Ref<Boolean> | 计时器运行中 |
| `isExpired` | Ref<Boolean> | 计时器已归零 |
| `isLowTime` | ComputedRef<Boolean> | 剩余 <= 10 秒 |
| `start()` | Function | 启动计时器 |
| `stop()` | Function | 停止计时器 |
| `reset(minutes?)` | Function | 重置计时器 |
| `forceExpire()` | Function | 强制归零 |

**实现：** 使用 `setInterval(1000)` 每秒递减 `totalSeconds`。`onUnmounted` 自动清理定时器。

### useComments(request, isLoggedIn)

评论列表管理。

**返回值：** `comments`、`commentLoading`、`loadComments()`、`handleCommentPost(content, replyData, onSuccess, onError)`、分页相关

### usePagination(defaultSize)

客户端分页。

**返回值：** `currentPage`、`pageSize`、`paginatedList(list)`、`resetPage()`

### useFilter(items, filterFields)

客户端过滤。

**返回值：** `keyword`、`filters`、`filteredList`（computed）、`setFilter(key, value)`、`clearFilters()`

### useStats(items, config)

统计数据计算。

**参数：** `config` -- 配置数组 `[{ label: '总数', value: 'count' }, { label: '总浏览', value: 'views' }]`

**返回值：** ComputedRef<Array> -- 统计结果

---

## useCountdown 的用法示例（来自 QuizSection）

```vue
<script setup>
import { useCountdown } from '@/composables/useInteraction'

const { formattedTime, isRunning, isExpired, isLowTime, start: startTimer, stop: stopTimer, reset: resetTimer } = useCountdown(5)
</script>

<template>
  <div class="timer" :class="{ 'timer-warning': isLowTime }">
    {{ formattedTime }}
  </div>
</template>
```

---

## usePersonalCenter(request, updateUserState) -- 个人中心

**文件：** `usePersonalCenter.js`

**参数：**
- `request` -- Axios 实例
- `updateUserState` -- 更新全局用户状态的回调函数

**导出的常量：**
- `actions` -- 快捷操作配置：我的收藏(Star)、答题记录(EditPen)、我的评论(ChatDotRound)、账号设置(Setting)
- `typeIconMap` -- 收藏类型到图标的映射
- `typeTagMap` -- 收藏类型到 Element Plus tag type 的映射
- `typeNameMap` -- 收藏类型到中文名称的映射

**核心功能：**

1. **数据获取 `fetchUserData()`：** 使用 `Promise.all` 并行请求收藏列表、答题记录、植物游戏记录、评论列表，通过 `extractData()` 提取数据

2. **收藏管理：** `filteredFavorites` 按类型筛选，`paginatedFavorites` 分页展示，`goToDetail(item)` 根据类型跳转

3. **密码修改：** 使用 `createPasswordValidator()` 生成验证规则，POST 到 `/user/change-password`，成功后自动退出

4. **退出登录：** `ElMessageBox.confirm` 确认后调用 `userStore.logout()`，然后 `updateUserState()` 刷新全局状态

5. **辅助函数：** `formatTime`、`getDifficultyName`、`getScoreClass`、`getTypeIcon`、`getTypeTag`、`getTypeName`

---

## useCompare() -- 药材对比

**文件：** `useCompare.js`

**核心机制：** 共享响应式状态 + localStorage 持久化

```js
// 模块级共享状态（跨组件共享同一个 ref）
const compareList = ref(loadFromStorage())

// 自动持久化
watch(compareList, saveToStorage, { deep: true })
```

**返回值：**
| 属性 | 说明 |
|------|------|
| `compareList` | Ref<Array> -- 对比列表（共享状态） |
| `isInCompare(id)` | 判断是否在对比列表中 |
| `addToCompare(plant)` | 添加（最多 3 项，超过返回 false） |
| `removeFromCompare(id)` | 移除 |
| `clearCompare()` | 清空 |
| `MAX_COMPARE` | 最大对比数 = 3 |

---

## useAdminData.js -- 管理后台数据

**文件：** `useAdminData.js`

**导出：** `useAdminSection(request, path, options)`

**功能：**
- 通用的管理后台数据加载逻辑
- 自动处理分页响应（适配多种 `{ data, data.data, data.data.records }` 结构）
- `load(force)` 加载数据（缓存已加载状态，force 强制刷新）
- `setPage(page)` / `setSize(size)` 切换分页并重新加载

**使用方式：** 在 `Admin.vue` 中为每个模块创建 section 实例，统一管理 CRUD 操作流程。

---

## useFormDialog.js -- 表单对话框

**文件：** `useFormDialog.js`

CRUD 表单对话框的通用逻辑。

**参数：**
- `getDefaultForm` -- 返回默认表单对象的函数
- `options` -- 配置选项：
  - `validate(form)` -- 自定义验证函数
  - `autoLogMessages` -- 自动日志消息映射 `{ create: '新增数据', update: '更新数据' }`
  - `arrayFields` -- 需要序列化为 JSON 字符串的数组字段
  - `singleArrayFields` -- 取第一个元素的数组字段

**返回值：**
| 属性 | 说明 |
|------|------|
| `form` | 响应式表单对象 |
| `saving` | 保存 loading 状态 |
| `isEdit` | Computed -- 根据 `form.id` 判断是否为编辑模式 |
| `updateLogs` | Computed -- 解析后的更新日志数组 |
| `initForm(data)` | 初始化表单（新增/编辑） |
| `getFormData()` | 获取提交数据（自动追加更新日志） |
| `handleSave()` | 验证并返回是否通过 |
| 日志相关 | `handleAddLog`、`handleEditLog`、`handleDeleteLog`、`handleSaveLog` |

---

## useDebounce.js -- 防抖

**导出：**

- `useDebounceFn(fn, delay)` -- 返回防抖后的函数，带 `.cancel()` 方法，组件卸载时自动清理
- `useDebounce(value, delay)` -- 对响应式值进行防抖，返回 `ComputedRef`

---

## useErrorHandler.js -- 错误处理

全局异步错误处理 composable，统一捕获和展示错误信息。

---

## useMedia.js -- 媒体显示

**导出 5 个 composable：**

- `useDocumentPreview()` -- 文档预览弹窗状态管理（`previewVisible`、`previewDocument`、`openPreview(doc)`、`closePreview()`）
- `useMediaTabs(defaultTab)` -- 媒体 Tab 切换（activeTab、videoPlayerRef、isVideoTab、handleTabChange -- 切换时暂停视频）
- `useDocumentList(parseDocumentList)` -- 文档列表加载
- `useMediaDisplay(data, options)` -- 从数据中提取图片/视频/文档列表，计算 `hasImages`、`hasVideos`、`hasDocuments`、`hasMedia`
- `useFileInfo(data, options)` -- 从数据中提取单个文件信息（url、name、size、type、ext）

---

## useFileUpload.js -- 文件上传

**文件：** `useFileUpload.js`

**函数签名：** `useFileUpload({ type, extensions, extensionLabel, uploadPath, simulateProgress, props, emit })`

**功能：**
- 统一管理上传状态（fileList、uploading）
- 自动拼接上传 URL（含 baseURL）和 Authorization 请求头
- 上传前校验（格式、大小、数量限制）
- 删除前确认 + 服务器文件删除
- modelValue 双向绑定支持
- 支持模拟进度条（图片上传）

被 ImageUploader、VideoUploader、DocumentUploader 三个组件复用。

---

## useVisualData.js -- 数据可视化数据

**文件：** `useVisualData.js`

**导出：** `useVisualData(request)`

**功能：**
- 从 `/stats/chart` 获取全部图表数据（分类统计、地域分布、使用频次、热度排行）
- 从 `/stats/trend` 获取用户增长趋势（失败时使用 7 天空数据兜底）
- 数据映射到 `chartData` ref（`therapyCategories`、`inheritorLevels`、`plantCategories`、`formulaFreq`、`plantDistribution` 等）
- `regionList` computed 从分布数据中提取唯一地区列表
