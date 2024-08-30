package dev.enjarai.minitardis.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.enjarai.minitardis.MiniTardis;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlock;
import dev.enjarai.minitardis.block.console.ConsoleScreenBlockEntity;
import dev.enjarai.minitardis.component.screen.canvas.patbox.CanvasUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class ConsoleScreenBlockRenderer implements BlockEntityRenderer<ConsoleScreenBlockEntity> {
    private static final Map<ConsoleScreenBlockEntity, CacheEntry> TEXTURE_CACHE = new WeakHashMap<>();
    private static final int DURATION = 100;

    private final BlockRenderManager blockRenderManager;

    public ConsoleScreenBlockRenderer(BlockEntityRendererFactory.Context ctx) {
        blockRenderManager = ctx.getRenderManager();
    }

    public static void tick() {
        TEXTURE_CACHE.entrySet().removeIf(e -> {
            if (e.getValue().ticksSinceUsed++ >= DURATION) {
                e.getValue().texture.close();
                return true;
            }
            return false;
        });
    }

    public static NativeImageBackedTexture getTexture(ConsoleScreenBlockEntity entity) {
        var entry = TEXTURE_CACHE.get(entity);
        var displayHash = entity.display.hashCode();
        if (entry != null) {
            if (entry.displayHash == displayHash) {
                entry.ticksSinceUsed = 0;
                return entry.texture;
            }
            entry.texture.close();
        }
        var newEntry = new CacheEntry(
                new NativeImageBackedTexture(entity.display.getWidth(), entity.display.getHeight(), false),
                displayHash
        );

        var nativeImage = newEntry.texture.getImage();
        for (int x = 0; x < entity.display.getWidth(); x++) {
            for (int y = 0; y < entity.display.getHeight(); y++) {
                nativeImage.setColor(x, y,
                        CanvasUtils.fromLimitedColor(entity.display.getRaw(x, y)) | 0xff000000);
            }
        }

        newEntry.texture.upload();
        TEXTURE_CACHE.put(entity, newEntry);
        return newEntry.texture;
    }

    @Override
    public void render(ConsoleScreenBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var facing = entity.getCachedState().get(ConsoleScreenBlock.FACING);

        matrices.push();

        var offset = facing.getUnitVector().mul(-0.5f);
        matrices.translate(offset.x(), offset.y(), offset.z());

        var model = blockRenderManager.getModels().getModel(entity.getCachedState());
        var random = Random.create(42);
        var vertexConsumer = vertexConsumers.getBuffer(TexturedRenderLayers.getEntityCutout());
        var matrix = matrices.peek();
        for (BakedQuad quad : model.getQuads(null, null, random)) {
            vertexConsumer.quad(matrix, quad, 1, 1, 1, 1, light, overlay);
        }

        matrices.pop();


        matrices.push();

        matrices.translate(0.5f, 0, 0.5f);
        matrices.multiply(RotationAxis.NEGATIVE_Y.rotationDegrees(180 + facing.asRotation()));
        matrices.translate(-0.5f, 0, -0.5f - 0.03125f);

        var texture = getTexture(entity);
        RenderSystem.setShaderTexture(0, texture.getGlId());
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        var position = matrices.peek().getPositionMatrix();
        var tessellator = Tessellator.getInstance();
        var bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferBuilder.vertex(position, 0, 0.125f, 0).texture(1, 1).color(0xffffffff);
        bufferBuilder.vertex(position, 0, 0.875f, 0).texture(1, 0).color(0xffffffff);
        bufferBuilder.vertex(position, 1, 0.875f, 0).texture(0, 0).color(0xffffffff);
        bufferBuilder.vertex(position, 1, 0.125f, 0).texture(0, 1).color(0xffffffff);
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

        matrices.pop();
    }

    static final class CacheEntry {
        private final NativeImageBackedTexture texture;
        private final int displayHash;
        private long ticksSinceUsed;

        CacheEntry(NativeImageBackedTexture texture, int displayHash) {
            this.texture = texture;
            this.displayHash = displayHash;
            this.ticksSinceUsed = 0;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (CacheEntry) obj;
            return Objects.equals(this.texture, that.texture) &&
                    this.displayHash == that.displayHash &&
                    this.ticksSinceUsed == that.ticksSinceUsed;
        }

        @Override
        public int hashCode() {
            return Objects.hash(texture, displayHash, ticksSinceUsed);
        }

        @Override
        public String toString() {
            return "CacheEntry[" +
                    "texture=" + texture + ", " +
                    "displayHash=" + displayHash + ", " +
                    "lastUsedTick=" + ticksSinceUsed + ']';
        }
    }
}
