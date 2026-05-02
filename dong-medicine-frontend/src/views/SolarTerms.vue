<template>
  <div class="solar-terms-page module-page">
    <div class="module-header">
      <h1>节气采药</h1>
      <p class="subtitle">
二十四节气与侗医药采集时令 — 顺应天时，药效更佳
</p>
    </div>

    <div class="season-tabs">
      <div
        v-for="season in seasons"
        :key="season.key"
        class="season-tab"
        :class="{ active: activeSeason === season.key }"
        :style="{ '--season-color': season.color, '--season-bg': season.bg }"
        @click="activeSeason = season.key"
      >
        <div class="season-icon">
          <el-icon :size="28">
<component :is="season.icon" />
</el-icon>
        </div>
        <span class="season-name">{{ season.label }}</span>
        <span class="season-count">{{ getSeasonTerms(season.key).length }}节气</span>
      </div>
    </div>

    <div class="terms-grid">
      <transition-group name="term-fade">
        <div
          v-for="term in filteredTerms"
          :key="term.key"
          class="term-card"
          :style="{ '--term-color': term.seasonColor, '--term-bg': term.seasonBg }"
        >
          <div class="term-header">
            <div class="term-icon-circle">
              <span class="term-char">{{ term.icon }}</span>
            </div>
            <div class="term-title-area">
              <h3 class="term-name">
{{ term.name }}
</h3>
              <span class="term-pinyin">{{ term.pinyin }}</span>
              <span class="term-dates">
                <el-icon><Calendar /></el-icon>
                {{ term.dateRange }}
              </span>
            </div>
            <div class="term-order">
No.{{ term.order }}
</div>
          </div>

          <el-divider />

          <div class="term-body">
            <p class="term-desc">
{{ term.description }}
</p>

            <div v-if="term.herbs.length > 0" class="term-herbs">
              <h4>
                <el-icon><DishDot /></el-icon>时令药材
              </h4>
              <div class="herbs-list">
                <el-tag
                  v-for="herb in term.herbs"
                  :key="herb"
                  class="herb-tag"
                  effect="plain"
                  @click="goToPlants(herb)"
                >
                  {{ herb }}
                </el-tag>
              </div>
            </div>

            <div v-if="term.dongPractices" class="term-practices">
              <h4>
                <el-icon><Stamp /></el-icon>侗医药习俗
              </h4>
              <p>{{ term.dongPractices }}</p>
            </div>

            <div v-if="term.healthTip" class="term-tip">
              <el-alert
                :title="'养生提示'"
                :description="term.healthTip"
                type="info"
                :closable="false"
                show-icon
              />
            </div>
          </div>
        </div>
      </transition-group>
    </div>

    <el-empty
      v-if="filteredTerms.length === 0"
      description="暂无节气数据"
      :image-size="120"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Calendar, Cherry, Cloud, DishEdge, DishDot, Moon, Orange, Pear, Plum, Stamp, Sun, Sunny, WindPower } from '@element-plus/icons-vue'

const router = useRouter()
const activeSeason = ref('spring')

const seasons = [
  { key: 'spring', label: '春季', color: '#4CAF50', bg: 'rgba(76, 175, 80, 0.08)', icon: Pear },
  { key: 'summer', label: '夏季', color: '#F44336', bg: 'rgba(244, 67, 54, 0.08)', icon: Sunny },
  { key: 'autumn', label: '秋季', color: '#FF9800', bg: 'rgba(255, 152, 0, 0.08)', icon: Cherry },
  { key: 'winter', label: '冬季', color: '#2196F3', bg: 'rgba(33, 150, 243, 0.08)', icon: Moon }
]

