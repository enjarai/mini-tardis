package dev.enjarai.minitardis;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.command.TardisCommand;
import dev.enjarai.minitardis.component.ModComponents;
import dev.enjarai.minitardis.component.Tardis;
import dev.enjarai.minitardis.data.TardisInteriorManager;
import dev.enjarai.minitardis.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MiniTardis implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final String MOD_ID = "mini_tardis";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Nullable
	private static MinecraftServer server;
	private static final TardisInteriorManager interiorManager = new TardisInteriorManager();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> TardisCommand.register(dispatcher));

		ServerLifecycleEvents.SERVER_STARTING.register(server -> MiniTardis.server = server);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			ModComponents.TARDIS_HOLDER.get(server.getSaveProperties()).getAllTardii().forEach(Tardis::getInteriorWorld);
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> MiniTardis.server = null);

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(interiorManager);

		ModBlocks.load();
		ModItems.load();
	}

	@Nullable
	public static MinecraftServer getServer() {
		return server;
	}

	public static TardisInteriorManager getInteriorManager() {
		return interiorManager;
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}