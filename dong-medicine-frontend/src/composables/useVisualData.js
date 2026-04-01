import { ref } from 'vue';
import { extractPageData, logFetchError } from '@/utils';
import { COLOR_PALETTE } from '@/utils/chartConfig';

const PROVINCE_MAP = {
  '贵州': '贵州省', '广西': '广西壮族自治区', '湖南': '湖南省', '云南': '云南省',
  '四川': '四川省', '甘肃': '甘肃省', '安徽': '安徽省', '广东': '广东省',
  '湖北': '湖北省', '江西': '江西省', '浙江': '浙江省', '福建': '福建省',
  '河南': '河南省', '陕西': '陕西省', '重庆': '重庆市', '海南': '海南省',
  '西藏': '西藏自治区', '新疆': '新疆维吾尔自治区', '内蒙古': '内蒙古自治区',
  '宁夏': '宁夏回族自治区', '青海': '青海省', '山西': '山西省', '河北': '河北省',
  '山东': '山东省', '江苏': '江苏省', '辽宁': '辽宁省', '吉林': '吉林省', '黑龙江': '黑龙江省'
};

const extractProvince = (location) => {
  if (!location || typeof location !== 'string') return null;
  for (const [key, value] of Object.entries(PROVINCE_MAP)) {
    if (location.startsWith(key) || location.includes(key)) return value;
  }
  return null;
};

const countBy = (arr, key, defaultVal = '其他') => {
  const map = {};
  arr.forEach(item => {
    const k = item[key] || defaultVal;
    map[k] = (map[k] || 0) + 1;
  });
  return map;
};

const parseLocations = (distribution) => {
  if (Array.isArray(distribution)) return distribution;
  if (typeof distribution === 'string') {
    try {
      const parsed = JSON.parse(distribution);
      return Array.isArray(parsed) ? parsed : [distribution];
    } catch {
      return [distribution];
    }
  }
  return [];
};

