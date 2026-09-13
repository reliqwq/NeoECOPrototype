package cn.dancingsnow.neoecoprototype.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * 猪咪存储矩阵外壳：带 6.6 攻击伤害、剑速、+2 实体交互距离与附魔光泽的彩蛋物品。
 *
 * <p>1.21.1 的 NeoForge 把横扫判定从原版的 {@code instanceof SwordItem}
 * 换成了 {@link ItemAbilities#SWORD_SWEEP} 能力查询（见 Player.attack 补丁），
 * 普通 Item 默认不具备该能力，因此这里显式放行横扫。</p>
 *
 * <p>光泽走 {@code ENCHANTMENT_GLINT_OVERRIDE} 纯布尔组件（注册表无关），
 * 不承载真实附魔——附魔注册表是数据驱动的，注册期拿不到 Holder，
 * 强行写入默认组件会在网络编码侧爆炸（见 DEV_LOG）。</p>
 */
public class PigcatStorageMatrixHousingItem extends Item {

    public PigcatStorageMatrixHousingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ability == ItemAbilities.SWORD_SWEEP;
    }
}