const termsData = [
  {
    key: 'lichun', order: 1, name: '立春', pinyin: 'Li Chun', icon: '春',
    dateRange: '2月3日-5日', season: 'spring', seasonColor: '#4CAF50', seasonBg: 'rgba(76, 175, 80, 0.08)',
    description: '立春是二十四节气之首，标志着春季的开始。此时阳气升发，万物复苏，是采挖根茎类药材的最佳时节。侗医认为此时采集的药材生命力最旺盛，药效最好。',
    herbs: ['茵陈', '鱼腥草', '金银花藤', '车前草', '蒲公英'],
    dongPractices: '侗族药农在立春后第一声春雷前上山采挖"雷公根"（积雪草），认为雷击之前的药力未散，尤具灵效。',
    healthTip: '春季养生重在养肝护肝，保持心情舒畅，适当运动助阳气生发。'
  },
  {
    key: 'yushui', order: 2, name: '雨水', pinyin: 'Yu Shui', icon: '雨',
    dateRange: '2月18日-20日', season: 'spring', seasonColor: '#4CAF50', seasonBg: 'rgba(76, 175, 80, 0.08)',
    description: '雨水节气降雨增多，空气湿润，草木萌动。此时适合采集嫩叶类药材，侗医称为"采青"，即采集植物新发的嫩芽和嫩叶入药。',
    herbs: ['艾叶', '薄荷', '紫苏叶', '桑叶', '马齿苋'],
    dongPractices: '雨水时节，侗族人家会采集嫩艾制作"艾糍"，既是食物也是药材，用于祛寒除湿。',
    healthTip: '注意防寒保暖，适当"春捂"，多食新鲜蔬菜。'
  },
  {
    key: 'jingzhe', order: 3, name: '惊蛰', pinyin: 'Jing Zhe', icon: '雷',
    dateRange: '3月5日-7日', season: 'spring', seasonColor: '#4CAF50', seasonBg: 'rgba(76, 175, 80, 0.08)',
    description: '惊蛰时分，春雷乍动，蛰虫惊醒。气温回升明显，是采集昆虫类药材和发芽类药材的关键时期。侗医认为此时采集的药材具有"通经活络"的特殊功效。',
    herbs: ['蝉蜕', '地龙', '桑螵蛸', '柴胡', '升麻'],
    dongPractices: '侗族有"惊蛰采虫"的传统，认为此时出土的虫类药材未受污染，药效纯净。',
    healthTip: '注意预防春季流感，可适当食用清热解毒的食物。'
  },
  {
    key: 'chunfen', order: 4, name: '春分', pinyin: 'Chun Fen', icon: '分',
    dateRange: '3月20日-22日', season: 'spring', seasonColor: '#4CAF50', seasonBg: 'rgba(76, 175, 80, 0.08)',
    description: '春分昼夜平分，阴阳平衡。此时万物生长旺盛，是采集花类药材的最佳时期。侗医注重"采花入药"，采集盛开的花朵制作花药。',
    herbs: ['槐花', '菊花', '辛夷花', '密蒙花', '金银花'],
    dongPractices: '春分日侗族药农会举行"开山采药"仪式，祈求药王保佑一年采药平安顺利。',
    healthTip: '春分时节注意阴阳平衡，饮食宜清淡，避免过寒过热。'
  },
  {
    key: 'qingming', order: 5, name: '清明', pinyin: 'Qing Ming', icon: '清',
    dateRange: '4月4日-6日', season: 'spring', seasonColor: '#4CAF50', seasonBg: 'rgba(76, 175, 80, 0.08)',
    description: '清明时节，气清景明，万物皆显。此时采集的"清明茶"是侗医药中的重要饮品。许多草药此时药力最为充足，侗医有"清明药，赛人参"之说。',
    herbs: ['明前茶', '枸杞叶', '荠菜', '茵陈蒿', '地丁'],
    dongPractices: '清明前后，侗族家家户户采制"清明药茶"，晾干后全年使用，用于清热解毒、明目降压。',
    healthTip: '清明踏青有益身心，注意防过敏和花粉。'
  },
  {
    key: 'guyu', order: 6, name: '谷雨', pinyin: 'Gu Yu', icon: '谷',
    dateRange: '4月19日-21日', season: 'spring', seasonColor: '#4CAF50', seasonBg: 'rgba(76, 175, 80, 0.08)',
    description: '谷雨是春季最后一个节气，雨生百谷。此时植物生长最快，是采集全草类药材的最后黄金期。侗医抓紧此时采集春夏之交的特有药材。',
    herbs: ['香椿', '桑寄生', '雷公藤', '七叶一枝花', '白花蛇舌草'],
    dongPractices: '谷雨时节，侗族药农会进山采集"雨前草"，认为谷雨前的草药含水润之气，药性温和。',
    healthTip: '谷雨防湿邪，宜食健脾祛湿的食物如薏米、山药。'
  },
  {
    key: 'lixia', order: 7, name: '立夏', pinyin: 'Li Xia', icon: '夏',
    dateRange: '5月5日-7日', season: 'summer', seasonColor: '#F44336', seasonBg: 'rgba(244, 67, 54, 0.08)',
    description: '立夏标志着夏季开始，气温升高，植物进入快速生长期。此时适合采集清热解暑类药材，侗医称为"备暑药"，为夏季常见病做准备。',
    herbs: ['荷叶', '莲子心', '石膏', '淡竹叶', '芦根'],
    dongPractices: '立夏之日，侗族人家会用新采草药煮"立夏汤"，全家人饮用以预防夏季疾病。',
    healthTip: '立夏养心，宜午休，饮食宜清淡，多食苦味食物清心火。'
  },
  {
    key: 'xiaoman', order: 8, name: '小满', pinyin: 'Xiao Man', icon: '满',
    dateRange: '5月20日-22日', season: 'summer', seasonColor: '#F44336', seasonBg: 'rgba(244, 67, 54, 0.08)',
    description: '小满时节，麦类等夏收作物籽粒渐满。空气湿度大增，湿热交加，是采集清热利湿类药材的关键时期。侗医采集"五黄"（黄连、黄芩、黄柏、大黄、黄芪）入药。',
    herbs: ['黄连', '黄芩', '黄柏', '栀仁', '龙胆草'],
    dongPractices: '侗族有"小满采五黄"的习俗，采集黄色药材用于清热解毒、燥湿泻火。',
    healthTip: '小满防湿热，注意通风除湿，可饮绿豆汤、冬瓜汤。'
  },
  {
    key: 'mangzhong', order: 9, name: '芒种', pinyin: 'Mang Zhong', icon: '种',
    dateRange: '6月5日-7日', season: 'summer', seasonColor: '#F44336', seasonBg: 'rgba(244, 67, 54, 0.08)',
    description: '芒种是夏季第三个节气，气温显著升高，雨量充沛。此时梅雨季节来临，是采集祛湿类药材的好时机。侗医上山采集"六月雪"等祛暑良药。',
    herbs: ['六月雪', '藿香', '佩兰', '茯苓', '薏苡仁'],
    dongPractices: '芒种时节侗族药农会制作"药浴包"，采集艾叶、石菖蒲等草药配制成药浴，祛暑除湿。',
    healthTip: '芒种易困倦，适当午休，饮食增加蛋白质补充体力。'
  },
  {
    key: 'xiazhi', order: 10, name: '夏至', pinyin: 'Xia Zhi', icon: '至',
    dateRange: '6月21日-22日', season: 'summer', seasonColor: '#F44336', seasonBg: 'rgba(244, 67, 54, 0.08)',
    description: '夏至是一年中白昼最长的一天，阳气最盛。此时采集的"夏至草"药力最强劲。侗医认为此日是采集根茎类药材的最佳日子，药力可达全年巅峰。',
    herbs: ['夏枯草', '白花蛇舌草', '板蓝根', '大青叶', '金银花'],
    dongPractices: '夏至日侗族药农会在正午时分采集药材，认为此时阳气最足，药材"得天之阳"，药效倍增。',
    healthTip: '夏至阳极阴生，注意防暑降温，可饮酸梅汤、绿豆汤。'
  },
  {
    key: 'xiaoshu', order: 11, name: '小暑', pinyin: 'Xiao Shu', icon: '暑',
    dateRange: '7月6日-8日', season: 'summer', seasonColor: '#F44336', seasonBg: 'rgba(244, 67, 54, 0.08)',
    description: '小暑为盛夏之始，天气炎热但尚未至极。此时是采集枝叶类药材的好时机，侗医大量采集"暑药"用于清热解暑、生津止渴。',
    herbs: ['薄荷', '荷叶', '西瓜翠衣', '金银花', '淡竹叶'],
    dongPractices: '小暑前后，侗族村寨会集体采药制作"避暑香囊"，内装薄荷、藿香等草药，佩戴身上防暑辟秽。',
    healthTip: '小暑防中暑，避免高温时段外出，多饮水，可饮菊花茶。'
  },
  {
    key: 'dashu', order: 12, name: '大暑', pinyin: 'Da Shu', icon: '炎',
    dateRange: '7月22日-24日', season: 'summer', seasonColor: '#F44336', seasonBg: 'rgba(244, 67, 54, 0.08)',
    description: '大暑是一年中最热的时期，也是中草药生长的黄金期。侗医抓紧此时采集花叶类药材，制作"三伏贴"等外用制剂，为冬病夏治做准备。',
    herbs: ['艾叶', '白芥子', '延胡索', '甘遂', '细辛'],
    dongPractices: '大暑时节侗族开始制作"三伏药贴"，用新鲜草药捣烂敷贴穴位，治疗冬季易发的寒性疾病。',
    healthTip: '大暑注意防暑降温，饮食宜清淡，可食冬瓜、苦瓜等消暑食材。'
  },
  {
    key: 'liqiu', order: 13, name: '立秋', pinyin: 'Li Qiu', icon: '秋',
    dateRange: '8月7日-9日', season: 'autumn', seasonColor: '#FF9800', seasonBg: 'rgba(255, 152, 0, 0.08)',
    description: '立秋标志秋季开始，但暑热未消。此时是果实类药材开始成熟的季节，侗医开始采集"秋实"入药，为秋冬进补储备药材。',
    herbs: ['枸杞子', '五味子', '山茱萸', '女贞子', '金樱子'],
    dongPractices: '立秋时节侗族有"咬秋"习俗，采集初秋成熟的山果野药，晒干储藏，作为秋冬滋补佳品。',
    healthTip: '立秋后早晚转凉，注意润肺防燥，可食梨、银耳等润燥食材。'
  },
  {
    key: 'chushu', order: 14, name: '处暑', pinyin: 'Chu Shu', icon: '凉',
    dateRange: '8月22日-24日', season: 'autumn', seasonColor: '#FF9800', seasonBg: 'rgba(255, 152, 0, 0.08)',
    description: '处暑意为暑热终止，秋意渐浓。昼夜温差加大，是采集种子类药材的黄金期。侗医采集"处暑籽"用于滋补肝肾、强筋健骨。',
    herbs: ['薏苡仁', '芡实', '莲子', '沙苑子', '菟丝子'],
    dongPractices: '处暑时节侗族药农会采集各种药用植物的种子，称为"收籽"，是全年药材储备的重要环节。',
    healthTip: '处暑防秋燥，多饮水，可饮蜂蜜水润肺。'
  },
  {
    key: 'bailu', order: 15, name: '白露', pinyin: 'Bai Lu', icon: '露',
    dateRange: '9月7日-9日', season: 'autumn', seasonColor: '#FF9800', seasonBg: 'rgba(255, 152, 0, 0.08)',
    description: '白露时节，天气转凉，露水凝结。此时采集的"露水药"在侗医药中极为珍贵，认为带露采集的药材更具润泽之性，是制作膏方的最佳原料。',
    herbs: ['玉竹', '天冬', '麦冬', '沙参', '百合'],
    dongPractices: '白露清晨，侗族药农会趁露水未干时采集草药，称为"采露药"，用于制作秋冬润肺药膏。',
    healthTip: '白露注意保暖，不宜赤膊，可食龙眼、红枣等温补食材。'
  },
  {
    key: 'qiufen', order: 16, name: '秋分', pinyin: 'Qiu Fen', icon: '分',
    dateRange: '9月22日-24日', season: 'autumn', seasonColor: '#FF9800', seasonBg: 'rgba(255, 152, 0, 0.08)',
    description: '秋分昼夜平分，燥气当令。此时是采挖根茎类药材的第二黄金期（仅次于立春）。侗医认为秋分采集的根茎"得金气"，是润肺止咳的良药。',
    herbs: ['生地', '玄参', '白芍', '桔梗', '前胡'],
    dongPractices: '秋分日侗族举行"祭药神"仪式，感谢药王恩赐，祈求来年药材丰收。',
    healthTip: '秋分养生重在"润"，可食山药、百合、梨等润肺食材。'
  },
  {
    key: 'hanlu', order: 17, name: '寒露', pinyin: 'Han Lu', icon: '寒',
    dateRange: '10月8日-9日', season: 'autumn', seasonColor: '#FF9800', seasonBg: 'rgba(255, 152, 0, 0.08)',
    description: '寒露时节气温骤降，露水寒凉。此时是采集温补类药材的关键时期，侗医采集"寒露温药"为冬季进补做准备。桂树类药材此时最佳。',
    herbs: ['桂枝', '肉桂', '附子', '干姜', '吴茱萸'],
    dongPractices: '寒露时节侗族开始制作"冬补药酒"，将采集的温性药材浸泡于米酒中，冬至后开始饮用。',
    healthTip: '寒露注意足部保暖，可泡脚驱寒，饮食增加温热食材。'
  },
  {
    key: 'shuangjiang', order: 18, name: '霜降', pinyin: 'Shuang Jiang', icon: '霜',
    dateRange: '10月23日-24日', season: 'autumn', seasonColor: '#FF9800', seasonBg: 'rgba(255, 152, 0, 0.08)',
    description: '霜降是秋季最后一个节气，气温降至零度附近。此时采集的"霜打药"在侗医药中特指经霜后的草药，认为霜打后的药材寒性降低，药性更为温和。',
    herbs: ['桑叶', '枇杷叶', '柿蒂', '款冬花', '紫菀'],
    dongPractices: '霜降后采集的桑叶称为"霜桑叶"，侗医认为其润肺止咳功效最佳，是治疗肺热咳嗽的要药。',
    healthTip: '霜降防寒保暖，宜食温热食物，可食柿子润肺。'
  },
  {
    key: 'lidong', order: 19, name: '立冬', pinyin: 'Li Dong', icon: '冬',
    dateRange: '11月7日-8日', season: 'winter', seasonColor: '#2196F3', seasonBg: 'rgba(33, 150, 243, 0.08)',
    description: '立冬为冬季之始，万物收藏。此时药材进入休眠期，有效成分集中于根部和果实。侗医开始制作膏方和丸药，为全年进补的关键节点。',
    herbs: ['黄芪', '当归', '党参', '熟地', '枸杞子'],
    dongPractices: '立冬之日，侗族家家户户开始熬制"冬令膏方"，将多种药材慢火熬制成膏，每日服用以滋补强身。',
    healthTip: '立冬进补，宜食温补食物如羊肉、核桃、黑芝麻等。'
  },
  {
    key: 'xiaoxue', order: 20, name: '小雪', pinyin: 'Xiao Xue', icon: '雪',
    dateRange: '11月22日-23日', season: 'winter', seasonColor: '#2196F3', seasonBg: 'rgba(33, 150, 243, 0.08)',
    description: '小雪时节，天寒地冻。侗医认为此时采集的"雪前药"储藏最为方便，药材不易霉变。适合采挖根茎类和皮类药材进行冬季储备。',
    herbs: ['桑白皮', '地骨皮', '丹皮', '防风', '羌活'],
    dongPractices: '小雪前后，侗族药农会整理全年采集的药材，进行分类、炮制和储藏，称为"封药"。',
    healthTip: '小雪注意防寒保暖，适当运动增强体质，可饮姜茶驱寒。'
  },
  {
    key: 'daxue', order: 21, name: '大雪', pinyin: 'Da Xue', icon: '雪',
    dateRange: '12月6日-8日', season: 'winter', seasonColor: '#2196F3', seasonBg: 'rgba(33, 150, 243, 0.08)',
    description: '大雪时节，天寒地冻雪量大。此时是采集"冬虫夏草"类珍贵药材的时期。侗医认为此季节的药材"藏精华于内"，药效浓厚绵长。',
    herbs: ['冬虫夏草', '鹿茸', '人参', '灵芝', '雪莲'],
    dongPractices: '大雪时节，老一辈侗医会向年轻传承人口授心传冬季采药的诀窍和注意事项。',
    healthTip: '大雪注意保暖护阳，宜早睡晚起，饮食宜温补。'
  },
  {
    key: 'dongzhi', order: 22, name: '冬至', pinyin: 'Dong Zhi', icon: '至',
    dateRange: '12月21日-23日', season: 'winter', seasonColor: '#2196F3', seasonBg: 'rgba(33, 150, 243, 0.08)',
    description: '冬至是一年中白昼最短的一天，阴气最盛，阳气始生。侗医认为冬至是全年进补的最佳时机，有"冬至补一冬，来年无病痛"的说法。',
    herbs: ['人参', '鹿茸', '阿胶', '龟板胶', '紫河车'],
    dongPractices: '冬至是侗族最重要的"补药节"，家家户户吃药膳、饮药酒，认为冬至进补事半功倍。',
    healthTip: '冬至宜进补，可食当归生姜羊肉汤、八宝粥等。'
  },
  {
    key: 'xiaohan', order: 23, name: '小寒', pinyin: 'Xiao Han', icon: '寒',
    dateRange: '1月5日-7日', season: 'winter', seasonColor: '#2196F3', seasonBg: 'rgba(33, 150, 243, 0.08)',
    description: '小寒是一年中最冷的节气之一。侗医在此时制备外用膏药和贴敷药物，利用严寒天气促进药膏凝固和药性稳定。',
    herbs: ['川芎', '红花', '乳香', '没药', '血竭'],
    dongPractices: '小寒时节侗族制作"三九贴"，选用温热药材贴敷穴位，温经散寒、扶正祛邪。',
    healthTip: '小寒注意防寒，尤其注意关节保暖，可艾灸关元、足三里等穴位。'
  },
  {
    key: 'dahan', order: 24, name: '大寒', pinyin: 'Da Han', icon: '寒',
    dateRange: '1月20日-21日', season: 'winter', seasonColor: '#2196F3', seasonBg: 'rgba(33, 150, 243, 0.08)',
    description: '大寒是二十四节气最后一个，寒气最盛。侗医完成全年采药收藏工作，整理药方药典，总结一年采药经验，筹划来年采药计划。',
    herbs: ['杜仲', '续断', '牛膝', '骨碎补', '菟丝子'],
    dongPractices: '大寒时节，侗族药农举行"封山"仪式，感谢山神一年的馈赠，并祈求来年药材丰饶。',
    healthTip: '大寒过后阳气渐升，注意保护阳气，宜食温补食物，准备迎接新春。'
  }
]

