package dev.enjarai.minitardis;

import net.minecraft.sound.SoundEvent;

public class ModSounds {
    public static SoundEvent CLOISTER_BELL = createSound("cloister_bell");
    public static SoundEvent TARDIS_TAKEOFF = createSound("tardis_takeoff");
    public static SoundEvent TARDIS_LANDING = createSound("tardis_landing");
    public static SoundEvent TARDIS_FLY_LOOP = createSound("tardis_fly_loop");
    public static SoundEvent TARDIS_FLY_LOOP_ERROR = createSound("tardis_fly_loop_error");
    public static SoundEvent TARDIS_CRASH_LAND = createSound("tardis_crash_land");
    public static SoundEvent CORAL_HUM = createSound("coral_hum");
    public static SoundEvent BAD_APPLE = createSound("bad_apple");
    public static SoundEvent SNAKE_MOVE = createSound("move_snake");
    public static SoundEvent EAT_APPLE = createSound("eat_apple");
    public static SoundEvent DIE_SNAKE = createSound("die_snake");

    private static SoundEvent createSound(String path) {
        return SoundEvent.of(MiniTardis.id(path));
    }

    public static void load() {
    }
}
