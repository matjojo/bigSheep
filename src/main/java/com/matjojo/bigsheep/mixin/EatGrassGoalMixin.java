package com.matjojo.bigsheep.mixin;

import com.matjojo.bigsheep.resizable;
import net.minecraft.entity.ai.goal.EatGrassGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EatGrassGoal.class)
public abstract class EatGrassGoalMixin extends Goal {

	@Shadow
	@Final
	private MobEntity mob;

	/**
	 * Relevant bytecode:
	 * public boolean canStart();
	 * Code:
	 * 0: aload_0
	 * 1: getfield      #29                 // Field mob:Lnet/minecraft/entity/mob/MobEntity;
	 * 4: invokevirtual #63                 // Method net/minecraft/entity/mob/MobEntity.getRand:()Ljava/util/Random;
	 * 7: aload_0
	 * 8: getfield      #29                 // Field mob:Lnet/minecraft/entity/mob/MobEntity;
	 * 11: invokevirtual #66                 // Method net/minecraft/entity/mob/MobEntity.isBaby:()Z
	 * 14: ifeq          22
	 * 17: bipush        50
	 * 19: goto          25
	 * 22: sipush        1000
	 * 25: invokevirtual #72                 // Method java/util/Random.nextInt:(I)I
	 * 28: ifeq          33
	 * 31: iconst_0
	 * 32: ireturn			    _0_
	 * 33: new           #74                 // class net/minecraft/util/math/BlockPos
	 * 36: dup
	 * 37: aload_0
	 * 38: getfield      #29                 // Field mob:Lnet/minecraft/entity/mob/MobEntity;
	 * 41: invokespecial #77                 // Method net/minecraft/util/math/BlockPos."<init>":(Lnet/minecraft/entity/Entity;)V
	 * 44: astore_1
	 * 45: getstatic     #79                 // Field GRASS_PREDICATE:Ljava/util/function/Predicate;
	 * 48: aload_0
	 * 49: getfield      #34                 // Field world:Lnet/minecraft/world/World;
	 * 52: aload_1
	 * 53: invokevirtual #85                 // Method net/minecraft/world/World.getBlockState:(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;
	 * 56: invokeinterface #91,  2           // InterfaceMethod java/util/function/Predicate.test:(Ljava/lang/Object;)Z
	 * 61: ifeq          66
	 * 64: iconst_1
	 * 65: ireturn			    _1_
	 * 66: aload_0
	 * 67: getfield      #34                 // Field world:Lnet/minecraft/world/World;
	 * 70: aload_1
	 * 71: invokevirtual #95                 // Method net/minecraft/util/math/BlockPos.down:()Lnet/minecraft/util/math/BlockPos;
	 * 74: invokevirtual #85                 // Method net/minecraft/world/World.getBlockState:(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;
	 * 77: invokevirtual #101                // Method net/minecraft/block/BlockState.getBlock:()Lnet/minecraft/block/Block;
	 * 80: getstatic     #107                // Field net/minecraft/block/Blocks.GRASS_BLOCK:Lnet/minecraft/block/Block;
	 * 83: if_acmpne     88
	 * 86: iconst_1
	 * 87: ireturn			    _2_
	 * 88: iconst_0
	 * 89: ireturn			    _3_
	 * <p>
	 * As you can see there are four returns. (numbers added by me)
	 * We want to inject there where the return is true. This is on _1_ and on _2_.
	 * We could use Slice to only select these, or make two injections, but that would be vulnerable to changes,
	 * mixing in on every return and then checking for true is a better option here.
	 *
	 * @param cbi Mixin CallbackInfoReturnable<Boolean>, this allows us to check the return value and not intervene if we don't want to.
	 * @author Matjojo
	 */
	@Inject(method = "canStart()Z",
			at = @At(value = "RETURN")
	)
	public void CanStartMixin(CallbackInfoReturnable<Boolean> cbi) {
		if (cbi.getReturnValue()) {
			resizable sheep = (resizable) this.mob; // this is okay due to the SheepEntityMixin
			// check how large the sheep is
			if (sheep.getSize() == 0) {
				// if 0, return true indeed
				return;
			}
			// if over 0, then ensure the chance gets lower and lower based on the size. 2400 = 20 * 60 * 2
			cbi.setReturnValue(this.mob.getRand().nextInt(sheep.getSize() * 2400) == 1);
		}
	}
}