const filteredTerms = computed(() => {
  return termsData.filter(t => t.season === activeSeason.value)
})

const getSeasonTerms = (season) => {
  return termsData.filter(t => t.season === season)
}

const goToPlants = (herbName) => {
  router.push({ path: '/plants', query: { search: herbName } })
}
</script>

<style scoped>
.solar-terms-page {
  min-height: 100vh;
}

.season-tabs {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
  margin-bottom: var(--space-2xl);
  max-width: 900px;
  margin-left: auto;
  margin-right: auto;
}

.season-tab {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-xl) var(--space-lg);
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-fast);
  border: 2px solid transparent;
  box-shadow: var(--shadow-sm);
}

.season-tab:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-md);
}

.season-tab.active {
  border-color: var(--season-color);
  background: var(--season-bg);
}

.season-icon {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: var(--season-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--season-color);
  transition: all var(--transition-fast);
}

.season-tab.active .season-icon {
  background: var(--season-color);
  color: var(--text-inverse);
}

.season-name {
  font-size: var(--font-size-md);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
}

.season-count {
  font-size: var(--font-size-xs);
  color: var(--text-muted);
}

.terms-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: var(--space-xl);
  max-width: 1200px;
  margin: 0 auto;
}

.term-card {
  background: var(--text-inverse);
  border-radius: var(--radius-lg);
  padding: var(--space-xl);
  box-shadow: var(--shadow-sm);
  border-top: 4px solid var(--term-color);
  transition: all var(--transition-fast);
}

