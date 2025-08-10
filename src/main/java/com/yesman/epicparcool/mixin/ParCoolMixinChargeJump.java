package com.yesman.epicparcool.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.alrex.parcool.common.action.impl.ChargeJump;

import net.minecraft.client.player.LocalPlayer;
import yesman.epicfight.client.ClientEngine;

@Mixin(value = ChargeJump.class)
public class ParCoolMixinChargeJump {
	
	@Redirect(
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/player/LocalPlayer;isShiftKeyDown()Z"
		),
		method = "onClientTick(Lnet/minecraft/world/entity/player/Player;Lcom/alrex/parcool/common/capability/Parkourability;Lcom/alrex/parcool/common/capability/IStamina;)V",
		remap = false
	)
	private boolean epicparcool_isShiftKeyDown(LocalPlayer self) {
		if (ClientEngine.getInstance().controlEngine.moverToggling()) {
			return true;
		}
		
		return self.isShiftKeyDown();
	}
}