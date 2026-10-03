package cn.dancingsnow.neoecoprototype.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * 猪咪存储矩阵外壳：带 6.6 攻击伤害、剑速、+2 实体交互距离的彩蛋物品。
 *
 * <p>1.21.1 的 NeoForge 把横扫判定从原版的 {@code instanceof SwordItem}
 * 换成了 {@link ItemAbilities#SWORD_SWEEP} 能力查询（见 Player.attack 补丁），
 * 普通 Item 默认不具备该能力，因此这里显式放行横扫。</p>
 */
public class PigmeeStorageMatrixHousingItem extends Item {

    public PigmeeStorageMatrixHousingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility ability) {
        return ability == ItemAbilities.SWORD_SWEEP;
    }
}
