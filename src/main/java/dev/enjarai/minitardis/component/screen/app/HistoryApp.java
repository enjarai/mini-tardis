package dev.enjarai.minitardis.component.screen.app;

import com.google.common.collect.Iterables;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.canvas.ModCanvasUtils;
import dev.enjarai.minitardis.component.HistoryEntry;
import dev.enjarai.minitardis.component.TardisControl;
import dev.enjarai.minitardis.component.screen.element.AppElement;
import dev.enjarai.minitardis.component.screen.element.SmallButtonElement;
import eu.pb4.mapcanvas.api.core.CanvasColor;
import eu.pb4.mapcanvas.api.core.DrawableCanvas;
import eu.pb4.mapcanvas.api.font.DefaultFonts;
import eu.pb4.mapcanvas.api.utils.CanvasUtils;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class HistoryApp implements ScreenApp {
    public static final Codec<HistoryApp> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HistoryEntry.CODEC.listOf().optionalFieldOf("history", List.of()).forGetter(app -> app.history)
    ).apply(instance, HistoryApp::new));
    private static final int ENTRIES_PER_PAGE = 3;

    public final List<HistoryEntry> history;

    private HistoryApp(List<HistoryEntry> history) {
        this.history = new ArrayList<>(history);
    }

    public HistoryApp() {
        this.history = new ArrayList<>();
    }

    @Override
    public AppView getView(TardisControl controls) {
        return new ElementHoldingView(controls) {
            private int currentPage;

            private final List<SmallButtonElement> shownEntryButtons = List.of(
                    new SmallButtonElement(98, 28, "Set", selectVisibleEntry(0)),
                    new SmallButtonElement(98, 54, "Set", selectVisibleEntry(1)),
                    new SmallButtonElement(98, 80, "Set", selectVisibleEntry(2))
            );

            {
                addElement(new SmallButtonElement(2, 2, "Prev", controls -> this.currentPage = Math.max(this.currentPage - 1, 0)));
                addElement(new SmallButtonElement(30, 2, "Next", controls -> this.currentPage = Math.min(this.currentPage + 1, (history.size() - 1) / ENTRIES_PER_PAGE)));
            }

            @Override
            public Iterable<AppElement> children() {
                return Iterables.concat(super.children(), shownEntryButtons.subList(0, Math.min(history.size() - currentPage * ENTRIES_PER_PAGE, 3)));
            }

            @Override
            public void draw(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                DefaultFonts.VANILLA.drawText(canvas, this.currentPage + 1 + "/" + ((history.size() - 1) / ENTRIES_PER_PAGE + 1), 62, 7, 8, CanvasColor.WHITE_HIGH);

                for (int i = currentPage * ENTRIES_PER_PAGE; i < Math.min((currentPage * ENTRIES_PER_PAGE) + ENTRIES_PER_PAGE, history.size()); i++) {
                    var relativeIndex = i - currentPage * ENTRIES_PER_PAGE;
                    var entry = history.get(i);

                    if (entry.location().equals(controls.getTardis().getDestination().orElse(null))) {
                        CanvasUtils.draw(canvas, 2, 18 + 26 * relativeIndex, ModCanvasUtils.HISTORY_CURRENT_OUTLINE);
                    }

                    var numText = (i + 1) + ".";
                    var numWidth = DefaultFonts.VANILLA.getTextWidth(numText, 8);
                    DefaultFonts.VANILLA.drawText(canvas, numText, 4, 20 + 26 * relativeIndex, 8, CanvasColor.WHITE_HIGH);
                    GpsApp.drawLocation(Optional.of(entry.location()), canvas, 4 + numWidth + 4, 20 + 26 * relativeIndex);
                }

                super.draw(blockEntity, canvas);
            }

            private Consumer<TardisControl> selectVisibleEntry(int index) {
                return controls -> {
                    if (controls.getTardis().setDestination(history.get(currentPage * ENTRIES_PER_PAGE + index).location(), false)) {
                        controls.setDestinationLocked(false, true);
                    }
                };
            }

            @Override
            public void drawBackground(ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
                CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.HISTORY_BACKGROUND);
            }
        };
    }

    @Override
    public void appendTooltip(List<Text> tooltip) {
        tooltip.add(Text.literal(" ").append(Text.translatable("mini_tardis.app.mini_tardis.history.tooltip", history.size()))
                .fillStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
    }

    @Override
    public void drawIcon(TardisControl controls, ConsoleScreenBlockEntity blockEntity, DrawableCanvas canvas) {
        CanvasUtils.draw(canvas, 0, 0, ModCanvasUtils.HISTORY_APP);
    }

    @Override
    public ScreenAppType<?> getType() {
        return ScreenAppTypes.HISTORY;
    }
}
