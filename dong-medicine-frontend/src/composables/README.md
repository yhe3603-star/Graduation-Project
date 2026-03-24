# 组合式API函数目录说明

## 文件夹结构

本目录包含项目中所有的Vue 3组合式API函数（Composables），用于封装可复用的逻辑。

```
composables/
├── useAdminData.js        # 管理后台数据处理
├── useDebounce.js         # 防抖函数
├── useMedia.js            # 媒体处理
├── useUpdateLog.js        # 更新日志
└── index.js               # 统一导出
```

## 详细说明

### 1. useAdminData.js - 管理后台数据处理

**功能**：管理后台数据处理逻辑，包含数据获取、分页、对话框管理、操作处理等。

#### useAdminData(request)

**参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| request | Object | axios请求实例 |

**返回值**：
| 属性/方法 | 类型 | 说明 |
|----------|------|------|
| users | Ref<Array> | 用户列表 |
| knowledgeList | Ref<Array> | 知识列表 |
| inheritorsList | Ref<Array> | 传承人列表 |
| plantsList | Ref<Array> | 植物列表 |
| qaList | Ref<Array> | 问答列表 |
| resourcesList | Ref<Array> | 资源列表 |
| feedbackList | Ref<Array> | 反馈列表 |
| quizList | Ref<Array> | 答题列表 |
| commentsList | Ref<Array> | 评论列表 |
| logList | Ref<Array> | 日志列表 |
| adminStats | Ref<Object> | 统计数据 |
| pagination | Ref<Object> | 分页信息 |
| sortedComments | Computed | 排序后的评论 |
| sortedFeedback | Computed | 排序后的反馈 |
| sortedUsers | Computed | 排序后的用户 |
| fetchData | Function | 加载所有数据 |
| handleAdminPage | Function | 处理页码变化 |
| handleAdminSize | Function | 处理每页条数变化 |

**数据源配置**：
```javascript
const SECTIONS = {
  users: { ref: users, path: '/admin/users' },
  knowledge: { ref: knowledgeList, path: '/admin/knowledge' },
  inheritors: { ref: inheritorsList, path: '/admin/inheritors' },
  plants: { ref: plantsList, path: '/admin/plants' },
  qa: { ref: qaList, path: '/admin/qa' },
  resources: { ref: resourcesList, path: '/admin/resources' },
  feedback: { ref: feedbackList, path: '/admin/feedback', extra: 'status=all' },
  comments: { ref: commentsList, path: '/admin/comments', extra: 'status=all' },
  quiz: { ref: quizList, path: '/quiz/list' }
}
```

**排序规则**：
- 评论：待审核优先，按ID升序
- 反馈：待处理优先，按ID升序
- 用户：按ID升序

#### useAdminDialogs()

**返回值**：
| 属性/方法 | 类型 | 说明 |
|----------|------|------|
| dialogVisible | Ref<Object> | 表单对话框可见性 |
| detailVisible | Ref<Object> | 详情对话框可见性 |
| currentDetail | Ref<Object> | 当前详情数据 |
| formData | Ref<Object> | 表单数据 |
| commentDetailVisible | Ref<Boolean> | 评论详情可见性 |
| currentComment | Ref<Object> | 当前评论 |
| feedbackDetailVisible | Ref<Boolean> | 反馈详情可见性 |
| currentFeedback | Ref<Object> | 当前反馈 |
| logDetailVisible | Ref<Boolean> | 日志详情可见性 |
| currentLog | Ref<Object> | 当前日志 |
| openDialog | Function | 打开表单对话框 |
| viewDetail | Function | 查看详情 |
| editItem | Function | 编辑项目 |

#### useAdminActions(request, fetchData)

**参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| request | Object | axios请求实例 |
| fetchData | Function | 刷新数据函数 |

