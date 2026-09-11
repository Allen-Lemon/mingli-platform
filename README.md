# 玄枢 · 盲派八字命理平台

依据《盲派命理规则（开发导向版）》实现的八字排盘与命理推理网站。
输入出生日期时间 → 真太阳时校正 → 四柱排盘 → 起大运 → 按盲派规则输出命理线索。

- **前端**：Vue 3 + Vite + Element Plus
- **后端**：Java 8 + Spring Boot 2.7 + MyBatis
- **数据库**：MySQL 8（规则字典 + 排盘记录），唯一数据源

---

## 一、目录结构

```
mingli-platform/
├── mingli-backend/                 Spring Boot 后端
│   ├── src/main/java/com/mingli/
│   │   ├── core/                   历法与排盘核心（不依赖框架，可单独运行）
│   │   │   ├── LunarCalendar.java  儒略日 / 二十四节气 / 真太阳时 / 干支公式
│   │   │   ├── BaziPaiPan.java     四柱排盘引擎
│   │   │   ├── TianGan.java DiZhi.java WuXing.java JiaZi.java ShiShen.java ZangGan.java
│   │   │   ├── SolarTerm.java      二十四节气（含太阳视黄经）
│   │   │   └── model/              排盘与分析结果模型
│   │   ├── rules/MingLiRuleEngine.java   盲派规则推理引擎
│   │   ├── service/                排盘服务、规则字典服务
│   │   ├── mapper/ entity/ dto/    MyBatis 持久层
│   │   └── controller/             REST 接口
│   └── src/main/resources/
│       ├── db/schema.sql           建表脚本
│       ├── db/data.sql             规则字典数据
│       ├── mapper/*.xml            MyBatis 映射
│       └── application.yml         mysql 单 profile（敏感信息走环境变量）
├── mingli-web/                     Vue 3 前端
│   └── src/{api,views,components,router,styles}
├── mvnw.cmd                        本机 Maven 包装脚本（Git Bash 下 mvn 不可用时使用）
└── docs/                           规则原文抽取文本
```

---

## 二、快速启动

### MySQL（默认数据源，生产用法）

1. 创建数据库（或让应用自动创建）：

