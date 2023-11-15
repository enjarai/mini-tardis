package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisExteriorBlockEntity;
import net.minecraft.block.FacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.*;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorLayer;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Tardis {
    public static final Identifier DEFAULT_INTERIOR = MiniTardis.id("debug");
    public static final BlockPos INTERIOR_CENTER = new BlockPos(0, 64, 0);
    public static final ChunkTicketType<BlockPos> INTERIOR_TICKET_TYPE =
            ChunkTicketType.create("tardis_interior", Vec3i::compareTo, 20);
    public static final Codec<Tardis> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(t -> t.uuid),
            Codec.BOOL.optionalFieldOf("interior_placed", false).forGetter(t -> t.interiorPlaced),
            Identifier.CODEC.optionalFieldOf("interior", DEFAULT_INTERIOR).forGetter(t -> t.interior),
            GlobalLocation.CODEC.optionalFieldOf("current_location").forGetter(t -> t.currentLocation),
            GlobalLocation.CODEC.optionalFieldOf("destination").forGetter(t -> t.destination),
            BlockPos.CODEC.optionalFieldOf("interior_door_position", BlockPos.ORIGIN).forGetter(t -> t.interiorDoorPosition)
    ).apply(instance, Tardis::new));

    TardisHolder holder;
    @Nullable
    RuntimeWorldHandle interiorWorld;
    private final UUID uuid;
    private boolean interiorPlaced;
    private Identifier interior;
    private Optional<GlobalLocation> currentLocation;
    private Optional<GlobalLocation> destination;
    private BlockPos interiorDoorPosition;

    private Tardis(UUID uuid, boolean interiorPlaced, Identifier interior, Optional<GlobalLocation> currentLocation, Optional<GlobalLocation> destination, BlockPos interiorDoorPosition) {
        this.uuid = uuid;
        this.interiorPlaced = interiorPlaced;
        this.interior = interior;
        this.currentLocation = currentLocation;
        this.destination = destination;
        this.interiorDoorPosition = interiorDoorPosition;
    }

    public Tardis(TardisHolder holder, @Nullable GlobalLocation location) {
        this(UUID.randomUUID(), false, DEFAULT_INTERIOR, Optional.ofNullable(location), Optional.empty(), BlockPos.ORIGIN);

        holder.addTardis(this);

        buildExterior();
        getInteriorWorld();
    }


    public void tick() {
        var world = getInteriorWorld();
        if (world.getTime() % 20 == 0) {
            world.getChunkManager().addTicket(INTERIOR_TICKET_TYPE, new ChunkPos(interiorDoorPosition), 1, interiorDoorPosition);
        }
    }

    public ServerWorld getInteriorWorld() {
        if (interiorWorld == null) {
            initializeInteriorWorld();
        }
        return interiorWorld.asWorld();
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
            var placementPos = INTERIOR_CENTER.add(-size.getX() / 2, 0, -size.getZ() / 2);

            structure.place(world, placementPos, BlockPos.ORIGIN, new StructurePlacementData(), world.getRandom(), 2);

            interiorPlaced = true;
        });
    }

    public void buildExterior() {
        currentLocation.ifPresent(location -> {
            var world = location.getWorld(holder.getServer());
            var pos = location.pos();

            world.setBlockState(pos, ModBlocks.TARDIS_EXTERIOR.getDefaultState());
            if (world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity tardisExteriorBlockEntity) {
                tardisExteriorBlockEntity.linkTardis(this);
            }
        });
    }

    public void teleportEntityIn(Entity entity) {
        var world = getInteriorWorld();
        // Try to put the entering entity at the interior door. If not, put them in the center as a fallback.
        var targetPos = interiorDoorPosition;
        var interiorDoorState = world.getBlockState(targetPos); // TODO this lags?
        float yaw = -90;

        do {
            if (interiorDoorState.isOf(ModBlocks.INTERIOR_DOOR)) {
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
        entity.teleport(world, entityPos.getX(), entityPos.getY(), entityPos.getZ(), PositionFlag.VALUES, yaw, 0);
    }

    public void teleportEntityOut(Entity entity) {
        currentLocation.ifPresent(location -> {
            var world = location.getWorld(holder.getServer());
            var pos = location.pos();
            var blockEntity = world.getBlockEntity(pos);

            // Potentially rebuild the exterior if it doesn't exist yet
            if (!(blockEntity instanceof TardisExteriorBlockEntity)) {
                buildExterior();
                blockEntity = world.getBlockEntity(pos);
            }

            if (blockEntity instanceof TardisExteriorBlockEntity exteriorEntity && this.equals(exteriorEntity.getLinkedTardis())) {
                var facing = Direction.NORTH; // exteriorEntity.getCachedState() TODO
                var exitPos = pos.add(facing.getVector());

                var entityPos = Vec3d.ofBottomCenter(exitPos);
                entity.teleport(world, entityPos.getX(), entityPos.getY(), entityPos.getZ(), PositionFlag.VALUES, facing.asRotation(), 0);
            }
        });
    }


    public UUID uuid() {
        return uuid;
    }

    public Optional<TardisInterior> getInterior() {
        return Optional.ofNullable(MiniTardis.getInteriorManager().getInterior(interior));
    }

    public void setCurrentLocation(@Nullable GlobalLocation location) {
        setCurrentLocation(Optional.ofNullable(location));
    }

    public void setCurrentLocation(Optional<GlobalLocation> location) {
        currentLocation = location;
    }

    public Optional<GlobalLocation> getCurrentLocation() {
        return currentLocation;
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
