package org.ef3d0c3e.hunt.kits.entities;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.animal.Animal;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.kits.KitMehdi;
import org.ef3d0c3e.hunt.player.HuntPlayer;

public class MehdiBee extends Animal implements NeutralMob, FlyingAnimal
{
	public static final float FLAP_DEGREES_PER_TICK = 120.32113F;
	public static final int TICKS_PER_FLAP = Mth.ceil(1.4959966F);
	private static final EntityDataAccessor<Byte> DATA_FLAGS_ID;
	private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME;
	private static final int FLAG_ROLL = 2;
	private static final int FLAG_HAS_STUNG = 4;
	private static final int FLAG_HAS_NECTAR = 8;
	private static final int STING_DEATH_COUNTDOWN = 1200;
	private static final int TICKS_BEFORE_GOING_TO_KNOWN_FLOWER = 2400;
	private static final int TICKS_WITHOUT_NECTAR_BEFORE_GOING_HOME = 3600;
	private static final int MIN_ATTACK_DIST = 4;
	private static final int MAX_CROPS_GROWABLE = 10;
	private static final int POISON_SECONDS_NORMAL = 10;
	private static final int POISON_SECONDS_HARD = 18;
	private static final int TOO_FAR_DISTANCE = 32;
	private static final int HIVE_CLOSE_ENOUGH_DISTANCE = 2;
	private static final int PATHFIND_TO_HIVE_WHEN_CLOSER_THAN = 16;
	private static final int HIVE_SEARCH_DISTANCE = 20;
	public static final String TAG_CROPS_GROWN_SINCE_POLLINATION = "CropsGrownSincePollination";
	public static final String TAG_CANNOT_ENTER_HIVE_TICKS = "CannotEnterHiveTicks";
	public static final String TAG_TICKS_SINCE_POLLINATION = "TicksSincePollination";
	public static final String TAG_HAS_STUNG = "HasStung";
	public static final String TAG_HAS_NECTAR = "HasNectar";
	public static final String TAG_FLOWER_POS = "FlowerPos";
	public static final String TAG_HIVE_POS = "HivePos";
	private static final UniformInt PERSISTENT_ANGER_TIME;
	private float rollAmount;
	private float rollAmountO;
	private int timeSinceSting;
	private static final int COOLDOWN_BEFORE_LOCATING_NEW_HIVE = 200;
	private static final int COOLDOWN_BEFORE_LOCATING_NEW_FLOWER = 200;
	private int underWaterTicks;
	private int attackCooldown;

	private HuntPlayer owner;

	@Override
	public LivingEntity getTarget()
	{
		CraftLivingEntity ent = ((CraftLivingEntity)((KitMehdi)owner.getKit()).getTarget());
		if (ent == null)
			return null;

		return ent.getHandle();
	}

	public AgeableMob getBreedOffspring(net.minecraft.server.level.ServerLevel serverlevel, AgeableMob mob)
	{
		return mob;
	}

	static
	{
		DATA_FLAGS_ID = SynchedEntityData.defineId(MehdiBee.class, EntityDataSerializers.BYTE);
		DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(MehdiBee.class, EntityDataSerializers.INT);
		PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
	}

	public HuntPlayer getOwner()
	{
		return owner;
	}

	public void setOwner(final HuntPlayer owner)
	{
		this.owner = owner;
		this.setCustomName(new TextComponent(MessageFormat.format("§6Mireille §8[§b{0}§8]", owner.getName())));
	}

	public void spawn()
	{
		((CraftWorld)owner.getPlayer().getWorld()).addEntity(this, CreatureSpawnEvent.SpawnReason.SPAWNER_EGG);
	}

	public MehdiBee(Location loc, HuntPlayer owner)
	{
		super(EntityType.BEE, ((CraftWorld)loc.getWorld()).getHandle());
		setOwner(owner);
		this.absMoveTo(loc.getX(), loc.getY(), loc.getZ());

		this.setCustomNameVisible(true);

		this.moveControl = new FlyingMoveControl(this, 20, true);
		this.lookControl = new BeeLookControl(this);
		this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
		this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
		this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);

