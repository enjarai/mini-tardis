package dev.enjarai.minitardis.block.console;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.DestinationScanner;
import dev.enjarai.minitardis.component.Tardis;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ConsoleScreenBlockEntity extends BlockEntity implements TardisAware {
    private static final int MAX_DISPLAY_DISTANCE = 30;
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4, new ThreadFactoryBuilder().setDaemon(true).build());

    private final VirtualDisplay display;
    private final List<ServerPlayerEntity> addedPlayers = new ArrayList<>();
    @Nullable
    private ScheduledFuture<?> threadFuture;

    public ConsoleScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONSOLE_SCREEN_ENTITY, pos, state);
        this.display = VirtualDisplay
                .builder(DrawableCanvas.create(), pos, state.get(ConsoleScreenBlock.FACING))
                .glowing()
                .invisible()
                .build();
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world instanceof ServerWorld serverWorld) {
            var nearbyPlayers = serverWorld.getPlayers(player -> player.getBlockPos().getManhattanDistance(pos) <= MAX_DISPLAY_DISTANCE);

            if (addedPlayers.isEmpty() && nearbyPlayers.isEmpty() && threadFuture != null) {
                threadFuture.cancel(true);
                threadFuture = null;
            }

            if (!nearbyPlayers.isEmpty() && threadFuture == null) {
                getTardis(world).ifPresent(tardis -> threadFuture = executor.scheduleAtFixedRate(() -> {
                    refreshPlayers(serverWorld);
                    refresh(tardis);
                }, 0, 1000 / 20, TimeUnit.MILLISECONDS));
            }
        }
    }

    public void cleanUpForRemoval() {
        display.destroy();
        addedPlayers.clear();
        if (threadFuture != null) {
            threadFuture.cancel(true);
        }
    }

    private void refreshPlayers(ServerWorld world) {
        var nearbyPlayers = world.getPlayers(player -> player.getBlockPos().getManhattanDistance(pos) <= MAX_DISPLAY_DISTANCE);

        addedPlayers.removeIf(player -> {
            if (!nearbyPlayers.contains(player)) {
                display.removePlayer(player);
                display.getCanvas().removePlayer(player);
                return true;
            }
            return false;
        });
        nearbyPlayers.forEach(player -> {
            if (!addedPlayers.contains(player)) {
                display.addPlayer(player);
                display.getCanvas().addPlayer(player);
                addedPlayers.add(player);
            }
        });
    }

    private void refresh(Tardis tardis) {
        var canvas = display.getCanvas();
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SCREEN_BACKGROUND);
//        DefaultFonts.VANILLA.drawText(canvas, "testlmao", 20, 16 + 20, 8, CanvasColor.WHITE_HIGH);
        CanvasUtils.fill(display.getCanvas(), 64 + (int) getWorld().getTime() % 100 / 10, 64, 90, 90, CanvasColor.BLUE_NORMAL);

        for (int i = 0; i < DestinationScanner.TOTAL_BLOCKS; i++) {
            byte value = tardis.getDestinationScanner().getForX(i);
            var pos = DestinationScanner.getPos(i);
            canvas.set(
                    pos.x + DestinationScanner.RANGE / 2, pos.y + DestinationScanner.RANGE / 2 + 16,
                    switch (value) {
                        case 0 -> CanvasColor.WHITE_HIGH;
                        case 1 -> CanvasColor.DEEPSLATE_GRAY_HIGH;
                        default -> CanvasColor.BLUE_NORMAL;
                    });
        }

        display.getCanvas().sendUpdates();
    }
}
