package dev.enjarai.minitardis.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class MiniTardisDataGenerator implements DataGeneratorEntrypoint {
	static CompletableFuture<RegistryWrapper.WrapperLookup> lookup;

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		var pack = fabricDataGenerator.createPack();
		lookup = fabricDataGenerator.getRegistries();
		pack.addProvider(ModLootGeneration::new);
		pack.addProvider(ModBlockStateGeneration::new);
	}
}
