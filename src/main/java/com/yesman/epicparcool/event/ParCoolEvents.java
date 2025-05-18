package com.yesman.epicparcool.event;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.function.BiFunction;

import com.alrex.parcool.api.unstable.action.ParCoolActionEvent;
import com.alrex.parcool.client.input.KeyBindings;
import com.alrex.parcool.common.action.Action;
import com.alrex.parcool.common.action.impl.CatLeap;
import com.alrex.parcool.common.action.impl.ChargeJump;
import com.alrex.parcool.common.action.impl.ClimbUp;
import com.alrex.parcool.common.action.impl.ClingToCliff;
import com.alrex.parcool.common.action.impl.Crawl;
import com.alrex.parcool.common.action.impl.Dodge;
import com.alrex.parcool.common.action.impl.Flipping;
import com.alrex.parcool.common.action.impl.HangDown;
import com.alrex.parcool.common.action.impl.JumpFromBar;
import com.alrex.parcool.common.action.impl.Roll;
import com.alrex.parcool.common.action.impl.Slide;
import com.alrex.parcool.common.action.impl.Vault;
import com.alrex.parcool.common.action.impl.VerticalWallRun;
import com.alrex.parcool.common.action.impl.WallJump;
import com.alrex.parcool.common.capability.IStamina;
import com.alrex.parcool.common.capability.Parkourability;
import com.google.common.collect.Maps;
import com.yesman.epicparcool.EpicParCool;
import com.yesman.epicparcool.ParCoolUtils;
import com.yesman.epicparcool.ParcoolLivingMotions;
import com.yesman.epicparcool.animations.ParCoolAnimations;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.forgeevent.InitAnimatorEvent;
import yesman.epicfight.client.input.EpicFightKeyMappings;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.SkillSlots;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

@Mod.EventBusSubscriber(modid = EpicParCool.MODID)
public class ParCoolEvents {
	private static final Map<Class<? extends com.alrex.parcool.common.action.Action>, BiFunction<PlayerPatch<?>, ParCoolActionEvent.StartEvent, AssetAccessor<? extends StaticAnimation>>> PARCOOL_ACTION_START_MAPPING = Maps.newHashMap();
	private static final Map<Class<? extends com.alrex.parcool.common.action.Action>, BiFunction<PlayerPatch<?>, Action, Boolean>> PARCOOL_ACTION_CANCEL_EVENTS = Maps.newHashMap();
	private static final ByteBuffer DUMMY_BUFFER = ByteBuffer.allocate(128);
	
	//Mod bus event
	public static void onSetup(FMLCommonSetupEvent event) {
		PARCOOL_ACTION_START_MAPPING.clear();
		PARCOOL_ACTION_CANCEL_EVENTS.clear();
		
		PARCOOL_ACTION_START_MAPPING.put(CatLeap.class, (playerpatch, startEvent) -> {
			Parkourability parkourability = Parkourability.get(startEvent.getPlayer());
			
			if (parkourability.get(Slide.class).isDoing() || parkourability.get(Crawl.class).isDoing()) {
				return null;
			}
			
			return ParCoolAnimations.BIPED_CAT_LEAP;
		});
		
		PARCOOL_ACTION_START_MAPPING.put(Roll.class, (playerpatch, startEvent) -> {
			return ParCoolAnimations.BIPED_ROLL_FORWARD;
		});
		
		PARCOOL_ACTION_START_MAPPING.put(Flipping.class, (playerpatch, startEvent) -> {
			LocalPlayer clientPlayer = (LocalPlayer)startEvent.getPlayer();
			
			if (clientPlayer.input.forwardImpulse < 0.0F) {
				return ParCoolAnimations.BIPED_FLIP_BACKWARD;
			} else {
				return ParCoolAnimations.BIPED_FLIP_FOWARD;
			}
		});
		
		PARCOOL_ACTION_START_MAPPING.put(HangDown.class, (playerpatch, startEvent) -> {
			HangDown action = ((HangDown)startEvent.getAction());
			float yRot = ParCoolUtils.idealYRotForHanging(action, startEvent.getPlayer());
			
			if (action.isOrthogonalToBar()) {
				playerpatch.setModelYRot(yRot, true);
				return ParCoolAnimations.BIPED_HANG_DOWN_INERTIA_ORTHOGONAL;
			} else {
				playerpatch.setModelYRot(yRot, true);
				return ParCoolAnimations.BIPED_HANG_DOWN_INERTIA;
			}
		});
		
		PARCOOL_ACTION_START_MAPPING.put(Vault.class, (playerpatch, startEvent) -> {
			Vault.AnimationType type = ((Vault)startEvent.getAction()).getCurrentAnimation();
			
			switch (type) {
			case KONG_VAULT -> {
				return ParCoolAnimations.BIPED_VAULT_FORWARD;
			}
			case SPEED_VAULT_LEFT -> {
				return ParCoolAnimations.BIPED_VAULT_LEFT;
			}
			case SPEED_VAULT_RIGHT -> {
				return ParCoolAnimations.BIPED_VAULT_RIGHT;
			}
			default -> {
				throw new UnsupportedOperationException("Invalid animation type");
			}
			}
		});
		
		PARCOOL_ACTION_START_MAPPING.put(ClimbUp.class, (playerpatch, startEvent) -> {
			playerpatch.getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLIFF_Y_ROT).ifPresent((yRot) -> {
				playerpatch.setModelYRot(yRot, true);
			});
			
			return ParCoolAnimations.BIPED_CLIMB_UP_NO_ACTION;
		});
		
