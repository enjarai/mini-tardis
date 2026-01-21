package dev.enjarai.minitardis.component.screen.app;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.component.PartialTardisLocation;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.TardisLocation;
import dev.enjarai.minitardis.component.screen.canvas.CanvasColors;
import dev.enjarai.minitardis.component.screen.canvas.patbox.DrawableCanvas;
import dev.enjarai.minitardis.component.screen.canvas.patbox.font.DefaultFonts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ClickType;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class GpsApp implements ScreenApp {
    public static final MapCodec<GpsApp> CODEC = MapCodec.unit(GpsApp::new);

    @Override
    public AppView getView(TardisControl controls) {
        return new AppView() {
            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                var current = controls.getTardis().getCurrentLocation();
                DefaultFonts.VANILLA.drawText(canvas, "Current Location", 3, 4, 8, CanvasColors.WHITE);
                drawLocation(current, canvas, 3, 4 + 20);

                var destination = controls.getTardis().getDestination();
                DefaultFonts.VANILLA.drawText(canvas, "Destination", 3, 4 + 41, 8, CanvasColors.WHITE);
                drawLocation(destination, canvas, 3, 4 + 61);

                var isLocked = controls.isDestinationLocked();
                var color = isLocked ? CanvasColors.LIME_DULL : CanvasColors.RED_DULL;
//        CanvasUtils.fill(canvas, 2, 84, 126, 94, color);
                var lockedText = isLocked ? ">> Locked <<" : "|| Unlocked ||";
                var lockedWidth = DefaultFonts.VANILLA.getTextWidth(lockedText, 8);
                DefaultFonts.VANILLA.drawText(canvas, lockedText, 64 - lockedWidth / 2, 86, 8, color);
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                canvas.draw(0, 0, TardisCanvasUtils.getSprite("gps_background"));
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                return false;
            }
        };
    }

    public static void drawLocation(Optional<TardisLocation> optionalLocation, DrawableCanvas canvas, int x, int y) {
        optionalLocation.ifPresentOrElse(location -> drawLocation(location, canvas, x, y), () -> {
            DefaultFonts.VANILLA.drawText(canvas, "Unknown", x, y + 9, 8, CanvasColors.LIGHT_GRAY);
        });
    }

    public static void drawLocation(Either<TardisLocation, PartialTardisLocation> eitherLocation, DrawableCanvas canvas, int x, int y) {
        eitherLocation.ifLeft(location -> drawLocation(location, canvas, x, y)).ifRight(partialLocation -> {
            DefaultFonts.VANILLA.drawText(canvas, "Unknown", x, y, 8, CanvasColors.LIGHT_GRAY);

            var worldId = partialLocation.worldKey();
            DefaultFonts.VANILLA.drawText(
                    canvas, DimensionsApp.translateWorldId(worldId).getString(),
                    x, y + 9, 8, CanvasColors.LIGHT_GRAY
            );
        });
    }

    private static void drawLocation(TardisLocation location, DrawableCanvas canvas, int x, int y) {
        var xText = String.valueOf(location.pos().getX());
        var yText = String.valueOf(location.pos().getY());
        var zText = String.valueOf(location.pos().getZ());
        var facingText = String.valueOf(location.facing().getName().toUpperCase().charAt(0));

        var spaceWidth = DefaultFonts.VANILLA.getTextWidth(" ", 8);
        DefaultFonts.VANILLA.drawText(canvas, xText, x, y, 8, CanvasColors.RED);
        var xWidth = DefaultFonts.VANILLA.getTextWidth(xText, 8);
        DefaultFonts.VANILLA.drawText(canvas, yText, x + xWidth + spaceWidth, y, 8, CanvasColors.LIGHT_BLUE);
        var yWidth = DefaultFonts.VANILLA.getTextWidth(yText, 8);
        DefaultFonts.VANILLA.drawText(canvas, zText, x + xWidth + spaceWidth + yWidth + spaceWidth, y, 8, CanvasColors.LIME);
        var zWidth = DefaultFonts.VANILLA.getTextWidth(zText, 8);

        var facingColor = location.facing().getOffsetX() != 0 ? CanvasColors.RED_DULL : (location.facing().getOffsetZ() != 0 ? CanvasColors.LIME_DULL : CanvasColors.LIGHT_BLUE_DULL);
        DefaultFonts.VANILLA.drawText(canvas, facingText, x + xWidth + spaceWidth + yWidth + spaceWidth + zWidth + spaceWidth, y, 8, facingColor);

        var worldId = location.worldKey();
        DefaultFonts.VANILLA.drawText(
                canvas, DimensionsApp.translateWorldId(worldId).getString(),
                x, y + 9, 8, CanvasColors.LIGHT_GRAY
        );
    }

    @Override
    public void drawIcon(TardisControl controls, ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        canvas.draw(0, 0, TardisCanvasUtils.getSprite("app/gps"));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.GPS;
    }
}