.term-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.term-header {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
}

.term-icon-circle {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: var(--term-color);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.term-char {
  font-size: 24px;
  color: var(--text-inverse);
  font-weight: var(--font-weight-bold);
}

.term-title-area {
  flex: 1;
  min-width: 0;
}

.term-name {
  margin: 0 0 4px 0;
  font-size: var(--font-size-lg);
  color: var(--text-primary);
}

.term-pinyin {
  display: block;
  font-size: var(--font-size-xs);
  color: var(--text-muted);
  margin-bottom: 4px;
  font-style: italic;
}

.term-dates {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: var(--font-size-xs);
  color: var(--text-secondary);
}

.term-order {
  font-size: var(--font-size-sm);
  color: var(--text-muted);
  font-weight: var(--font-weight-bold);
  flex-shrink: 0;
  opacity: 0.5;
}

.term-body {
  margin-top: var(--space-sm);
}

.term-desc {
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0 0 var(--space-md) 0;
}

.term-herbs h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-sm);
  color: var(--dong-green);
}

.herbs-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-xs);
  margin-bottom: var(--space-md);
}

.herb-tag {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.herb-tag:hover {
  transform: translateY(-1px);
  border-color: var(--dong-green);
  color: var(--dong-green);
}

.term-practices {
  margin-bottom: var(--space-md);
}

.term-practices h4 {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 var(--space-sm) 0;
  font-size: var(--font-size-sm);
  color: var(--dong-gold);
}

.term-practices p {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--text-secondary);
  line-height: 1.6;
  padding: var(--space-sm) var(--space-md);
  background: var(--bg-rice);
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--dong-gold);
}

