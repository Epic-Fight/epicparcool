package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.alrex.parcool.client.animation.Animator;
import com.alrex.parcool.client.animation.impl.SpeedVaultAnimator;
import com.alrex.parcool.common.capability.Parkourability;

import net.minecraft.world.entity.player.Player;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = SpeedVaultAnimator.class)
public abstract class ParCoolMixinSpeedVaultAnimator extends Animator {
	private static final int EPICFIGHT_MAX_TICK = 6;
	
	@Inject(at = @At(value = "HEAD"), method = "shouldRemoved(Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/capability/Parkourability;)Z", cancellable = true, remap = false)
	public void epicfight_shouldRemoved(Player player, Parkourability parkourability, CallbackInfoReturnable<Boolean> info) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isBattleMode()) {
			info.setReturnValue(super.getTick() >= EPICFIGHT_MAX_TICK);
			info.cancel();
		}
	}
}