		PARCOOL_ACTION_START_MAPPING.put(ChargeJump.class, (playerpatch, startEvent) -> {
			return ParCoolAnimations.BIPED_CAT_LEAP;
		});
		
		PARCOOL_ACTION_START_MAPPING.put(Dodge.class, (playerpatch, startEvent) -> {
			LocalPlayer clientPlayer = (LocalPlayer)startEvent.getPlayer();
			AnimationAccessor<? extends StaticAnimation> rollAnimation = ParCoolAnimations.BIPED_ROLL_FORWARD;
			
			if (clientPlayer.input.leftImpulse < -0.5F) {
				rollAnimation = ParCoolAnimations.BIPED_ROLL_RIGHT;
			} else if (clientPlayer.input.leftImpulse > 0.5F) {
				rollAnimation = ParCoolAnimations.BIPED_ROLL_LEFT;
			} else if (clientPlayer.input.forwardImpulse < -0.5F) {
				rollAnimation = ParCoolAnimations.BIPED_ROLL_BACKWARD;
			}
			
			playerpatch.setStamina(playerpatch.getStamina() - EpicFightSkills.ROLL.getConsumption());
			
			return rollAnimation;
		});
		
		PARCOOL_ACTION_START_MAPPING.put(VerticalWallRun.class, (playerpatch, startEvent) -> {
			return ParCoolAnimations.BIPED_WALL_RUN_VERTICAL;
		});
		
		PARCOOL_ACTION_CANCEL_EVENTS.put(ClimbUp.class, (playerpatch, action) -> {
			if (playerpatch.getEntityState().inaction()) {
				return true;
			}
			
			return false;
		});
		
		PARCOOL_ACTION_CANCEL_EVENTS.put(JumpFromBar.class, (playerpatch, action) -> {
			Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
			IStamina stamina = IStamina.get(playerpatch.getOriginal());
			DUMMY_BUFFER.clear();
			
			if (parkourability.get(JumpFromBar.class).canStart(playerpatch.getOriginal(), parkourability, stamina, DUMMY_BUFFER)) {
				if (parkourability.get(HangDown.class).isOrthogonalToBar()) {
					playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_JUMP_FROM_BAR_START_ORTHOGONAL, 0.0F);
				} else {
					playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_JUMP_FROM_BAR_START, 0.0F);
				}
				
				return true;
			}
			
