# 组件目录说明

## 文件夹结构

本目录包含项目中所有的Vue组件，按照功能和用途进行分类。

```
components/
├── base/            # 基础组件
│   ├── ErrorBoundary.vue
│   ├── VirtualList.vue
│   └── index.js
├── business/        # 业务组件
│   ├── admin/       # 管理后台组件
│   ├── dialogs/     # 详情对话框
│   ├── display/     # 展示组件
│   ├── interact/    # 交互组件
│   ├── layout/      # 布局组件
│   ├── media/       # 媒体组件
│   └── upload/      # 上传组件
├── common/          # 通用组件
│   └── SkeletonGrid.vue
└── README.md
```

## 详细说明

### 1. base/ 目录 - 基础组件

#### ErrorBoundary.vue

**功能**：错误边界组件，捕获子组件的错误，防止整个应用崩溃。

**Props**：无

**Slots**：
- `default`：正常渲染的内容

**事件**：无

**核心逻辑**：
```javascript
onErrorCaptured((error, instance, info) => {
  hasError.value = true
  errorMessage.value = error.message || '未知错误'
  errorStack.value = `${error.stack || ''}\n\nComponent: ${info}`
  return false // 阻止错误继续传播
})
```

**方法**：
| 方法 | 说明 |
|------|------|
| `retry()` | 重置错误状态，重新渲染 |
| `goHome()` | 返回首页 |

#### VirtualList.vue

**功能**：虚拟列表组件，高效渲染大量数据。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| items | Array | [] | 数据列表 |
| itemHeight | Number | 50 | 每项高度 |
| buffer | Number | 5 | 缓冲项数量 |

### 2. business/admin/ 目录 - 管理后台组件

#### AdminDashboard.vue

**功能**：管理后台仪表盘，展示统计数据和快捷操作。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| stats | Object | null | 统计数据对象 |
| users | Array | [] | 用户列表 |
| knowledge | Array | [] | 知识列表 |
| inheritors | Array | [] | 传承人列表 |
| plants | Array | [] | 植物列表 |
| qa | Array | [] | 问答列表 |
| resources | Array | [] | 资源列表 |
| feedback | Array | [] | 反馈列表 |
| quiz | Array | [] | 答题列表 |
| comments | Array | [] | 评论列表 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| view-feedback | - | 查看全部反馈 |
| navigate | menu | 导航到指定菜单 |

**统计卡片**：
- 用户总数、知识条目、传承人、药用植物
- 问答数据、资源文件、答题题目、评论数量
- 系统日志、反馈总数

**快捷操作**：
- 用户管理、答题管理、评论管理、日志管理
- 管理知识、管理传承人、管理植物、管理问答
- 管理资源、反馈管理

#### AdminDataTable.vue

**功能**：通用数据表格组件，支持服务端分页、搜索、排序。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| title | String | - | 表格标题 |
| titleName | String | '名称' | 标题列名称 |
| data | Array | [] | 表格数据 |
| columns | Array | [] | 列配置 |
| showAdd | Boolean | true | 是否显示添加按钮 |
| showEdit | Boolean | true | 是否显示编辑按钮 |
| showTitle | Boolean | true | 是否显示标题列 |
| actionWidth | String | '200' | 操作列宽度 |
| serverPagination | Boolean | false | 是否服务端分页 |
| serverTotal | Number | 0 | 服务端总数 |
| page | Number | 1 | 当前页码 |
| pageSize | Number | 12 | 每页条数 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| add | - | 添加按钮点击 |
| edit | row | 编辑按钮点击 |
| delete | id | 删除按钮点击 |
| view | row | 查看按钮点击 |
| selection-change | selection | 选择变化 |
| page-change | page | 页码变化 |
| size-change | size | 每页条数变化 |

**Slots**：
- `extraColumns`：额外列
- `actions`：自定义操作列

**列配置格式**：
```javascript
{
  prop: 'name',        // 字段名
  label: '名称',       // 列标题
  width: '100',        // 列宽度
  minWidth: '150',     // 最小宽度
  type: 'tag',         // 类型（tag/text）
  slotName: 'custom'   // 插槽名
}
```

