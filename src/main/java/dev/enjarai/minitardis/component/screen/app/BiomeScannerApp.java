package dev.enjarai.minitardis.component.screen.app;

import java.util.List;
import java.util.NoSuchElementException;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.BiomeScanner;
import dev.enjarai.minitardis.component.DestinationScanner;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.SmallButtonElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.Rotate90ClockwiseView;
import net.minecraft.block.MapColor;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

public class BiomeScannerApp implements ScreenApp {
	//public static final Codec<BiomeScannerApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
    //        h.optionalFieldOf("biome_scanner", List.of()).forGetter(app -> app.scanner)
    //).apply(instance, BiomeScannerApp::new));
	
	private final BiomeScanner scanner;

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            //{
                //addElement(new SmallButtonElement(96 + 2, 2 + 14, "XAxis", controls -> controls.getTardis().getDestinationScanner().useXAxis()));
                //addElement(new SmallButtonElement(96 + 2, 2 + 14 + 14, "ZAxis", controls -> controls.getTardis().getDestinationScanner().useZAxis()));
            //}

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                for (int x = 0; x < BiomeScanner.RANGE; x++) {
                    for (int y = 0; y < BiomeScanner.RANGE; y++) {
                        byte value = controls.getTardis().getBiomeScanner().getFor(x, y);
                        var color = CanvasColor.from(MapColor.get(value), MapColor.Brightness.NORMAL);
                        canvas.set(x, -y - 1 + BiomeScanner.RANGE, color);
                    }
                }

                CanvasUtils.draw(canvas, 96, 64, TardisCanvasUtils.getSprite("coord_widget_x"));
                controls.getTardis().getDestination().ifPresent(destination -> {
                    var rotation = destination.facing().getHorizontal();
                    //if (isZ) {
                    //    rotation = (rotation + 3) % 4;
                    //}

                    //DrawableCanvas view = TardisCanvasUtils.getSprite("destination_facing_widget");
                    //for (int i = 0; i < rotation; i++) {
                    //    view = new Rotate90ClockwiseView(view);
                    //}
                    //CanvasUtils.draw(canvas, 96, 64, view);
                });

                super.draw(blockEntity, canvas);
            }

            @Override
            public void screenTick(ScreenBlockEntity blockEntity) {
                controls.getTardis().getBiomeScanner().shouldScanNextTick();
            }
        };
    }


    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("app/dummy"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.BIOME_SCANNER;
    }
}