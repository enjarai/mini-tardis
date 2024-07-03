package dev.enjarai.minitardis.item;

import net.minecraft.component.ComponentType;

public class ModDataComponents {

    public static final ComponentType<ScreenApps> SCREEN_APPS_COMPONENT_TYPE = ComponentType.<ScreenApps>builder().codec(ScreenApps.CODEC).build();

}
