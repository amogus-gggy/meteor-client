/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.combat;

import voidstrike_dev.voidstrike_client.events.packets.PacketEvent;
import voidstrike_dev.voidstrike_client.events.world.TickEvent;
import voidstrike_dev.voidstrike_client.pathing.PathManagers;
import voidstrike_dev.voidstrike_client.settings.*;
import voidstrike_dev.voidstrike_client.settings.*;
import voidstrike_dev.voidstrike_client.systems.friends.Friends;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import voidstrike_dev.voidstrike_client.systems.modules.Modules;
import voidstrike_dev.voidstrike_client.systems.modules.combat.CrystalAura;
import voidstrike_dev.voidstrike_client.systems.modules.movement.Sprint;
import voidstrike_dev.voidstrike_client.utils.entity.EntityUtils;
import voidstrike_dev.voidstrike_client.utils.entity.SortPriority;
import voidstrike_dev.voidstrike_client.utils.entity.Target;
import voidstrike_dev.voidstrike_client.utils.entity.TargetUtils;
import voidstrike_dev.voidstrike_client.utils.player.FindItemResult;
import voidstrike_dev.voidstrike_client.utils.player.InvUtils;
import voidstrike_dev.voidstrike_client.utils.player.PlayerUtils;
import voidstrike_dev.voidstrike_client.utils.player.Rotations;
import voidstrike_dev.voidstrike_client.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import voidstrike_dev.voidstrike_client.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static voidstrike_dev.voidstrike_client.utils.Utils.random;

