package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.alrex.parcool.common.action.impl.ClingToCliff;
import com.alrex.parcool.common.attachment.common.Parkourability;
import com.yesman.epicparcool.animations.ParCoolAnimations;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = ClingToCliff.class)
public class ParCoolMixinClingToCliff {
	@Inject(at = @At(value = "HEAD"), method = "onRenderTick(Lnet/neoforged/neoforge/client/event/RenderFrameEvent;Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/attachment/common/Parkourability;)V", cancellable = true, remap = false)
	public void epicparcool$onRenderTick(RenderFrameEvent event, Player player, Parkourability parkourability, CallbackInfo callback) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isEpicFightMode()) {
			callback.cancel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "canContinue(Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/attachment/common/Parkourability;)Z", cancellable = true, remap = false)
	public void epicparcool$canContinue(Player player, Parkourability parkourability, CallbackInfoReturnable<Boolean> callback) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isEpicFightMode()) {
			AssetAccessor<? extends StaticAnimation> nowPlaying = playerpatch.getAnimator().getPlayerFor(null).getRealAnimation();
			
			if (nowPlaying == ParCoolAnimations.BIPED_CLING_START ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_INNER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_INNER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_INNER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_INNER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_OUTER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT_OUTER_CORNER2 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER1 ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT_OUTER_CORNER2
			) {
				callback.setReturnValue(true);
				callback.cancel();
			}
		}
	}
}