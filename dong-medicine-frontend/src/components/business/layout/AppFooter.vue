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
.dong-footer {
  background:
    repeating-linear-gradient(
      45deg,
      transparent 0px,
      transparent 12px,
      rgba(255, 255, 255, 0.01) 12px,
      rgba(255, 255, 255, 0.01) 14px
    ),
    linear-gradient(135deg, #0a2a3f 0%, var(--dong-indigo) 40%, var(--dong-indigo-dark) 100%);
  color: var(--text-inverse);
  padding: 48px 24px 24px;
  margin-top: auto;
  position: relative;
}
.dong-footer::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg,
    var(--dong-jade) 0%,
    var(--dong-gold) 25%,
    var(--dong-indigo-light) 50%,
    var(--dong-gold) 75%,
    var(--dong-jade) 100%
  );
}
.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 48px;
}
.footer-section h4 {
  font-family: var(--font-display);
  font-size: 16px;
  margin: 0 0 16px 0;
  padding-bottom: 10px;
  position: relative;
}
.footer-section h4::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 32px;
  height: 2px;
  background: var(--dong-jade);
  border-radius: 1px;
}
.footer-section p { font-size: 13px; line-height: 1.8; opacity: 0.8; margin: 0; }
.footer-links { display: flex; flex-direction: column; gap: 10px; }
.footer-links a {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-size: 13px;
  transition: all 0.25s;
  padding: 2px 0;
}
.footer-links a:hover {
  color: var(--dong-jade-light);
  padding-left: 6px;
}
.footer-bottom {
  max-width: 1200px;
  margin: 32px auto 0;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  text-align: center;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.footer-bottom::before {
  content: '◆';
  font-size: 6px;
  color: var(--dong-gold);
  opacity: 0.5;
}
.footer-bottom::after {
  content: '◆';
  font-size: 6px;
  color: var(--dong-gold);
  opacity: 0.5;
}
.footer-bottom p { font-size: 12px; opacity: 0.6; margin: 0; }

@media (max-width: 768px) {
  .dong-footer { padding: 36px 20px 20px; }
  .footer-content { grid-template-columns: 1fr; gap: 28px; }
}
</style>
