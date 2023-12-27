package dev.enjarai.minitardis.component;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.ModSounds;
import dev.enjarai.minitardis.block.InteriorDoorBlock;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisExteriorBlock;
import dev.enjarai.minitardis.block.TardisExteriorBlockEntity;
import dev.enjarai.minitardis.component.flight.*;
import net.minecraft.block.FacingBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Tardis {
    public static final Identifier DEFAULT_INTERIOR = MiniTardis.id("wooden_coral");
    public static final BlockPos INTERIOR_CENTER = new BlockPos(0, 64, 0);
    public static final ChunkTicketType<BlockPos> INTERIOR_TICKET_TYPE =
            ChunkTicketType.create("tardis_interior", Vec3i::compareTo, 20);
    public static final Codec<Tardis> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(t -> t.uuid),
            Codec.BOOL.optionalFieldOf("interior_placed", false).forGetter(t -> t.interiorPlaced),
            Identifier.CODEC.optionalFieldOf("interior", DEFAULT_INTERIOR).forGetter(t -> t.interior),
            Codec.either(TardisLocation.CODEC, PartialTardisLocation.CODEC).fieldOf("current_location").forGetter(t -> t.currentLocation),
            TardisLocation.CODEC.optionalFieldOf("destination").forGetter(t -> t.destination),
            BlockPos.CODEC.optionalFieldOf("interior_door_position", BlockPos.ORIGIN).forGetter(t -> t.interiorDoorPosition),
            TardisControl.CODEC.optionalFieldOf("controls", new TardisControl()).forGetter(t -> t.controls),
            FlightState.CODEC.optionalFieldOf("flight_state", new LandedState()).forGetter(t -> t.state),
            Codec.INT.optionalFieldOf("stability", 1000).forGetter(t -> t.stability),
            Codec.INT.optionalFieldOf("fuel", 500).forGetter(t -> t.fuel),
            HistoryEntry.CODEC.listOf().optionalFieldOf("history", List.of()).forGetter(t -> t.history),
            Codec.BOOL.optionalFieldOf("door_open", false).forGetter(t -> t.doorOpen)
    ).apply(instance, Tardis::new));

    TardisHolder holder;
    @Nullable
    RuntimeWorldHandle interiorWorld;
    DestinationScanner destinationScanner = new DestinationScanner(this, 128);
    private int sparksQueued;

    private final UUID uuid;
    private boolean interiorPlaced;
    private Identifier interior;
    private Either<TardisLocation, PartialTardisLocation> currentLocation;
    private Optional<TardisLocation> destination;
    private BlockPos interiorDoorPosition;
    private final TardisControl controls;
    private FlightState state;
    private int stability;
    private int fuel;
    private final List<HistoryEntry> history;
    private boolean doorOpen;

    private Tardis(UUID uuid, boolean interiorPlaced, Identifier interior, Either<TardisLocation, PartialTardisLocation> currentLocation, Optional<TardisLocation> destination, BlockPos interiorDoorPosition, TardisControl controls, FlightState state, int stability, int fuel, List<HistoryEntry> history, boolean doorOpen) {
        this.uuid = uuid;
        this.interiorPlaced = interiorPlaced;
        this.interior = interior;
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.interiorDoorPosition = interiorDoorPosition;
        this.controls = new TardisControl(controls);
        this.state = state;
        this.stability = stability;
        this.fuel = fuel;
        this.history = new ArrayList<>(history);
        this.doorOpen = doorOpen;

        this.controls.tardis = this;
    }

    public Tardis(TardisHolder holder, @Nullable TardisLocation location, Identifier interior) {
        this(
                UUID.randomUUID(), false, interior,
                location == null ?
                        Either.right(new PartialTardisLocation(holder.getServer().getOverworld().getRegistryKey())) :
                        Either.left(location),
                Optional.ofNullable(location), BlockPos.ORIGIN, new TardisControl(),
                location == null ? new DisabledState() : new LandingState(location, true),
                1000, 500, List.of(), false
        );

        holder.addTardis(this);

        state.init(this);

        buildExterior();
        getInteriorWorld();
    }

    public Tardis(TardisHolder holder, TardisLocation destination) {
        this(
                UUID.randomUUID(), false, DEFAULT_INTERIOR,
                Either.right(new PartialTardisLocation(destination.worldKey())),
                Optional.of(destination), BlockPos.ORIGIN, new TardisControl(),
                new FlyingState(),
                842, 567, List.of(), false
        );

        holder.addTardis(this);

        state.init(this);

        getInteriorWorld();

        controls.setEnergyConduits(true);
    }


    public void tick() {
        var world = getInteriorWorld();
        if (world.getTime() % 20 == 0) {
            world.getChunkManager().addTicket(INTERIOR_TICKET_TYPE, new ChunkPos(interiorDoorPosition), 1, interiorDoorPosition);
        }

        var newState = state.tick(this);
        if (newState != state) {
            state.complete(this);
            state = newState;
            newState.init(this);
        }

        // Interior hum
        if (state.isPowered(this) && world.getTime() % (20 * 12) == 0) {
            state.playForInterior(this, ModSounds.CORAL_HUM, SoundCategory.AMBIENT, 0.3f, 1);
        }

        // Sparks
        if (sparksQueued > 0 && state.isPowered(this) && world.getRandom().nextBetween(0, 20) == 0) {
            createInteriorSparks(false); // todo when exploding sparks?
            sparksQueued--;
        }

        if (stability < 200 && world.getRandom().nextBetween(0, 20000) < 800 - stability * 4 && sparksQueued < 5) {
            sparksQueued++;
        }

        if (!state.isSolid(this)) {
            setDoorOpen(false, true);
        }

        destinationScanner.tick();
    }

    public ServerWorld getInteriorWorld() {
        if (interiorWorld == null) {
            initializeInteriorWorld();
        }
        return interiorWorld.asWorld();
    }

    public Optional<ServerWorld> getExteriorWorld() {
        return Optional.of(currentLocation.map(l -> l.getWorld(holder.getServer()), p -> p.getWorld(holder.getServer())));
    }

    public RegistryKey<World> getExteriorWorldKey() {
        return currentLocation.map(TardisLocation::worldKey, PartialTardisLocation::worldKey);
    }

    public Optional<ServerWorld> getDestinationWorld() {
        return destination.map(l -> l.getWorld(holder.getServer()));
    }

    public DestinationScanner getDestinationScanner() {
        return destinationScanner;
    }

    private void initializeInteriorWorld() {
        var server = holder.getServer();
        var dimensionRegistry = server.getRegistryManager().get(RegistryKeys.DIMENSION_TYPE);
        var voidBiome = server.getRegistryManager().get(RegistryKeys.BIOME).getEntry(BiomeKeys.THE_VOID).orElseThrow();
        var chunkGenerator = new FlatChunkGenerator(new FlatChunkGeneratorConfig(
                Optional.empty(), voidBiome, List.of()
        ).with(
                List.of(new FlatChunkGeneratorLayer(256, ModBlocks.TARDIS_PLATING)),
                Optional.empty(), voidBiome
        ));
        var config = new RuntimeWorldConfig()
                .setDimensionType(dimensionRegistry.entryOf(RegistryKey.of(RegistryKeys.DIMENSION_TYPE, MiniTardis.id("tardis_interior"))))
                .setGenerator(chunkGenerator)
                .setGameRule(GameRules.DO_DAYLIGHT_CYCLE, false)
                .setSeed(1234L);
        interiorWorld = holder.getFantasy().getOrOpenPersistentWorld(MiniTardis.id("tardis/" + uuid.toString()), config);
        interiorWorld.setTickWhenEmpty(false);
        interiorWorld.asWorld().getComponent(ModComponents.TARDIS_REFERENCE).tardis = this;

        if (!interiorPlaced) {
            buildInterior();
        }
    }

    private void buildInterior() {
        getInterior().ifPresent(interior -> {
            var structure = interior.getStructure(holder.getServer().getStructureTemplateManager());
            var world = getInteriorWorld();
            var size = structure.getSize();
            var centerOffset = interior.templateCenter();
            var placementPos = centerOffset.equals(Vec3i.ZERO)
                    ? INTERIOR_CENTER.add(-size.getX() / 2, 0, -size.getZ() / 2)
                    : INTERIOR_CENTER.add(centerOffset.multiply(-1));

            structure.place(world, placementPos, BlockPos.ORIGIN, new StructurePlacementData(), world.getRandom(), 2);

            interiorPlaced = true;
        });
    }

    public void buildExterior() {
        currentLocation.ifLeft(location -> {
            var world = location.getWorld(holder.getServer());
            var pos = location.pos();

            world.setBlockState(pos, ModBlocks.TARDIS_EXTERIOR.getDefaultState().with(TardisExteriorBlock.FACING, location.facing()));
            if (world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardisExteriorBlockEntity) {
                tardisExteriorBlockEntity.linkTardis(this);
            }
        });
    }

    public void teleportEntityIn(Entity entity) {
        if (state.isSolid(this)) {
            var world = getInteriorWorld();
            // Try to put the entering entity at the interior door. If not, put them in the center as a fallback.
            var targetPos = interiorDoorPosition;
            var interiorDoorState = world.getBlockState(targetPos);
            float yaw = -90;

            do {
                if (interiorDoorState.isOf(ModBlocks.INTERIOR_DOOR) && interiorDoorState.get(InteriorDoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                    var facing = interiorDoorState.get(FacingBlock.FACING);
                    interiorDoorPosition = targetPos;

                    targetPos = targetPos.add(facing.getVector());
                    yaw = facing.asRotation();

                    break;
                } else {
                    targetPos = world.getPointOfInterestStorage().getInSquare(
                            poi -> poi.value().equals(ModBlocks.INTERIOR_DOOR_POI), INTERIOR_CENTER,
                            64, PointOfInterestStorage.OccupationStatus.ANY
                    ).findAny().map(PointOfInterest::getPos).orElse(INTERIOR_CENTER);
                    interiorDoorState = world.getBlockState(targetPos);
                }
            } while (interiorDoorState.isOf(ModBlocks.INTERIOR_DOOR));

            var entityPos = Vec3d.ofBottomCenter(targetPos);
            entity.teleport(world, entityPos.getX(), entityPos.getY(), entityPos.getZ(), Set.of(), yaw, 0);

            // Play a loop of hum to the player the moment they enter to ensure a seamless experience™
            if (!(state instanceof DisabledState) && entity instanceof PlayerEntity player) {
                world.playSoundFromEntity(null, player, ModSounds.CORAL_HUM, SoundCategory.AMBIENT, 0.3f, 1);
            }
        }
    }

    public void teleportEntityOut(Entity entity) {
        if (state.isSolid(this)) {
            currentLocation.ifLeft(location -> {
                var world = location.getWorld(holder.getServer());
                var pos = location.pos();
                var blockEntity = world.getBlockEntity(pos);

                // Potentially rebuild the exterior if it doesn't exist yet
                if (!(blockEntity instanceof TardisExteriorBlockEntity exteriorEntity && this.equals(exteriorEntity.getLinkedTardis()))) {
                    buildExterior();
                    blockEntity = world.getBlockEntity(pos);
                }

                if (blockEntity instanceof TardisExteriorBlockEntity exteriorEntity && this.equals(exteriorEntity.getLinkedTardis())) {
                    var facing = exteriorEntity.getCachedState().get(TardisExteriorBlock.FACING);
                    var exitPos = pos.add(facing.getVector());

                    var entityPos = Vec3d.ofBottomCenter(exitPos);
                    entity.teleport(world, entityPos.getX(), entityPos.getY(), entityPos.getZ(), Set.of(), facing.asRotation(), 0);
                }
            });
        }
    }

    public boolean canLandAt(TardisLocation location) {
        var world = location.getWorld(holder.getServer());
        var pos = location.pos();
        if (!world.isInBuildLimit(pos)) return false;
        var facing = location.facing();

        return world.getBlockState(pos).isReplaceable()
                && world.getBlockState(pos.up()).isReplaceable()
                && world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos, Direction.UP)
                && world.getBlockState(pos.offset(facing)).getCollisionShape(world, pos).isEmpty()
                && world.getBlockState(pos.up().offset(facing)).getCollisionShape(world, pos).isEmpty()
                && world.getBlockState(pos.down().offset(facing)).isSideSolidFullSquare(world, pos, Direction.UP);
    }

    public boolean canSnapDestinationTo(TardisLocation location) {
        var world = location.getWorld(holder.getServer());
        var pos = location.pos();
        if (!world.isInBuildLimit(pos)) return false;

        var bottomState = world.getBlockState(pos);
        if (!bottomState.isReplaceable() && !bottomState.isIn(ModBlocks.TARDIS_EXTERIOR_PARTS)) return false;
        var floorState = world.getBlockState(pos.down());
        return !floorState.isReplaceable() && !floorState.isIn(ModBlocks.TARDIS_EXTERIOR_PARTS);
    }


    public MinecraftServer getServer() {
        return holder.getServer();
    }

    public Random getRandom() {
        return getInteriorWorld().getRandom();
    }

    public BlockPos getInteriorCenter() {
        return INTERIOR_CENTER;
    }

    public UUID uuid() {
        return uuid;
    }

    public Optional<TardisInterior> getInterior() {
        return Optional.ofNullable(MiniTardis.getInteriorManager().getInterior(interior));
    }

    public void setCurrentLocation(TardisLocation location) {
        setCurrentLocation(Either.left(location));
    }

    public void setCurrentLocation(PartialTardisLocation location) {
        setCurrentLocation(Either.right(location));
    }

    public void setCurrentLocation(Either<TardisLocation, PartialTardisLocation> location) {
        currentLocation = location;
    }

    public Optional<TardisLocation> getCurrentLandedLocation() {
        return currentLocation.left();
    }

    public Either<TardisLocation, PartialTardisLocation> getCurrentLocation() {
        return currentLocation;
    }

    public boolean setDestination(@Nullable TardisLocation destination, boolean force) {
        return setDestination(Optional.ofNullable(destination), force);
    }

    public boolean setDestination(Optional<TardisLocation> destination, boolean force) {
        if (!force && !state.tryChangeCourse(this)) return false;

        this.destination = destination;
        destinationScanner.resetIterators();
        return true;
    }

    public Optional<TardisLocation> getDestination() {
        return destination;
    }

    public TardisControl getControls() {
        return controls;
    }

    public boolean suggestStateTransition(FlightState newState) {
        var accepted = state.suggestTransition(this, newState);
        if (accepted) {
            state.complete(this);
            state = newState;
            newState.init(this);
        }
        return accepted;
    }

    public FlightState getState() {
        return state;
    }

    public <T extends FlightState> Optional<T> getState(Class<T> stateType) {
        if (stateType.isInstance(state)) {
            return Optional.of(stateType.cast(state));
        }
        return Optional.empty();
    }

    public int getStability() {
        return stability;
    }

    public void setStability(int stability) {
        if (stability < this.stability) {
            getState(FlyingState.class).ifPresent(state -> state.errorLoops = 2);
            if (sparksQueued < 5) {
                sparksQueued = Math.min(5, sparksQueued + (this.stability - stability) / getInteriorWorld().getRandom().nextBetween(50, 150));
            }
        }
        this.stability = stability;
    }

    public void destabilize(int amount) {
        setStability(Math.max(0, getStability() - amount));
    }

    public int getFuel() {
        return fuel;
    }

    public boolean addOrDrainFuel(int amount) {
        var oldFuel = fuel;
        fuel = MathHelper.clamp(oldFuel + amount, 0, 1000);
        return fuel != oldFuel;
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }

    public void addHistoryEntry(HistoryEntry entry) {
        history.add(0, entry);
    }

    public boolean isDoorOpen() {
        return doorOpen;
    }

    public boolean setDoorOpen(boolean open, boolean force) {
        if (!force && !state.isSolid(this)) {
            return false;
        }

        this.doorOpen = open;
        return true;
    }

    public void createInteriorSparks(boolean damage) {
        findSparkPos().ifPresent(pos -> {
            var world = getInteriorWorld();

            world.spawnParticles(ParticleTypes.FIREWORK, pos.x, pos.y, pos.z, 20, 0, 0, 0, 0.2);
            world.spawnParticles(ParticleTypes.LAVA, pos.x, pos.y, pos.z, 10, 0, 0, 0, 0.2);
            world.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, SoundCategory.BLOCKS, 2, 0.5f + world.getRandom().nextFloat());
        });
    }

    private Optional<Vec3d> findSparkPos() {
        var world = getInteriorWorld();
        for (var pos : BlockPos.iterateRandomly(world.getRandom(), 128, getInteriorCenter(), 16)) {
            if (!world.getBlockState(pos).isReplaceable()) continue;

            for (var direction : Direction.values()) {
                if (world.getBlockState(pos.offset(direction)).isSideSolidFullSquare(world, pos, direction.getOpposite())) {
                    return Optional.of(Vec3d.ofCenter(pos).add(Vec3d.of(direction.getVector()).multiply(0.5)));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tardis tardis = (Tardis) o;

        return uuid.equals(tardis.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