public class KillAura2 extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgTiming = settings.createGroup("Timing");
    private final SettingGroup sgMakima = settings.createGroup("Makima Settings");

    // General

    private final Setting<AttackItems> attackWhenHolding = sgGeneral.add(new EnumSetting.Builder<AttackItems>()
        .name("attack-when-holding")
        .description("Only attacks an entity when a specified item is in your hand.")
        .defaultValue(AttackItems.Weapons)
        .build()
    );

    private final Setting<List<Item>> weapons = sgGeneral.add(new ItemListSetting.Builder()
        .name("selected-weapon-types")
        .description("Which types of weapons to attack with (if you select the diamond sword, any type of sword may be used to attack).")
        .defaultValue(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.TRIDENT)
        .filter(FILTER::contains)
        .visible(() -> attackWhenHolding.get() == AttackItems.Weapons)
        .build()
    );

    private final Setting<RotationMode> rotation = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
        .name("rotate")
        .description("Determines when you should rotate towards the target.")
        .defaultValue(RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Switches to an acceptable weapon when attacking the target.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Switches to your previous slot when done attacking the target.")
        .defaultValue(false)
        .visible(autoSwitch::get)
        .build()
    );

    private final Setting<ShieldMode> shieldMode = sgGeneral.add(new EnumSetting.Builder<ShieldMode>()
        .name("shield-mode")
        .description("Will try and use an axe to break target shields.")
        .defaultValue(ShieldMode.Break)
        .visible(autoSwitch::get)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only attacks when holding left click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyOnLook = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-look")
        .description("Only attacks when looking at an entity.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> pauseOnCombat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-baritone")
        .description("Freezes Baritone temporarily until you are finished attacking the entity.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> legitSprintReset = sgGeneral.add(new BoolSetting.Builder()
        .name("legit-sprint-reset")
        .description("Resets sprinting after attacking for legit gameplay.")
        .defaultValue(false)
        .build()
    );

    // Targeting

    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<SortPriority> priority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.ClosestAngle)
        .build()
    );

    private final Setting<Integer> maxTargets = sgTargeting.add(new IntSetting.Builder()
        .name("max-targets")
        .description("How many entities to target at once.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .visible(() -> !onlyOnLook.get())
        .build()
    );

    private final Setting<Double> range = sgTargeting.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range the entity can be to attack it.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> wallsRange = sgTargeting.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("The maximum range the entity can be attacked through walls.")
        .defaultValue(3.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<EntityAge> mobAgeFilter = sgTargeting.add(new EnumSetting.Builder<EntityAge>()
        .name("mob-age-filter")
        .description("Determines the age of the mobs to target (baby, adult, or both).")
        .defaultValue(EntityAge.Adult)
        .build()
    );

    private final Setting<Boolean> ignoreNamed = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-named")
        .description("Whether or not to attack mobs with a name.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignorePassive = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-passive")
        .description("Will only attack sometimes passive mobs if they are targeting you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreTamed = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-tamed")
        .description("Will avoid attacking mobs you tamed.")
        .defaultValue(false)
        .build()
    );

    // Timing

    private final Setting<Boolean> pauseOnLag = sgTiming.add(new BoolSetting.Builder()
        .name("pause-on-lag")
        .description("Pauses if the server is lagging.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnUse = sgTiming.add(new BoolSetting.Builder()
        .name("pause-on-use")
        .description("Does not attack while using an item.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> pauseOnCA = sgTiming.add(new BoolSetting.Builder()
        .name("pause-on-CA")
        .description("Does not attack while CA is placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> tpsSync = sgTiming.add(new BoolSetting.Builder()
        .name("TPS-sync")
        .description("Tries to sync attack delay with the server's TPS.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> customDelay = sgTiming.add(new BoolSetting.Builder()
        .name("custom-delay")
        .description("Use a custom delay instead of the vanilla cooldown.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> hitDelay = sgTiming.add(new IntSetting.Builder()
        .name("hit-delay")
        .description("How fast you hit the entity in ticks.")
        .defaultValue(8)
        .min(0)
        .sliderMax(60)
        .visible(customDelay::get)
        .build()
    );

    private final Setting<Integer> switchDelay = sgTiming.add(new IntSetting.Builder()
        .name("switch-delay")
        .description("How many ticks to wait before hitting an entity after switching hotbar slots.")
        .defaultValue(0)
        .min(0)
        .sliderMax(10)
        .build()
    );

    // Makima Settings

    private final Setting<Double> rotationSpeed = sgMakima.add(new DoubleSetting.Builder()
        .name("rotation-speed")
        .description("Base rotation speed multiplier.")
        .defaultValue(1.3)
        .min(0.1)
        .sliderMax(3.0)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> enableJitter = sgMakima.add(new BoolSetting.Builder()
        .name("enable-jitter")
        .description("Enable realistic jitter when looking at target.")
        .defaultValue(true)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Double> jitterStrength = sgMakima.add(new DoubleSetting.Builder()
        .name("jitter-strength")
        .description("Strength of the jitter effect.")
        .defaultValue(1.0)
        .min(0.1)
        .sliderMax(5.0)
        .visible(() -> enableJitter.get() && rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> randomizeSpeed = sgMakima.add(new BoolSetting.Builder()
        .name("randomize-speed")
        .description("Add randomization to rotation speed.")
        .defaultValue(true)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> aggressiveMode = sgMakima.add(new BoolSetting.Builder()
        .name("aggressive-mode")
        .description("Enables aggressive targeting with faster rotations and attacks.")
        .defaultValue(true)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> predictiveAiming = sgMakima.add(new BoolSetting.Builder()
        .name("predictive-aiming")
        .description("Predicts target movement for more accurate hits.")
        .defaultValue(true)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> instantAttack = sgMakima.add(new BoolSetting.Builder()
        .name("instant-attack")
        .description("Attack immediately when target is in range.")
        .defaultValue(true)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> gcdFix = sgMakima.add(new BoolSetting.Builder()
        .name("gcd-fix")
        .description("Apply GCD fix to rotations.")
        .defaultValue(true)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final Setting<Boolean> cameraDecouple = sgMakima.add(new BoolSetting.Builder()
        .name("camera-decouple")
        .description("Decouple camera movement from head movement. Head auto-aims, camera user-controlled.")
        .defaultValue(true)
        .visible(() -> rotation.get() == RotationMode.MakimaAngle)
        .build()
    );

    private final static ArrayList<Item> FILTER = new ArrayList<>(List.of(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.MACE, Items.DIAMOND_SPEAR, Items.TRIDENT));
    private final List<Entity> targets = new ArrayList<>();
    private int switchTimer, hitTimer;
    private boolean wasPathing = false;
    public boolean attacking, swapped;
    public static int previousSlot;
    private final Random random = new Random();
    private boolean wasSprinting = false;
    private long lastAttackTime = 0;
    private Entity lastTarget = null;
    private int aggressiveTicks = 0;

    // MakimaAngle rotation system
    private static final SecureRandom secureRandom = new SecureRandom();
    private Vec3d currentRotation = Vec3d.ZERO;
    private Vec3d targetRotation = Vec3d.ZERO;
    
    // User camera control system
    private float userCameraYaw;
    private float userCameraPitch;
    private boolean lastAttackingState = false;

    public KillAura2() {
        super(Categories.Combat, "kill-aura-2", "Enhanced KillAura with MakimaAngle rotation system.");
    }

    @Override
    public void onActivate() {
        previousSlot = -1;
        swapped = false;
        wasSprinting = false;
        lastAttackTime = 0;
        targets.clear();
        currentRotation = Vec3d.ZERO;
        targetRotation = Vec3d.ZERO;
        lastTarget = null;
        aggressiveTicks = 0;
        
        // Initialize user camera angles
        userCameraYaw = mc.player.getYaw();
        userCameraPitch = mc.player.getPitch();
        lastAttackingState = false;
    }

    @Override
    public void onDeactivate() {
        targets.clear();
        stopAttacking();
    }

    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        // Update user camera angles when not attacking
        if (!attacking && isCameraDecoupled()) {
            userCameraYaw = mc.player.getYaw();
            userCameraPitch = mc.player.getPitch();
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.player.isAlive() || PlayerUtils.getGameMode() == GameMode.SPECTATOR) {
            stopAttacking();
            return;
        }
        if (pauseOnUse.get() && (mc.interactionManager.isBreakingBlock() || mc.player.isUsingItem())) {
            stopAttacking();
            return;
        }
        if (onlyOnClick.get() && !mc.options.attackKey.isPressed()) {
            stopAttacking();
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1f && pauseOnLag.get()) {
            stopAttacking();
            return;
        }
        if (pauseOnCA.get() && Modules.get().get(CrystalAura.class).isActive() && Modules.get().get(CrystalAura.class).kaTimer > 0) {
            stopAttacking();
            return;
        }
        if (onlyOnLook.get()) {
            Entity targeted = mc.targetedEntity;

            if (targeted == null || !entityCheck(targeted)) {
                stopAttacking();
                return;
            }

            targets.clear();
            targets.add(mc.targetedEntity);
        } else {
            targets.clear();
            TargetUtils.getList(targets, this::entityCheck, priority.get(), maxTargets.get());
        }

        if (targets.isEmpty()) {
            stopAttacking();
            return;
        }

        Entity primary = targets.getFirst();

        if (autoSwitch.get()) {
            FindItemResult weaponResult = new FindItemResult(mc.player.getInventory().getSelectedSlot(), -1);
            if (attackWhenHolding.get() == AttackItems.Weapons) weaponResult = InvUtils.find(this::acceptableWeapon, 0, 8);

            if (shouldShieldBreak()) {
                FindItemResult axeResult = InvUtils.find(itemStack -> itemStack.getItem() instanceof AxeItem, 0, 8);
                if (axeResult.found()) weaponResult = axeResult;
            }

            if (!swapped) {
                previousSlot  = mc.player.getInventory().getSelectedSlot();
                swapped = true;
            }

            InvUtils.swap(weaponResult.slot(), false);
        }

        if (!acceptableWeapon(mc.player.getMainHandStack())) {
            stopAttacking();
            return;
        }

        attacking = true;

        // Track user camera angles when just started attacking
        if (attacking && !lastAttackingState && isCameraDecoupled()) {
            // Just started attacking - capture current player rotation as user camera angles
            userCameraYaw = mc.player.getYaw();
            userCameraPitch = mc.player.getPitch();
        }
        
        lastAttackingState = attacking;

        // Handle different rotation modes
        if (rotation.get() == RotationMode.Always) {
            Rotations.rotate(Rotations.getYaw(primary), Rotations.getPitch(primary, Target.Body));
        } else if (rotation.get() == RotationMode.MakimaAngle) {
            makimaAngleRotate(primary);
        }

        if (pauseOnCombat.get() && PathManagers.get().isPathing() && !wasPathing) {
            PathManagers.get().pause();
            wasPathing = true;
        }

        // Attack with MakimaAngle logic
        if (primary != null && !targets.isEmpty()) {
            boolean shouldAttackNow = instantAttack.get() ? 
                canAttack(primary, 180) : 
                (canAttack(primary, 180) || !getAttackTimer().finished(150));
                
            if (shouldAttackNow) {
                if (aggressiveMode.get() && aggressiveTicks > 0) {
                    // In aggressive mode, attack even faster
                    if (mc.player.getAttackCooldownProgress(0.3f) >= 1.0) {
                        targets.forEach(this::attack);
                        aggressiveTicks--;
                    }
                } else {
                    if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0) {
                        targets.forEach(this::attack);
                        if (aggressiveMode.get()) {
                            aggressiveTicks = 3; // Next 3 ticks will be more aggressive
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            switchTimer = switchDelay.get();
        }
    }

    private void stopAttacking() {
        if (!attacking) return;

        attacking = false;
        if (wasPathing) {
            PathManagers.get().resume();
            wasPathing = false;
        }
        if (swapBack.get() && swapped) {
            InvUtils.swap(previousSlot, false);
            swapped = false;
        }
    }

    private boolean shouldShieldBreak() {
        for (Entity target : targets) {
            if (target instanceof PlayerEntity player) {
                if (player.isBlocking() && shieldMode.get() == ShieldMode.Break) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.getCameraEntity())) return false;
        if ((entity instanceof LivingEntity livingEntity && livingEntity.isDead()) || !entity.isAlive()) return false;

        // More aggressive range checking in aggressive mode
        double checkRange = aggressiveMode.get() ? range.get() + 0.5 : range.get();
        
        Box hitbox = entity.getBoundingBox();
        if (!PlayerUtils.isWithin(
            MathHelper.clamp(mc.player.getX(), hitbox.minX, hitbox.maxX),
            MathHelper.clamp(mc.player.getY(), hitbox.minY, hitbox.maxY),
            MathHelper.clamp(mc.player.getZ(), hitbox.minZ, hitbox.maxZ),
            checkRange
        )) return false;

        if (!entities.get().contains(entity.getType())) return false;
        if (ignoreNamed.get() && entity.hasCustomName()) return false;
        
        // More aggressive wall checking in aggressive mode
        double checkWallsRange = aggressiveMode.get() ? wallsRange.get() + 0.5 : wallsRange.get();
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, checkWallsRange)) return false;
        if (ignoreTamed.get()) {
            if (entity instanceof Tameable tameable
                && tameable.getOwner() != null
                && tameable.getOwner().equals(mc.player)
            ) return false;
        }
        if (ignorePassive.get()) {
            if (entity instanceof EndermanEntity enderman && !enderman.isAngry()) return false;
            if (entity instanceof PiglinEntity piglin && !piglin.isAttacking()) return false;
            if (entity instanceof ZombifiedPiglinEntity zombifiedPiglin && !zombifiedPiglin.isAttacking()) return false;
            if (entity instanceof WolfEntity wolf && !wolf.isAttacking()) return false;
        }
        if (entity instanceof PlayerEntity player) {
            if (player.isCreative()) return false;
            if (!Friends.get().shouldAttack(player)) return false;
            if (shieldMode.get() == ShieldMode.Ignore && player.isBlocking()) return false;
        }
        if (entity instanceof AnimalEntity animal) {
            return switch (mobAgeFilter.get()) {
                case Baby -> animal.isBaby();
                case Adult -> !animal.isBaby();
                case Both -> true;
            };
        }
        return true;
    }

    private boolean delayCheck() {
        if (switchTimer > 0) {
            switchTimer--;
            return false;
        }

        float delay = (customDelay.get()) ? hitDelay.get() : 0.5f;
        if (tpsSync.get()) delay /= (TickRate.INSTANCE.getTickRate() / 20);

        // More aggressive delay in aggressive mode
        if (aggressiveMode.get()) {
            delay *= 0.7f; // 30% faster attacks
        }

        if (customDelay.get()) {
            if (hitTimer < delay) {
                hitTimer++;
                return false;
            } else return true;
        } else return mc.player.getAttackCooldownProgress(delay) >= 1;
    }

    private void attack(Entity target) {
        // Simulate mouse click using the same method as InputCommand
        KeyBindingAccessor accessor = (KeyBindingAccessor) mc.options.attackKey;
        accessor.meteor$setTimesPressed(accessor.meteor$getTimesPressed() + 1);

        lastAttackTime = System.currentTimeMillis();
        hitTimer = 0;
    }

    private void makimaAngleRotate(Entity entity) {
        Vec3d targetPos = getPredictedPosition(entity);
        float[] targetAngles = PlayerUtils.calculateAngle(targetPos);
        
        Turns currentAngle = new Turns(mc.player.getYaw(), mc.player.getPitch());
        Turns targetAngle = new Turns(targetAngles[0], targetAngles[1]);
        
        // Track target changes for aggressive mode
        if (lastTarget != entity) {
            lastTarget = entity;
            if (aggressiveMode.get()) {
                aggressiveTicks = 5; // More aggressive when switching targets
            }
        }
        
        Turns newAngle = limitAngleChange(currentAngle, targetAngle, targetPos, entity);
        
        if (cameraDecouple.get()) {
            // Auto-aim head only, camera stays user-controlled
            mc.player.setYaw(newAngle.getYaw());
            mc.player.setPitch(newAngle.getPitch());
        } else {
            // Normal mode - both head and camera move together
            mc.player.setYaw(newAngle.getYaw());
            mc.player.setPitch(newAngle.getPitch());
        }
    }

    private Vec3d getPredictedPosition(Entity entity) {
        if (!predictiveAiming.get()) {
            return new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        }
        
        // Predict target position based on velocity
        Vec3d velocity = entity.getVelocity();
        double distance = mc.player.distanceTo(entity);
        int ticksToPredict = (int) (distance / 8.0); // Predict based on distance
        
        double predictedX = entity.getX() + velocity.x * ticksToPredict;
        double predictedY = entity.getEyeY() + velocity.y * ticksToPredict;
        double predictedZ = entity.getZ() + velocity.z * ticksToPredict;
        
        return new Vec3d(predictedX, predictedY, predictedZ);
    }

    private Turns limitAngleChange(Turns currentAngle, Turns targetAngle, Vec3d vec3d, Entity entity) {
        Turns angleDelta = calculateDelta(currentAngle, targetAngle);

        float yawDelta = angleDelta.getYaw();
        float pitchDelta = angleDelta.getPitch();

        float rotationDifference = (float) Math.hypot(Math.abs(yawDelta), Math.abs(pitchDelta));
        boolean isLookingAtTarget = rotationDifference < 30.0f;
        boolean canAttack = entity != null && canAttack(entity, 35);

        float speed;
        if (aggressiveMode.get()) {
            // More aggressive speed calculation
            if (!isLookingAtTarget) {
                speed = randomLerp(1.2F, 1.4F); // Faster initial rotation
            } else {
                float accuracyFactor = MathHelper.clamp(rotationDifference / 15.0f, 0.3f, 1.0f);
                float baseSpeed = canAttack ? randomLerp(1.1F, 1.3F) : randomLerp(0.8F, 1.0F);
                speed = baseSpeed * accuracyFactor;
            }
        } else {
            // Original logic for non-aggressive mode
            if (!isLookingAtTarget) {
                speed = randomLerp(0.95F, 1.0F);
            } else {
                float accuracyFactor = MathHelper.clamp(rotationDifference / 20.0f, 0.2f, 1.0f);
                float baseSpeed = canAttack ? randomLerp(0.9F, 0.98F) : randomLerp(0.5F, 0.7F);
                speed = baseSpeed * accuracyFactor;
            }
        }

        // Apply rotation speed multiplier
        speed *= rotationSpeed.get().floatValue();
        
        // Apply randomization if enabled
        if (randomizeSpeed.get()) {
            speed *= randomLerp(0.8F, 1.2F);
        }

        float div = (rotationDifference < 0.0001f) ? 0.0001f : rotationDifference;
        float lineYaw = (Math.abs(yawDelta / div) * 180F);
        float linePitch = (Math.abs(pitchDelta / div) * 180F);

        float targetYawDelta = MathHelper.clamp(yawDelta, -lineYaw, lineYaw) * speed;
        float targetPitchDelta = MathHelper.clamp(pitchDelta, -linePitch, linePitch) * speed;

        if (isLookingAtTarget && enableJitter.get()) {
            float dist = (entity != null) ? (float) entity.distanceTo(mc.player) : 3.0f;
            float distFactor = MathHelper.clamp(dist / 4.0f, 0.3f, 1.0f);
            float gaussianYaw = (float) secureRandom.nextGaussian();
            float gaussianPitch = (float) secureRandom.nextGaussian();
            float movementStress = MathHelper.clamp(rotationDifference / 10.0f, 0.5f, 1.5f);

            // More aggressive jitter in aggressive mode
            float jitterMultiplier = aggressiveMode.get() ? 1.5f : 1.0f;
            
            float shakeStrengthYaw = 3.5f * distFactor * movementStress * jitterStrength.get().floatValue() * jitterMultiplier;
            float shakeStrengthPitch = 2.0f * distFactor * movementStress * jitterStrength.get().floatValue() * jitterMultiplier;

            float shakeYaw = gaussianYaw * shakeStrengthYaw;
            float shakePitch = gaussianPitch * shakeStrengthPitch;

            targetYawDelta += shakeYaw;
            targetPitchDelta += shakePitch;
        }

        if (gcdFix.get()) {
            float gcd = getGCDValue();
            targetYawDelta -= (targetYawDelta % gcd);
            targetPitchDelta -= (targetPitchDelta % gcd);
        }
        
        float fixYaw = currentAngle.getYaw() + targetYawDelta;
        float fixPitch = currentAngle.getPitch() + targetPitchDelta;
        fixPitch = MathHelper.clamp(fixPitch, -90.0F, 90.0F);
        return new Turns(fixYaw, fixPitch);
    }

    private Turns calculateDelta(Turns current, Turns target) {
        float yawDelta = MathHelper.wrapDegrees(target.getYaw() - current.getYaw());
        float pitchDelta = MathHelper.wrapDegrees(target.getPitch() - current.getPitch());
        return new Turns(yawDelta, pitchDelta);
    }

    private float getGCDValue() {
        float sens = (float) (mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2);
        float t = sens * sens * sens * 8.0f;
        return t * 0.15f;
    }

    private float randomLerp(float min, float max) {
        return MathHelper.lerp(secureRandom.nextFloat(), min, max);
    }

    private boolean canAttack(Entity entity, int cooldown) {
        return entity != null && entity.isAlive() && !entity.equals(mc.player) 
               && mc.player.getAttackCooldownProgress(0.5f) >= 1.0f;
    }

    private AttackTimer getAttackTimer() {
        return new AttackTimer(hitTimer);
    }

    private boolean acceptableWeapon(ItemStack stack) {
        if (shouldShieldBreak()) return stack.getItem() instanceof AxeItem;
        if (attackWhenHolding.get() == AttackItems.All) return true;

        if (weapons.get().contains(Items.DIAMOND_SWORD) && stack.isIn(ItemTags.SWORDS)) return true;
        if (weapons.get().contains(Items.DIAMOND_AXE) && stack.isIn(ItemTags.AXES)) return true;
        if (weapons.get().contains(Items.DIAMOND_PICKAXE) && stack.isIn(ItemTags.PICKAXES)) return true;
        if (weapons.get().contains(Items.DIAMOND_SHOVEL) && stack.isIn(ItemTags.SHOVELS)) return true;
        if (weapons.get().contains(Items.DIAMOND_HOE) && stack.isIn(ItemTags.HOES)) return true;
        if (weapons.get().contains(Items.MACE) && stack.getItem() instanceof MaceItem) return true;
        if (weapons.get().contains(Items.DIAMOND_SPEAR) && stack.isIn(ItemTags.SPEARS)) return true;
        return weapons.get().contains(Items.TRIDENT) && stack.getItem() instanceof TridentItem;
    }

    public boolean cameraMode() {
        // Camera is always user-controlled in KillAura2
        // Only head auto-aims when attacking
        return false;
    }

    public boolean isCameraDecoupled() {
        return cameraDecouple.get();
    }

    public float getUserCameraYaw() {
        return userCameraYaw;
    }

    public float getUserCameraPitch() {
        return userCameraPitch;
    }

    public Entity getTarget() {
        if (!targets.isEmpty()) return targets.getFirst();
        return null;
    }

    @Override
    public String getInfoString() {
        if (!targets.isEmpty()) return EntityUtils.getName(getTarget());
        return null;
    }

    // Helper classes
    public static class Turns {
        private final float yaw;
        private final float pitch;

        public Turns(float yaw, float pitch) {
            this.yaw = yaw;
            this.pitch = pitch;
        }

        public float getYaw() { return yaw; }
        public float getPitch() { return pitch; }
    }

    public static class AttackTimer {
        private final int timer;

        public AttackTimer(int timer) {
            this.timer = timer;
        }

        public boolean finished(int ticks) {
            return timer >= ticks;
        }
    }

    public enum AttackItems {
        Weapons,
        All
    }

    public enum RotationMode {
        Always,
        OnHit,
        MakimaAngle,
        None
    }

    public enum ShieldMode {
        Ignore,
        Break,
        None
    }

    public enum EntityAge {
        Baby,
        Adult,
        Both
    }
}
