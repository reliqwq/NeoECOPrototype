"""Recipe overview: every crafting recipe this mod ships, rendered as docs/配方总览.md.

Run from the repository root:
    python tools/recipe_overview.py

It counts the recipe JSON files in the datapack and renders one table per recipe type, with names
resolved from our own lang file first and then from the upstream jars' zh_cn files. Vanilla names
come from the client jar's en_us only, so the vanilla ids this pack actually uses carry a small
hand-written Chinese table below -- when a recipe starts using a new vanilla item, add it there or
the row falls back to the English name.

Upstream jars are located through build.gradle (the eco baseline and the AE2 runtime jar) and then
globbed by mod id, so a version bump does not silently pin this tool to an old file.
"""

import glob
import json
import re
import zipfile
from collections import Counter, defaultdict
from pathlib import Path

RECIPE_DIR = Path("src/main/resources/data/neoecoprototype/recipe")
OUT = Path("docs/配方总览.md")
OUR_LANG = Path("src/main/resources/assets/neoecoprototype/lang")
GRADLE = Path("build.gradle")
MC_CLIENT_GLOB = str(Path.home() / ".gradle/caches/neoformruntime/artifacts/minecraft_*_client.jar")

# optional-mod jars are matched by file-name prefix, newest wins if several are present
OPTIONAL_JARS = ["ae2omnicells", "megacells", "ars_nouveau", "Mekanism", "appflux", "data_energistics"]

# the client jar only ships en_us for vanilla, so every vanilla id used by these recipes gets a zh name
ZH_VANILLA = {
    "redstone": "红石粉", "snowball": "雪球", "glass_bottle": "玻璃瓶", "snow_block": "雪块",
    "bucket": "铁桶", "sugar": "糖", "pink_dye": "粉红色染料", "porkchop": "生猪排",
    "powder_snow_bucket": "细雪桶", "copper_block": "铜块", "ice": "冰",
    "cooked_porkchop": "熟猪排", "sand": "沙子", "gravel": "砂砾", "piston": "活塞",
    "redstone_block": "红石块", "iron_ingot": "铁锭", "glowstone_dust": "荧石粉",
    "dried_kelp": "干海带", "brown_mushroom": "棕色蘑菇", "diamond": "钻石", "water": "水",
}

TYPE_LABEL = {
    "minecraft:crafting_shaped": "工作台 · 成形",
    "minecraft:crafting_shapeless": "工作台 · 无序",
    "neoecoae:integrated_working_station": "集成工作站",
    "neoecoae:large_integrated_working_station": "大型集成工作站（多方块）",
    "neoecoprototype:processor_assembler": "处理器装配室",
    "ae2:inscriber": "AE2 压印器",
    "ae2:storage_cell_upgrade": "存储单元升格",
    "ae2:storage_cell_disassembly": "存储单元拆解",
    "neoecoprototype:dissociation": "解离机",
    "neoecoae:cooling": "冷却液表（eco）",
    "data_energistics:data_reassembler": "数据重组器（数据能源）",
    "mekanism:metallurgic_infusing": "冶金注魔（Mekanism）",
    "ars_nouveau:enchanting_apparatus": "附魔 apparatus（新生魔艺）",
}

NOTES = {
    "ae2:inscriber": "槽位名沿用配方 JSON 里的字段名（`top` / `bottom` 是上下压印，`middle` 是放中间的料）。",
    "ae2:storage_cell_upgrade": "在 AE2 工作台里做：旧盘 + 升格卡 → 新盘，并返还上一档的组件。",
    "ae2:storage_cell_disassembly": "在 AE2 工作台里做：把整盘拆回外壳与组件，盘内已有内容不在这条配方里。",
    "neoecoae:cooling": "`（无产物）` 是故意的：这一族登记的是冷却液本身能提供多少冷却值与超频档，不产出物品。",
}

KEEP_PREFIX = ("item.", "block.", "tag.", "fluid.", "fluid_type.", "chemical.", "key_type.")
PLACEHOLDER = re.compile(r"%(\d+\$)?s\[(?P<pre>[A-Za-z0-9_]+)\.(?P<sub>[A-Za-z0-9_.]+)\]")

LANG = {}


def gradle_jar(needle):
    """The jar path build.gradle names on the line containing `needle`."""
    if not GRADLE.exists():
        return None
    for line in GRADLE.read_text(encoding="utf-8", errors="replace").splitlines():
        if needle in line:
            found = re.search(r'files\("([^"]+\.jar)"\)', line)
            if found:
                return Path(found.group(1))
    return None


