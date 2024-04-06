package dev.enjarai.minitardis;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.command.TardisCommand;
import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.component.screen.app.ScreenAppTypes;
import dev.enjarai.minitardis.data.ModDataStuff;
import dev.enjarai.minitardis.data.TardisArcTemplatesManager;
import dev.enjarai.minitardis.data.TardisInteriorManager;
import dev.enjarai.minitardis.item.ModItems;
import dev.enjarai.minitardis.item.PolymerModels;
import dev.enjarai.minitardis.net.ICanHasMiniTardisPayload;
import eu.pb4.polymer.networking.api.PolymerNetworking;
import eu.pb4.polymer.networking.api.server.PolymerServerNetworking;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniTardis implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "mini_tardis";
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static final Version VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata().getVersion();
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Identifier HANDSHAKE_CHANNEL = id("handshake/2");
//	public static final HandshakeServer<Unit> HANDSHAKE_SERVER = new HandshakeServer<>(
//			Codec.unit(Unit.INSTANCE), MiniTardis.HANDSHAKE_CHANNEL, () -> Unit.INSTANCE);

	@Nullable
	private static MinecraftServer server;
	private static final TardisInteriorManager interiorManager = new TardisInteriorManager();
	private static final TardisArcTemplatesManager arcTemplatesManager = new TardisArcTemplatesManager();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TardisCommand.register(dispatcher));

		ServerLifecycleEvents.SERVER_STARTING.register(server -> MiniTardis.server = server);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ModComponents.TARDIS_HOLDER.get(server.getSaveProperties()).getAllTardii().forEach(Tardis::getInteriorWorld);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> MiniTardis.server = null);

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(interiorManager);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(arcTemplatesManager);

		ModBlocks.load();
		ModItems.load();
		ModSounds.load();
		TardisCanvasUtils.load();

		// Load screenapps and their loot tables in order
		ScreenAppTypes.load();
		ModDataStuff.load();

		PolymerResourcePackUtils.addModAssets(MOD_ID);
		PolymerResourcePackUtils.markAsRequired();
		PolymerModels.load();

		PolymerNetworking.registerCommonPayload(MiniTardis.HANDSHAKE_CHANNEL, 0, ICanHasMiniTardisPayload::read);
	}

	@Nullable
	public static MinecraftServer getServer() {
		return server;
	}

	public static TardisInteriorManager getInteriorManager() {
		return interiorManager;
	}

	public static TardisArcTemplatesManager getArcTemplatesManager() {
		return arcTemplatesManager;
	}

	public static boolean playerIsRealGamer(ServerPlayNetworkHandler player) {
		return PolymerServerNetworking.getSupportedVersion(player, HANDSHAKE_CHANNEL) != -1;
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}