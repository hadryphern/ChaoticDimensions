package net.blue.chaoticd.gameplay;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** Development-only convenience commands for testing Sir. Orens safely. */
public final class SirOrensCommands {
    private SirOrensCommands() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            register(dispatcher)
        );
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("chaoticd")
                .then(
                    Commands.literal("sir_orens")
                        .then(
                            Commands.literal("summon")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> summonForPlayer(context.getSource()))
                        )
                )
        );
    }

    private static int summonForPlayer(CommandSourceStack source) {
        ServerPlayer player;

        try {
            player = source.getPlayerOrException();
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException exception) {
            source.sendFailure(Component.translatable("command.chaoticd.sir_orens.players_only"));
            return 0;
        }

        if (!SirOrensSpawnSystem.summonForTesting(player)) {
            source.sendFailure(Component.translatable("command.chaoticd.sir_orens.no_space"));
            return 0;
        }

        source.sendSuccess(
            () -> Component.translatable("command.chaoticd.sir_orens.summoned"),
            false
        );
        return 1;
    }
}