export function useVisualData(request) {
  const loading = ref(false);
  const stats = ref({ plants: 0, knowledge: 0, inheritors: 0, qa: 0, resources: 0 });
  const chartData = ref({
    therapyCategories: [],
    inheritorLevels: [],
    plantCategories: [],
    plantCategoryNames: [],
    qaCategories: [],
    qaCategoryNames: [],
    plantDistribution: [],
    knowledgePopularity: [],
    formulaFreq: [],
    formulaNames: [],
    userTrend: [],
    userTrendDates: []
  });
  const regionList = ref([]);

  const fetchData = async () => {
    loading.value = true;
    try {
      const [pRes, kRes, iRes, qRes, rRes] = await Promise.all([
        request.get('/plants/list', { params: { page: 1, size: 999 } }).catch(() => ({})),
        request.get('/knowledge/list', { params: { page: 1, size: 999 } }).catch(() => ({})),
        request.get('/inheritors/list', { params: { page: 1, size: 999 } }).catch(() => ({})),
        request.get('/qa/list', { params: { page: 1, size: 999 } }).catch(() => ({})),
        request.get('/resources/list', { params: { page: 1, size: 999 } }).catch(() => ({}))
      ]);

      const plantsData = extractPageData(pRes);
      const knowledgeData = extractPageData(kRes);
      const inheritorsData = extractPageData(iRes);
      const qaData = extractPageData(qRes);
      const resourcesData = extractPageData(rRes);

      const plants = plantsData.records || [];
      const knowledge = knowledgeData.records || [];
      const inheritors = inheritorsData.records || [];
      const qa = qaData.records || [];

      stats.value = {
        plants: plantsData.total || 0,
        knowledge: knowledgeData.total || 0,
        inheritors: inheritorsData.total || 0,
        qa: qaData.total || 0,
        resources: resourcesData.total || 0
      };

      const therapyMap = countBy(knowledge, 'therapyCategory');
      chartData.value.therapyCategories = Object.entries(therapyMap)
        .sort((a, b) => b[1] - a[1]).slice(0, 8)
        .map(([name, value], i) => ({ value, name, itemStyle: { color: COLOR_PALETTE[i % COLOR_PALETTE.length] } }));

      const levelMap = { '国家级': 0, '省级': 0, '市级': 0, '县级': 0 };
      inheritors.forEach(item => { if (levelMap[item.level] !== undefined) levelMap[item.level]++; });
      chartData.value.inheritorLevels = Object.values(levelMap);

      const categoryMap = countBy(plants, 'category');
      const categoryEntries = Object.entries(categoryMap).sort((a, b) => b[1] - a[1]).slice(0, 8);
      chartData.value.plantCategoryNames = categoryEntries.map(([name]) => name);
      chartData.value.plantCategories = categoryEntries.map(([, value]) => value);

      const qaMap = {};
      qa.forEach(q => {
        const cat = q.category || '其他';
        qaMap[cat] = (qaMap[cat] || 0) + (q.popularity || 0);
      });
      const qaEntries = Object.entries(qaMap).sort((a, b) => b[1] - a[1]).slice(0, 6);
      chartData.value.qaCategoryNames = qaEntries.map(([name]) => name);
      chartData.value.qaCategories = qaEntries.map(([, value]) => value);

      const distributionMap = {};
      plants.forEach(p => {
        const locations = parseLocations(p.distribution);
        const counted = new Set();
        locations.forEach(loc => {
          const province = extractProvince(loc);
          if (province && !counted.has(province)) {
            distributionMap[province] = (distributionMap[province] || 0) + 1;
            counted.add(province);
          }
        });
      });
      chartData.value.plantDistribution = Object.entries(distributionMap)
        .map(([name, value]) => ({ name, value }))
        .sort((a, b) => b.value - a.value).slice(0, 15);
      regionList.value = [...new Set(chartData.value.plantDistribution.map(d => d.name))];

      const popularityMap = {};
      knowledge.forEach(k => {
        if (k.popularity > 0) popularityMap[k.title || '未知'] = k.popularity;
      });
      chartData.value.knowledgePopularity = Object.entries(popularityMap)
        .map(([name, value]) => ({ name: name.length > 12 ? name.substring(0, 12) + '...' : name, value }))
        .sort((a, b) => b.value - a.value).slice(0, 10);

      const formulaMap = {};
      knowledge.forEach(k => {
        if (k.type === '药方' || k.title?.includes('方')) {
          formulaMap[k.title || '未知'] = (formulaMap[k.title || '未知'] || 0) + (k.popularity || Math.floor(Math.random() * 50) + 10);
        }
      });
      const formulaEntries = Object.entries(formulaMap).sort((a, b) => b[1] - a[1]).slice(0, 8);
      chartData.value.formulaNames = formulaEntries.map(([name]) => name.length > 8 ? name.substring(0, 8) + '...' : name);
      chartData.value.formulaFreq = formulaEntries.map(([, value]) => value);

      if (chartData.value.formulaNames.length === 0) {
        const popular = knowledge.filter(k => k.popularity > 0).sort((a, b) => b.popularity - a.popularity).slice(0, 8);
        chartData.value.formulaNames = popular.map(k => k.title?.substring(0, 8) || '未知');
        chartData.value.formulaFreq = popular.map(k => k.popularity);
      }

      try {
        const trendRes = await request.get('/stats/trend');
        if (trendRes.data?.dates && trendRes.data?.counts) {
          chartData.value.userTrendDates = trendRes.data.dates;
          chartData.value.userTrend = trendRes.data.counts;
        }
      } catch {
        const dates = [];
        const data = [];
        for (let i = 6; i >= 0; i--) {
          const d = new Date();
          d.setDate(d.getDate() - i);
          dates.push(`${d.getMonth() + 1}/${d.getDate()}`);
          data.push(0);
        }
        chartData.value.userTrendDates = dates;
        chartData.value.userTrend = data;
      }
    } catch (e) {
      logFetchError('可视化数据', e);
    } finally {
      loading.value = false;
    }
  };

  return { loading, stats, chartData, regionList, fetchData };
}
