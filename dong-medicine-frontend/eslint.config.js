import js from '@eslint/js'
import vue from 'eslint-plugin-vue'
import vueParser from 'vue-eslint-parser'
import globals from 'globals'

export default [
  {
    ignores: ['dist/**', 'node_modules/**', '**/*_fixed.js']
  },
  js.configs.recommended,
  ...vue.configs['flat/recommended'],
  {
    files: ['**/*.{js,vue}'],
    languageOptions: {
      parser: vueParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module'
      },
      globals: {
        ...globals.browser,
        ...globals.node
      }
    },
    rules: {
      'no-console': 'off',
      'no-empty': 'off',
      'no-undef': 'off',
      'no-unused-vars': ['warn', { argsIgnorePattern: '^_' }],
      'vue/html-indent': 'off',
      'vue/max-attributes-per-line': 'off',
      'vue/require-explicit-emits': 'off',
      'vue/no-v-html': 'warn',
      'vue/multi-word-component-names': 'off',
      'vue/no-mutating-props': 'warn',
      'vue/require-default-prop': 'off'
    }
  }
]