			return false;
		});
		
		PARCOOL_ACTION_CANCEL_EVENTS.put(Vault.class, (playerpatch, action) -> {
			if (playerpatch.getAnimator().getPlayerFor(null).getAnimation().get().getRealAnimation() == ParCoolAnimations.BIPED_CLIMB_UP_NO_ACTION) {
				return true;
			}
			
			return false;
		});
		
		PARCOOL_ACTION_CANCEL_EVENTS.put(WallJump.class, (playerpatch, action) -> {
			AssetAccessor<? extends StaticAnimation> currentPlay = playerpatch.getAnimator().getPlayerFor(null).getAnimation().get().getRealAnimation();
			
			if (
				currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF ||
				currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_INNER_CORNER ||
				currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_OUTER_CORNER ||
				currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_LEFT ||
				currentPlay == ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_RIGHT
			) {
				Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
				IStamina stamina = IStamina.get(playerpatch.getOriginal());
				DUMMY_BUFFER.clear();
				
				if (parkourability.get(ClingToCliff.class).isDoing() && parkourability.get(WallJump.class).canStart(playerpatch.getOriginal(), parkourability, stamina, DUMMY_BUFFER)) {
					DUMMY_BUFFER.flip();
					Vec3 jumpDirection = new Vec3(DUMMY_BUFFER.getDouble(), DUMMY_BUFFER.getDouble(), DUMMY_BUFFER.getDouble());
					Vec3 wallDirection = new Vec3(DUMMY_BUFFER.getDouble(), 0.0D, DUMMY_BUFFER.getDouble());
					byte animType = DUMMY_BUFFER.get();
					
					switch (animType) {
					case 0 -> {
						//entitypatch.playAnimationSynchronized(ParCoolAnimations.BIPED_JUMP_FROM_BAR_START, 0.0f);
					}
					case 1 -> {
						playerpatch.getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLIFF_Y_ROT).ifPresent((yRot) -> {
							playerpatch.setModelYRot(yRot - 90.0F, true);
						});
						
						playerpatch.getAnimator().getVariables().put(ParCoolAnimations.JUMP_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_LEFT_START, jumpDirection);
						playerpatch.getAnimator().getVariables().put(ParCoolAnimations.WALL_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_LEFT_START, wallDirection);
						playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_WALL_JUMP_LEFT_START, 0.0F);
					}
					case 2 -> {
						playerpatch.getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLIFF_Y_ROT).ifPresent((yRot) -> {
							playerpatch.setModelYRot(yRot + 90.0F, true);
						});
						
						playerpatch.getAnimator().getVariables().put(ParCoolAnimations.JUMP_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_RIGHT_START, jumpDirection);
						playerpatch.getAnimator().getVariables().put(ParCoolAnimations.WALL_DIRECTION, ParCoolAnimations.BIPED_WALL_JUMP_RIGHT_START, wallDirection);
						playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_WALL_JUMP_RIGHT_START, 0.0F);
					}
					default -> {
						throw new UnsupportedOperationException("No matching wall jump animation type " + animType);
					}
					}
				}
				
				return true;
			} else {
				Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
				IStamina stamina = IStamina.get(playerpatch.getOriginal());
				DUMMY_BUFFER.clear();
				
				if (parkourability.get(WallJump.class).canStart(playerpatch.getOriginal(), parkourability, stamina, DUMMY_BUFFER)) {
					DUMMY_BUFFER.flip();
					DUMMY_BUFFER.getDouble();
					DUMMY_BUFFER.getDouble();
					DUMMY_BUFFER.getDouble();
					DUMMY_BUFFER.getDouble();
					DUMMY_BUFFER.getDouble();
					byte animType = DUMMY_BUFFER.get();
					
					switch (animType) {
					case 0 -> {
					}
					case 1 -> {
						playerpatch.playAnimationInClientSide(ParCoolAnimations.BIPED_WALL_JUMP_LEFT, 0.0F);
					}
					case 2 -> {
						playerpatch.playAnimationInClientSide(ParCoolAnimations.BIPED_WALL_JUMP_RIGHT, 0.0F);
					}
					}
				}
				
				return false;
			}
		});
		
		PARCOOL_ACTION_CANCEL_EVENTS.put(ClingToCliff.class, (playerpatch, action) -> {
			if (playerpatch.getAnimator().getPlayerFor(null).getAnimation().get().getRealAnimation() == ParCoolAnimations.BIPED_CLIMB_UP_NO_ACTION) {
				return true;
			}
			
			Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
			IStamina stamina = IStamina.get(playerpatch.getOriginal());
			DUMMY_BUFFER.clear();
			
			if (parkourability.get(ClingToCliff.class).canStart(playerpatch.getOriginal(), parkourability, stamina, DUMMY_BUFFER)) {
				if (!ParCoolUtils.scanTerrainAndStartClingAction(playerpatch, ParCoolUtils.WallMoveType.CLING_START)) {
					return true;
				}
			}
			
			return false;
		});
		
		PARCOOL_ACTION_CANCEL_EVENTS.put(Dodge.class, (playerpatch, action) -> {
			if (EpicFightKeyMappings.DODGE.getKey() == KeyBindings.getKeyDodge().getKey()) {
				if (playerpatch.getSkill(SkillSlots.DODGE).getSkill() != null) {
					return true;
				}
			}
			
			if (!playerpatch.getOriginal().isCreative() && !playerpatch.hasStamina(EpicFightSkills.ROLL.getConsumption())) {
				return true;
			}
			
			return false;
		});
		
		PARCOOL_ACTION_CANCEL_EVENTS.put(Flipping.class, (playerpatch, action) -> {
			if (playerpatch.getEntityState().movementLocked()) {
				return true;
			}
			
			return false;
		});
	}
	
	@SubscribeEvent
	public static void onInitAnimatorEvent(InitAnimatorEvent event) {
		if (event.getEntityPatch() instanceof PlayerPatch<?>) {
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CAT_LEAP_PREPARATION, ParCoolAnimations.BIPED_CAT_LEAP_PREPARATION);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF, ParCoolAnimations.BIPED_CLING_TO_CLIFF);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_INNER_CORNER, ParCoolAnimations.BIPED_CLING_TO_CLIFF_INNER_CORNER);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_OUTER_CORNER, ParCoolAnimations.BIPED_CLING_TO_CLIFF_OUTER_CORNER);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_LEFT, ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_LEFT);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CLING_TO_CLIFF_RIGHT, ParCoolAnimations.BIPED_CLING_TO_CLIFF_LOOK_RIGHT);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.DIVE, ParCoolAnimations.BIPED_DIVE);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.SKY_DIVE, ParCoolAnimations.BIPED_SKY_DIVE);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_SLIDING_LEFT, ParCoolAnimations.BIPED_WALL_SLIDE_LEFT);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_SLIDING_RIGHT, ParCoolAnimations.BIPED_WALL_SLIDE_RIGHT);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_RUNNING_LEFT, ParCoolAnimations.BIPED_WALL_RUN_LEFT);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.WALL_RUNNING_RIGHT, ParCoolAnimations.BIPED_WALL_RUN_RIGHT);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.FAST_RUN, ParCoolAnimations.BIPED_FAST_RUN);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.HANG_DOWN_ORTHOGONAL, ParCoolAnimations.BIPED_HANG_DOWN_ORTHOGONAL);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.HANG_DOWN, ParCoolAnimations.BIPED_HANG_DOWN);
			event.getAnimator().addLivingAnimation(ParcoolLivingMotions.SLIDING, ParCoolAnimations.BIPED_SLIDE);
			//event.getAnimator().addLivingAnimation(ParcoolLivingMotions.CRAWL, ParCoolAnimations.BIPED_CRAWL);
		}
	}
	
	@SubscribeEvent
	public static void onParCoolActionEvent$TryToStartEvent(ParCoolActionEvent.TryToStartEvent event) {
		if (PARCOOL_ACTION_CANCEL_EVENTS.containsKey(event.getAction().getClass())) {
			PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(event.getPlayer(), PlayerPatch.class);
			
			if (playerpatch != null && playerpatch.isEpicFightMode() && PARCOOL_ACTION_CANCEL_EVENTS.get(event.getAction().getClass()).apply(playerpatch, event.getAction())) {
				event.setCanceled(true);
			}
		}
	}
	
	@SubscribeEvent
	public static void onParCoolActionEvent$StartEvent(ParCoolActionEvent.StartEvent event) {
		PlayerPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(event.getPlayer(), PlayerPatch.class);
		
		if (entitypatch != null && entitypatch.isLogicalClient() && entitypatch.getOriginal().isLocalPlayer() && entitypatch.isEpicFightMode() && PARCOOL_ACTION_START_MAPPING.containsKey(event.getAction().getClass())) {
			AssetAccessor<? extends StaticAnimation> animation = PARCOOL_ACTION_START_MAPPING.get(event.getAction().getClass()).apply(entitypatch, event);
			
			if (animation != null) {
				entitypatch.playAnimationSynchronized(animation, 0.0F);
			}
		}
		
		if (event.getAction().getClass() == ClingToCliff.class) {
			event.getPlayer().setForcedPose(net.minecraft.world.entity.Pose.STANDING);
		}
	}
	
	@SubscribeEvent
	public static void onParCoolActionEvent$StopEvent(ParCoolActionEvent.StopEvent event) {
		if (event.getAction().getClass() == ClingToCliff.class) {
			event.getPlayer().setForcedPose(null);
		}
	}
}
