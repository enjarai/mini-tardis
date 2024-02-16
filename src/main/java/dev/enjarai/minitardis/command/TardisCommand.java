package dev.enjarai.minitardis.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisLocation;
import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.TardisHolder;
import dev.enjarai.minitardis.component.flight.FlightState;
import dev.enjarai.minitardis.component.screen.app.ScreenApp;
import dev.enjarai.minitardis.component.screen.app.ScreenAppType;
import dev.enjarai.minitardis.item.FloppyItem;
import dev.enjarai.minitardis.item.ModItems;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

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
                .then(CommandManager.literal("floppy")
                        .then(CommandManager.literal("install")
                                .then(CommandManager.argument("app", IdentifierArgumentType.identifier())
                                        .suggests((context, builder) -> CommandSource.suggestIdentifiers(ScreenAppType.REGISTRY.getIds(), builder))
                                        .executes(context -> installApps(context, List.of(Objects.requireNonNull(ScreenAppType.REGISTRY.get(
                                                IdentifierArgumentType.getIdentifier(context, "app"))).constructor().get())))
                                )
                        )
                        .then(CommandManager.literal("installAll")
                                .executes(context -> installApps(context, ScreenAppType.REGISTRY.stream().map(type -> type.constructor().get()).toList()))
                        )
                )
                .then(CommandManager.literal("door")
                        .then(CommandManager.literal("toggle")
                                .then(CommandManager.argument("tardis", UuidArgumentType.uuid())
                                        .suggests(TardisCommand::suggestTardii)
                                        .executes(context -> toggleDoor(context, UuidArgumentType.getUuid(context, "tardis")))
                                )
                        )
                )
                .then(CommandManager.literal("refuel")
                        .executes(context -> refuel(context, null))
                        .then(CommandManager.argument("tardis", UuidArgumentType.uuid())
                                .suggests(TardisCommand::suggestTardii)
                                .executes(context -> refuel(context, UuidArgumentType.getUuid(context, "tardis")))
                        )
                )
                .then(CommandManager.literal("state")
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("state", IdentifierArgumentType.identifier())
                                        .suggests(TardisCommand::suggestStates)
                                        .executes(context -> setState(context, IdentifierArgumentType.getIdentifier(context, "state"), null))
                                        .then(CommandManager.argument("tardis", UuidArgumentType.uuid())
                                                .suggests(TardisCommand::suggestTardii)
                                                .executes(context -> setState(context, IdentifierArgumentType.getIdentifier(context, "state"), UuidArgumentType.getUuid(context, "tardis")))
                                        )
                                )
                        )
                )
        );
    }

    private static int create(CommandContext<ServerCommandSource> context) {
        var holder = getHolder(context);

        var pos = BlockPos.ofFloored(context.getSource().getPosition());
        var worldKey = context.getSource().getWorld().getRegistryKey();
        var location = new TardisLocation(worldKey, pos, Direction.NORTH);

        new Tardis(holder, location, Tardis.DEFAULT_INTERIOR);

        return 1;
    }

    private static int restoreExterior(CommandContext<ServerCommandSource> context, UUID uuid) {
        getHolder(context).getTardis(uuid).ifPresent(Tardis::buildExterior);
        return 1;
    }

    private static int toggleDoor(CommandContext<ServerCommandSource> context, UUID uuid) {
        getHolder(context).getTardis(uuid).ifPresent(t -> t.setDoorOpen(!t.isDoorOpen(), false));
        return 1;
    }

    private static int refuel(CommandContext<ServerCommandSource> context, @Nullable UUID uuid) {
        var tardis = uuid == null ? getTardis(context.getSource().getWorld()) : getHolder(context).getTardis(uuid);
        tardis.ifPresent(t -> t.addOrDrainFuel(1000));
        return 1;
    }

    private static int setState(CommandContext<ServerCommandSource> context, Identifier stateId, @Nullable UUID uuid) {
        if (FlightState.CONSTRUCTORS.containsKey(stateId)) {
            var state = FlightState.CONSTRUCTORS.get(stateId);
            var tardis = uuid == null ? getTardis(context.getSource().getWorld()) : getHolder(context).getTardis(uuid);
            tardis.ifPresent(t -> t.forceSetState(state.get()));
        }
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

    private static CompletableFuture<Suggestions> suggestStates(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(
                FlightState.CONSTRUCTORS.keySet().stream()
                        .map(Identifier::toString),
                builder
        );
    }

    private static int installApps(CommandContext<ServerCommandSource> context, List<? extends ScreenApp> apps) {
        var player = context.getSource().getPlayer();
        if (player == null) return 0;

        var handStack = player.getMainHandStack();
        if (!handStack.isOf(ModItems.FLOPPY)) {
            context.getSource().sendFeedback(() -> Text.of("Please hold a Floppy in your main hand"), false);
            return 0;
        }

        for (var app : apps) {
            FloppyItem.addApp(handStack, app);
        }
        return apps.size();
    }

    private static TardisHolder getHolder(CommandContext<ServerCommandSource> context) {
        return ModComponents.TARDIS_HOLDER.get(context.getSource().getServer().getSaveProperties());
    }

    private static Optional<Tardis> getTardis(World world) {
        return world.getComponent(ModComponents.TARDIS_REFERENCE).getTardis();
    }
}
