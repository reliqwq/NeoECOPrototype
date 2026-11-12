package cn.dancingsnow.neoecoprototype.integration.ae2;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.parts.crafting.PatternProviderPart;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPatternProviderBlockEntity;
import cn.dancingsnow.neoecoprototype.part.SimplifyPatternProviderPart;

public final class PatternProviderUpgradeTarget {
    private PatternProviderUpgradeTarget() {
    }

    public static Class<?> normalize(Class<?> type) {
        if (PatternProviderBlockEntity.class.isAssignableFrom(type)
                && type == SimplifyPatternProviderBlockEntity.class) {
            return PatternProviderBlockEntity.class;
        }
        if (PatternProviderPart.class.isAssignableFrom(type) && type == SimplifyPatternProviderPart.class) {
            return PatternProviderPart.class;
        }
        return type;
    }
}