**返回值**：
| 属性/方法 | 类型 | 说明 |
|----------|------|------|
| selectedLogs | Ref<Array> | 选中的日志 |
| confirmDelete | Function | 确认删除 |
| saveItem | Function | 保存项目 |
| deleteItem | Function | 删除项目 |
| approveComment | Function | 审核通过评论 |
| rejectComment | Function | 拒绝评论 |
| handleLogSelectionChange | Function | 处理日志选择变化 |
| batchDeleteLogs | Function | 批量删除日志 |
| clearAllLogs | Function | 清空所有日志 |
| replyFeedback | Function | 回复反馈 |

**使用示例**：
```javascript
import { useAdminData, useAdminDialogs, useAdminActions } from '@/composables/useAdminData'

const request = inject('request')

const {
  users, knowledgeList, plantsList,
  adminStats, pagination, fetchData
} = useAdminData(request)

const {
  dialogVisible, detailVisible, formData,
  openDialog, viewDetail, editItem
} = useAdminDialogs()

const {
  confirmDelete, approveComment, batchDeleteLogs
} = useAdminActions(request, fetchData)
```

### 2. useDebounce.js - 防抖函数

**功能**：防抖处理，优化频繁触发的操作。

#### useDebounceFn(fn, delay)

**参数**：
| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| fn | Function | - | 需要防抖的函数 |
| delay | Number | 300 | 延迟时间(ms) |

**返回值**：
| 属性/方法 | 类型 | 说明 |
|----------|------|------|
| debouncedFn | Function | 防抖后的函数 |
| debouncedFn.cancel | Function | 取消待执行的函数 |

**使用示例**：
```javascript
import { useDebounceFn } from '@/composables/useDebounce'

const debouncedSearch = useDebounceFn((keyword) => {
  searchAPI(keyword)
}, 400)

// 在输入时调用
debouncedSearch('搜索关键词')

// 取消待执行的搜索
debouncedSearch.cancel()
```

#### useDebounce(value, delay)

**参数**：
| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| value | Ref | - | 需要防抖的响应式值 |
| delay | Number | 300 | 延迟时间(ms) |

**返回值**：
| 属性 | 类型 | 说明 |
|------|------|------|
| debouncedValue | Ref | 防抖后的响应式值 |

**使用示例**：
```javascript
import { useDebounce } from '@/composables/useDebounce'

const keyword = ref('')
const debouncedKeyword = useDebounce(keyword, 400)

watch(debouncedKeyword, (newVal) => {
  // 延迟400ms后触发搜索
  searchAPI(newVal)
})
```

### 3. useMedia.js - 媒体处理

**功能**：处理媒体文件相关的逻辑。

#### useDocumentPreview()

**返回值**：
| 属性/方法 | 类型 | 说明 |
|----------|------|------|
| previewVisible | Ref<Boolean> | 预览可见性 |
| previewDocument | Ref<Object> | 预览文档 |
| openPreview | Function | 打开预览 |
| closePreview | Function | 关闭预览 |

#### useMediaTabs(defaultTab)

**参数**：
| 参数 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| defaultTab | String | 'image' | 默认标签页 |

**返回值**：
| 属性/方法 | 类型 | 说明 |
|----------|------|------|
| activeTab | Ref<String> | 当前标签页 |
| videoPlayerRef | Ref | 视频播放器引用 |
| isVideoTab | Computed | 是否为视频标签页 |
| handleTabChange | Function | 处理标签页切换 |
| pauseVideo | Function | 暂停视频 |

#### useMediaDisplay(data, options)

**参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| data | Ref<Object> | 数据对象 |
| options.filesField | String | 文件字段名，默认'files' |

**返回值**：
| 属性 | 类型 | 说明 |
|------|------|------|
| allFiles | Computed<Array> | 所有文件 |
| imageList | Computed<Array> | 图片列表 |
| videoList | Computed<Array> | 视频列表 |
| documentList | Computed<Array> | 文档列表 |
| imagePreviewList | Computed<Array> | 图片预览URL列表 |
| hasImages | Computed<Boolean> | 是否有图片 |
| hasVideos | Computed<Boolean> | 是否有视频 |
| hasDocuments | Computed<Boolean> | 是否有文档 |
| hasMedia | Computed<Boolean> | 是否有媒体 |

