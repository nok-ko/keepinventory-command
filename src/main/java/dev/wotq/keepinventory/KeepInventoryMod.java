package dev.wotq.keepinventory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static dev.wotq.keepinventory.bridge.PlayerEntityBridge.bridge;
import static net.minecraft.command.arguments.EntityArgumentType.getPlayers;
import static net.minecraft.command.arguments.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class KeepInventoryMod implements ModInitializer {
    /**
     * Initialize the mod.
     *
     * All this does is register the command.
     */
    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(this::register);
    }

    /**
     * Register the /keepinventory command with the CommandDispatcher.
     *
     * @param dispatcher the CommandDispatcher to register with
     * @param dedicated whether or not the server is dedicated (unused)
     */
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        // could be done better with a custom ArgumentType but that would break completion
        // if the client doesn't have the mod installed

        Command<ServerCommandSource> self = context -> {
            execute(context.getSource(), Collections.singleton(context.getSource().getPlayer()), context.getNodes().get(1).getNode());
            return 1;
        };

        Command<ServerCommandSource> others = context -> {
            execute(context.getSource(), getPlayers(context, "targets"), context.getNodes().get(2).getNode());
            return 1;
        };

        dispatcher.register(literal("keepinventory")
                .requires(source -> source.getEntity() instanceof ServerPlayerEntity)
                .then(literal("true").executes(self))
                .then(literal("false").executes(self))
                .then(literal("default").executes(self)));

        dispatcher.register(literal("keepinventory")
                .requires(source -> source.hasPermissionLevel(4))
                .then(argument("targets", players())
                        .then(literal("true").executes(others))
                        .then(literal("false").executes(others))
                        .then(literal("default").executes(others))));
    }

    /**
     * Execute the /keepinventory command.
     *
     * This sets the keepInventory state of all target players to the given value.
     *
     * @param source the source executing the command
     * @param players the target players
     * @param valueNode the CommandNode containing the literal value (true, false, default)
     */
    private void execute(ServerCommandSource source, Collection<ServerPlayerEntity> players, CommandNode<ServerCommandSource> valueNode) {
        Optional<Boolean> value = Optional.empty();

        switch (valueNode.getName()) {
            case "true": value = Optional.of(true); break;
            case "false": value = Optional.of(false); break;
        }

        final Optional<Boolean> valueFinal = value;

        players.forEach(player -> bridge(player).$wotq_setKeepInventory(valueFinal));

        if (players.size() == 1) {
            source.sendFeedback(new LiteralText("Set keepInventory to " + valueNode.getName() + " for ").append(players.iterator().next().getDisplayName()), false);
        } else {
            source.sendFeedback(new LiteralText("Set keepInventory to " + valueNode.getName() + " for " + players.size() + " players"), false);
        }
    }
}
