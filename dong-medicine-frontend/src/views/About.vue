<template>
  <div class="about-page module-page">
    <div class="about-hero">
      <div class="hero-bg" />
      <div class="hero-content">
        <div class="hero-badge">
          广西壮族自治区级非物质文化遗产
        </div>
        <h1>侗乡医药</h1>
        <p class="hero-subtitle">
          三江侗族自治县 · 传统医药
        </p>
        <p class="hero-desc">
          保护 · 传承 · 活态传播
        </p>
      </div>
    </div>

    <div class="about-content">
      <section class="about-section">
        <h2 class="section-title">
          <el-icon><Document /></el-icon> 选题背景
        </h2>
        <div class="section-content">
          <p>侗乡医药作为三江侗族自治县自治区级非物质文化遗产，承载着侗族独特的诊疗理念、药用资源与炮制技艺，现有 <strong>10 名各级传承人</strong>，其鼻炎疗法、药浴疗法等传统智慧兼具文化价值与实用价值。</p>
          <p>从非遗保护视角来看，当前侗乡医药面临传承危机：知识体系依赖口传心授，缺乏标准化数字化归档；传播渠道狭窄，公众认知度低，年轻群体接触途径匮乏，非遗活态传承受阻。</p>
          <p>本平台聚焦「非遗活态传承」核心目标，通过标准化数据整理、沉浸式展示与便捷传播，为小众非遗提供可复制的数字化保护方案。</p>
        </div>
      </section>

      <section class="about-section">
        <h2 class="section-title">
          <el-icon><Aim /></el-icon> 平台特色
        </h2>
        <div class="feature-grid">
          <div
            v-for="(f, i) in features"
            :key="i"
            class="feature-card"
          >
            <div
              class="feature-icon"
              :style="{ background: f.color }"
            >
              <el-icon :size="28">
                <component :is="f.icon" />
              </el-icon>
            </div>
            <h3>{{ f.title }}</h3>
            <p>{{ f.desc }}</p>
          </div>
        </div>
      </section>

      <section class="about-section">
        <h2 class="section-title">
          <el-icon><Grid /></el-icon> 功能模块
        </h2>
        <div class="module-grid">
          <div
            v-for="(m, i) in modules"
            :key="i"
            class="module-item"
            @click="$router.push(m.path)"
          >
            <div class="module-icon">
              <el-icon :size="24">
                <component :is="m.icon" />
              </el-icon>
            </div>
            <div class="module-info">
              <h4>{{ m.title }}</h4><p>{{ m.desc }}</p>
            </div>
            <el-icon class="module-arrow">
              <ArrowRight />
            </el-icon>
          </div>
        </div>
      </section>

      <section class="about-section">
        <h2 class="section-title">
          <el-icon><DataLine /></el-icon> 平台数据
        </h2>
        <div class="stats-grid">
          <div
            v-for="(s, i) in statsList"
            :key="i"
            class="stat-card"
          >
            <div class="stat-num">
              {{ s.value }}
            </div>
            <div class="stat-label">
              {{ s.label }}
            </div>
          </div>
        </div>
      </section>

      <section class="about-section">
        <h2 class="section-title">
          <el-icon><Collection /></el-icon> 侗医文化
        </h2>
        <div class="culture-grid">
          <div
            v-for="(c, i) in cultures"
            :key="i"
            class="culture-card"
          >
            <div class="culture-icon">
              <el-icon :size="32">
                <component :is="c.icon" />
              </el-icon>
            </div>
            <h4>{{ c.title }}</h4>
            <p>{{ c.desc }}</p>
          </div>
        </div>
      </section>

      <section class="about-section compliance-section">
        <h2 class="section-title">
          <el-icon><Warning /></el-icon> 合规与声明
        </h2>
        <div class="compliance-grid">
          <div class="compliance-card">
            <div class="compliance-icon privacy">
              <el-icon :size="24"><Lock /></el-icon>
            </div>
            <h4>隐私保护</h4>
            <p>本平台严格保护传承人及用户隐私，传承人联系方式不予公开展示。用户个人信息仅用于平台功能服务，不会向第三方泄露。</p>
          </div>
          <div class="compliance-card">
            <div class="compliance-icon medical">
              <el-icon :size="24"><FirstAidKit /></el-icon>
            </div>
            <h4>医疗免责声明</h4>
            <p>本平台所有医药知识内容仅供科普学习，不构成医疗建议。如有健康问题，请前往正规医疗机构就诊，切勿自行用药。</p>
          </div>
          <div class="compliance-card">
            <div class="compliance-icon copyright">
              <el-icon :size="24"><Document /></el-icon>
            </div>
            <h4>版权声明</h4>
            <p>平台图片、视频及文档资料均标注来源，尊重原作者知识产权。如涉及版权问题，请通过反馈渠道联系我们处理。</p>
          </div>
          <div class="compliance-card">
            <div class="compliance-icon sustainable">
              <el-icon :size="24"><RefreshRight /></el-icon>
            </div>
            <h4>可持续运营</h4>
            <p>平台设有内容更新日志与后台运营管理功能，确保知识内容的持续维护与更新迭代，保障非遗数字资源的长期可用性。</p>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, inject, onMounted, ref } from "vue";
