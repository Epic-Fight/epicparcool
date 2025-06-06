package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import yesman.epicfight.api.animation.types.LinkAnimation;
import yesman.epicfight.api.client.animation.Layer;

@Mixin(value = Layer.class)
public interface EpicFightMixinLayer {
	@Accessor
	public LinkAnimation getLinkAnimation();
}