**标签类型映射**：
```javascript
const TAG_TYPES = { 
  easy: "success", medium: "warning", hard: "danger", 
  "省级": "warning", "自治区级": "success", 
  approved: "success", pending: "warning", rejected: "danger",
  video: "primary", document: "success", image: "warning",
  active: "success", banned: "danger"
}
```

#### AdminSidebar.vue

**功能**：管理后台侧边栏导航。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| activeMenu | String | 'dashboard' | 当前激活菜单 |
| userName | String | '管理员' | 用户名 |
| logoutLoading | Boolean | false | 登出加载状态 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| update:active-menu | menu | 菜单切换 |
| logout | - | 登出 |

**菜单项**：
- 仪表盘、用户管理、知识管理、传承人管理
- 植物管理、问答管理、资源管理、答题管理
- 评论管理、反馈管理、日志管理

### 3. business/dialogs/ 目录 - 详情对话框

#### PlantDetailDialog.vue

**功能**：药材详情对话框，展示药材的完整信息。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| visible | Boolean | false | 对话框可见性 |
| plant | Object | null | 药材数据 |
| isFavorited | Boolean | false | 是否已收藏 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| update:visible | boolean | 更新可见性 |
| toggle-favorite | - | 切换收藏状态 |

**展示内容**：
- 基本信息：中文名、侗语名、学名、分类、用法、产地、功效
- 媒体内容：视频、图片、文档（Tab切换）
- 统计信息：浏览次数、收藏次数

**子组件**：
- VideoPlayer：视频播放器
- ImageCarousel：图片轮播
- DocumentList：文档列表
- DocumentPreview：文档预览

### 4. business/display/ 目录 - 展示组件

#### SearchFilter.vue

**功能**：搜索过滤组件，支持关键词搜索和多条件筛选。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| placeholder | String | '搜索...' | 输入框占位符 |
| filters | Array | [] | 筛选配置 |
| modelValue | String | - | 搜索关键词 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| update:modelValue | value | 更新关键词 |
| search | keyword | 搜索触发 |
| filter | filters | 筛选条件变化 |

**筛选配置格式**：
```javascript
{
  key: 'category',           // 筛选字段名
  label: '难度',             // 筛选标签
  type: 'success',           // 标签类型
  options: [                 // 选项列表
    { label: '全部', value: '' },
    { label: '入门', value: 'easy' }
  ]
}
```

**防抖处理**：搜索输入400ms防抖

#### Pagination.vue

**功能**：分页组件，封装Element Plus分页器。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| page | Number | 1 | 当前页码 |
| size | Number | 12 | 每页条数 |
| total | Number | 0 | 总条数 |
| pageSizes | Array | [12, 24, 36, 48] | 每页条数选项 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| update:page | page | 页码变化 |
| update:size | size | 每页条数变化 |

#### PageSidebar.vue

**功能**：页面侧边栏，展示统计信息和热门内容。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| title | String | - | 侧边栏标题 |
| stats | Array | [] | 统计数据 |
| hotTitle | String | - | 热门内容标题 |
| hotItems | Array | [] | 热门内容列表 |

**Slots**：
- `default`：额外内容

### 5. business/media/ 目录 - 媒体组件

#### MediaDisplay.vue

**功能**：媒体展示组件，统一展示图片、视频、文档。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| files | String/Array | '' | 文件列表（JSON或数组） |
| showImages | Boolean | true | 是否显示图片 |
| showVideos | Boolean | true | 是否显示视频 |
| showDocuments | Boolean | true | 是否显示文档 |
| showDivider | Boolean | true | 是否显示分隔线 |
| imageTitle | String | '相关图片' | 图片区域标题 |
| videoTitle | String | '相关视频' | 视频区域标题 |
| documentTitle | String | '相关文档' | 文档区域标题 |

**功能**：
- 自动解析文件列表
- 按类型分组展示
- 图片支持预览
- 文档支持下载

#### ImageCarousel.vue

**功能**：图片轮播组件。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| images | Array | [] | 图片列表 |
| autoPlay | Boolean | true | 自动播放 |
| height | String | '300px' | 轮播高度 |

#### VideoPlayer.vue

**功能**：视频播放器组件。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| videos | Array | [] | 视频列表 |
| autoPlay | Boolean | false | 自动播放 |
| height | String | '300px' | 播放器高度 |

