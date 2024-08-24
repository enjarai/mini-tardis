package dev.enjarai.minitardis.ccacomponent.screen.app;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.block.console.ScreenBlockEntity;
import dev.enjarai.minitardis.canvas.TardisCanvasUtils;
import dev.enjarai.minitardis.ccacomponent.TardisControl;
import dev.enjarai.minitardis.ccacomponent.screen.element.FloppyBirdElement;
import dev.enjarai.minitardis.ccacomponent.screen.element.FloppyPipeElement;
import dev.enjarai.minitardis.ccacomponent.screen.element.ResizableButtonElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import eu.pb4.mapcanvas.impl.view.SubView;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FloppyBirdApp implements ScreenApp {
    public static final Codec<FloppyBirdApp> CODEC = RecordCodecBuilder.<FloppyBirdApp>create(instance -> instance.group(
            Codec.unboundedMap(Uuids.STRING_CODEC, Codec.INT).optionalFieldOf("high_scores", Map.of()).<FloppyBirdApp>forGetter(app -> app.highScores)
    ).<FloppyBirdApp>apply(instance, FloppyBirdApp::new));
    public static final int WIN_DURATION = 30;
    public static final int PIPE_SPACING = 42;

    private HashMap<UUID, Integer> highScores;

    private FloppyBirdApp(Map<UUID, Integer> highScores) {
        this.highScores = new HashMap<>(highScores);
    }

    public FloppyBirdApp() {
        this(Map.of());
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            final Random gamerRandom = new LocalRandom(controls.getTardis().getRandom().nextInt());
            final FloppyBirdElement ballElement = new FloppyBirdElement(32, 32);
            final ResizableButtonElement restartButton = new ResizableButtonElement(62 - 22, 42, 44, "Restart", controls1 -> {
                gameOver = false;
                points = 0;
                currentLevel = 1;
                beatHighScore = false;
                showHighScores = null;
                resetLevel();

                children.removeIf(FloppyPipeElement.class::isInstance);
            });
            final ResizableButtonElement highScoresButton = new ResizableButtonElement(62 - 32, 58, 64, "High scores", controls1 -> {
                //noinspection DataFlowIssue
                showHighScores = highScores.entrySet().stream()
                        .sorted(Comparator.<Map.Entry<UUID, Integer>>comparingInt(Map.Entry::getValue).reversed())
                        .map(e -> Pair.of(controls1
                                .getTardis().getServer().getUserCache()
                                .getByUuid(e.getKey()).map(GameProfile::getName)
                                .orElse("Unknown"), e.getValue()))
                        .toList();
            });
            final ResizableButtonElement backButton = new ResizableButtonElement(62 - 14, 4, 28, "Back", controls1 -> {
                showHighScores = null;
            });
            int frameCounter;
            int spacingCounter = PIPE_SPACING;
            boolean gameOver;
            int deadFrames;
            boolean hanging = true;
            int points;
            int framesSincePoint = 999;
            int currentLevel = 1;
            int pipesSpawned;
            int winningLevelFrames = -1;
            boolean beatHighScore;
            @Nullable
            List<Pair<String, Integer>> showHighScores;
            @Nullable
            ServerPlayerEntity lastInteractor;

            {
                addElement(ballElement);
                addElement(restartButton);
                addElement(highScoresButton);
                addElement(backButton);
            }

            @Override
            public void draw(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                frameCounter++;
                framesSincePoint++;
                if (!gameOver) {
                    deadFrames = 0;

                    var iterator = children.listIterator();
                    while (iterator.hasNext()) {
                        if (iterator.next() instanceof FloppyPipeElement pipe) {
                            if (ballElement.overlapsWith(pipe) && winningLevelFrames < 0) {
                                gameOver();
                            }

                            pipe.x -= 1;
                            if (pipe.x < -10) {
                                iterator.remove();
                            }

                            if (pipe.y < 0 && pipe.x == ballElement.x) {
                                points++;
                                framesSincePoint = 0;
                            }
                        }
                    }

                    spacingCounter++;
                    if (spacingCounter >= PIPE_SPACING && pipesSpawned < 10) {
                        spacingCounter = 0;

                        var holeSize = gamerRandom.nextBetween(25, 40);
                        var vOffset = gamerRandom.nextBetween(0, 76 - 10 - 8 - holeSize);
                        var lowerPipe = new FloppyPipeElement(124, 8 + vOffset + holeSize);
                        var upperPipe = new FloppyPipeElement(124, 8 + vOffset - lowerPipe.height);
                        children.add(0, lowerPipe);
                        children.add(0, upperPipe);

                        pipesSpawned++;
                    }

                    if (ballElement.gradualY >= 72 || ballElement.gradualY <= -4) {
                        gameOver();
                    }

                    if (points == currentLevel * 10 && winningLevelFrames < 0) {
                        winningLevelFrames = 0;
                    }

                    switch (framesSincePoint) {
                        case 1 -> blockEntity.playClickSound(1.2f);
                        case 5 -> blockEntity.playClickSound(1.3f);
                    }
                } else {
                    deadFrames++;

                    switch (deadFrames) {
                        case 1 -> blockEntity.playClickSound(0.9f);
                        case 5 -> blockEntity.playClickSound(0.8f);
                        case 9 -> blockEntity.playClickSound(0.7f);
                    }
                }

                if (frameCounter == 40) {
                    hanging = false;
                }

                if (winningLevelFrames >= 0) {
                    winningLevelFrames++;

                    ballElement.gradualY = MathHelper.lerp(winningLevelFrames / (float) WIN_DURATION, ballElement.gradualY, 32f);

                    switch (winningLevelFrames) {
                        case 1 -> blockEntity.playClickSound(1.1f);
                        case 5 -> blockEntity.playClickSound(1.3f);
                        case 9 -> blockEntity.playClickSound(1.5f);
                    }

                    if (winningLevelFrames >= WIN_DURATION) {
                        resetLevel();
                        currentLevel++;
                    }
                } else {
                    ballElement.deltaY += hanging ? 0 : gameOver ? 1f : 0.3f;
                }
                restartButton.visible = deadFrames >= 12 && showHighScores == null;
                highScoresButton.visible = deadFrames >= 12 && showHighScores == null;
                backButton.visible = deadFrames >= 12 && showHighScores != null;

                super.draw(blockEntity, new SubView(canvas, 2, 18, 124, 76));

                DefaultFonts.VANILLA.drawText(canvas, "Pts: " + points, 5, 5, 8, CanvasColor.WHITE_HIGH);
                DefaultFonts.VANILLA.drawText(canvas, "Lv: " + currentLevel, 64, 5, 8, CanvasColor.WHITE_HIGH);

                if (deadFrames >= 12) {
                    if (showHighScores == null) {
                        TardisCanvasUtils.drawCenteredText(canvas, "Game Over", 64, 32, CanvasColor.GRAY_HIGH);
                        if (lastInteractor != null) {
                            TardisCanvasUtils.drawCenteredText(canvas, beatHighScore ? "New High Score!" :
                                    ("High Score: " + highScores.getOrDefault(lastInteractor.getUuid(), 0)), 64, 44, CanvasColor.GRAY_HIGH);
                        }
                    } else {
                        var world = blockEntity.getWorld();
                        if (world != null) {
                            for (int i = 0; i < Math.min(showHighScores.size(), 5); i++) {
                                var entry = showHighScores.get(i);
                                TardisCanvasUtils.drawCenteredText(canvas, entry.getFirst() + ": " + entry.getSecond(), 64, 40 + i * 12, CanvasColor.GRAY_HIGH);
                            }
                        }
                    }
                }
            }

            private void gameOver() {
                gameOver = true;
                ballElement.deltaY = -6f;
                if (lastInteractor != null && points > highScores.getOrDefault(lastInteractor.getUuid(), 0)) {
                    beatHighScore = true;
                    highScores.put(lastInteractor.getUuid(), points);
                }
            }

            private void resetLevel() {
                frameCounter = 0;
                hanging = true;
                spacingCounter = PIPE_SPACING;
                pipesSpawned = 0;
                winningLevelFrames = -1;
                ballElement.gradualX = 32;
                ballElement.gradualY = 32;
                ballElement.deltaX = 0;
                ballElement.deltaY = 0;
            }

            @Override
            public void drawBackground(ScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 2, 18, TardisCanvasUtils.getSprite("floppy_bird_background"));
                CanvasUtils.draw(canvas, 0, 0, TardisCanvasUtils.getSprite("status_background"));
            }

            @Override
            public boolean onClick(ScreenBlockEntity blockEntity, ServerPlayerEntity player, ClickType type, int x, int y) {
                lastInteractor = player;
                if (!gameOver && winningLevelFrames < 0) {
                    ballElement.deltaY = -2.5f;
                    hanging = false;
                    blockEntity.playClickSound(1);
                    return true;
                }
                return super.onClick(blockEntity, player, type, x - 2, y - 18);
            }
        };
    }

    @Override
    public void appendTooltip(List<Text> tooltip) {
        tooltip.add(Text.literal(" ").append(Text.translatable("mini_tardis.app.mini_tardis.floppy_bird.tooltip", highScores.size()))
                .fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.FLOPPY_BIRD;
    }
}