import { Aim, ArrowRight, ChatDotRound, Collection, DataLine, Document, FirstAidKit, Grid, Lock, MagicStick, Picture, RefreshRight, Sunny, User, Warning, Folder, Opportunity } from "@element-plus/icons-vue";
import { extractPageData } from "@/utils";
import { logFetchError } from '@/utils/logger';

const request = inject("request");
const pageLoading = ref(false);
const stats = ref({ plants: 0, formulas: 12, therapies: 6, inheritors: 0, qa: 0, resources: 0 });

const features = computed(() => [
  { icon: Collection, title: "知识体系化", desc: `${stats.value.formulas}+经典药方、${stats.value.therapies}类核心疗法、${stats.value.plants}种药用植物`, color: "linear-gradient(135deg, #667eea, #764ba2)" },
  { icon: User, title: "传承人风采", desc: "省级、自治区级、州级传承人档案", color: "linear-gradient(135deg, #11998e, #38ef7d)" },
  { icon: Picture, title: "图文联动", desc: "药用植物图鉴、疗法视频、传承人影像", color: "linear-gradient(135deg, #f5af19, #f12711)" },
  { icon: ChatDotRound, title: "互动传播", desc: "趣味答题、植物识别游戏、评论交流", color: "linear-gradient(135deg, #f093fb, #f5576c)" }
]);

const modules = [
  { title: "知识库", desc: "侗医药理论知识", path: "/knowledge", icon: Document },
  { title: "传承人", desc: "非遗传承人档案", path: "/inheritors", icon: User },
  { title: "药用图鉴", desc: "道地药材详解", path: "/plants", icon: Picture },
  { title: "问答社区", desc: "互动答疑解惑", path: "/qa", icon: ChatDotRound },
  { title: "学习资源", desc: "视频文档资料", path: "/resources", icon: Folder },
  { title: "文化互动", desc: "趣味答题游戏", path: "/interact", icon: Aim },
  { title: "数据可视化", desc: "统计分析展示", path: "/visual", icon: DataLine }
];

const statsList = computed(() => [
  { value: stats.value.plants, label: "种药用植物" },
  { value: stats.value.formulas, label: "项经典药方" },
  { value: stats.value.therapies, label: "类核心疗法" },
  { value: stats.value.inheritors, label: "名传承人" },
  { value: stats.value.qa, label: "条问答知识" },
  { value: stats.value.resources, label: "项学习资源" }
]);

const cultures = [
  { icon: Opportunity, title: "就地取材", desc: "侗医用药多取自当地山林田野，强调道地药材的药效" },
  { icon: Sunny, title: "内外兼治", desc: "结合内服药物与外治疗法，形成独特诊疗体系" },
  { icon: MagicStick, title: "口传心授", desc: "传承人通过师徒制传授技艺，保护非遗传承谱系" },
  { icon: FirstAidKit, title: "整体观念", desc: "注重人体与自然和谐统一，强调预防与调理并重" }
];

const fetchData = async () => {
  try {
    const [pRes, iRes, qRes, rRes] = await Promise.all([
      request.get("/plants/list", { params: { page: 1, size: 1 } }).catch(() => ({})),
      request.get("/inheritors/list", { params: { page: 1, size: 1 } }).catch(() => ({})),
      request.get("/qa/list", { params: { page: 1, size: 1 } }).catch(() => ({})),
      request.get("/resources/list", { params: { page: 1, size: 1 } }).catch(() => ({}))
    ])
    stats.value.plants = extractPageData(pRes).total
    stats.value.inheritors = extractPageData(iRes).total
    stats.value.qa = extractPageData(qRes).total
    stats.value.resources = extractPageData(rRes).total
  } catch (e) {
    logFetchError('关于页面', e)
  } finally {}
};

