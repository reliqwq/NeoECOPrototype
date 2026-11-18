package cn.dancingsnow.neoecoprototype.items;

import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

/**
 * The powder snow bucket read as a container of {@code neoecoprototype:powder_snow}.
 *
 * <p>Vanilla cannot answer for the fluid by itself. {@code SolidBucketItem} extends {@code BlockItem} rather
 * than {@code BucketItem}, so NeoForge's fallback - which matches {@code getClass() == BucketItem.class}
 * exactly - never reaches it, and {@link FluidBucketWrapper#getFluid()} would read the item's own fluid field
 * and find nothing: the 1000 mB would leave a tank and land nowhere. Filling it is still upstream's job,
 * because {@code FluidUtil.getFilledBucket} asks the fluid type and our type names this bucket; this class is
 * only the reading half. Handing the coolant away leaves a plain iron bucket, like every other bucket.
 *
 * <p>Nothing can be poured <i>into</i> a snow bucket, and that is the parent's doing rather than ours:
 * {@code FluidBucketWrapper#fill} refuses while {@link #getFluid()} reports a full bucket (L82), and this
 * class always reports one. So a snow bucket left at a water pump does not quietly become coolant - the one
 * direction that works is iron bucket in, snow bucket out.
 *
 * <p>What this costs is one item with two readings, taken knowingly on 2026-11-18: snow scooped out of a
 * powder snow bank comes back as the same item, so a machine that asks the handler sees coolant in it, and
 * that is the survival route to the bath. The route that filled the bucket never consults a handler, so the
 * two origins can be told apart by a player but not by code.
 */
public final class PowderSnowBucketFluidHandler extends FluidBucketWrapper {
    private final Supplier<Fluid> content;

    public PowderSnowBucketFluidHandler(ItemStack container, Supplier<Fluid> content) {
        super(container);
        this.content = content;
    }

    @Override
    public FluidStack getFluid() {
        return container.is(Items.POWDER_SNOW_BUCKET)
                ? new FluidStack(content.get(), FluidType.BUCKET_VOLUME)
                : super.getFluid();
    }

    @Override
    protected void setFluid(FluidStack fluidStack) {
        container = fluidStack.isEmpty()
                ? new ItemStack(Items.BUCKET)
                : new ItemStack(Items.POWDER_SNOW_BUCKET);
    }
}
