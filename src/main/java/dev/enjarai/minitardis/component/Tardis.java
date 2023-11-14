package dev.enjarai.minitardis.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisExteriorBlock;
import dev.enjarai.minitardis.block.TardisExteriorBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
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
    public static final Codec<Tardis> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("uuid").forGetter(t -> t.uuid),
            Codec.BOOL.optionalFieldOf("interior_placed", false).forGetter(t -> t.interiorPlaced),
            Identifier.CODEC.optionalFieldOf("interior", DEFAULT_INTERIOR).forGetter(t -> t.interior),
            GlobalLocation.CODEC.optionalFieldOf("current_location").forGetter(t -> t.currentLocation),
            GlobalLocation.CODEC.optionalFieldOf("destination").forGetter(t -> t.destination)
    ).apply(instance, Tardis::new));

    TardisHolder holder;
    @Nullable
    RuntimeWorldHandle interiorWorld;
    private final UUID uuid;
    private boolean interiorPlaced;
    private Identifier interior;
    private Optional<GlobalLocation> currentLocation;
    private Optional<GlobalLocation> destination;

    private Tardis(UUID uuid, boolean interiorPlaced, Identifier interior, Optional<GlobalLocation> currentLocation, Optional<GlobalLocation> destination) {
        this.uuid = uuid;
        this.interiorPlaced = interiorPlaced;
        this.interior = interior;
        this.currentLocation = currentLocation;
        this.destination = destination;
    }

    public Tardis(TardisHolder holder, @Nullable GlobalLocation location) {
        this(UUID.randomUUID(), false, DEFAULT_INTERIOR, Optional.ofNullable(location), Optional.empty());

        holder.add(this);

        buildExterior();
        getInteriorWorld();
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
        var targetPos = getInteriorWorld().getPointOfInterestStorage().getInSquare(
                poi -> poi.value().equals(ModBlocks.INTERIOR_DOOR_POI), INTERIOR_CENTER,
                64, PointOfInterestStorage.OccupationStatus.ANY
        ).findAny().map(PointOfInterest::getPos).orElse(INTERIOR_CENTER);

        entity.teleport(getInteriorWorld(), targetPos.getX(), targetPos.getY(), targetPos.getZ(), PositionFlag.VALUES, -90, 0);
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
