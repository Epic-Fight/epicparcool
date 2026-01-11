package com.yesman.epicparcool.client.screen;

import com.alrex.parcool.common.stamina.StaminaType;
import com.alrex.parcool.config.ParCoolConfig;
import com.yesman.epicparcool.EpicParCool;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ResizableComponent.HorizontalSizing;
import yesman.epicfight.client.gui.datapack.widgets.Static;
import yesman.epicfight.client.gui.widgets.EpicFightOptionList;

@OnlyIn(Dist.CLIENT)
public class EpicParCoolConfigurations extends Screen {
	protected final Screen parentScreen;
	private EpicFightOptionList optionsList;
	private static final StaminaType[] STAMINA_TYPE_ENUMS = StaminaType.values();
	
	public EpicParCoolConfigurations(final ModContainer mod, Screen screen) {
		super(Component.translatable("gui.epicparcool.config_screen.title"));
		
		this.parentScreen = screen;
	}
	
	@Override
	protected void init() {
		this.optionsList = new EpicFightOptionList(this.minecraft, this.width, this.height - 64, 32, 25);
		
		Button staminaSource = Button.builder(Component.translatable("gui." + EpicParCool.MODID + ".stamina_source." + ParseUtil.toLowerCase(ParCoolConfig.Client.getInstance().StaminaType.get().name())), (button) -> {
			StaminaType nextValue = STAMINA_TYPE_ENUMS[(ParCoolConfig.Client.getInstance().StaminaType.get().ordinal() + 1) % STAMINA_TYPE_ENUMS.length];
			ParCoolConfig.Client.getInstance().StaminaType.set(nextValue);
			button.setMessage(Component.translatable("gui." + EpicParCool.MODID + ".stamina_source." + ParseUtil.toLowerCase(nextValue.name())));
		}).pos(this.width - 170, this.height / 4 - 8).size(80, 20).build();
		
		this.optionsList.addSmall(new Static(this, 100, 100, 0, 15, HorizontalSizing.LEFT_WIDTH, null, "gui." + EpicParCool.MODID + ".config_screen.stamina_source"), staminaSource);
		
		this.addRenderableWidget(this.optionsList);
		
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
			ParCoolConfig.Client.getInstance().StaminaType.save();
			this.onClose();
		}).bounds(this.width / 2 - 100, this.height - 28, 200, 20).build());
	}
	
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		Component warning = Component.translatable("gui.epicparcool.config_screen.warn");
		
		this.renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
		guiGraphics.drawString(this.font, warning, 6, this.height - 50, 16777215);
		
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}
}
