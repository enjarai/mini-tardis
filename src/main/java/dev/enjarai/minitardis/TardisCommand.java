package dev.enjarai.minitardis;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.enjarai.minitardis.component.GlobalLocation;
import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisHolder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public class TardisCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("minitardis")
                .requires(Permissions.require("command.minitardis", 2))
                .then(CommandManager.literal("create")
                        .executes(TardisCommand::create)
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

    private static TardisHolder getHolder(CommandContext<ServerCommandSource> context) {
        return ModComponents.TARDIS_HOLDER.get(context.getSource().getServer().getSaveProperties());
    }
}
