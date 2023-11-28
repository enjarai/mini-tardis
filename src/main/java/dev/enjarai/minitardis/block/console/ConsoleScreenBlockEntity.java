package dev.enjarai.minitardis.block.console;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.flight.DisabledState;
import dev.enjarai.minitardis.component.screen.TardisScreenView;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.api.utils.VirtualDisplay;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConsoleScreenBlockEntity extends BlockEntity implements TardisAware {
    private static final int MAX_DISPLAY_DISTANCE = 30;
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4, new ThreadFactoryBuilder().setDaemon(true).build());

    private final VirtualDisplay display;
    private final TardisScreenView canvas;
    public final Random drawRandom = new LocalRandom(69420); // funny numbers haha
    private final List<ServerPlayerEntity> addedPlayers = new ArrayList<>();
    @Nullable
    private ScheduledFuture<?> threadFuture;
    public int badAppleFrameCounter;
    public boolean loaded;

    @Nullable
    Identifier selectedApp;
    public SimpleInventory inventory = new SimpleInventory(1);

    public ConsoleScreenBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CONSOLE_SCREEN_ENTITY, pos, state);
        var facing = state.get(ConsoleScreenBlock.FACING);
        this.display = VirtualDisplay
                .builder(DrawableCanvas.create(), pos.offset(facing), facing)
                .glowing()
                .invisible()
                .callback(this::handleClick)
                .build();
        this.canvas = new TardisScreenView(new SubView(display.getCanvas(), 0, 16, 128, 96));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        if (selectedApp != null) {
            nbt.putString("selectedApp", selectedApp.toString());
        }

        nbt.put("inventory", inventory.toNbtList());
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        if (nbt.contains("selectedApp")) {
            selectedApp = new Identifier(nbt.getString("selectedApp"));
        }

        if (nbt.contains("inventory")) {
            inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE));
        }
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world instanceof ServerWorld serverWorld) {
            if (!loaded) {
                getTardis(world).ifPresent(tardis -> tardis.getControls().getScreenApp(selectedApp)
                        .ifPresent(app -> app.screenOpen(tardis.getControls(), this)));
                loaded = true;
            }

            var isDisabled = getTardis(world).map(t -> t.getState() instanceof DisabledState).orElse(true);
            var nearbyPlayers = isDisabled ? List.of() : serverWorld.getPlayers(player -> player.getBlockPos().getManhattanDistance(pos) <= MAX_DISPLAY_DISTANCE);

            if (addedPlayers.isEmpty() && nearbyPlayers.isEmpty() && threadFuture != null) {
                threadFuture.cancel(true);
                threadFuture = null;
            }

            if (!nearbyPlayers.isEmpty() && threadFuture == null) {
                getTardis(world).ifPresent(tardis -> threadFuture = executor.scheduleAtFixedRate(() -> {
                    try {
                        refreshPlayers(serverWorld);
                        refresh(tardis);
                    } catch (Exception e) {
                        MiniTardis.LOGGER.error("Tardis screen draw thread failed:", e);
                        throw e;
                    }
                }, 0, 1000 / 30, TimeUnit.MILLISECONDS));
            }

            if (!nearbyPlayers.isEmpty() && selectedApp != null) {
                getTardis(world).ifPresent(tardis -> tardis.getControls().getScreenApp(selectedApp)
                        .ifPresent(app -> app.screenTick(tardis.getControls(), this)));
            }
        }
    }

    public void cleanUpForRemoval() {
        display.destroy();
        addedPlayers.clear();
        if (threadFuture != null) {
            threadFuture.cancel(true);
        }

        // If we have an app selected, close it properly
        if (selectedApp != null) {
            //noinspection DataFlowIssue
            getTardis(getWorld()).ifPresent(tardis -> tardis.getControls().getScreenApp(selectedApp)
                    .ifPresent(app -> app.screenClose(tardis.getControls(), this)));
        }
    }

    private void refreshPlayers(ServerWorld world) {
        var isDisabled = getTardis(world).map(t -> t.getState() instanceof DisabledState).orElse(true);
        List<ServerPlayerEntity> nearbyPlayers = isDisabled ? List.of() : world.getPlayers(player -> player.getBlockPos().getManhattanDistance(pos) <= MAX_DISPLAY_DISTANCE);

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
        CanvasUtils.fill(canvas, 0, 0, canvas.getWidth(), canvas.getHeight(), CanvasColor.TERRACOTTA_BLUE_LOWEST);

        var controls = tardis.getControls();
        Optional.ofNullable(selectedApp).flatMap(controls::getScreenApp).ifPresentOrElse(app -> {
            app.drawBackground(controls, this, canvas);
            app.draw(controls, this, canvas);
            CanvasUtils.draw(canvas, 96 + 2, 2, ModCanvasUtils.SCREEN_SIDE_BUTTON);
            DefaultFonts.VANILLA.drawText(canvas, "Menu", 96 + 2 + 2, 2 + 4, 8, CanvasColor.WHITE_HIGH);
        }, () -> {
            CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.SCREEN_BACKGROUND);

            var apps = controls.getAllApps();
            for (int i = 0; i < apps.size(); i++) {
                var app = apps.get(i);
                app.drawIcon(controls, this, new SubView(canvas, getAppX(i), getAppY(i), 24, 24)); // TODO wrapping
            }
        });

        var stability = tardis.getStability();
        if (drawRandom.nextBetween(0, 2000) < 50 - stability) {
            canvas.addGlitchFrames(30);
        }

        canvas.refresh(drawRandom);
        display.getCanvas().sendUpdates();
    }

    private void handleClick(ServerPlayerEntity player, ClickType type, int x, int y) {
        //noinspection DataFlowIssue
        getTardis(getWorld()).ifPresent(tardis -> {
            var controls = tardis.getControls();
            Optional.ofNullable(selectedApp).flatMap(controls::getScreenApp).ifPresentOrElse(app -> {
                if (type == ClickType.RIGHT && x >= 96 + 2 && x < 96 + 2 + 28 && y >= 16 + 2 && y < 16 + 2 + 14) {
                    app.screenClose(controls, this);
                    selectedApp = null;
                    playClickSound(0.8f);
                } else {
                    app.onClick(controls, this, player, type, x, y - 16);
                }
            }, () -> {
                var apps = controls.getAllApps();
                if (type == ClickType.RIGHT) {
                    for (int i = 0; i < apps.size(); i++) {
                        var appX = getAppX(i);
                        var appY = getAppY(i) + 16;

                        if (x >= appX && x < appX + 24 && y >= appY && y < appY + 24) {
                            var app = apps.get(i);
                            selectedApp = app.id();
                            app.screenOpen(controls, this);
                            playClickSound(1.5f);
                        }
                    }
                }
            });
        });
    }

    private int getAppX(int i) {
        return i % 4 * 30 + 6;
    }

    private int getAppY(int i) {
        return i / 4 * 30 + 6;
    }

    public void playClickSound(float pitch) {
        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, getPos(), SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), SoundCategory.BLOCKS, 0.5f, pitch);
        }
    }
}
