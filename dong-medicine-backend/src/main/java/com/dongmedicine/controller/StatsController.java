package com.dongmedicine.controller;

import com.dongmedicine.common.R;
import com.dongmedicine.mapper.InheritorMapper;
import com.dongmedicine.mapper.KnowledgeMapper;
import com.dongmedicine.mapper.PlantMapper;
import com.dongmedicine.mapper.QaMapper;
import com.dongmedicine.mapper.ResourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final PlantMapper plantMapper;
    private final KnowledgeMapper knowledgeMapper;
    private final InheritorMapper inheritorMapper;
    private final QaMapper qaMapper;
    private final ResourceMapper resourceMapper;

    private static final Map<String, String> PROVINCE_MAP = Map.ofEntries(
        Map.entry("贵州", "贵州省"), Map.entry("广西", "广西壮族自治区"), Map.entry("湖南", "湖南省"),
        Map.entry("云南", "云南省"), Map.entry("四川", "四川省"), Map.entry("甘肃", "甘肃省"),
        Map.entry("安徽", "安徽省"), Map.entry("广东", "广东省"), Map.entry("湖北", "湖北省"),
        Map.entry("江西", "江西省"), Map.entry("浙江", "浙江省"), Map.entry("福建", "福建省"),
        Map.entry("河南", "河南省"), Map.entry("陕西", "陕西省"), Map.entry("重庆", "重庆市"),
        Map.entry("海南", "海南省"), Map.entry("西藏", "西藏自治区"), Map.entry("新疆", "新疆维吾尔自治区"),
        Map.entry("内蒙古", "内蒙古自治区"), Map.entry("宁夏", "宁夏回族自治区"),
        Map.entry("青海", "青海省"), Map.entry("山西", "山西省"), Map.entry("河北", "河北省"),
        Map.entry("山东", "山东省"), Map.entry("江苏", "江苏省"), Map.entry("辽宁", "辽宁省"),
        Map.entry("吉林", "吉林省"), Map.entry("黑龙江", "黑龙江省")
    );

    @GetMapping("/chart")
    public R<Map<String, Object>> getChartData() {
        Map<String, Object> data = new LinkedHashMap<>();

        data.put("counts", getCounts());

        data.put("therapyCategories", knowledgeMapper.countByTherapyCategory(8));

        Map<String, Integer> levelMap = new LinkedHashMap<>();
        levelMap.put("国家级", inheritorMapper.countByLevel("国家级"));
        levelMap.put("省级", inheritorMapper.countByLevel("省级"));
        levelMap.put("市级", inheritorMapper.countByLevel("市级"));
        levelMap.put("县级", inheritorMapper.countByLevel("县级"));
        data.put("inheritorLevels", levelMap.values().stream().toList());

        List<Map<String, Object>> plantCategories = plantMapper.countByCategory(8);
        data.put("plantCategoryNames", plantCategories.stream().map(m -> m.get("name").toString()).toList());
        data.put("plantCategories", plantCategories.stream().map(m -> ((Number) m.get("value")).longValue()).toList());

        List<Map<String, Object>> qaCategories = qaMapper.topCategoryByPopularity(6);
        data.put("qaCategoryNames", qaCategories.stream().map(m -> m.get("name").toString()).toList());
        data.put("qaCategories", qaCategories.stream().map(m -> ((Number) m.get("value")).longValue()).toList());

        data.put("plantDistribution", buildPlantDistribution());

        data.put("knowledgePopularity", knowledgeMapper.topByPopularity(10));

        List<Map<String, Object>> formulaData = knowledgeMapper.topFormulaByViewCount(8);
        if (formulaData.isEmpty()) {
            formulaData = knowledgeMapper.topByViewCount(8);
        }
        data.put("formulaNames", formulaData.stream().map(m -> truncate(m.get("name").toString(), 8)).toList());
        data.put("formulaFreq", formulaData.stream().map(m -> ((Number) m.get("value")).longValue()).toList());

        return R.ok(data);
    }

    private Map<String, Long> getCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("plants", (long) plantMapper.selectCount(null));
        counts.put("knowledge", (long) knowledgeMapper.selectCount(null));
        counts.put("inheritors", (long) inheritorMapper.selectCount(null));
        counts.put("qa", (long) qaMapper.selectCount(null));
        counts.put("resources", (long) resourceMapper.selectCount(null));
        return counts;
    }

    private List<Map<String, Object>> buildPlantDistribution() {
        List<Map<String, Object>> rawDistribution = plantMapper.countByDistribution(100);
        Map<String, Long> provinceMap = new LinkedHashMap<>();

        for (Map<String, Object> entry : rawDistribution) {
            String distribution = entry.get("name").toString();
            long count = ((Number) entry.get("value")).longValue();

            String province = extractProvince(distribution);
            if (province != null) {
                provinceMap.merge(province, count, Long::sum);
            }
        }

        return provinceMap.entrySet().stream()
            .map(e -> Map.<String, Object>of("name", e.getKey(), "value", e.getValue()))
            .sorted((a, b) -> Long.compare(((Number) b.get("value")).longValue(), ((Number) a.get("value")).longValue()))
            .limit(15)
            .toList();
    }

    private String extractProvince(String location) {
        if (location == null) return null;
        for (Map.Entry<String, String> entry : PROVINCE_MAP.entrySet()) {
            if (location.startsWith(entry.getKey()) || location.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String truncate(String str, int maxLen) {
        return str.length() > maxLen ? str.substring(0, maxLen) + "..." : str;
    }
}
