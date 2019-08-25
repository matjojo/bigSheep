package com.matjojo.bigsheep.mixin;

import com.matjojo.bigsheep.resizable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin extends AnimalEntity implements resizable {
	@Shadow
	public abstract void setSheared(boolean boolean_1);

	private static final TrackedData<Byte> BIGSHEEP_SIZE;

	static {
		BIGSHEEP_SIZE = DataTracker.registerData(SheepEntity.class, TrackedDataHandlerRegistry.BYTE);
	}

	protected SheepEntityMixin(EntityType<? extends AnimalEntity> entityType_1, World world_1) {
		super(entityType_1, world_1);
	}

	/**
	 * Just like colour we need to start tracking our amount_eaten
	 *
	 * @param cbi the CallbackInfo from Mixin
	 * @author matjojo
	 */
	@Inject(
			method = "Lnet/minecraft/entity/passive/SheepEntity;initDataTracker()V",
			at = @At(value = "HEAD")
	)
	public void initDataTrackerMixin(CallbackInfo cbi) {
		this.dataTracker.startTracking(BIGSHEEP_SIZE, (byte) 1); // a sheep starts with it's wool, so size is one
	}

	public void expand(byte amount) {
		this.dataTracker.set(BIGSHEEP_SIZE, (byte) (this.getSize() + amount));
	}

	public byte getSize() {
		return this.dataTracker.get(BIGSHEEP_SIZE);
	}

	public void setSize(byte amount) {
		this.dataTracker.set(BIGSHEEP_SIZE, amount);
	}

	/**
	 * When grass is eaten the sheep grows
	 * Then we setSheared to false
	 *
	 * @param cbi Mixin CallbackInfo
	 * @author matjojo
	 */
	@Inject(
			method = "Lnet/minecraft/entity/passive/SheepEntity;onEatingGrass()V",
			at = @At(value = "HEAD")
	)
	public void onEatingGrassMixin(CallbackInfo cbi) {
		this.expand((byte) 1);
		this.setSheared(false);
	}

//	/**
//	 * We need to inject in dropItems when setSheared is called.
//	 * The setSheared method is also called when the world is initialised,
//	 * so if we just injected in setSheared the sheep would shrink or grown when the world is loaded.
//	 * For that reason we inject into the end of dropItems
//	 *
//	 * @param cbi the mixin CallbackInfo
//	 * @author Matjojo
//	 */
//	@Inject(
//			method = "Lnet/minecraft/entity/passive/SheepEntity;dropItems()V",
//			at = @At(
//					value = "INVOKE",
//					target = "Lnet/minecraft/entity/passive/SheepEntity;setSheared(Z)V"
//			)
//	)
//	public void bigSheep_onShearedMixin(CallbackInfo cbi) {
//		this.setSize((byte)0);
//	}

	/**
	 * @param compoundTag The CompoundTag that gets passed into the writeCustomDataToTag method we mixin to
	 * @param cbi         the mixin CallbackInfo
	 * @author matjojo
	 * @reason To ensure that the sheep's size is saved when the world is saved.
	 */
	@Inject(
			method = "writeCustomDataToTag(Lnet/minecraft/nbt/CompoundTag;)V",
			at = @At(value = "RETURN")
	)
	public void writeCustomDataToTagMixin(CompoundTag compoundTag, CallbackInfo cbi) {
		compoundTag.putByte("bigSheep_size", this.getSize());
		compoundTag.putBoolean("Sheared", this.getSize() == 0); // when the sheep is saved we need to ensure that the sheared call is set back correctly
	}

	@Inject(
			method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V",
			at = @At(value = "RETURN")
	)
	public void readCustomDataFromTagMixin(CompoundTag compoundTag, CallbackInfo cbi) {
		this.setSize(compoundTag.getByte("bigSheep_size"));
	}

	/**
	 * We redirect the call to the random to our method, that then returns a different amount of items.
	 *
	 * @param random The random instance of the sheep
	 * @param bound  The bound given by the minecraft code, at the time of writing always 3
	 * @return A new bound that takes into account the size of the sheep.
	 * @reason Could not find a different way to edit this local variable, will research a better way.
	 * @author matjojo
	 */
	@Redirect(method = "dropItems()V",
			at = @At(value = "INVOKE",
					target = "Ljava/util/Random;nextInt(I)I"))
	private int nextIntMixin(Random random, int bound) {
		int next = random.nextInt((this.getSize() * 2) + 1);
		this.setSize((byte) 0);
		return next;
	}




}
