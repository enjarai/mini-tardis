package dev.enjarai.minitardis;

import io.wispforest.endec.Endec;

import java.util.function.IntFunction;

public class ImGoingToStabBasique {
    public static <T> Endec<T[]> arrayOf(Endec<T> elementEndec, IntFunction<T[]> arrayFactory) {
        return Endec.of((ctx, serializer, array) -> {
            try (var sequence = serializer.sequence(ctx, elementEndec, array.length)) {
                for (var element : array) {
                    sequence.element(element);
                }
            }
        }, (ctx, deserializer) -> {
            var sequenceState = deserializer.sequence(ctx, elementEndec);

            var array = arrayFactory.apply(sequenceState.estimatedSize());

            int i = 0;
            for (var element : (Iterable<T>) () -> sequenceState) {
                array[i++] = element;
            }

            return array;
        });
    }

    public static final Endec<short[]> DEEZ = arrayOf(Endec.SHORT, Short[]::new)
            .xmap(boxed -> {
                short[] prim = new short[boxed.length];
                for (int i = 0; i < boxed.length; i++) prim[i] = boxed[i];
                return prim;
            }, prim -> {
                Short[] boxed = new Short[prim.length];
                for (int i = 0; i < prim.length; i++) boxed[i] = prim[i];
                return boxed;
            });
}
