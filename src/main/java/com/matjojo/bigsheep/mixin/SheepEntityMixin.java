package com.matjojo.bigsheep.mixin;

import com.matjojo.bigsheep.bigSheep_Resizable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SheepEntity.class)
public abstract class SheepEntityMixin extends AnimalEntity implements bigSheep_Resizable {
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
	public void bigSheep_initDataTrackerMixin(CallbackInfo cbi) {
		this.dataTracker.startTracking(BIGSHEEP_SIZE, (byte) 1); // a sheep starts with it's wool, so size is one
	}

	public void bigSheep_expand(byte amount) {
		this.dataTracker.set(BIGSHEEP_SIZE, (byte) (this.bigSheep_getSize() + amount));
	}

	public byte bigSheep_getSize() {
		return this.dataTracker.get(BIGSHEEP_SIZE);
	}

	public void bigSheep_setSize(byte amount) {
		this.dataTracker.set(BIGSHEEP_SIZE, amount);
	}

	/**
	 * Set the size back to 0 when the sheep is sheared.
	 *
	 * @param cbi Mixin CallbackInfo
	 * @author matjojo
	 */
	@Inject(
			method = "Lnet/minecraft/entity/passive/SheepEntity;setSheared(Z)V",
			at = @At(value = "HEAD")
	)
	public void bigSheep_setShearedMixin(boolean sheared, CallbackInfo cbi) {
		if (sheared) {
			this.bigSheep_setSize((byte) 0);
		} else {
			this.bigSheep_expand((byte) 1);
		}
	}


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
	public void bigSheep_writeDataToTagMixin(CompoundTag compoundTag, CallbackInfo cbi) {
		compoundTag.putByte("bigSheep_size", this.bigSheep_getSize());
	}


	@Inject(
			method = "readCustomDataFromTag(Lnet/minecraft/nbt/CompoundTag;)V",
			at = @At(value = "RETURN")
	)
	public void bigSheep_readCustomDataFromTagMixin(CompoundTag compoundTag, CallbackInfo cbi) {
		this.bigSheep_setSize(compoundTag.getByte("bigSheep_size"));
	}


}
