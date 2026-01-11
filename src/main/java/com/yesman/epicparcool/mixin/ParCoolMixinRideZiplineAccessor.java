package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.common.action.impl.RideZipline;

import net.minecraft.world.phys.Vec3;

@Mixin(value = RideZipline.class)
public interface ParCoolMixinRideZiplineAccessor {
	@Accessor()
	Vec3 getEndOffsetFromStart();
	
	@Accessor()
	double getSpeed();
}