def newest(pattern):
    hits = sorted(Path("libs").glob(pattern))
    return hits[-1] if hits else None


def jar_lang(jar, namespace):
    if jar is None or not jar.exists():
        return {}
    with zipfile.ZipFile(jar) as z:
        names = set(z.namelist())
        for fn in ("zh_cn.json", "en_us.json"):
            entry = "assets/%s/lang/%s" % (namespace, fn)
            if entry in names:
                return json.loads(z.read(entry).decode("utf-8"))
    return {}


def vanilla_lang():
    hits = sorted(glob.glob(MC_CLIENT_GLOB))
    if not hits:
        return {}
    with zipfile.ZipFile(hits[-1]) as z:
        entry = "assets/minecraft/lang/en_us.json"
        if entry not in z.namelist():
            return {}
        return json.loads(z.read(entry).decode("utf-8"))


def load_lang():
    for fn in ("zh_cn.json", "en_us.json"):
        p = OUR_LANG / fn
        if p.exists():
            for k, v in json.loads(p.read_text(encoding="utf-8")).items():
                LANG.setdefault(k, v)

    jars = [("ae2", newest("ae2-*.jar")), ("neoecoae", gradle_jar("neoecobeta/"))]
    jars += [(ns.lower(), newest(ns + "*")) for ns in OPTIONAL_JARS]
    for ns, jar in jars:
        for k, v in jar_lang(jar, ns).items():
            if k.startswith(KEEP_PREFIX):
                LANG.setdefault(k, v)

    for k, v in vanilla_lang().items():
        if k.startswith(KEEP_PREFIX):
            LANG.setdefault(k, v)


def resolve(value, depth=0):
    """Vanilla en_us chains through %s[key]; AE2 and our files inherit it for a few names."""
    if not isinstance(value, str) or depth > 4:
        return value

    def repl(match):
        v = LANG.get(match.group("pre") + "." + match.group("sub"))
        return resolve(v, depth + 1) if v else match.group(0)

    return PLACEHOLDER.sub(repl, value)


def leaf(item_id):
    return item_id.split(":")[-1].split("/")[-1]


def name_of(item_id):
    if ":" not in item_id:
        item_id = "minecraft:" + item_id
    ns, path = item_id.split(":", 1)
    if ns == "minecraft" and path in ZH_VANILLA:
        return ZH_VANILLA[path]
    for prefix in ("item", "block", "fluid_type", "fluid", "chemical", "key_type"):
        v = LANG.get("%s.%s.%s" % (prefix, ns, path))
        if isinstance(v, str) and v.strip():
            return resolve(v)
    return "`%s`" % item_id


def tag_name(tag):
    if ":" not in tag:
        tag = "minecraft:" + tag
    ns, path = tag.split(":", 1)
    for key in ("tag.%s.%s" % (ns, path), "tag.item.%s.%s" % (ns, path), "tag.block.%s.%s" % (ns, path)):
        v = LANG.get(key)
        if isinstance(v, str) and v.strip():
            return resolve(v)
    # conventional c: tags (and fluid tags) carry no display name anywhere; borrow the same-worded item
    for guess in ("ae2:" + leaf(path), "neoecoae:" + leaf(path), "minecraft:" + leaf(path)):
        hit = name_of(guess)
        if "`" not in hit:
            return hit + "（标签）"
    return "`#%s`" % tag


def aekey(x):
    kt = x.get("key_type", "?")
    ns, _slash, path = kt.partition(":")
    v = LANG.get("key_type.%s.%s" % (ns, path))
    label = resolve(v) if isinstance(v, str) and v.strip() else "`%s`" % kt
    inner = x.get("resource")
    if inner:
        label += " " + name_of(inner)
    return "%s x%s" % (label, x.get("amount", 1))


def ing(x):
    if isinstance(x, str):
        return tag_name(x[1:]) if x.startswith("#") else name_of(x)
    if isinstance(x, list):
        return " / ".join(ing(i) for i in x)
    if isinstance(x, dict):
        if "tag" in x:
            return tag_name(x["tag"]) if x.get("count", 1) == 1 else "%s x%s" % (tag_name(x["tag"]), x["count"])
        if "item" in x:
            return name_of(x["item"]) if x.get("count", 1) == 1 else "%s x%s" % (name_of(x["item"]), x["count"])
        if "id" in x:
            n = name_of(x["id"])
            if "amount" in x:
                return "%s x%s" % (n, x["amount"])
            return n if x.get("count", 1) == 1 else "%s x%s" % (n, x["count"])
        if "fluid" in x:
            return "%s x%s" % (name_of(x["fluid"]), x.get("amount", 1))
        if "chemical" in x:
            return "%s x%s" % (name_of(x["chemical"]), x.get("amount", 1))
        if isinstance(x.get("resource"), dict):
            return aekey(x["resource"])
        if "key_type" in x:
            return aekey(x)
        if not x:
            return "（无）"
        return json.dumps(x, ensure_ascii=False)
    return str(x)


