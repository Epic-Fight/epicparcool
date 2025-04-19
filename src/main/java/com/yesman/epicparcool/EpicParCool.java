package com.yesman.epicparcool;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.slf4j.Logger;

import com.alrex.parcool.ParCool;
import com.mojang.logging.LogUtils;
import com.yesman.epicparcool.client.event.ParCoolClientEvents;
import com.yesman.epicparcool.event.ParCoolEvents;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingException;
import net.minecraftforge.fml.ModLoadingStage;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import yesman.epicfight.api.animation.LivingMotion;

/**
 *  Major version change
 *  Created 20.10.1
 *  
 *  Animations for listed parcool actions
 *  - Cat leap
 *  - Charge jump
 *  - Cling to cliff
 *  - Dive
 *  - Fast run
 *  - Hang down
 *  - Roll
 *  - Sliding
 *  - Speed & Kong Vault
 *  - Wall climb
 *  - Wall jump
 *  - Wall run
 *  - Wall sliding
 *  
 *  ***************************************************************
 *  
 *  Minor changes in alpha verison
 *  20.10.1.3 -> 20.10.1.4
 *  
 *  Added Breakfall animation played when players succeed in canceling fall damage
 *  Added Vertical wall run animation
 *  Added Sky dive animation
 *  Fixed players sometimes unable to move while clinging to walls and moving
 *  Fixed players able to guard while running walls
 *  Fixed a crash caused by grabbing walls in the mining mode then move in the battle mode
 *  Now Epic Fight models are visible when filter animation is off in the mining mode
 *  Now players can't cancel attacks by flip
 *  
 *  @author yesman
 */
@Mod(EpicParCool.MODID)
public class EpicParCool {
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final String MODID = "epicparcool";
	public static final String LEAST_PARCOOL_VERSION = "3.3.1.0";
	
	public EpicParCool(FMLJavaModLoadingContext context) {
		ModContainer mc = ModList.get().getModContainerById(ParCool.MOD_ID).orElseThrow();
		IModInfo mInfo = mc.getModInfo();
		ArtifactVersion currentVersion = mc.getModInfo().getVersion();
		ArtifactVersion target = new DefaultArtifactVersion(EpicParCool.LEAST_PARCOOL_VERSION);
		
		if (currentVersion.compareTo(target) < 0) {
			throw new ModLoadingException(mInfo, ModLoadingStage.COMMON_SETUP, "Epic Parcool requires Parcool version " + EpicParCool.LEAST_PARCOOL_VERSION + " or over", null);
		}
		
		IEventBus modEventbus = context.getModEventBus();
		
		modEventbus.addListener(ParCoolEvents::onSetup);
		modEventbus.addListener(this::constructMod);
		
		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
    		modEventbus.addListener(ParCoolClientEvents::onSetup);
        });
	}
	
	public void constructMod(FMLConstructModEvent event) {
		LivingMotion.ENUM_MANAGER.registerEnumCls(EpicParCool.MODID, ParcoolLivingMotions.class);
	}
}
