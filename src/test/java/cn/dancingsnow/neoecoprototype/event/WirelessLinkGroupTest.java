package cn.dancingsnow.neoecoprototype.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class WirelessLinkGroupTest {

    @Test
    void groupNamesNormalizeAndPersistPerLink() {
        var dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.withDefaultNamespace("overworld"));
        var a = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(1, 2, 3));
        var b = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(4, 5, 6));
        var c = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(7, 8, 9));
        var grouped = WirelessLinks.linkOf(a, b);
        var ungrouped = WirelessLinks.linkOf(a, c);
        var data = new WirelessLinks.Data();
        data.record(grouped, "  North" + (char) 1 + " Hub  ");
        data.record(ungrouped, "");

        assertEquals("North Hub", data.group(grouped));
        assertEquals("", data.group(ungrouped));
        var longGroup = WirelessLinks.normalizeGroup("a".repeat(40));
        assertEquals(32, longGroup.codePointCount(0, longGroup.length()));

        var saved = WirelessLinks.SERIALIZER.write(data, null);
        var restored = WirelessLinks.SERIALIZER.read(null, saved, null);
        assertEquals("North Hub", restored.group(grouped));
        assertEquals("", restored.group(ungrouped));
    }

    @Test
    void removingAGroupLeavesOtherGroupsAndUngroupedLinks() {
        var dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.withDefaultNamespace("overworld"));
        var a = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(1, 1, 1));
        var b = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(2, 2, 2));
        var c = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(3, 3, 3));
        var d = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(4, 4, 4));
        var first = WirelessLinks.linkOf(a, b);
        var second = WirelessLinks.linkOf(a, c);
        var other = WirelessLinks.linkOf(a, d);
        var data = new WirelessLinks.Data();
        data.record(first, "North");
        data.record(second, "North");
        data.record(other, "South");

        assertEquals(2, data.linksInGroup("North"));
        assertEquals(java.util.List.of(first, second), data.removeGroup(" North "));
        assertFalse(data.links().contains(first));
        assertFalse(data.links().contains(second));
        assertTrue(data.links().contains(other));
        assertEquals("South", data.group(other));
        assertTrue(data.removeGroup("").isEmpty());
    }

    /**
     * Renaming is what the manager's "apply group" does to links that already exist. Clearing the name has to
     * forget the entry rather than store an empty one, because the serializer writes a group only when it is
     * non-empty and a stored "" would come back as "no group" through a different path than it left by.
     */
    @Test
    void setGroupRenamesAndClearsAnExistingLink() {
        var dimension = ResourceKey.create(Registries.DIMENSION,
                ResourceLocation.withDefaultNamespace("overworld"));
        var a = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(1, 1, 1));
        var b = GlobalPos.of(dimension, new net.minecraft.core.BlockPos(2, 2, 2));
        var link = WirelessLinks.linkOf(a, b);
        var data = new WirelessLinks.Data();
        data.record(link, "");

        data.setGroup(link, " North ");
        assertEquals("North", data.group(link));
        assertEquals(1, data.links().size(), "renaming a pair must not record it a second time");

        var saved = WirelessLinks.SERIALIZER.write(data, null);
        assertEquals("North", WirelessLinks.SERIALIZER.read(null, saved, null).group(link));

        data.setGroup(link, "   ");
        assertEquals("", data.group(link));
        assertEquals("", WirelessLinks.SERIALIZER.read(null, WirelessLinks.SERIALIZER.write(data, null), null)
                .group(link), "an emptied group must survive a round trip as 'no group', not as an empty name");
    }
}
