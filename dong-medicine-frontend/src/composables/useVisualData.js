import { ref } from 'vue';
import { logFetchError } from '@/utils';
import { COLOR_PALETTE } from '@/utils/chartConfig';

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
    formulaNames: []
  });
  const regionList = ref([]);

  const fetchData = async () => {
    loading.value = true;
    try {
      const res = await request.get('/stats/chart');
      const data = res.data || res || {};

      if (data.counts) {
        stats.value = {
          plants: data.counts.plants || 0,
          knowledge: data.counts.knowledge || 0,
          inheritors: data.counts.inheritors || 0,
          qa: data.counts.qa || 0,
          resources: data.counts.resources || 0
        };
      }

      if (data.therapyCategories) {
        chartData.value.therapyCategories = data.therapyCategories.map((item, i) => ({
          value: item.value,
          name: item.name,
          itemStyle: { color: COLOR_PALETTE[i % COLOR_PALETTE.length] }
        }));
      }

      if (data.inheritorLevels) {
        chartData.value.inheritorLevels = data.inheritorLevels;
      }

      if (data.plantCategoryNames) {
        chartData.value.plantCategoryNames = data.plantCategoryNames;
      }
      if (data.plantCategories) {
        chartData.value.plantCategories = data.plantCategories;
      }

      if (data.qaCategoryNames) {
        chartData.value.qaCategoryNames = data.qaCategoryNames;
      }
      if (data.qaCategories) {
        chartData.value.qaCategories = data.qaCategories;
      }

      if (data.plantDistribution) {
        chartData.value.plantDistribution = data.plantDistribution;
        regionList.value = [...new Set(data.plantDistribution.map(d => d.name))];
      }

      if (data.knowledgePopularity) {
        chartData.value.knowledgePopularity = data.knowledgePopularity.map(item => ({
          name: item.name?.length > 12 ? item.name.substring(0, 12) + '...' : item.name,
          value: item.value
        }));
      }

      if (data.formulaNames) {
        chartData.value.formulaNames = data.formulaNames;
      }
      if (data.formulaFreq) {
        chartData.value.formulaFreq = data.formulaFreq;
      }

    } catch (e) {
      logFetchError('可视化数据', e);
    } finally {
      loading.value = false;
    }
  };

  return { loading, stats, chartData, regionList, fetchData };
}
