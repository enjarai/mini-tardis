package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ConsoleScreenBlockEntity extends BlockEntity implements TardisAware {
    private static final int MAX_DISPLAY_DISTANCE = 30;

    private final VirtualDisplay display;
    private final List<ServerPlayerEntity> nearbyPlayers = new ArrayList<>();

    public ConsoleScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONSOLE_SCREEN_ENTITY, pos, state);
        this.display = VirtualDisplay
                .builder(DrawableCanvas.create(), pos, state.get(ConsoleScreenBlock.FACING))
                .glowing()
                .build();
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world instanceof ServerWorld serverWorld) {
            var players = serverWorld.getPlayers(player -> player.getBlockPos().getManhattanDistance(pos) <= MAX_DISPLAY_DISTANCE);
            var wasEmpty = nearbyPlayers.isEmpty() && !players.isEmpty();

            if (wasEmpty) {
                var thread = new Thread(() -> {
                    do {
                        refreshPlayers(serverWorld);
                        refresh();
                        try {
                            Thread.sleep(1000 / 20);
                        } catch (InterruptedException ignored) {
                        }
                    } while (!nearbyPlayers.isEmpty());
                });
                thread.setDaemon(true);
                thread.start();
            }
        }
    }

    public void cleanUpForRemoval() {
        display.destroy();
        nearbyPlayers.clear();
    }

    private void refreshPlayers(ServerWorld world) {
        var players = world.getPlayers(player -> player.getBlockPos().getManhattanDistance(pos) <= MAX_DISPLAY_DISTANCE);

        nearbyPlayers.removeIf(player -> {
            if (!players.contains(player)) {
                display.removePlayer(player);
                display.getCanvas().removePlayer(player);
                return true;
            }
            return false;
        });
        players.forEach(player -> {
            if (!nearbyPlayers.contains(player)) {
                display.addPlayer(player);
                display.getCanvas().addPlayer(player);
                nearbyPlayers.add(player);
            }
        });
    }

    private void refresh() {
        CanvasUtils.clear(display.getCanvas(), CanvasColor.BLACK_HIGH);
        CanvasUtils.fill(display.getCanvas(), 64 + (int) getWorld().getTime() % 100, 64, 128, 127, CanvasColor.BLUE_NORMAL);
        display.getCanvas().sendUpdates();
    }
}
