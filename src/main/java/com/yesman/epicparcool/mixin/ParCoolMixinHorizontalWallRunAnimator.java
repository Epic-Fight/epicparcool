package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.alrex.parcool.client.animation.impl.HorizontalWallRunAnimator;

@Mixin(value = HorizontalWallRunAnimator.class)
public interface ParCoolMixinHorizontalWallRunAnimator {
	@Accessor
	public boolean getWallIsRightSide();
}