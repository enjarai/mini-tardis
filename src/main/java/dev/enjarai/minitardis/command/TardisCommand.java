package dev.enjarai.minitardis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.enjarai.minitardis.component.GlobalLocation;
import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisHolder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class TardisCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("minitardis")
                .requires(Permissions.require("command.minitardis", 2))
                .then(CommandManager.literal("create")
                        .executes(TardisCommand::create)
                )
                .then(CommandManager.literal("restore")
                        .then(CommandManager.literal("exterior")
                                .then(CommandManager.argument("tardis", UuidArgumentType.uuid())
                                        .suggests(TardisCommand::suggestTardii)
                                        .executes(context -> restoreExterior(context, UuidArgumentType.getUuid(context, "tardis")))
                                )
                        )
                )
        );
    }

    private static int create(CommandContext<ServerCommandSource> context) {
        var holder = getHolder(context);

        var pos = BlockPos.ofFloored(context.getSource().getPosition());
        var worldKey = context.getSource().getWorld().getRegistryKey();
        var location = new GlobalLocation(worldKey, pos);

        new Tardis(holder, location);

        return 1;
    }

    private static int restoreExterior(CommandContext<ServerCommandSource> context, UUID uuid) {
        getHolder(context).getTardis(uuid).ifPresent(Tardis::buildExterior);
        return 1;
    }

    private static CompletableFuture<Suggestions> suggestTardii(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                ModComponents.TARDIS_HOLDER.get(context.getSource().getServer().getSaveProperties())
                        .getAllTardii().stream()
                        .map(Tardis::uuid)
                        .map(UUID::toString),
                builder
        );
    }

    private static TardisHolder getHolder(CommandContext<ServerCommandSource> context) {
        return ModComponents.TARDIS_HOLDER.get(context.getSource().getServer().getSaveProperties());
    }
}