**方法**：
| 方法 | 说明 |
|------|------|
| `play()` | 播放 |
| `pause()` | 暂停 |
| `switchToVideo(index)` | 切换视频 |

#### DocumentList.vue

**功能**：文档列表组件。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| documents | Array | [] | 文档列表 |
| loading | Boolean | false | 加载状态 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| document-click | doc | 文档点击 |

### 6. business/upload/ 目录 - 上传组件

#### ImageUploader.vue

**功能**：图片上传组件，支持多图上传、拖拽排序、预览删除。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| modelValue | String/Array | '' | 图片数据 |
| category | String | 'plants' | 上传分类 |
| limit | Number | 9 | 最大数量 |
| multiple | Boolean | true | 是否多选 |
| maxSize | Number | 10 | 最大大小(MB) |
| disabled | Boolean | false | 是否禁用 |
| showName | Boolean | false | 是否显示文件名 |
| showTip | Boolean | true | 是否显示提示 |
| replaceConfirm | Boolean | false | 替换时确认 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| update:modelValue | value | 更新数据 |
| change | images | 图片变化 |
| success | data | 上传成功 |
| error | error | 上传失败 |
| remove | image | 删除图片 |

**支持格式**：jpg, jpeg, png, gif, bmp, webp

**暴露方法**：
| 方法 | 说明 |
|------|------|
| `clearImages()` | 清空图片 |
| `getImages()` | 获取图片列表 |

### 7. business/interact/ 目录 - 交互组件

#### CommentSection.vue

**功能**：评论区组件，支持评论发布、回复、排序。

**Props**：
| 属性 | 类型 | 默认值 | 说明 |
|------|------|-------|------|
| comments | Array | [] | 评论列表 |
| isLoggedIn | Boolean | false | 是否登录 |
| userName | String | - | 用户名 |
| loading | Boolean | false | 加载状态 |
| total | Number | 0 | 总数 |
| page | Number | 1 | 当前页 |
| size | Number | 6 | 每页条数 |

**Events**：
| 事件 | 参数 | 说明 |
|------|------|------|
| post | (content, replyData, onSuccess, onError) | 发布评论 |
| reply | (content, replyData) | 回复评论 |
| page-change | page | 页码变化 |
| size-change | size | 每页条数变化 |

**功能**：
- 评论发布（最多500字）
- 回复评论
- 排序（最新/最早）
- 分页显示
- 时间格式化（刚刚、X分钟前、X小时前）

#### QuizSection.vue

**功能**：答题组件。

**功能**：
- 获取随机题目
- 提交答案
- 显示得分和解析

#### PlantGame.vue

**功能**：植物识别游戏组件。

**功能**：
- 植物图片识别
- 计分系统
- 游戏记录

### 8. business/layout/ 目录 - 布局组件

#### AppHeader.vue

**功能**：应用头部导航栏。

**功能**：
- Logo展示
- 导航菜单
- 用户信息
- 登录/登出

#### AppFooter.vue

**功能**：应用页脚。

**内容**：
- 版权信息
- 联系方式
- 相关链接

## 组件统计

| 目录 | 组件数 | 主要用途 |
|------|-------|---------|
| base/ | 2 | 基础组件（错误边界、虚拟列表） |
| business/admin/ | 13 | 管理后台组件 |
| business/dialogs/ | 5 | 详情对话框 |
| business/display/ | 8 | 展示组件 |
| business/interact/ | 4 | 交互组件 |
| business/layout/ | 2 | 布局组件 |
| business/media/ | 5 | 媒体组件 |
| business/upload/ | 4 | 上传组件 |
| common/ | 1 | 通用组件 |
| **总计** | **44** | - |

## 开发规范

1. **命名规范**：
   - 组件名使用大驼峰命名法
   - 文件名与组件名保持一致

2. **Props规范**：
   - 定义类型和默认值
   - 使用required标记必填项
   - 添加注释说明

3. **Events规范**：
   - 使用kebab-case命名
   - 使用emit触发事件
   - 传递有意义的参数

4. **Slots规范**：
   - 提供默认插槽
   - 命名插槽使用语义化名称

5. **样式规范**：
   - 使用scoped样式
   - 使用CSS变量
   - 支持响应式设计

---

**最后更新时间**：2026年3月25日
