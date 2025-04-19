package com.yesman.epicparcool.client.event;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.UUID;

import com.alrex.parcool.client.animation.impl.ClingToCliffAnimator;
import com.alrex.parcool.client.animation.impl.DiveAnimationHostAnimator;
import com.alrex.parcool.client.animation.impl.FastRunningAnimator;
import com.alrex.parcool.client.animation.impl.HangAnimator;
import com.alrex.parcool.client.animation.impl.HorizontalWallRunAnimator;
import com.alrex.parcool.client.animation.impl.JumpChargingAnimator;
import com.alrex.parcool.client.animation.impl.SlidingAnimator;
import com.alrex.parcool.client.animation.impl.WallSlideAnimator;
import com.alrex.parcool.common.action.impl.ClingToCliff;
import com.alrex.parcool.common.action.impl.HangDown;
import com.alrex.parcool.common.action.impl.HangDown.BarAxis;
import com.alrex.parcool.common.action.impl.VerticalWallRun;
import com.alrex.parcool.common.action.impl.WallJump;
import com.alrex.parcool.common.action.impl.WallSlide;
import com.alrex.parcool.common.capability.IStamina;
import com.alrex.parcool.common.capability.Parkourability;
import com.alrex.parcool.common.capability.capabilities.Capabilities;
import com.alrex.parcool.utilities.VectorUtil;
import com.google.common.collect.Maps;
import com.yesman.epicparcool.EpicParCool;
import com.yesman.epicparcool.ParCoolUtils;
import com.yesman.epicparcool.ParcoolLivingMotions;
import com.yesman.epicparcool.ParCoolUtils.ClingType;
import com.yesman.epicparcool.animations.ParCoolAnimations;
import com.yesman.epicparcool.mixin.ParCoolMixinAnimation;
import com.yesman.epicparcool.mixin.ParCoolMixinDiveAnimationHostAnimator;
import com.yesman.epicparcool.mixin.ParCoolMixinHorizontalWallRunAnimator;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.gameasset.EpicFightSkills;
import yesman.epicfight.skill.SkillDataKeys;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

@Mod.EventBusSubscriber(modid = EpicParCool.MODID, value = Dist.CLIENT)
public class ParCoolClientEvents {
	@FunctionalInterface
	public interface LifecycleAnimationLinker {
		void accept(com.alrex.parcool.client.animation.Animator animator, Parkourability parkourability, UpdatePlayerMotionEvent.BaseLayer animationUpdateEvent);
	}
	
	private static final Map<Class<? extends com.alrex.parcool.client.animation.Animator>, LifecycleAnimationLinker> PARCOOL_ANIMATOR_MAPPING = Maps.newHashMap();
	private static final ByteBuffer DUMMY_BUFFER = ByteBuffer.allocate(128);
	private static final UUID EVENT_UUID = UUID.fromString("bc79276d-a0d1-4e58-867f-6bdd25d1ba23");
	
