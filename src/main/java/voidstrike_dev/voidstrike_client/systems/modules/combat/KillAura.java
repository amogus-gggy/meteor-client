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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class KillAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgTiming = settings.createGroup("Timing");
    private final SettingGroup sgGrimBypass = settings.createGroup("Grim Bypass");

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
        .defaultValue(RotationMode.Aimbot)
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
        .defaultValue(11)
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

    // Grim Bypass (simplified - only essential settings)

    private final Setting<Boolean> grimBypass = sgGrimBypass.add(new BoolSetting.Builder()
        .name("grim-bypass")
        .description("Enable basic anti-cheat bypass.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> aimbotSpeed = sgGrimBypass.add(new DoubleSetting.Builder()
        .name("aimbot-speed")
        .description("Ticks needed for a 360 degree turn.")
        .defaultValue(3.0)
        .min(1.0)
        .sliderMax(20.0)
        .visible(() -> rotation.get() == RotationMode.Aimbot)
        .build()
    );

    private final Setting<Boolean> randomDelay = sgGrimBypass.add(new BoolSetting.Builder()
        .name("random-delay")
        .description("Add small random delays to look more legit.")
        .defaultValue(true)
        .visible(grimBypass::get)
        .build()
    );

    private final Setting<Boolean> hitboxCheck = sgGrimBypass.add(new BoolSetting.Builder()
        .name("hitbox-check")
        .description("Validate target hitboxes before attacking.")
        .defaultValue(true)
        .visible(grimBypass::get)
        .build()
    );

    private final Setting<Boolean> reachCheck = sgGrimBypass.add(new BoolSetting.Builder()
        .name("reach-check")
        .description("Validate attack reach before attacking.")
        .defaultValue(true)
        .visible(grimBypass::get)
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

    public KillAura() {
        super(Categories.Combat, "kill-aura", "Attacks specified entities around you.");
    }

    @Override
    public void onActivate() {
        previousSlot = -1;
        swapped = false;
        wasSprinting = false;
        lastAttackTime = 0;
    }

    @Override
    public void onDeactivate() {
        targets.clear();
        stopAttacking();
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

        // Handle different rotation modes
        if (rotation.get() == RotationMode.Always) {
            Rotations.rotate(Rotations.getYaw(primary), Rotations.getPitch(primary, Target.Body));
        } else if (rotation.get() == RotationMode.Aimbot) {
            aimbotRotate(primary);
        }

        if (pauseOnCombat.get() && PathManagers.get().isPathing() && !wasPathing) {
            PathManagers.get().pause();
            wasPathing = true;
        }

        // Simple attack with cooldown check
        if (primary != null && !targets.isEmpty()) {
            // Check attack cooldown (1.0 = ready, 0.0 = just attacked)
            if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0) {
                targets.forEach(this::attack);
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

        Box hitbox = entity.getBoundingBox();
        if (!PlayerUtils.isWithin(
            MathHelper.clamp(mc.player.getX(), hitbox.minX, hitbox.maxX),
            MathHelper.clamp(mc.player.getY(), hitbox.minY, hitbox.maxY),
            MathHelper.clamp(mc.player.getZ(), hitbox.minZ, hitbox.maxZ),
            range.get()
        )) return false;

        // Additional hitbox validation for grim bypass
        if (grimBypass.get() && hitboxCheck.get()) {
            if (!isValidHitbox(entity, hitbox)) return false;
        }

        if (!entities.get().contains(entity.getType())) return false;
        if (ignoreNamed.get() && entity.hasCustomName()) return false;
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, wallsRange.get())) return false;
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

        // Add simple randomization if enabled
        if (grimBypass.get() && randomDelay.get() && customDelay.get()) {
            int variation = random.nextInt(3) + 1; // 1-3 ticks variation
            if (random.nextBoolean()) {
                delay += variation;
            } else {
                delay = Math.max(0, delay - variation);
            }
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

    private void aimbotRotate(Entity target) {
        double targetYaw = Rotations.getYaw(target);
        double targetPitch = Rotations.getPitch(target, Target.Body);
        double currentYaw = mc.player.getYaw();
        double currentPitch = mc.player.getPitch();

        double yawDiff = MathHelper.wrapDegrees(targetYaw - currentYaw);
        double pitchDiff = MathHelper.wrapDegrees(targetPitch - currentPitch);

        // Calculate total rotation needed
        double totalRotation = Math.sqrt(yawDiff * yawDiff + pitchDiff * pitchDiff);
        
        // Calculate rotation per tick (360 degrees = aimbotSpeed ticks)
        double degreesPerTick = 360.0 / aimbotSpeed.get();
        
        // Calculate how much we can rotate this tick
        double rotationThisTick = Math.min(totalRotation, degreesPerTick);
        
        // Calculate the ratio of movement
        if (totalRotation > 0) {
            double yawRatio = Math.abs(yawDiff) / totalRotation;
            double pitchRatio = Math.abs(pitchDiff) / totalRotation;
            
            // Apply rotation based on ratios
            double yawChange = Math.signum(yawDiff) * rotationThisTick * yawRatio;
            double pitchChange = Math.signum(pitchDiff) * rotationThisTick * pitchRatio;
            
            // Apply new rotation
            double newYaw = currentYaw + yawChange;
            double newPitch = currentPitch + pitchChange;
            
            // Clamp to prevent overshooting
            if (Math.abs(yawChange) > Math.abs(yawDiff)) {
                newYaw = targetYaw;
            }
            if (Math.abs(pitchChange) > Math.abs(pitchDiff)) {
                newPitch = targetPitch;
            }
            
            mc.player.setYaw((float) newYaw);
            mc.player.setPitch((float) newPitch);
        }
    }

    private boolean isLookingAtTarget(Entity target) {
        // Check if player is looking at the target within a reasonable angle
        double yaw = mc.player.getYaw();
        double pitch = mc.player.getPitch();
        double targetYaw = Rotations.getYaw(target);
        double targetPitch = Rotations.getPitch(target, Target.Body);

        double yawDiff = Math.abs(MathHelper.wrapDegrees(targetYaw - yaw));
        double pitchDiff = Math.abs(MathHelper.wrapDegrees(targetPitch - pitch));

        // Much larger tolerance for more responsive attacks
        return yawDiff < 15.0 && pitchDiff < 15.0;
    }

    private void aimbotAttack(Entity target) {
        // Simple attack - just click if looking at target and in range
        mc.options.attackKey.setPressed(true);
        mc.options.attackKey.setPressed(false);

        lastAttackTime = System.currentTimeMillis();
        hitTimer = 0;
    }

    private boolean isValidHitbox(Entity entity, Box hitbox) {
        // Check if hitbox is too small or too large (potential anti-cheat flags)
        double hitboxVolume = (hitbox.maxX - hitbox.minX) * (hitbox.maxY - hitbox.minY) * (hitbox.maxZ - hitbox.minZ);
        
        // Minimum volume check (prevent attacking entities with tiny hitboxes)
        if (hitboxVolume < 0.1) return false;
        
        // Maximum volume check (prevent attacking entities with oversized hitboxes)
        if (hitboxVolume > 8.0) return false;
        
        // Check for unusual hitbox dimensions
        double width = Math.max(hitbox.maxX - hitbox.minX, hitbox.maxZ - hitbox.minZ);
        double height = hitbox.maxY - hitbox.minY;
        
        // Validate aspect ratio (prevent extremely flat or tall hitboxes)
        if (width > 0 && height > 0) {
            double ratio = height / width;
            if (ratio < 0.1 || ratio > 10.0) return false;
        }
        
        // Check if hitbox is within reasonable distance from entity position
        double entityCenterX = entity.getX();
        double entityCenterY = entity.getY();
        double entityCenterZ = entity.getZ();
        double hitboxCenterX = (hitbox.minX + hitbox.maxX) / 2.0;
        double hitboxCenterY = (hitbox.minY + hitbox.maxY) / 2.0;
        double hitboxCenterZ = (hitbox.minZ + hitbox.maxZ) / 2.0;
        
        double centerDistance = Math.sqrt(
            Math.pow(entityCenterX - hitboxCenterX, 2) +
            Math.pow(entityCenterY - hitboxCenterY, 2) +
            Math.pow(entityCenterZ - hitboxCenterZ, 2)
        );
        
        // Hitbox center should be close to entity position
        if (centerDistance > 2.0) return false;
        
        // Additional strict validation: check if hitbox extends too far from entity
        double maxExtentX = Math.max(Math.abs(hitbox.minX - entityCenterX), Math.abs(hitbox.maxX - entityCenterX));
        double maxExtentY = Math.max(Math.abs(hitbox.minY - entityCenterY), Math.abs(hitbox.maxY - entityCenterY));
        double maxExtentZ = Math.max(Math.abs(hitbox.minZ - entityCenterZ), Math.abs(hitbox.maxZ - entityCenterZ));
        
        // For players, hitbox should not extend more than 1 block from center in any direction
        if (entity instanceof PlayerEntity) {
            if (maxExtentX > 1.0 || maxExtentY > 2.0 || maxExtentZ > 1.0) return false;
        }
        
        return true;
    }

    private boolean isWithinReach(Entity target) {
        if (!grimBypass.get() || !reachCheck.get()) return true;
        
        // Get actual attack distance from player's eyes to center of target's hitbox
        Vec3d eyePos = new Vec3d(mc.player.getX(), mc.player.getEyeY(), mc.player.getZ());
        Box targetBox = target.getBoundingBox();
        
        // Calculate center of hitbox
        double centerX = (targetBox.minX + targetBox.maxX) / 2.0;
        double centerY = (targetBox.minY + targetBox.maxY) / 2.0;
        double centerZ = (targetBox.minZ + targetBox.maxZ) / 2.0;
        
        // Calculate distance to center of hitbox
        double distance = Math.sqrt(
            Math.pow(eyePos.x - centerX, 2) +
            Math.pow(eyePos.y - centerY, 2) +
            Math.pow(eyePos.z - centerZ, 2)
        );
        
        // Use vanilla entity interaction range as base (3.0 for survival, varies with effects)
        double baseReach = mc.player.getEntityInteractionRange();
        double maxReach = Math.min(range.get(), baseReach);
        
        // No buffer - strict validation to center of hitbox
        return distance <= maxReach;
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

    public Entity getTarget() {
        if (!targets.isEmpty()) return targets.getFirst();
        return null;
    }

    @Override
    public String getInfoString() {
        if (!targets.isEmpty()) return EntityUtils.getName(getTarget());
        return null;
    }

    public enum AttackItems {
        Weapons,
        All
    }

    public enum RotationMode {
        Always,
        OnHit,
        Aimbot,
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
