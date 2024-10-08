package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasImage;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasUtils;
import dev.enjarai.minitardis.component.screen.canvas.patbox.SubView;
import dev.enjarai.minitardis.component.screen.canvas.TardisScreenView;
import dev.enjarai.minitardis.component.screen.app.AppView;
import dev.enjarai.minitardis.component.screen.app.ScreenAppType;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.wispforest.endec.impl.KeyedEndec;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class ScreenBlockEntity extends BlockEntity implements TardisAware {
    private static final int MAX_DISPLAY_DISTANCE = 30;
    private static final ScheduledExecutorService executor = Executors.newScheduledThreadPool(4,
            new DefaultThreadFactory(ScreenBlockEntity.class, true));
    public static final KeyedEndec<CanvasImage> IMAGE_ENDEC = CanvasImage.ENDEC.keyed("display", () -> new CanvasImage(128, 96));

    public final Random drawRandom = new LocalRandom(69420); // funny numbers haha
    protected final TardisScreenView canvas;
    public CanvasImage display;
    protected int lastDisplayHash = 0;
    private final List<ServerPlayerEntity> addedPlayers = new ArrayList<>();
    public SimpleInventory inventory = new SimpleInventory(1);
    public short backgroundColor = CanvasColors.BACKGROUND;
    @Nullable
    AppView currentView;
    @Nullable
    Identifier selectedApp;
    @Nullable
    private ScheduledFuture<?> threadFuture;
    private boolean inactive;

    public ScreenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.display = new CanvasImage(128, 96);
        this.canvas = new TardisScreenView(display);
    }

    protected abstract BlockPos getPos(BlockPos pos, BlockState state);

    protected abstract Direction getFacing(BlockPos pos, BlockState state);

    protected abstract BlockRotation getRotation(BlockPos pos, BlockState state);

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (selectedApp != null) {
            nbt.putString("selectedApp", selectedApp.toString());
        }

        nbt.put("inventory", inventory.toNbtList(registryLookup));

        nbt.putShort("backgroundColor", backgroundColor);
    }

    protected void writeNetNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        if (!inactive) {
            nbt.put(IMAGE_ENDEC, display);
        }
        nbt.put("inventory", inventory.toNbtList(registryLookup));
        nbt.putBoolean("inactive", inactive);
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        display = nbt.get(IMAGE_ENDEC);
        canvas.setSource(display);

        if (nbt.contains("selectedApp")) {
            selectedApp = Identifier.tryParse(nbt.getString("selectedApp"));
        }

        if (nbt.contains("inventory")) {
            inventory.readNbtList(nbt.getList("inventory", NbtElement.COMPOUND_TYPE), registryLookup);
        }

        if (nbt.contains("backgroundColor")) {
            backgroundColor = nbt.getShort("backgroundColor");
        }

        if (nbt.contains("inactive")) {
            inactive = nbt.getBoolean("inactive");
        }
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        NbtCompound nbtCompound = new NbtCompound();
        this.writeNetNbt(nbtCompound, registryLookup);
        return nbtCompound;
    }

    public void tick(World world, BlockPos pos, BlockState state) {
        if (world instanceof ServerWorld serverWorld) {
            if (selectedApp != null && currentView == null) {
                getTardis(world).ifPresent(tardis -> tardis.getControls().getScreenApp(ScreenAppType.REGISTRY.get(selectedApp)).ifPresent(app -> {
                    currentView = app.getView(tardis.getControls());
                    currentView.screenOpen(this);
                }));
            }

            var isDisabled = getTardis(world).map(t -> !t.getState().isPowered(t)).orElse(true);
            var viewOverridden = getTardis(world).map(t -> t.getState().overrideScreenImage(t)).orElse(false);
            setInactive(isDisabled && !viewOverridden);
            var nearby = serverWorld.getPlayers(player -> player.getBlockPos().getManhattanDistance(pos) <= MAX_DISPLAY_DISTANCE);
            List<ServerPlayerEntity> shouldSyncWith = getInactive() ? List.of() : nearby;

            addedPlayers.removeIf(player -> {
                if (!shouldSyncWith.contains(player)) {
                    if (player.getWorld() == getWorld()) {
                        player.networkHandler.sendPacket(toUpdatePacket());
                    }
                    return true;
                }
                return false;
            });
            shouldSyncWith.forEach(player -> {
                if (!addedPlayers.contains(player)) {
                    player.networkHandler.sendPacket(toUpdatePacket());
                    addedPlayers.add(player);
                }
            });

            if (addedPlayers.isEmpty() && shouldSyncWith.isEmpty() && threadFuture != null) {
                threadFuture.cancel(true);
                threadFuture = null;
            }

            if (!shouldSyncWith.isEmpty() && threadFuture == null) {
                getTardis(world).ifPresent(tardis -> threadFuture = executor.scheduleAtFixedRate(() -> {
                    try {
                        refresh(tardis);
                    } catch (Exception e) {
                        MiniTardis.LOGGER.error("Tardis screen draw thread failed:", e);
                        throw e;
                    }
                }, 0, 1000 / 30, TimeUnit.MILLISECONDS));
            }

            if (!shouldSyncWith.isEmpty() && currentView != null && !viewOverridden) {
                currentView.screenTick(this);
            }
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();

        addedPlayers.clear();
        if (threadFuture != null) {
            threadFuture.cancel(true);
        }
    }

    public void cleanUpForRemoval() {
        addedPlayers.clear();
        if (threadFuture != null) {
            threadFuture.cancel(true);
        }

        // If we have an app selected, close it properly
        if (currentView != null) {
            currentView.screenClose(this);
        }
    }

    public void pushUpdates() {
        var hash = display.hashCode();

        if (hash != lastDisplayHash) {
            addedPlayers.forEach(player -> player.networkHandler.sendPacket(toUpdatePacket()));

            lastDisplayHash = hash;
        }
    }

    private void refresh(Tardis tardis) {
        canvas.fillRaw(backgroundColor);

        var controls = tardis.getControls();
        if (!tardis.getState().overrideScreenImage(tardis)) {
            if (currentView != null) {
                currentView.drawBackground(this, canvas);
                currentView.draw(this, canvas);
                canvas.draw(96 + 2, 2, TardisCanvasUtils.getSprite("screen_side_button"));
                DefaultFonts.VANILLA.drawText(canvas, "Menu", 96 + 2 + 2, 2 + 4, 8, CanvasUtils.toLimitedColor(0xffffff));
            } else {
                canvas.draw(0, 0, TardisCanvasUtils.getSprite("screen_background"));

                var apps = controls.getAllApps();
                for (int i = 0; i < apps.size(); i++) {
                    var app = apps.get(i);
                    app.drawIcon(controls, this, new SubView(canvas, getAppX(i), getAppY(i), 24, 24));
                }
            }
        } else {
            tardis.getState().drawScreenImage(controls, canvas, this);
        }

        var stability = tardis.getStability();
        if (drawRandom.nextBetween(0, 2000) < 50 - stability) {
            canvas.addGlitchFrames(30);
        }

        canvas.refresh(drawRandom);
//        CanvasUtils.draw(display.getCanvas(), 0, 0, display);
//        display.getCanvas().sendUpdates();
        pushUpdates();
    }

    protected void handleClick(ServerPlayerEntity player, ClickType type, int x, int y) {
        //noinspection DataFlowIssue
        getTardis(getWorld()).ifPresent(tardis -> {
            if (tardis.getState().overrideScreenImage(tardis)) {
                return;
            }

            var controls = tardis.getControls();
            if (currentView != null) {
                if (type == ClickType.RIGHT && x >= 96 + 2 && x < 96 + 2 + 28 && y >= 16 + 2 && y < 16 + 2 + 14) {
                    closeApp();
                    playClickSound(0.8f);
                } else {
                    currentView.onClick(this, player, type, x, y - 16);
                }
            } else {
                var apps = controls.getAllApps();
                if (type == ClickType.RIGHT) {
                    for (int i = 0; i < apps.size(); i++) {
                        var appX = getAppX(i);
                        var appY = getAppY(i) + 16;

                        if (x >= appX && x < appX + 24 && y >= appY && y < appY + 24) {
                            var app = apps.get(i);
                            selectedApp = app.getId();
                            currentView = app.getView(controls);
                            currentView.screenOpen(this);
                            playClickSound(1.5f);
                            markDirty();
                        }
                    }
                }
            }
        });
    }

    private int getAppX(int i) {
        return i % 4 * 30 + 6;
    }

    private int getAppY(int i) {
        return i / 4 * 30 + 6;
    }

    public void closeApp() {
        if (currentView == null) {
            MiniTardis.LOGGER.error("Tried to close app while currentView is null!");
            return;
        }
        currentView.screenClose(this);
        selectedApp = null;
        currentView = null;
        markDirty();
    }

    public boolean getInactive() {
        return inactive;
    }

    public void setInactive(boolean inactive) {
        var old = this.inactive;
        this.inactive = inactive;
        if (old != inactive) {
            markDirty();
        }
    }

    public void playClickSound(float pitch) {
        playSound(SoundEvents.BLOCK_NOTE_BLOCK_BIT.value(), 0.5f, pitch);
    }

    public void playSound(SoundEvent soundEvent, float volume, float pitch) {
        if (getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.playSound(null, getPos(), soundEvent, SoundCategory.BLOCKS, volume, pitch);
        }
    }
}