#### useFileInfo(data, options)

**参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| data | Ref<Object> | 数据对象 |
| options.filesField | String | 文件字段名 |
| options.urlField | String | URL字段名 |
| options.nameField | String | 名称字段名 |
| options.sizeField | String | 大小字段名 |

**返回值**：
| 属性 | 类型 | 说明 |
|------|------|------|
| fileInfo | Computed<Object> | 文件信息对象 |
| fileUrl | Computed<String> | 文件URL |
| fileName | Computed<String> | 文件名 |
| fileSize | Computed<Number> | 文件大小 |
| fileType | Computed<String> | 文件类型 |
| fileExt | Computed<String> | 文件扩展名 |

### 4. useUpdateLog.js - 更新日志

**功能**：处理更新日志相关的逻辑。

#### useUpdateLog()

**返回值**：
| 属性/方法 | 类型 | 说明 |
|----------|------|------|
| newLogContent | Ref<String> | 新日志内容 |
| logDialogVisible | Ref<Boolean> | 日志对话框可见性 |
| editingLog | Ref<Object> | 正在编辑的日志 |
| parseUpdateLog | Function | 解析更新日志 |
| stringifyUpdateLog | Function | 序列化更新日志 |
| addLog | Function | 添加日志 |
| updateLog | Function | 更新日志 |
| deleteLog | Function | 删除日志 |
| generateAutoLog | Function | 生成自动日志 |
| openLogDialog | Function | 打开日志对话框 |
| closeLogDialog | Function | 关闭日志对话框 |
| saveLog | Function | 保存日志 |
| formatLogTime | Function | 格式化日志时间 |

**日志格式**：
```javascript
{
  id: 1698123456789,      // 时间戳ID
  time: '2024-10-24',     // 日期
  content: '更新了内容',   // 日志内容
  operator: '管理员'      // 操作人
}
```

**自动日志生成**：
```javascript
generateAutoLog('create', '药材', '新增了钩藤')
// 返回: '新增药材：新增了钩藤'
```

#### useUpdateLogDisplay(logData)

**参数**：
| 参数 | 类型 | 说明 |
|------|------|------|
| logData | Ref/String | 日志数据 |

**返回值**：
| 属性 | 类型 | 说明 |
|------|------|------|
| logs | Computed<Array> | 日志列表 |
| recentLogs | Computed<Array> | 最近5条日志 |
| hasLogs | Computed<Boolean> | 是否有日志 |
| formatLogTime | Function | 格式化时间 |

## 函数统计

| 文件 | 导出函数数量 | 主要用途 |
|------|-------------|---------|
| useAdminData.js | 3 | 管理后台数据处理 |
| useDebounce.js | 2 | 防抖优化 |
| useMedia.js | 4 | 媒体处理 |
| useUpdateLog.js | 2 | 更新日志 |
| **总计** | **11** | - |

## 开发规范

1. **命名规范**：
   - 函数名使用 `use` 前缀
   - 使用驼峰命名法

2. **返回值规范**：
   - 返回对象包含响应式数据和方法
   - 使用 `ref`/`computed` 包装响应式数据

3. **参数规范**：
   - 接收 `ref` 或普通值作为参数
   - 提供合理的默认值

4. **生命周期**：
   - 在 `onUnmounted` 中清理定时器
   - 在 `onUnmounted` 中取消监听

## 使用建议

- 组合式API函数应该专注于单一功能
- 避免在组合式函数中直接操作DOM
- 合理使用 `ref`、`reactive`、`computed`
- 对于异步操作，返回 Promise 或使用 async/await

---

**最后更新时间**：2026年3月25日
