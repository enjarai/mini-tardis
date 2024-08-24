package dev.enjarai.minitardis.ccacomponent;

import com.mojang.serialization.Codec;

public record HistoryEntry(TardisLocation location) {
    public static final Codec<HistoryEntry> CODEC = TardisLocation.CODEC
            .xmap(HistoryEntry::new, HistoryEntry::location).fieldOf("location").codec();
}