	//Mod bus event
	public static void onSetup(FMLClientSetupEvent event) {
		PARCOOL_ANIMATOR_MAPPING.clear();
		
		PARCOOL_ANIMATOR_MAPPING.put(JumpChargingAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CAT_LEAP_PREPARATION);
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(ClingToCliffAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.getPlayerPatch().getAnimator().getVariables().getSharedVariable(ParCoolAnimations.CLING_TYPE).ifPresentOrElse((clingDirection) -> {
				if (clingDirection == ClingType.OUTER_CORNER) {
					livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF_OUTER_CORNER);
				} else if (clingDirection == ClingType.INNER_CORNER) {
					livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF_INNER_CORNER);
				} else {
					livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF);
				}
			}, () -> {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CLING_TO_CLIFF);
			});
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(DiveAnimationHostAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			if (livingMotionUpdateEvent.getPlayerPatch().getOriginal().isFallFlying()) {
				return;
			}
			
			if (((ParCoolMixinDiveAnimationHostAnimator)animator).getSkyDiveAnimator() != null) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.SKY_DIVE);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.DIVE);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(WallSlideAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			Vec3 wall = parkourability.get(WallSlide.class).getLeanedWallDirection();
			
			if (wall == null) {
				return;
			}
			
			Vec3 bodyVec = VectorUtil.fromYawDegree(livingMotionUpdateEvent.getPlayerPatch().getOriginal().yBodyRot);
			Vec3 vec = new Vec3(bodyVec.x, 0, bodyVec.z).normalize();
			Vec3 dividedVec = new Vec3(vec.x * wall.x + vec.z * wall.z, 0, -vec.x * wall.z + vec.z * wall.x).normalize();
			
			if (dividedVec.z < 0) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_SLIDING_RIGHT);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_SLIDING_LEFT);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(HorizontalWallRunAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			if (((ParCoolMixinHorizontalWallRunAnimator)animator).getWallIsRightSide()) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_RUNNING_RIGHT);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.WALL_RUNNING_LEFT);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(FastRunningAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			if (livingMotionUpdateEvent.getMotion() == LivingMotions.RUN) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.FAST_RUN);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(HangAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			HangDown hangDown = parkourability.get(HangDown.class);
			
			if (hangDown.isOrthogonalToBar()) {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.HANG_DOWN_ORTHOGONAL);
			} else {
				livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.HANG_DOWN);
			}
		});
		
		PARCOOL_ANIMATOR_MAPPING.put(SlidingAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.SLIDING);
		});
		
		/**
		PARCOOL_ANIMATOR_MAPPING.put(CrawlAnimator.class, (animator, parkourability, livingMotionUpdateEvent) -> {
			livingMotionUpdateEvent.setMotion(ParcoolLivingMotions.CRAWL);
		});
		**/
	}
	
	@SubscribeEvent
	public static void onJoinWorldEvent(EntityJoinLevelEvent event) {
		PlayerPatch<?> playerpatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), PlayerPatch.class);
		
		if (playerpatch == null) {
			return;
		}
		
		playerpatch.getEventListener().addEventListener(EventType.SKILL_EXECUTE_EVENT, EVENT_UUID, (skillexecuteevent) -> {
			// Extend phantom ascent
			if (skillexecuteevent.getSkillContainer().getSkill() == EpicFightSkills.PHANTOM_ASCENT) {
				Parkourability parkourability = Parkourability.get(playerpatch.getOriginal());
				
				if (parkourability.get(ClingToCliff.class).isDoing()) {
					skillexecuteevent.setCanceled(true);
					return;
				}
				
				DUMMY_BUFFER.clear();
				
				if (parkourability.get(WallJump.class).canStart(playerpatch.getOriginal(), parkourability, IStamina.get(playerpatch.getOriginal()), DUMMY_BUFFER)) {
					DUMMY_BUFFER.flip();
					skillexecuteevent.setCanceled(true);
					skillexecuteevent.getSkillContainer().getDataManager().setData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), true);
				}
				
				DUMMY_BUFFER.clear();
				
				if (parkourability.get(VerticalWallRun.class).canStart(playerpatch.getOriginal(), parkourability, IStamina.get(playerpatch.getOriginal()), DUMMY_BUFFER)) {
					DUMMY_BUFFER.flip();
					skillexecuteevent.setCanceled(true);
					skillexecuteevent.getSkillContainer().getDataManager().setData(SkillDataKeys.JUMP_KEY_PRESSED_LAST_TICK.get(), true);
				}
			}
		});
	}
	
	/** Code disables rendering epic fight model while taking Parcool action
	@SubscribeEvent
	public static void onRenderPlayerEvent(RenderEpicFightPlayerEvent event) {
		event.getPlayerPatch().getOriginal().getCapability(Capabilities.ANIMATION_CAPABILITY).ifPresent((animation) -> {
			ParCoolMixinAnimation animationAccessor = (ParCoolMixinAnimation)animation;
			com.alrex.parcool.client.animation.Animator animator = animationAccessor.getAnimator();
			if (animator != null && !event.getPlayerPatch().isBattleMode()) {
				//event.setShouldRender(false);
			}
		});
	}
	**/
	
	@SubscribeEvent
	public static void onBaseLayerUpdateEvent(UpdatePlayerMotionEvent.BaseLayer event) {
		if (event.inaction()) {
			return;
		}
		
		event.getPlayerPatch().getOriginal().getCapability(Capabilities.ANIMATION_CAPABILITY).ifPresent((animation) -> {
			ParCoolMixinAnimation animationAccessor = (ParCoolMixinAnimation)animation;
			com.alrex.parcool.client.animation.Animator animator = animationAccessor.getAnimator();
			Parkourability parkourability = Parkourability.get(event.getPlayerPatch().getOriginal());
			
			if (parkourability != null && animator != null && PARCOOL_ANIMATOR_MAPPING.containsKey(animator.getClass())) {
				PARCOOL_ANIMATOR_MAPPING.get(animator.getClass()).accept(animator, parkourability, event);
			}
		});
	}
	
	@SubscribeEvent
	public static void onMovementInputUpdateEvent(MovementInputUpdateEvent event) {
		LocalPlayerPatch playerpatch = EpicFightCapabilities.getEntityPatch(event.getEntity(), LocalPlayerPatch.class);
		
		if (playerpatch == null || !playerpatch.isBattleMode()) {
			return;
		}
		
		Parkourability parkourability = Parkourability.get(event.getEntity());
		HangDown hangDown;
		
		if ((hangDown = (HangDown)parkourability.get(HangDown.class)) != null && hangDown.isDoing()) {
			if (!playerpatch.getEntityState().inaction()) {
				float yRot = ParCoolUtils.idealYRotForHanging(hangDown, event.getEntity());
				BarAxis barAxis = hangDown.getHangingBarAxis();
				boolean axisMismatches = false;
				
				if (hangDown.isOrthogonalToBar()) {
					if (barAxis == ParCoolUtils.getLookBarAxis(yRot)) {
						axisMismatches = true;
					}
				} else {
					if (barAxis != ParCoolUtils.getLookBarAxis(yRot)) {
						axisMismatches = true;
					}
				}
				
				if (!axisMismatches) {
					playerpatch.setModelYRot(yRot, true);
					
					if (hangDown.isOrthogonalToBar()) {
						if (event.getInput().left) {
							playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_HANG_DOWN_MOVE_LEFT, 0.0F);
						} else if (event.getInput().right) {
							playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_HANG_DOWN_MOVE_RIGHT, 0.0F);
						}
					} else {
						if (event.getInput().up) {
							playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_HANG_DOWN_MOVE_FORWARD_START, 0.0F);
						} else if (event.getInput().down) {
							playerpatch.playAnimationSynchronized(ParCoolAnimations.BIPED_HANG_DOWN_MOVE_BACKWARD, 0.0F);
						}
					}
				}
			}
			
			event.getInput().left = false;
			event.getInput().right = false;
			event.getInput().up = false;
			event.getInput().down = false;
			event.getInput().forwardImpulse = 0.0F;
			event.getInput().leftImpulse = 0.0F;
		}
		
		if (parkourability.get(ClingToCliff.class).isDoing()) {
			if (!playerpatch.getEntityState().inaction()) {
				if (event.getInput().left) {
					ParCoolUtils.scanTerrainAndStartClingAction(playerpatch, ParCoolUtils.WallMoveType.MOVE_LEFT);
				} else if (event.getInput().right) {
					ParCoolUtils.scanTerrainAndStartClingAction(playerpatch, ParCoolUtils.WallMoveType.MOVE_RIGHT);
				}
			}
			
			event.getInput().left = false;
			event.getInput().right = false;
			event.getInput().up = false;
			event.getInput().down = false;
			event.getInput().forwardImpulse = 0.0F;
			event.getInput().leftImpulse = 0.0F;
		}
	}
}
