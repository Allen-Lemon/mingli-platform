# 项目长期记忆：玄枢 · 盲派八字命理平台

## 项目概览
依据《盲派命理规则（开发导向版）》实现的八字排盘 + 命理推理网站。
工作目录 `D:\project\mingli-platform`，含 `mingli-backend`（Java8/SpringBoot2.7/MyBatis/MySQL+H2）
与 `mingli-web`（Vue3/Vite/ElementPlus）。

## 本机环境约定
- JDK：`D:\zen_v1.0\jdk-8u201`（Java 8）
- Maven：`D:\developer\apache-maven-3.8.6` —— **Git Bash 下 `mvn` 不可用**，
  须用根目录 `mvnw.cmd`，或
  `java -classpath "<maven>\boot\plexus-classworlds-2.6.0.jar" "-Dclassworlds.conf=<maven>\bin\m2.conf" "-Dmaven.home=<maven>" org.codehaus.plexus.classworlds.launcher.Launcher <goals>`
- Node：`C:\Users\yt\.workbuddy\binaries\node\versions\22.22.2-2\node.exe`
  （npm：`node.exe "<node>\node_modules\npm\bin\npm-cli.js"`）
- 无本地 MySQL 服务 —— 默认 profile 为 mysql，演示用 `--spring.profiles.active=h2`
- curl 访问 localhost 必须加 `--noproxy '*'`；Vite 绑定在 `localhost`（127.0.0.1 有时连不上，用 localhost）
- **致命坑：Windows 下 `spring.sql.init` 默认用平台编码(GBK) 读 UTF-8 的 `schema.sql`/`data.sql`**，
  会把中文（天干/地支/物象）读成乱码，导致以中文为唯一键的表（dict_lushen/dict_gan_wuxiang/dict_zhi_wuxiang）
  插入时整条因唯一约束冲突被丢弃（0 行）。**必须在 application.yml 显式设 `spring.sql.init.encoding: UTF-8`**。
- curl 发含中文的 POST 用 `-d` 会被 shell 编码污染 → 后端报 `Invalid UTF-8 middle byte`(400)。
  **测试中文接口务必用 Python urllib（正确 UTF-8 编码）**，不要裸 curl -d 中文。
- 后台起服务不要用 `java -jar ... &` 再套 `run_in_background` 双后台，进程会被回收；直接 `run_in_background` 起单进程。

## 架构约定
- 规则字典以 MySQL `dict_*` 表为唯一数据源，`DictService` 启动加载 + 懒加载重试 + 内置默认回退
- 推理引擎只输出「规则线索」，命中项必须带原文编号；存疑规则打 doubtful，不给单一确定结论
- 所有页面与接口结果都带免责声明（非科学结论 / 不作重大决策依据）

## 已知边界
- 节气时刻精度约 ±15 分钟（Meeus 截断级数），临界 30 分钟内给出两套年/月柱
- 仅支持公历输入（农历需额外月建数据表，未实现）
- 日主强弱为简化量化模型，仅作线索
