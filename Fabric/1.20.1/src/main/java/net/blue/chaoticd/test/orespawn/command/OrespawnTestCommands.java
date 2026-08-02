package net.blue.chaoticd.test.orespawn.command;

import com.mojang.brigadier.CommandDispatcher;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.test.orespawn.OrespawnTestModule;
import net.blue.chaoticd.test.orespawn.registry.OrespawnTestEntities;
import net.blue.chaoticd.test.orespawn.registry.OrespawnTestItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Development commands for original test harness content only. */
public final class OrespawnTestCommands {
    private OrespawnTestCommands() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            register(dispatcher)
        );
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("orespawntest")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(context -> status(context.getSource())))
                .then(Commands.literal("list")
                    .then(Commands.literal("items").executes(context -> list(context.getSource(), "items")))
                    .then(Commands.literal("entities").executes(context -> list(context.getSource(), "entities")))
                    .then(Commands.literal("bosses").executes(context -> list(context.getSource(), "bosses")))
                    .then(Commands.literal("blocked").executes(context -> list(context.getSource(), "blocked")))
                )
                .then(Commands.literal("give")
                    .then(Commands.literal("reference_marker")
                        .executes(context -> giveMarker(context.getSource()))
                    )
                )
                .then(Commands.literal("summon")
                    .then(Commands.literal("reference_proxy")
                        .executes(context -> summonProxy(context.getSource()))
                    )
                )
                .then(Commands.literal("validate").executes(context -> validate(context.getSource())))
        );
    }

    private static int status(CommandSourceStack source) {
        String key = OrespawnTestModule.isEnabled()
            ? "command.chaoticd.orespawn_test.status_enabled"
            : "command.chaoticd.orespawn_test.status_disabled";
        source.sendSuccess(
            () -> Component.translatable(key),
            false
        );
        return 1;
    }

    private static int list(CommandSourceStack source, String category) {
        if (!requireEnabled(source)) {
            return 0;
        }

        String key = switch (category) {
            case "items" -> "command.chaoticd.orespawn_test.list_items";
            case "entities" -> "command.chaoticd.orespawn_test.list_entities";
            case "bosses" -> "command.chaoticd.orespawn_test.list_bosses";
            case "blocked" -> "command.chaoticd.orespawn_test.list_blocked";
            default -> throw new IllegalArgumentException("Unknown Orespawn test category: " + category);
        };
        source.sendSuccess(() -> Component.translatable(key), false);
        return 1;
    }

    private static int giveMarker(CommandSourceStack source) {
        if (!requireEnabled(source)) {
            return 0;
        }

        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }

        ItemStack stack = new ItemStack(OrespawnTestItems.REFERENCE_MARKER);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        source.sendSuccess(() -> Component.translatable("command.chaoticd.orespawn_test.marker_given"), false);
        return 1;
    }

    private static int summonProxy(CommandSourceStack source) {
        if (!requireEnabled(source)) {
            return 0;
        }

        ServerPlayer player = player(source);
        if (player == null) {
            return 0;
        }

        var entity = OrespawnTestEntities.REFERENCE_PROXY.create(player.serverLevel());
        if (entity == null) {
            source.sendFailure(Component.translatable("command.chaoticd.orespawn_test.proxy_failed"));
            return 0;
        }

        entity.moveTo(
            player.getX(),
            player.getY(),
            player.getZ(),
            player.getYRot(),
            0.0F
        );
        if (!player.serverLevel().addFreshEntity(entity)) {
            source.sendFailure(Component.translatable("command.chaoticd.orespawn_test.proxy_failed"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("command.chaoticd.orespawn_test.proxy_summoned"), false);
        return 1;
    }

    private static int validate(CommandSourceStack source) {
        boolean valid = BuiltInRegistries.ITEM.containsKey(id("orespawn_test_reference_marker"))
            && BuiltInRegistries.ITEM.containsKey(id("orespawn_test_reference_proxy_spawn_egg"))
            && BuiltInRegistries.ENTITY_TYPE.containsKey(id("orespawn_test_reference_proxy"))
            && BuiltInRegistries.CREATIVE_MODE_TAB.containsKey(id("orespawn"));

        if (!valid) {
            source.sendFailure(Component.translatable("command.chaoticd.orespawn_test.validation_failed"));
            return 0;
        }

        source.sendSuccess(() -> Component.translatable("command.chaoticd.orespawn_test.validation_passed"), false);
        return 1;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, path);
    }

    private static boolean requireEnabled(CommandSourceStack source) {
        if (OrespawnTestModule.isEnabled()) {
            return true;
        }

        source.sendFailure(Component.translatable("command.chaoticd.orespawn_test.disabled"));
        return false;
    }

    private static ServerPlayer player(CommandSourceStack source) {
        try {
            return source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendFailure(Component.translatable("command.chaoticd.orespawn_test.players_only"));
            return null;
        }
    }
}
