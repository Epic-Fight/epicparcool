package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.client.animation.impl.DiveAnimationHostAnimator;
import com.alrex.parcool.client.animation.impl.DiveAnimationHostAnimator.SkyDiveAnimator;

@Mixin(value = DiveAnimationHostAnimator.class)
public interface ParCoolMixinDiveAnimationHostAnimator {
	@Accessor("skyDiveAnimator")
	public SkyDiveAnimator getSkyDiveAnimator();
}