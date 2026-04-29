/**
 * 共享表单验证规则工厂
 * 提取 App.vue 和 usePersonalCenter.js 中重复的密码验证逻辑
 */

/**
 * 创建密码验证规则集
 * @param {Object} options - 配置选项
 * @param {number} [options.minLength=8] - 密码最小长度
 * @param {number} [options.maxLength=50] - 密码最大长度
 * @returns {{ password: Array, confirmPassword: (formRef: Ref, fieldName?: string) => Array }}
 */
export function createPasswordValidator(options = {}) {
  const { minLength = 8, maxLength = 50 } = options

  return {
    /** 密码字段验证规则 */
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' },
      { min: minLength, max: maxLength, message: `密码长度为${minLength}-${maxLength}位`, trigger: 'blur' },
      {
        validator: (_, value, callback) => {
          if (!value) return callback()
          if (!/[a-zA-Z]/.test(value)) {
            callback(new Error('密码必须包含字母'))
          } else if (!/[0-9]/.test(value)) {
            callback(new Error('密码必须包含数字'))
          } else if (/\s/.test(value)) {
            callback(new Error('密码不能包含空格'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ],

    /**
     * 确认密码验证规则（工厂方法）
     * @param {Ref} formRef - 表单响应式引用
     * @param {string} [fieldName='newPassword'] - 密码字段名
     * @returns {Array} 确认密码验证规则数组
     */
    confirmPassword: (formRef, fieldName = 'newPassword') => [
      { required: true, message: '请确认密码', trigger: 'blur' },
      {
        validator: (_, value, callback) => {
          if (!value) return callback()
          const form = formRef.value
          if (form && value !== form[fieldName]) {
            callback(new Error('两次输入的密码不一致'))
          } else {
            callback()
          }
        },
        trigger: 'blur'
      }
    ]
  }
}