.term-fade-enter-active,
.term-fade-leave-active {
  transition: all 0.4s ease;
}

.term-fade-enter-from {
  opacity: 0;
  transform: translateY(20px);
}

.term-fade-leave-to {
  opacity: 0;
  transform: translateY(-20px);
}

@media (max-width: 1024px) {
  .season-tabs {
    gap: var(--space-md);
  }

  .terms-grid {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: var(--space-lg);
  }
}

@media (max-width: 768px) {
  .season-tabs {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-sm);
  }

  .season-tab {
    padding: var(--space-md);
  }

  .season-icon {
    width: 44px;
    height: 44px;
  }

  .terms-grid {
    grid-template-columns: 1fr;
    gap: var(--space-lg);
  }

  .term-card {
    padding: var(--space-lg);
  }

  .term-name {
    font-size: var(--font-size-md);
  }
}

@media (max-width: 480px) {
  .season-tabs {
    grid-template-columns: repeat(2, 1fr);
    gap: var(--space-xs);
  }

  .season-icon {
    width: 36px;
    height: 36px;
  }

  .season-name {
    font-size: var(--font-size-sm);
  }

  .season-count {
    font-size: 11px;
  }

  .term-card {
    padding: var(--space-md);
  }

  .term-icon-circle {
    width: 42px;
    height: 42px;
  }

  .term-char {
    font-size: 20px;
  }

  .term-name {
    font-size: var(--font-size-sm);
  }

  .term-desc {
    font-size: var(--font-size-xs);
    line-height: 1.5;
  }

  .herb-tag {
    font-size: 11px;
  }
}
</style>
