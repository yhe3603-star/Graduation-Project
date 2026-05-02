import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, ChatDotRound, Document, EditPen, Folder, Lock, Picture, Setting, Star, SwitchButton, User } from '@element-plus/icons-vue'
import { logFetchError, logOperationWarn, extractData } from '@/utils'
import { useUserStore } from '@/stores/user'
import { createPasswordValidator } from '@/utils/validators'

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
  const userStore = useUserStore()

  const isLoggedIn = computed(() => userStore.isLoggedIn)
  const userName = computed(() => userStore.userName || '')
  const isAdmin = computed(() => userStore.isAdmin)

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
  const passwordCaptchaRef = ref(null)
  const passwordLoading = ref(false)
  const logoutLoading = ref(false)
  const passwordForm = ref({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
    captchaKey: '',
    captchaCode: ''
  })

  const { password: newPasswordRules, confirmPassword: confirmPasswordRules } = createPasswordValidator()

  const passwordRules = {
    currentPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: newPasswordRules,
    confirmPassword: confirmPasswordRules(passwordFormRef, 'newPassword'),
    captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
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
    return [...quiz, ...game].sort((a, b) => new Date(b.createdAt || b.createTime) - new Date(a.createdAt || a.createTime))
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
        request.get('/favorites/my').catch(() => ({})),
        request.get('/quiz/records').catch(() => ({})),
        request.get('/plant-game/records').catch(() => ({})),
        request.get('/comments/my').catch(() => ({}))
      ])
      
      favorites.value = extractData(favRes)
      quizRecords.value = extractData(quizRes)
      gameRecords.value = extractData(gameRes)
      comments.value = extractData(commentRes)
    } catch (e) {
      logFetchError('个人中心数据', e)
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
      await request.post('/user/change-password', {
        currentPassword: passwordForm.value.currentPassword,
        newPassword: passwordForm.value.newPassword,
        captchaKey: passwordForm.value.captchaKey,
        captchaCode: passwordForm.value.captchaCode
      })
      ElMessage.success('密码修改成功，请重新登录')
      userStore.clearAuth()
      router.push('/')
    } catch (e) {
      logOperationWarn('修改密码')
      ElMessage.error(e.msg || '密码修改失败')
      passwordCaptchaRef.value?.refreshCaptcha()
    } finally {
      passwordLoading.value = false
    }
  }

  const resetPasswordForm = () => {
    passwordForm.value = { currentPassword: '', newPassword: '', confirmPassword: '', captchaKey: '', captchaCode: '' }
    passwordFormRef.value?.resetFields()
    passwordCaptchaRef.value?.refreshCaptcha()
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
      await userStore.logout()
    } finally {
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
    passwordCaptchaRef,
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
