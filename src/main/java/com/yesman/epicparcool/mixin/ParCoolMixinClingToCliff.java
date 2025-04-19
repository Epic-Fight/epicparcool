package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.alrex.parcool.common.action.impl.ClingToCliff;
import com.alrex.parcool.common.capability.IStamina;
import com.alrex.parcool.common.capability.Parkourability;
import com.yesman.epicparcool.animations.ParCoolAnimations;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = ClingToCliff.class)
public class ParCoolMixinClingToCliff {
	@Inject(at = @At(value = "HEAD"), method = "onRenderTick(Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/capability/Parkourability;)V", cancellable = true, remap = false)
	public void epicfight_onRenderTick(TickEvent.RenderTickEvent event, Player player, Parkourability parkourability, CallbackInfo callback) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isBattleMode()) {
			callback.cancel();
		}
	}
	
	@Inject(at = @At(value = "HEAD"), method = "canContinue(Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/capability/Parkourability;Lcom/alrex/parcool/common/capability/IStamina;)Z", cancellable = true, remap = false)
	public void epicfight_canContinue(Player player, Parkourability parkourability, IStamina stamina, CallbackInfoReturnable<Boolean> callback) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isBattleMode()) {
			AssetAccessor<? extends StaticAnimation> nowPlaying = playerpatch.getAnimator().getPlayerFor(null).getAnimation().get().getRealAnimation();
			
			if (nowPlaying == ParCoolAnimations.BIPED_CLING_START ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_LEFT ||
				nowPlaying == ParCoolAnimations.BIPED_CLING_MOVE_RIGHT ||
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
	
	@Inject(at = @At(value = "TAIL"), method = "onWorkingTickInLocalClient(Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/capability/Parkourability;Lcom/alrex/parcool/common/capability/IStamina;)V", cancellable = true, remap = false)
	public void epicfight_onWorkingTickInLocalClient(Player player, Parkourability parkourability, IStamina stamina, CallbackInfo callback) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isBattleMode()) {
			player.setDeltaMovement(0, 0, 0);
		}
	}
}