```sql
CREATE DATABASE IF NOT EXISTS mingli DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

2. 连接信息（主机 / 账号 / 密码）全部通过环境变量读取，源码不含明文。默认值：
   `DB_HOST=127.0.0.1`、`DB_PORT=3306`、`DB_NAME=mingli`、`MYSQL_USERNAME=root`、
   `MYSQL_ROOT_PASSWORD=changeme`（生产务必通过环境变量或 `.env` 提供强密码）。

3. 启动：

```bash
cd mingli-backend
mvn -DskipTests package
java -jar target/mingli-backend.jar
```

首次启动会自动执行 `db/schema.sql` 建表，并执行 `db/data.sql` 写入规则字典（脚本「先清后写」，可重复执行）。

> 本仓库 Maven 位于 `D:\developer\apache-maven-3.8.6`，JDK 位于 `D:\zen_v1.0\jdk-8u201`；
> Git Bash 下 `mvn` 不可用时可用 `mvnw.cmd` 或 `start.bat` 一键启动。
> 若 MySQL 未就绪，应用仍可启动并完成排盘与推理（规则字典自动回退到内置默认数据），仅历史记录功能不可用。

#### 容器化部署
参见 `README.docker.md`：复制 `.env.example` 为 `.env` 填入强密码后，`docker compose up -d --build` 即可。

---

## 三、核心算法说明

### 1. 二十四节气

采用 Meeus《Astronomical Algorithms》太阳视黄经算法，迭代求解太阳视黄经等于 `k × 15°` 的时刻：

- 太阳平黄经 L0、平近点角 M、中心差 C（三阶）
- 黄经章动 Δψ（17″ 主项）
- 光行差常数项与周期项
- 力学时 TD 与世界时 UT 的 ΔT 换算（Espenak & Meeus 分段多项式，覆盖 1620—2150 年）

**精度**：与《中国天文年历》对照，误差约 **±15 分钟**（来自截断级数的固有误差）。

**临界处理**：出生时刻距某个「节」不足 30 分钟时，界面会给出提示并同时列出两套可能的年柱 / 月柱，供人工复核。

### 2. 真太阳时

```
真太阳时 = 钟表时间 + (出生地经度 − 时区中央经线) × 4 分钟 + 时差 EOT
```

时差由太阳平黄经与赤经之差求得。时柱按真太阳时划分（子时 23:00—01:00），
23:00 之后为夜子时，日柱顺延一天。

### 3. 四柱

| 柱 | 定法 |
|---|---|
| 年柱 | 以立春为界，`(命理年 − 4) mod 60` |
| 月柱 | 五虎遁：`寅月序号 = (年干序号 × 12 + 2) mod 60`，再按月支偏移 |
| 日柱 | `(儒略日数 + 49) mod 60`（以 2024-02-10 甲辰日为基准反推） |
| 时柱 | 五鼠遁：`子时序号 = (日干序号 mod 5) × 12`，再按时辰偏移 |

另含：地支藏干（本气 / 中气 / 余气）、六十甲子纳音、六甲空亡、起运与大运（阳男阴女顺排，三天折一岁）。

### 4. 芒种…起运

距上一「节」或下一「节」的时间换算：三天折一岁、一日折四月、一时辰折十日。

---

## 四、已实现的规则范围

| 编号 | 内容 |
|---|---|
| 1.1—1.9 | 禄神、羊刃、驿马、六甲空亡、四柱宫位、十神类象、十干/十二支物象、墓库象 |
| 2.1—2.3 | 财富替代规则、取财方式判定、过河拆桥（大财结构） |
| 3.1—3.3 | 官命判定流程、学历判定、行业取象 |
| 四 | 取象七原则：共象 / 合象 / 化象 / 墓象 / 制象 / 带象 / 借象 |
| 五 | 正局与反局判定流程（步骤 1—5） |
| 6.1—6.2 | 婚姻判定、桃花、子女取法 |
| 7.1—7.2 | 大限年龄段、起运、应期规则 |
| 八 | 牢狱与违法风险取象（保留为线索，不作判定） |

所有字典数据存于 MySQL `dict_*` 表，与推理引擎共用同一份数据，可在「规则库」页面查看。
原文标注「待验 / 不明 / 有误」或存在多种讲法者一律标记 **存疑**，不以单一结论输出。

---

## 五、接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/bazi/analyze` | 排盘 + 命理推理（`save=true` 时落库） |
| GET | `/api/bazi/history` | 历史记录分页（支持 keyword / gender） |
| GET | `/api/bazi/history/{id}` | 记录详情 |
| DELETE | `/api/bazi/history/{id}` | 删除记录 |
| GET | `/api/bazi/stat` | 概览统计 |
| GET | `/api/bazi/cities` | 城市经度表（真太阳时校正用） |
| GET | `/api/dict/all` | 全部规则字典 |
| GET | `/api/dict/rules?category=` | 规则条文 |
| GET | `/api/dict/shishen` | 十神类象 |
| GET | `/api/dict/wuxiang` | 干支物象 |

---

## 六、已知边界

- 节气时刻精度约 ±15 分钟，临界情形已给出两可提示。
- 当前仅支持**公历**输入；农历输入需额外的农历月建数据表，未纳入本次实现。
- 日主强弱评分为简化量化模型，仅作线索，实际须依原局做功定夺。
- 大运排定 10 步；起运前与超出的区间不计算。

---

## 七、免责声明

本平台为传统命理文献的结构化整理与文化研究工具。
结果为规则线索，不构成科学结论或现实预测承诺，
不应作为医疗、法律、投资、婚恋等重大决策依据。
