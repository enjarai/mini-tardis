package dev.enjarai.minitardis.component.screen.app;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.TardisLocation;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GpsApp implements ScreenApp {
    public static final Codec<GpsApp> CODEC = Codec.unit(GpsApp::new);
    public static final Identifier ID = MiniTardis.id("gps");

    @Override
    public AppView getView(TardisControl controls, ConsoleScreenBlockEntity blockEntity) {
        return new AppView() {
            @Override
            public void draw(DrawableCanvas canvas) {
                var current = controls.getTardis().getCurrentLocation();
                DefaultFonts.VANILLA.drawText(canvas, "Current Location", 3, 4, 8, CanvasColor.WHITE_HIGH);
                drawLocation(current, canvas, 3, 4 + 20);

                var destination = controls.getTardis().getDestination();
                DefaultFonts.VANILLA.drawText(canvas, "Destination", 3, 4 + 41, 8, CanvasColor.WHITE_HIGH);
                drawLocation(destination, canvas, 3, 4 + 61);

                var isLocked = controls.isDestinationLocked();
                var color = isLocked ? CanvasColor.LIME_HIGH : CanvasColor.RED_HIGH;
//        CanvasUtils.fill(canvas, 2, 84, 126, 94, color);
                var lockedText = isLocked ? ">> Locked <<" : "|| Unlocked ||";
                var lockedWidth = DefaultFonts.VANILLA.getTextWidth(lockedText, 8);
                DefaultFonts.VANILLA.drawText(canvas, lockedText, 64 - lockedWidth / 2, 86, 8, color);
            }

            @Override
            public void drawBackground(DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.GPS_BACKGROUND);
            }

            @Override
            public boolean onClick(ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    public static void drawLocation(Optional<TardisLocation> optionalLocation, DrawableCanvas canvas, int x, int y) {
        optionalLocation.ifPresentOrElse(location -> drawLocation(location, canvas, x, y), () -> {
            DefaultFonts.VANILLA.drawText(canvas, "Unknown", x, y + 9, 8, CanvasColor.LIGHT_GRAY_HIGH);
        });
    }

    public static void drawLocation(Either<TardisLocation, PartialTardisLocation> eitherLocation, DrawableCanvas canvas, int x, int y) {
        eitherLocation.ifLeft(location -> drawLocation(location, canvas, x, y)).ifRight(partialLocation -> {
            DefaultFonts.VANILLA.drawText(canvas, "Unknown", x, y, 8, CanvasColor.LIGHT_GRAY_HIGH);

            var worldId = partialLocation.worldKey().getValue();
            DefaultFonts.VANILLA.drawText(
                    canvas, Text.translatable("dimension." + worldId.getNamespace() + "." + worldId.getPath()).getString(),
                    x, y + 9, 8, CanvasColor.LIGHT_GRAY_HIGH
            );
        });
    }

    private static void drawLocation(TardisLocation location, DrawableCanvas canvas, int x, int y) {
        var xText = String.valueOf(location.pos().getX());
        var yText = String.valueOf(location.pos().getY());
        var zText = String.valueOf(location.pos().getZ());
        var facingText = String.valueOf(location.facing().getName().toUpperCase().charAt(0));

        var spaceWidth = DefaultFonts.VANILLA.getTextWidth(" ", 8);
        DefaultFonts.VANILLA.drawText(canvas, xText, x, y, 8, CanvasColor.BRIGHT_RED_HIGH);
        var xWidth = DefaultFonts.VANILLA.getTextWidth(xText, 8);
        DefaultFonts.VANILLA.drawText(canvas, yText, x + xWidth + spaceWidth, y, 8, CanvasColor.LIGHT_BLUE_HIGH);
        var yWidth = DefaultFonts.VANILLA.getTextWidth(yText, 8);
        DefaultFonts.VANILLA.drawText(canvas, zText, x + xWidth + spaceWidth + yWidth + spaceWidth, y, 8, CanvasColor.LIME_HIGH);
        var zWidth = DefaultFonts.VANILLA.getTextWidth(zText, 8);

        var facingColor = location.facing().getOffsetX() != 0 ? CanvasColor.RED_HIGH : (location.facing().getOffsetZ() != 0 ? CanvasColor.GREEN_HIGH : CanvasColor.BLUE_HIGH);
        DefaultFonts.VANILLA.drawText(canvas, facingText, x + xWidth + spaceWidth + yWidth + spaceWidth + zWidth + spaceWidth, y, 8, facingColor);

        var worldId = location.worldKey().getValue();
        DefaultFonts.VANILLA.drawText(
                canvas, Text.translatable("dimension." + worldId.getNamespace() + "." + worldId.getPath()).getString(),
                x, y + 9, 8, CanvasColor.LIGHT_GRAY_HIGH
        );
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.GPS_APP);
    }

    @Override
    public Identifier id() {
        return ID;
    }
}
