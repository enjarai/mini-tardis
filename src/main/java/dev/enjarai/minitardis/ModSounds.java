package dev.enjarai.minitardis;

import net.minecraft.sound.SoundEvent;

public class ModSounds {
    public static SoundEvent CLOISTER_BELL = createSound("cloister_bell");
    public static SoundEvent TARDIS_TAKEOFF = createSound("tardis_takeoff");
    public static SoundEvent TARDIS_LANDING = createSound("tardis_landing");
    public static SoundEvent TARDIS_FLY_LOOP = createSound("tardis_fly_loop");
    public static SoundEvent TARDIS_FAILURE_SINGLE = createSound("tardis_failure_single"); // TODO
    public static SoundEvent CORAL_HUM = createSound("coral_hum");
    public static SoundEvent BAD_APPLE = createSound("bad_apple");

    private static SoundEvent createSound(String path) {
        return SoundEvent.of(MiniTardis.id(path));
    }

    public static void load() {
    }
}
