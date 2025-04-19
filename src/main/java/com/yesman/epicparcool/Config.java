package com.yesman.epicparcool;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = EpicParCool.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    /**
    private static final ForgeConfigSpec.ConfigValue<String> ACCESS_TOKEN = BUILDER
            .comment("Web cache data for auto login to epic fight patron server")
            .define("access_token", "");
    
    private static final ForgeConfigSpec.ConfigValue<String> REFRESH_TOKNE = BUILDER
            .define("refresh_token", "");
    **/
    static final ForgeConfigSpec SPEC = BUILDER.build();
    
    @SubscribeEvent
	static void onLoad(final ModConfigEvent event) {
    }
}
