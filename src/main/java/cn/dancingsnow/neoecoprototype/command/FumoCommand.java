package cn.dancingsnow.neoecoprototype.command;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Optional;

/**
 * Gives a plushie wearing any player's skin. The name is resolved through
 * {@link ResolvableProfile#resolve()}, the same head lookup vanilla uses, so there is no custom skin
 * fetching here and an unknown name degrades to the placeholder skin instead of failing.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class FumoCommand {
    private FumoCommand() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (!NeoECOPrototypeServerConfig.FUMO_COMMAND_ENABLED.get()) return;
        // Shipped in the jar, so the name is namespaced and the gate is OP-or-creative.
        dispatcher.register(Commands.literal("prototypefumo")
                .requires(source -> source.hasPermission(2)
                        || (source.getEntity() instanceof ServerPlayer player && player.isCreative()))
                .then(Commands.argument("player", StringArgumentType.word())
                        .executes(FumoCommand::request)));
    }

    private static int request(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String name = StringArgumentType.getString(context, "player");
        if (!StringUtil.isValidPlayerName(name)) {
            source.sendFailure(Component.translatable("cmd.neoecoprototype.fumo.invalid", name));
            return 0;
        }
        source.sendSystemMessage(Component.translatable("cmd.neoecoprototype.fumo.searching", name));
        ResolvableProfile requested =
                new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap());
        MinecraftServer server = source.getServer();
        requested.resolve().thenAccept(profile -> server.execute(() -> give(source, profile)));
        return 1;
    }

    private static void give(CommandSourceStack source, ResolvableProfile profile) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.translatable("commands.playeronlyrequired"));
            return;
        }
        ItemStack stack = new ItemStack(ModRegistration.FUMO_RELIQWQ_ITEM.get());
        stack.set(ModRegistration.FUMO_OWNER.get(), profile);
        player.getInventory().add(stack);
        if (!stack.isEmpty()) player.drop(stack, false);
        String name = profile.name().orElse("");
        source.sendSuccess(() -> Component.translatable("cmd.neoecoprototype.fumo.given", name), true);
    }
}
