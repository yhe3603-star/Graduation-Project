<template>
  <footer class="dong-footer">
    <div class="footer-content">
      <div class="footer-section">
        <h4>关于我们</h4>
        <p>侗乡医药非遗数字展示平台致力于保护和传承侗族医药文化，让更多人了解和认识这一珍贵的民族文化遗产。</p>
      </div>
      <div class="footer-section">
        <h4>快速导航</h4>
        <div class="footer-links">
          <router-link to="/knowledge">
            知识库
          </router-link>
          <router-link to="/inheritors">
            传承人
          </router-link>
          <router-link to="/plants">
            药用图鉴
          </router-link>
          <router-link to="/qa">
            问答社区
          </router-link>
        </div>
      </div>
      <div class="footer-section">
        <h4>联系方式</h4>
        <p>邮箱：1805369567qq.com</p>
        <p>地址：桂林电子科技大学</p>
      </div>
    </div>
    <div class="footer-bottom">
      <p>© 2026 侗乡医药非遗数字展示平台 | 保护和传承侗族医药文化</p>
    </div>
  </footer>
</template>

<style scoped>
.dong-footer { background: linear-gradient(135deg, var(--dong-blue), var(--dong-indigo-dark)); color: var(--text-inverse); padding: 40px 24px 20px; margin-top: auto; }
.footer-content { max-width: 1200px; margin: 0 auto; display: grid; grid-template-columns: 2fr 1fr 1fr; gap: 40px; }
.footer-section h4 { font-size: 16px; margin: 0 0 16px 0; padding-bottom: 8px; border-bottom: 2px solid var(--dong-green); }
.footer-section p { font-size: 13px; line-height: 1.8; opacity: 0.85; margin: 0; }
.footer-links { display: flex; flex-direction: column; gap: 8px; }
.footer-links a { color: rgba(255, 255, 255, 0.85); text-decoration: none; font-size: 13px; transition: color 0.2s; }
.footer-links a:hover { color: var(--dong-green); }
.footer-bottom { max-width: 1200px; margin: 24px auto 0; padding-top: 16px; border-top: 1px solid rgba(255, 255, 255, 0.1); text-align: center; }
.footer-bottom p { font-size: 12px; opacity: 0.7; margin: 0; }

@media (max-width: 768px) {
  .footer-content { grid-template-columns: 1fr; gap: 24px; }
}
</style>
