import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, ChatDotRound, Document, EditPen, Folder, Lock, Picture, Setting, Star, SwitchButton, User } from '@element-plus/icons-vue'

export const actions = [
  { key: 'favorites', icon: Star, label: '我的收藏' },
  { key: 'quiz', icon: EditPen, label: '答题记录' },
  { key: 'comments', icon: ChatDotRound, label: '我的评论' },
  { key: 'settings', icon: Setting, label: '账号设置' }
]

export const typeIconMap = {
  plant: Picture,
  knowledge: Document,
  inheritor: User,
  resource: Folder,
  qa: ChatDotRound
}

export const typeTagMap = {
  plant: 'success',
  knowledge: 'primary',
  inheritor: 'warning',
  resource: 'info',
  qa: ''
}

export const typeNameMap = {
  plant: '药材',
  knowledge: '知识',
  inheritor: '传承人',
  resource: '资源',
  qa: '问答'
}

export function usePersonalCenter(request, updateUserState) {
  const router = useRouter()

  const isLoggedIn = computed(() => !!localStorage.getItem('token'))
  const userName = computed(() => localStorage.getItem('userName') || '')
  const isAdmin = computed(() => {
    const role = localStorage.getItem('role') || ''
    return role === 'ADMIN' || role === 'admin'
  })

  const pageLoading = ref(false)
  const activeTab = ref('favorites')
  const favoriteType = ref('all')
  const favorites = ref([])
  const quizRecords = ref([])
  const gameRecords = ref([])
  const comments = ref([])

  const favPage = ref(1)
  const favPageSize = ref(6)
  const quizPage = ref(1)
  const quizPageSize = ref(6)
  const commentPage = ref(1)
  const commentPageSize = ref(6)

  const passwordFormRef = ref(null)
  const passwordLoading = ref(false)
  const logoutLoading = ref(false)
  const passwordForm = ref({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  const passwordRules = {
    currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
    ],
    confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }]
  }

  function validateConfirmPassword(rule, value, callback) {
    if (value === '') callback(new Error('请再次输入新密码'))
    else if (value !== passwordForm.value.newPassword) callback(new Error('两次输入的密码不一致'))
    else callback()
  }

  const filteredFavorites = computed(() => {
    if (favoriteType.value === 'all') return favorites.value
    return favorites.value.filter(f => f.type === favoriteType.value)
  })

  const paginatedFavorites = computed(() => {
    const start = (favPage.value - 1) * favPageSize.value
    return filteredFavorites.value.slice(start, start + favPageSize.value)
  })

  const allRecords = computed(() => {
    const quiz = quizRecords.value.map(r => ({ ...r, type: 'quiz' }))
    const game = gameRecords.value.map(r => ({ ...r, type: 'game' }))
    return [...quiz, ...game].sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
  })

  const paginatedRecords = computed(() => {
    const start = (quizPage.value - 1) * quizPageSize.value
    return allRecords.value.slice(start, start + quizPageSize.value)
  })

  const paginatedComments = computed(() => {
    const start = (commentPage.value - 1) * commentPageSize.value
    return comments.value.slice(start, start + commentPageSize.value)
  })

  const fetchUserData = async () => {
    if (!isLoggedIn.value) return
    pageLoading.value = true
    try {
      const [favRes, quizRes, gameRes, commentRes] = await Promise.all([
        request.get('/favorites/list').catch(() => ({})),
        request.get('/quiz/records').catch(() => ({})),
        request.get('/game/records').catch(() => ({})),
        request.get('/comments/user').catch(() => ({}))
      ])
      
      const extractData = (res) => res?.data?.data || res?.data || []
      favorites.value = extractData(favRes)
      quizRecords.value = extractData(quizRes)
      gameRecords.value = extractData(gameRes)
      comments.value = extractData(commentRes)
    } catch {
      ElMessage.error('数据加载失败')
    } finally {
      pageLoading.value = false
    }
  }

  const goToDetail = (item) => {
    const pathMap = {
      plant: '/plants',
      knowledge: '/knowledge',
      inheritor: '/inheritors',
      resource: '/resources',
      qa: '/qa'
    }
    router.push(`${pathMap[item.type] || ''}/${item.targetId || item.id}`)
  }

  const handleChangePassword = async () => {
    if (!passwordFormRef.value) return
    try {
      await passwordFormRef.value.validate()
      passwordLoading.value = true
      await request.put('/user/password', {
        currentPassword: passwordForm.value.currentPassword,
        newPassword: passwordForm.value.newPassword
      })
      ElMessage.success('密码修改成功，请重新登录')
      handleLogout()
    } catch {
      ElMessage.error('密码修改失败')
    } finally {
      passwordLoading.value = false
    }
  }

  const resetPasswordForm = () => {
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '' }
    passwordFormRef.value?.resetFields()
  }

  const handleLogout = async () => {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
    logoutLoading.value = true
    try {
      await request.post('/user/logout').catch(() => {})
    } finally {
      localStorage.removeItem('token')
      localStorage.removeItem('userName')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
      localStorage.removeItem('userInfo')
      updateUserState()
      ElMessage.success('已退出登录')
      router.push('/')
    }
  }

  const formatTime = (time) => {
    if (!time) return '-'
    return new Date(time).toLocaleString('zh-CN')
  }

  const getDifficultyName = (d) => ({ easy: '简单', medium: '中等', hard: '困难' }[d] || d)

  const getScoreClass = (score) => {
    if (score >= 80) return 'score-high'
    if (score >= 60) return 'score-medium'
    return 'score-low'
  }

  const getTypeIcon = (type) => typeIconMap[type] || Document
  const getTypeTag = (type) => typeTagMap[type] || 'info'
  const getTypeName = (type) => typeNameMap[type] || type

  onMounted(fetchUserData)

  return {
    isLoggedIn,
    userName,
    isAdmin,
    pageLoading,
    activeTab,
    favoriteType,
    favorites,
    quizRecords,
    gameRecords,
    comments,
    favPage,
    favPageSize,
    quizPage,
    quizPageSize,
    commentPage,
    commentPageSize,
    passwordFormRef,
    passwordLoading,
    logoutLoading,
    passwordForm,
    passwordRules,
    filteredFavorites,
    paginatedFavorites,
    allRecords,
    paginatedRecords,
    paginatedComments,
    fetchUserData,
    goToDetail,
    handleChangePassword,
    resetPasswordForm,
    handleLogout,
    formatTime,
    getDifficultyName,
    getScoreClass,
    getTypeIcon,
    getTypeTag,
    getTypeName
  }
}
