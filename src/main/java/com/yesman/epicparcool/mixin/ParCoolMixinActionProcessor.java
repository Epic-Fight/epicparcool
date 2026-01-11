package com.yesman.epicparcool.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.ActionProcessor;
import com.alrex.parcool.common.action.StaminaConsumeTiming;
import com.alrex.parcool.common.action.impl.Dodge;

import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = ActionProcessor.class)
public class ParCoolMixinActionProcessor {
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lcom/alrex/parcool/common/action/Action;getStaminaConsumeTiming()Lcom/alrex/parcool/common/action/StaminaConsumeTiming;"
		),
		method = "processAction(Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/attachment/common/Parkourability;Ljava/util/LinkedList;ZLcom/alrex/parcool/common/action/Action;)V",
		remap = false
	)
	public StaminaConsumeTiming epicparcool$getStaminaConsumeTimingInTick(Action action, Player player) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);

		if (playerpatch != null && playerpatch.isEpicFightMode()) {
			if (action.getClass() == Dodge.class) {
				return null;
			}
		}

		return action.getStaminaConsumeTiming();
	}
}