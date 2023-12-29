package dev.enjarai.minitardis.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.JsonSerializer;

public class CodecUtils {
    /**
     * Create a loot table json serializer from a codec, only supports codecs that serialize to objects.
     */
    public static <T> JsonSerializer<T> toJsonSerializer(Codec<T> codec) {
        return new JsonSerializer<T>() {
            @Override
            public void toJson(JsonObject json, T object, JsonSerializationContext context) {
                codec.encodeStart(JsonOps.INSTANCE, object).result().ifPresent(element ->
                        element.getAsJsonObject().entrySet().forEach(entry -> json.add(entry.getKey(), entry.getValue())));
            }

            @Override
            public T fromJson(JsonObject json, JsonDeserializationContext context) {
                //noinspection OptionalGetWithoutIsPresent
                return codec.decode(JsonOps.INSTANCE, json).result().get().getFirst();
            }
        };
    }
}
