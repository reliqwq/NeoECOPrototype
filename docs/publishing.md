# 发布与平台设置

这份从 README 移出来，因为它是作者的操作清单，不是玩家要读的内容。

## 一次发布要带什么

- 模组 JAR；
- 对应的源码版本（tag 或 commit）；
- 构建时所依据的依赖版本，即 `README.md` 环境表里那一组；
- 本仓库的许可与 `THIRD_PARTY_NOTICES.md`。

JAR 不打包任何依赖。eco、AE2、GuideME、LowDragLib2 与可选的 MegaCells、Mekanism、Applied Mekanistics 都要由玩家自己安装，交付时把版本卡一起给出，别只给一个 JAR。

## 平台环境标签

本模组两端必需。CurseForge 上项目页与每个上传文件都要设成 `Client: Required`、`Server: Required`。

这组标签是发布平台的元数据，`neoforge.mods.toml` 里存的是依赖声明本身（`side = "BOTH"`），两边各自独立，改一边不会带动另一边。发布前要人工核对一次。

## 发布前

版本号、是否已打 tag、依赖下限有没有变，都在 `进度/STATUS_MATRIX.md` 里逐条现量记录。那份文档是 gitignore 的工作台账，不进仓库，也不会随发布输出。