def qty(out):
    if isinstance(out, dict):
        if not out:
            return "（无产物）"
        if "id" in out:
            return "**%s** x%s" % (name_of(out["id"]), out.get("count", 1))
        if "resource" in out or "key_type" in out:
            return "**%s**" % aekey(out.get("resource") or out)
        return json.dumps(out, ensure_ascii=False)
    if isinstance(out, list):
        return " + ".join(qty(o) for o in out)
    if isinstance(out, str):
        return "**%s**" % name_of(out)
    return str(out)


def condition(conds):
    mods, extra = [], []
    for c in conds:
        if c.get("type") == "neoforge:mod_loaded":
            mods.append(c.get("modid", "?"))
        elif c.get("type") == "neoforge:item_exists":
            extra.append("需 " + c.get("item", "?"))
        else:
            extra.append(json.dumps(c, ensure_ascii=False))
    parts = ["装 " + " + ".join("`%s`" % m for m in mods)] if mods else []
    return "；".join(parts + extra)


def render(d):
    """One recipe JSON -> (产物, 材料, 附加, 条件)."""
    t = d.get("type", "?")
    extra = []
    for field, fmt in (("energy", "%s AE"), ("duration", "%s 刻"), ("sourceCost", "魔力 %s")):
        if field in d:
            extra.append(fmt % d[field])
    if t == "neoecoae:cooling" and "coolant" in d:
        extra.append("冷却值 %s" % d["coolant"])
    if "max_overclock" in d:
        extra.append("超频档 %s" % d["max_overclock"])

    if t == "minecraft:crafting_shaped":
        seen = Counter()
        alt = d.get("key", {})
        for ch in "".join(d.get("pattern", [])):
            if ch != " ":
                seen[ch] += 1
        mats = " + ".join(
            ing(alt[ch]) if seen[ch] == 1 else "%s x%s" % (ing(alt[ch]), seen[ch])
            for ch in sorted(seen, key=lambda c: -seen[c])
        )
        out = qty(d.get("result"))
    elif t == "minecraft:crafting_shapeless":
        counted = Counter(ing(i) for i in d.get("ingredients", []))
        mats = " + ".join(n if counted[n] == 1 else "%s x%s" % (n, counted[n])
                          for n in sorted(counted, key=lambda k: -counted[k]))
        out = qty(d.get("result"))
    elif t == "ae2:storage_cell_upgrade":
        mats = "%s + %s" % (ing(d["input_cell"]), ing(d["input_component"]))
        out = "%s（返还 %s）" % (qty(d["result_cell"]), ing(d["result_component"]))
    elif t == "ae2:storage_cell_disassembly":
        mats = ing(d["cell"])
        out = " + ".join(ing(i) for i in d.get("cell_disassembly_items", []))
        extra.append("整盘拆回材料")
    elif t in ("neoecoae:integrated_working_station", "neoecoae:large_integrated_working_station"):
        mats = " + ".join(ing(i) for i in d.get("inputItems", []))
        if "inputFluid" in d:
            mats += " + " + ing(d["inputFluid"])
        if "itemOutput" in d:
            out = qty(d["itemOutput"])
            if "fluidOutput" in d:
                extra.append("出液 " + ing(d["fluidOutput"]))
        elif "fluidOutput" in d:
            out = "%s（只出流体）" % ing(d["fluidOutput"])
        else:
            out = "（无产物）"
    elif t == "neoecoprototype:processor_assembler":
        mats = " + ".join(ing(i) for i in d.get("ingredients", []))
        out = qty(d.get("result"))
    elif t == "ae2:inscriber":
        slots = d.get("ingredients", {})
        mats = " + ".join("%s = %s" % (slot, ing(slots[slot]))
                          for slot in ("primary", "secondary", "middle", "top", "bottom") if slot in slots)
        out = qty(d.get("result"))
        extra.append("模式 " + str(d.get("mode", "?")))
    elif t == "neoecoprototype:dissociation":
        mats = ing(d.get("input"))
        out = "产物权重（份额，非必出）：" + " / ".join(
            "%s %s" % (name_of(k), v) for k, v in d.get("products", {}).items())
        extra.append("抽 %s 次 x%s，%s 刻" % (d.get("draws"), d.get("per_hit"), d.get("ticks")))
        extra.append("冷却液 " + ing(d.get("coolant")))
        extra.append("溶液 " + ing(d.get("solution")))
    elif t == "neoecoae:cooling":
        mats = ing(d.get("input"))
        out = qty(d.get("output"))
    elif t == "data_energistics:data_reassembler":
        mats = ing(d.get("inputs"))
        out = qty(d.get("outputs"))
    elif t == "mekanism:metallurgic_infusing":
        mats = "%s + %s" % (ing(d.get("item_input")), ing(d.get("chemical_input")))
        out = qty(d.get("output"))
    elif t == "ars_nouveau:enchanting_apparatus":
        mats = "%s + 底座 " % ing(d.get("reagent")) + " + ".join(ing(i) for i in d.get("pedestalItems", []))
        out = qty(d.get("result"))
    else:
        mats = json.dumps(d, ensure_ascii=False)
        out = "?"
    return out, mats, "，".join(extra), condition(d.get("neoforge:conditions", []))