		attackCooldown = 0;
	}

	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(DATA_FLAGS_ID, (byte)0);
		this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
	}

	public float getWalkTargetValue(BlockPos blockposition, LevelReader iworldreader)
	{
		return iworldreader.getBlockState(blockposition).isAir() ? 10.0F : 0.0F;
	}

	protected void registerGoals()
	{
		this.goalSelector.addGoal(0, new BeeAttackGoal(this, 2.1D, true));
		this.goalSelector.addGoal(1, new FollowPlayerGoal(this, 2.0D, 6.f, 32.f, true));
		this.goalSelector.addGoal(3, new TemptGoal(this, 1.25D, Ingredient.of(ItemTags.FLOWERS), false));
		this.goalSelector.addGoal(8, new BeeWanderGoal());
		this.goalSelector.addGoal(9, new FloatGoal(this));

		this.targetSelector.addGoal(1, (new BeeHurtByOtherGoal(this)).setAlertOthers(new Class[0]));
		this.targetSelector.addGoal(2, new BeeBecomeAngryTargetGoal(this));
		// this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal(this, false));
	}

	public void addAdditionalSaveData(CompoundTag nbttagcompound)
	{
		super.addAdditionalSaveData(nbttagcompound);

		nbttagcompound.putBoolean("HasNectar", hasNectar());
		nbttagcompound.putBoolean("HasStung", hasStung());
		nbttagcompound.putInt("TicksSincePollination", 0);
		nbttagcompound.putInt("CannotEnterHiveTicks", 0);
		nbttagcompound.putInt("CropsGrownSincePollination", 0);
		this.addPersistentAngerSaveData(nbttagcompound);
	}

	public void readAdditionalSaveData(CompoundTag nbttagcompound)
	{
		super.readAdditionalSaveData(nbttagcompound);
		this.setHasNectar(nbttagcompound.getBoolean("HasNectar"));
		this.setHasStung(nbttagcompound.getBoolean("HasStung"));
		this.readPersistentAngerSaveData(this.level, nbttagcompound);
	}

	public boolean doHurtTarget(Entity entity)
	{
		boolean flag;
		if (!(entity instanceof Player))
			flag = entity.hurt(DamageSource.sting(this), 3.f*(float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
		else
			flag = entity.hurt(DamageSource.sting(this), (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
		if (flag)
		{
			// Register owner as attacker
			if (entity instanceof Player)
			{
				final HuntPlayer hp = Game.getPlayer(((Player) entity).getName().getString());
				hp.registerAttack(owner);
			}

			this.doEnchantDamageEffects(this, entity);
			if (entity instanceof LivingEntity) {
				((LivingEntity)entity).setStingerCount(((LivingEntity)entity).getStingerCount() + 1);
			}

			this.setHasStung(true);
			attackCooldown = 40;
			this.playSound(SoundEvents.BEE_STING, 1.0F, 1.0F);
		}

		return flag;
	}

	public void tick()
	{
		super.tick();
		if (this.hasNectar() && this.random.nextFloat() < 0.05F) {
			for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
				this.spawnFluidParticle(this.level, this.getX() - 0.30000001192092896D, this.getX() + 0.30000001192092896D, this.getZ() - 0.30000001192092896D, this.getZ() + 0.30000001192092896D, this.getY(0.5D), ParticleTypes.FALLING_NECTAR);
			}
		}

		this.updateRollAmount();
	}

	private void spawnFluidParticle(Level world, double d0, double d1, double d2, double d3, double d4, ParticleOptions particleparam)
	{
		world.addParticle(particleparam, Mth.lerp(world.random.nextDouble(), d0, d1), d4, Mth.lerp(world.random.nextDouble(), d2, d3), 0.0D, 0.0D, 0.0D);
	}

	void pathfindRandomlyTowards(BlockPos blockposition)
	{
		Vec3 vec3d = Vec3.atBottomCenterOf(blockposition);
		byte b0 = 0;
		BlockPos blockposition1 = this.blockPosition();
		int i = (int)vec3d.y - blockposition1.getY();
		if (i > 2) {
			b0 = 4;
		} else if (i < -2) {
			b0 = -4;
		}

		int j = 6;
		int k = 8;
		int l = blockposition1.distManhattan(blockposition);
		if (l < 15) {
			j = l / 2;
			k = l / 2;
		}

		Vec3 vec3d1 = AirRandomPos.getPosTowards(this, j, k, b0, vec3d, 0.3141592741012573D);
		if (vec3d1 != null) {
			this.navigation.setMaxVisitedNodesMultiplier(0.5F);
			this.navigation.moveTo(vec3d1.x, vec3d1.y, vec3d1.z, 1.0D);
		}

	}

	public float getRollAmount(float f) {
		return Mth.lerp(f, this.rollAmountO, this.rollAmount);
	}

	private void updateRollAmount() {
		this.rollAmountO = this.rollAmount;
		if (this.isRolling()) {
			this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
		} else {
			this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
		}

	}

	protected void customServerAiStep()
	{
		if (this.isInWaterOrBubble())
			++this.underWaterTicks;
			this.underWaterTicks = 0;

		if (this.underWaterTicks > 20)
			this.hurt(DamageSource.DROWN, 1.0F);

		if (!this.level.isClientSide)
			this.updatePersistentAnger((ServerLevel)this.level, false);
	}

	public int getRemainingPersistentAngerTime()
	{
		return (Integer)this.entityData.get(DATA_REMAINING_ANGER_TIME);
	}

	public void setRemainingPersistentAngerTime(int i)
	{
		this.entityData.set(DATA_REMAINING_ANGER_TIME, i);
	}

	public UUID getPersistentAngerTarget()
	{
		org.bukkit.entity.LivingEntity ent = ((KitMehdi)owner.getKit()).getTarget();
		if (ent != null)
			return ent.getUniqueId();
		return null;
	}

	public void setPersistentAngerTarget(@Nullable UUID uuid)
	{
		/*this.persistentAngerTarget = uuid;*/
	}

	public void startPersistentAngerTimer()
	{
		this.setRemainingPersistentAngerTime(65536);
	}

	@VisibleForDebug
	public GoalSelector getGoalSelector()
	{
		return this.goalSelector;
	}

	public void aiStep()
	{
		if (attackCooldown > 0)
			--attackCooldown;
		super.aiStep();
		if (!this.level.isClientSide)
		{
			boolean flag = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0D;
			this.setRolling(flag);

			if (this.hasStung() && attackCooldown == 0)
				this.setHasStung(false);
		}

	}

	public boolean hasNectar()
	{
		return this.getFlag(4);
	}

	public void setHasNectar(boolean flag)
	{
		this.setFlag(8, flag);
	}

	public boolean hasStung()
	{
		return this.getFlag(4);
	}

	public void setHasStung(boolean flag)
	{
		this.setFlag(4, flag);
	}

	private boolean isRolling()
	{
		return this.getFlag(2);
	}

	private void setRolling(boolean flag)
	{
		this.setFlag(2, flag);
	}

	boolean isTooFarAway(BlockPos blockposition)
	{
		return !this.closerThan(blockposition, 32);
	}

	private void setFlag(int i, boolean flag)
	{
		if (flag)
			this.entityData.set(DATA_FLAGS_ID, (byte)((Byte)this.entityData.get(DATA_FLAGS_ID) | i));
		else
			this.entityData.set(DATA_FLAGS_ID, (byte)((Byte)this.entityData.get(DATA_FLAGS_ID) & ~i));

	}

	private boolean getFlag(int i)
	{
		return ((Byte)this.entityData.get(DATA_FLAGS_ID) & i) != 0;
	}

	public static AttributeSupplier.Builder createAttributes()
	{
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.FLYING_SPEED, 1.9000000238418579D).add(Attributes.MOVEMENT_SPEED, 0.90000001192092896D).add(Attributes.ATTACK_DAMAGE, 0.25D).add(Attributes.FOLLOW_RANGE, 48.0D);
	}

	protected PathNavigation createNavigation(Level world) {
		FlyingPathNavigation navigationflying = new FlyingPathNavigation(this, world) {
			public boolean isStableDestination(BlockPos blockposition) {
				return !this.level.getBlockState(blockposition.below()).isAir();
			}

			public void tick()
			{
				super.tick();
			}
		};
		navigationflying.setCanOpenDoors(false);
		navigationflying.setCanFloat(false);
		navigationflying.setCanPassDoors(true);
		return navigationflying;
	}

	public boolean isFood(ItemStack itemstack) {
		return itemstack.is(ItemTags.FLOWERS);
	}

	protected void playStepSound(BlockPos blockposition, BlockState iblockdata) {
	}

	protected SoundEvent getAmbientSound() {
		return null;
	}

	protected SoundEvent getHurtSound(DamageSource damagesource) {
		return SoundEvents.BEE_HURT;
	}

	protected SoundEvent getDeathSound() {
		return SoundEvents.BEE_DEATH;
	}

	protected float getSoundVolume() {
		return 0.4F;
	}

	protected float getStandingEyeHeight(Pose entitypose, EntityDimensions entitysize) {
		return this.isBaby() ? entitysize.height * 0.5F : entitysize.height * 0.5F;
	}

	public boolean causeFallDamage(float f, float f1, DamageSource damagesource) {
		return false;
	}

	protected void checkFallDamage(double d0, boolean flag, BlockState iblockdata, BlockPos blockposition) {
	}

	public boolean isFlapping() {
		return this.isFlying() && this.tickCount % TICKS_PER_FLAP == 0;
	}

	public boolean isFlying() {
		return !this.onGround;
	}

	public boolean hurt(DamageSource damagesource, float f)
	{
		if (this.isInvulnerableTo(damagesource))
			return false;
		else
		{
			boolean result = super.hurt(damagesource, f);

			return result;
		}
	}

	public MobType getMobType()
	{
		return MobType.ARTHROPOD;
	}

	protected void jumpInLiquid(Tag<Fluid> tag)
	{
		this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
	}

	public Vec3 getLeashOffset()
	{
		return new Vec3(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.2F));
	}

	boolean closerThan(BlockPos blockposition, int i)
	{
		return blockposition.closerThan(this.blockPosition(), (double)i);
	}

	private abstract class BaseBeeGoal extends Goal
	{
		BaseBeeGoal() {
		}

		public abstract boolean canBeeUse();

		public abstract boolean canBeeContinueToUse();

		public boolean canUse() {
			return this.canBeeUse() && !MehdiBee.this.isAngry();
		}

		public boolean canContinueToUse() {
			return this.canBeeContinueToUse() && !MehdiBee.this.isAngry();
		}
	}

	private class BeeAttackGoal extends MeleeAttackGoal
	{
		BeeAttackGoal(PathfinderMob entitycreature, double d0, boolean flag) {
			super(entitycreature, d0, flag);
		}

		public boolean canUse() {
			return super.canUse() && MehdiBee.this.isAngry() && !MehdiBee.this.hasStung();
		}

		public boolean canContinueToUse() {
			return super.canContinueToUse() && MehdiBee.this.isAngry() && !MehdiBee.this.hasStung();
		}
	}

	private static class BeeBecomeAngryTargetGoal extends NearestAttackableTargetGoal<Player>
	{
		BeeBecomeAngryTargetGoal(MehdiBee entitybee)
		{
			super(entitybee, Player.class, 10, true, false, entitybee::isAngryAt);
		}

		public boolean canUse()
		{
			return this.beeCanTarget() && super.canUse();
		}

		public boolean canContinueToUse()
		{
			boolean flag = this.beeCanTarget();
			if (flag && this.mob.getTarget() != null)
			{
				return super.canContinueToUse();
			}
			else
			{
				this.targetMob = null;
				return false;
			}
		}

		private boolean beeCanTarget() {
			MehdiBee entitybee = (MehdiBee)this.mob;
			return entitybee.isAngry() && !entitybee.hasStung();
		}
	}

	private class BeeHurtByOtherGoal extends HurtByTargetGoal
	{
		BeeHurtByOtherGoal(MehdiBee entitybee)
		{
			super(entitybee, new Class[0]);
		}

		public boolean canContinueToUse() {
			return MehdiBee.this.isAngry() && super.canContinueToUse();
		}

		protected void alertOther(Mob entityinsentient, LivingEntity entityliving)
		{
			if (entityinsentient instanceof MehdiBee && ((MehdiBee)entityinsentient).owner == MehdiBee.this.owner)
				entityinsentient.setTarget(entityliving, EntityTargetEvent.TargetReason.TARGET_ATTACKED_ENTITY, true);
		}
	}

	private class BeeLookControl extends LookControl
	{
		BeeLookControl(Mob entityinsentient)
		{
			super(entityinsentient);
		}

		public void tick()
		{
			if (!MehdiBee.this.isAngry())
				super.tick();
		}

		protected boolean resetXRotOnTick()
		{
			return true;
		}
	}

	private class BeeWanderGoal extends Goal
	{
		private static final int WANDER_THRESHOLD = 22;

		BeeWanderGoal()
		{
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		public boolean canUse()
		{
			return MehdiBee.this.navigation.isDone() && MehdiBee.this.random.nextInt(10) == 0;
		}

		public boolean canContinueToUse()
		{
			return MehdiBee.this.navigation.isInProgress();
		}

		public void start()
		{
			Vec3 vec3d = this.findPos();
			if (vec3d != null)
				MehdiBee.this.navigation.moveTo(MehdiBee.this.navigation.createPath(new BlockPos(vec3d), 1), 1.0D);

		}

		@Nullable
		private Vec3 findPos()
		{
			Vec3 vec3d = MehdiBee.this.getViewVector(0.0F);

			boolean flag = true;
			Vec3 vec3d2 = HoverRandomPos.getPos(MehdiBee.this, 8, 7, vec3d.x, vec3d.z, 1.5707964F, 3, 1);
			return vec3d2 != null ? vec3d2 : AirAndWaterRandomPos.getPos(MehdiBee.this, 8, 4, -2, vec3d.x, vec3d.z, 1.5707963705062866D);
		}
	}

	public class FollowPlayerGoal extends Goal
	{
		public static final int TELEPORT_WHEN_DISTANCE_IS = 12;
		private static final int MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2;
		private static final int MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3;
		private static final int MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1;
		private final MehdiBee ent;
		private final LevelReader level;
		private final double speedModifier;
		private final PathNavigation navigation;
		private int timeToRecalcPath;
		private final float stopDistance;
		private final float startDistance;
		private float oldWaterCost;
		private final boolean canFly;

		public FollowPlayerGoal(MehdiBee ent, double speed, float startDist, float stopDist, boolean canFly)
		{
			this.ent = ent;
			this.level = ent.level;
			this.speedModifier = speed;
			this.navigation = ent.getNavigation();
			this.startDistance = startDist;
			this.stopDistance = stopDist;
			this.canFly = canFly;
			this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
		}

		public boolean canUse()
		{
			return this.ent.distanceToSqr(((CraftPlayer)ent.owner.getPlayer()).getHandle()) > (double)(this.startDistance * this.startDistance);
		}

		public boolean canContinueToUse()
		{
			return this.navigation.isDone() ? false : this.ent.distanceToSqr(((CraftPlayer)ent.owner.getPlayer()).getHandle()) > (double)(this.stopDistance * this.stopDistance);
		}

		public void start()
		{
			this.timeToRecalcPath = 0;
			this.oldWaterCost = this.ent.getPathfindingMalus(BlockPathTypes.WATER);
			this.ent.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
		}

		public void stop()
		{
			this.navigation.stop();
			this.ent.setPathfindingMalus(BlockPathTypes.WATER, this.oldWaterCost);
		}

		public void tick()
		{
			this.ent.getLookControl().setLookAt(((CraftPlayer)ent.owner.getPlayer()).getHandle(), 10.0F, (float)this.ent.getMaxHeadXRot());
			if (--this.timeToRecalcPath <= 0)
			{
				this.timeToRecalcPath = 10;
				if (!this.ent.isLeashed() && !this.ent.isPassenger())
				{
					if (this.ent.distanceToSqr(((CraftPlayer)ent.owner.getPlayer()).getHandle()) >= 400.0D)
						this.teleportToOwner();
					else
						navigation.moveTo
						(
							navigation.createPath
							(
								new BlockPos
								(
									new Vec3
									(
										owner.getPlayer().getLocation().getX(),
										owner.getPlayer().getLocation().getY() + 4.0D,
										owner.getPlayer().getLocation().getZ()
									)
								),
								1
							),
							speedModifier
						);
				}
			}

		}

		private void teleportToOwner()
		{
			BlockPos blockposition = ((CraftPlayer)this.ent.owner.getPlayer()).getHandle().blockPosition();

			for(int i = 0; i < 10; ++i)
			{
				int j = this.randomIntInclusive(-3, 3);
				int k = this.randomIntInclusive(-1, 1);
				int l = this.randomIntInclusive(-3, 3);
				boolean flag = this.maybeTeleportTo(blockposition.getX() + j, blockposition.getY() + k, blockposition.getZ() + l);
				if (flag)
					return;
			}
		}

		private boolean maybeTeleportTo(int i, int j, int k)
		{
			if (Math.abs((double)i - ((CraftPlayer)this.ent.owner.getPlayer()).getHandle().getX()) < 2.0D && Math.abs((double)k - ((CraftPlayer)this.ent.owner.getPlayer()).getHandle().getZ()) < 2.0D)
				return false;
			else if (!this.canTeleportTo(new BlockPos(i, j, k)))
				return false;
			else
			{
				CraftEntity entity = this.ent.getBukkitEntity();
				Location to = new Location(entity.getWorld(), (double)i + 0.5D, (double)j, (double)k + 0.5D, this.ent.getYRot(), this.ent.getXRot());
				EntityTeleportEvent event = new EntityTeleportEvent(entity, entity.getLocation(), to);
				this.ent.level.getCraftServer().getPluginManager().callEvent(event);
				if (event.isCancelled())
					return false;
				else
				{
					to = event.getTo();
					this.ent.moveTo(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
					this.navigation.stop();
					return true;
				}
			}
		}

		private boolean canTeleportTo(BlockPos blockposition) {
			BlockPathTypes pathtype = WalkNodeEvaluator.getBlockPathTypeStatic(this.level, blockposition.mutable());
			if (isAngry())
				return false;
			else if (pathtype != BlockPathTypes.WALKABLE)
				return false;
			else
			{
				BlockState iblockdata = this.level.getBlockState(blockposition.below());
				if (!this.canFly && iblockdata.getBlock() instanceof LeavesBlock) {
					return false;
				} else {
					BlockPos blockposition1 = blockposition.subtract(this.ent.blockPosition());
					return this.level.noCollision(this.ent, this.ent.getBoundingBox().move(blockposition1));
				}
			}
		}

		private int randomIntInclusive(int i, int j)
		{
			return this.ent.getRandom().nextInt(j - i + 1) + i;
		}
	}
}