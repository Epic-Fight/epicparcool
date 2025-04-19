package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = com.alrex.parcool.common.capability.Animation.class)
public interface ParCoolMixinAnimation {
	@Accessor
	public com.alrex.parcool.client.animation.Animator getAnimator();
}