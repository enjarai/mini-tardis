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
import org.joml.Vector2i;

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
        var facing = state.get(ConsoleScreenBlock.FACING);
        this.display = VirtualDisplay
                .builder(DrawableCanvas.create(), pos.offset(facing), facing)
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
//        CanvasUtils.fill(display.getCanvas(), 64 + (int) getWorld().getTime() % 100 / 10, 64, 90, 90, CanvasColor.BLUE_NORMAL);

        for (int x = 0; x < DestinationScanner.RANGE; x++) {
            for (int y = 0; y < DestinationScanner.RANGE; y++) {
                byte value = tardis.getDestinationScanner().getForX(x, y);
                canvas.set(
                        x, -y + 15 + DestinationScanner.RANGE,
                        switch (value) {
                            case 0 -> CanvasColor.BLACK_HIGH;
                            case 1 -> CanvasColor.DEEPSLATE_GRAY_HIGH;
                            case 2 -> CanvasColor.BLUE_NORMAL;
                            case 3 -> CanvasColor.LIGHT_BLUE_NORMAL;
                            case 4 -> CanvasColor.ORANGE_LOWEST;
                            default -> CanvasColor.WHITE_HIGH;
                        });
            }
        }
        canvas.set(DestinationScanner.RANGE / 2, 16 + DestinationScanner.RANGE / 2, CanvasColor.ORANGE_HIGH);
        canvas.set(DestinationScanner.RANGE / 2, 16 + DestinationScanner.RANGE / 2 - 1, CanvasColor.ORANGE_HIGH);

        var destination = tardis.getDestination();
        DefaultFonts.VANILLA.drawText(canvas,
                "X: " + destination.map(l -> String.valueOf(l.pos().getX())).orElse("-"),
                96 + 3, 16 + 3, 8, CanvasColor.WHITE_HIGH);

        display.getCanvas().sendUpdates();
    }
}
