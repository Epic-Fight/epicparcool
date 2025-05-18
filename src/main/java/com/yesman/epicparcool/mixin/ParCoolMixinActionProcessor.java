package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.ActionProcessor;
import com.alrex.parcool.common.action.StaminaConsumeTiming;
import com.alrex.parcool.common.action.impl.Dodge;

import net.minecraftforge.event.TickEvent;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mixin(value = ActionProcessor.class)
public class ParCoolMixinActionProcessor {
	@Redirect(	  at = @At( value = "INVOKE"
						  , target = "Lcom/alrex/parcool/common/action/Action;getStaminaConsumeTiming()Lcom/alrex/parcool/common/action/StaminaConsumeTiming;"
						  )
				, method = "onTick(Lnet/minecraftforge/event/TickEvent$PlayerTickEvent;)V"
				, remap = false
			 )
	public StaminaConsumeTiming epicfight_getStaminaConsumeTimingInTick(Action action, TickEvent.PlayerTickEvent event) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(event.player, PlayerPatch.class);
		
		if (playerpatch != null && playerpatch.isEpicFightMode()) {
			if (action.getClass() == Dodge.class) {
				return null;
			}
		}
		
		return action.getStaminaConsumeTiming();
	}
}