onMounted(async () => {
  pageLoading.value = true;
  try {
    await fetchData();
  } finally {
    pageLoading.value = false;
  }
});
</script>

<style scoped>
.about-hero {
  position: relative;
  padding: var(--space-4xl) var(--space-3xl);
  border-radius: var(--radius-2xl);
  margin-bottom: var(--space-2xl);
  overflow: hidden;
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  color: var(--text-inverse);
  text-align: center;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.hero-badge {
  display: inline-block;
  padding: var(--space-sm) var(--space-lg);
  background: rgba(255, 255, 255, 0.2);
  border-radius: var(--radius-full);
  font-size: var(--font-size-xs);
  margin-bottom: var(--space-lg);
}

.about-hero h1 {
  font-size: var(--font-size-4xl);
  margin: 0 0 var(--space-md) 0;
}

.hero-subtitle {
  font-size: var(--font-size-xl);
  opacity: 0.9;
  margin: 0 auto var(--space-sm);
  max-width: 600px;
}

.hero-desc {
  font-size: var(--font-size-sm);
  opacity: 0.7;
  margin: 0;
}

.about-content {
  display: flex;
  flex-direction: column;
  gap: var(--space-2xl);
}

.about-section {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-2xl);
  box-shadow: var(--shadow-sm);
}

.section-title {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: var(--font-size-xl);
  color: var(--dong-indigo);
  margin: 0 0 var(--space-xl) 0;
  padding-left: var(--space-md);
  border-left: 4px solid var(--dong-jade);
}

.section-content p {
  font-size: var(--font-size-md);
  line-height: var(--line-height-relaxed);
  color: var(--text-secondary);
  margin: 0 0 var(--space-md) 0;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
}

.feature-card {
  text-align: center;
  padding: var(--space-xl);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
}

.feature-icon {
  width: 56px;
  height: 56px;
  margin: 0 auto var(--space-lg);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.feature-card h3 {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-md);
}

.feature-card p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-muted);
}

.module-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: var(--space-lg);
}

.module-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-lg);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.module-item:hover {
  background: rgba(26, 82, 118, 0.08);
}

.module-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, var(--dong-indigo), var(--dong-indigo-dark));
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-inverse);
}

.module-info {
  flex: 1;
}

.module-info h4 {
  margin: 0 0 var(--space-xs) 0;
  font-size: var(--font-size-md);
}

.module-info p {
  margin: 0;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.module-arrow {
  color: var(--text-light);
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: var(--space-lg);
}

.stat-card {
  text-align: center;
  padding: var(--space-xl);
  background: linear-gradient(135deg, rgba(26, 82, 118, 0.05), rgba(40, 180, 99, 0.05));
  border-radius: var(--radius-md);
}

.stat-num {
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  color: var(--dong-indigo);
}

.stat-label {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  margin-top: var(--space-xs);
}

.culture-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
}

.culture-card {
  text-align: center;
  padding: var(--space-xl);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
}

.culture-icon {
  color: var(--dong-jade);
  margin-bottom: var(--space-md);
}

.culture-card h4 {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-md);
  color: var(--dong-indigo);
}

.culture-card p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
}

@media (max-width: 1024px) {
  .feature-grid,
  .culture-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .stats-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 640px) {
  .feature-grid,
  .culture-grid,
  .module-grid,
  .stats-grid,
  .compliance-grid {
    grid-template-columns: 1fr;
  }
  .about-hero {
    padding: var(--space-2xl) var(--space-xl);
  }
  .about-hero h1 {
    font-size: var(--font-size-2xl);
  }
}

.compliance-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-xl);
}

.compliance-card {
  text-align: center;
  padding: var(--space-xl);
  background: var(--bg-rice);
  border-radius: var(--radius-md);
  border-top: 3px solid transparent;
}

.compliance-card .privacy { background: linear-gradient(135deg, #667eea, #764ba2); }
.compliance-card .medical { background: linear-gradient(135deg, #e74c3c, #c0392b); }
.compliance-card .copyright { background: linear-gradient(135deg, #f39c12, #e67e22); }
.compliance-card .sustainable { background: linear-gradient(135deg, #27ae60, #2ecc71); }

.compliance-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto var(--space-md);
}

.compliance-card h4 {
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-md);
  color: var(--dong-indigo);
}

.compliance-card p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-relaxed);
}

@media (max-width: 1024px) {
  .compliance-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