def main():
    load_lang()
    rows = defaultdict(list)
    files = 0
    for path in sorted(RECIPE_DIR.rglob("*.json")):
        files += 1
        data = json.loads(path.read_text(encoding="utf-8"))
        out, mats, extra, cond = render(data)
        rows[data.get("type", "?")].append((out, mats, extra, cond,
                                            path.relative_to(RECIPE_DIR).as_posix()))
    assert sum(len(v) for v in rows.values()) == files

    order = list(TYPE_LABEL) + [t for t in rows if t not in TYPE_LABEL]
    withheld = [n for n in ("music_recorder", "music_jukebox", "music_data_disc")
                if not (RECIPE_DIR / (n + ".json")).exists()]

    L = ["# 配方总览", "",
         "本模组 `src/main/resources/data/neoecoprototype/recipe/` 下的全部配方，共 **%d 条**。" % files,
         "材料名取自各 mod 自带的 `zh_cn` 语言文件（我们自己的键优先），缺中文退回英文，再退回 id。",
         "", "怎么读这张表：", "",
         "- `x2` = 该材料需要的数量；`#` 开头的是标签，任意匹配物品即可（如羊毛、原木）。",
         "- 用 `/` 分隔的一项是可替换槽位。",
         "- `条件` 一列只写额外需要的 mod；空着表示只依赖 AE2 + eco，装了就会出。",
         "- `文件` 一列就是配方 JSON 在数据包里的路径，改数值改那里。", "",
         "不在这一份清单里的两件事：处理器装配室还能从 AE2 压印器配方**派生**出部分处理器配方（派生规则见 "
         "`docs/processor-assembler-recipes.md`）；KubeJS 与整合包自己在运行时增删的配方也不在这里，这里只数数据包文件。", ""]
    if len(withheld) == 3:
        L.append("音乐那一族（录制机 / AE 唱片机 / 数据唱片）从 1.4.9 起整族对玩家隐藏，三条配方文件已出包，"
                 "所以不在这 %d 条里。" % files)
        L.append("")
    L += ["## 一览", "", "| 制作途径 | 类型 | 条数 |", "|---|---|---|"]
    for t in order:
        if t in rows:
            L.append("| %s | `%s` | %d |" % (TYPE_LABEL.get(t, t), t, len(rows[t])))
    L.append("| **合计** |  | **%d** |" % files)
    L.append("")

    for t in order:
        if t not in rows:
            continue
        L.append("## %s — `%s`（%d 条）" % (TYPE_LABEL.get(t, t), t, len(rows[t])))
        L.append("")
        if t in NOTES:
            L.append(NOTES[t])
            L.append("")
        L += ["| 产物 | 材料 | 附加 | 条件 | 文件 |", "|---|---|---|---|---|"]
        for out, mats, extra, cond, rel in sorted(rows[t], key=lambda r: r[4]):
            cells = [out, mats, extra, cond, "`%s`" % rel]
            L.append("| " + " | ".join(c.replace("|", "\\|").replace("\n", " ") for c in cells) + " |")
        L.append("")

    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text("\n".join(L) + "\n", encoding="utf-8", newline="\n")
    print("recipes:", files, "| types:", len(rows), "| lang keys:", len(LANG), "| wrote", OUT)


if __name__ == "__main__":
    main()
