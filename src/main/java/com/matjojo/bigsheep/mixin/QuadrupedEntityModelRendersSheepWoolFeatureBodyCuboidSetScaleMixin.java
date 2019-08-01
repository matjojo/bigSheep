package com.matjojo.bigsheep.mixin;

import com.matjojo.bigsheep.bigSheep_Resizable;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(QuadrupedEntityModel.class)
public abstract class QuadrupedEntityModelRendersSheepWoolFeatureBodyCuboidSetScaleMixin extends EntityModel {
	/**
	 * Relevant code in QuadrupedEntityModel:
	 * public void render(T entity_1, float float_1, float float_2, float float_3, float float_4, float float_5, float float_6) {
	 * this.setAngles(entity_1, float_1, float_2, float_3, float_4, float_5, float_6);
	 * if (this.isChild) {
	 * float float_7 = 2.0F;
	 * GlStateManager.pushMatrix();
	 * GlStateManager.translatef(0.0F, this.field_3540 * float_6, this.field_3537 * float_6);
	 * this.head.render(float_6);                                                                      0
	 * GlStateManager.popMatrix();
	 * GlStateManager.pushMatrix();
	 * GlStateManager.scalef(0.5F, 0.5F, 0.5F);
	 * GlStateManager.translatef(0.0F, 24.0F * float_6, 0.0F);
	 * this.body.render(float_6);                                                                      1
	 * this.leg1.render(float_6);                                                                      2
	 * this.leg2.render(float_6);                                                                      3
	 * this.leg3.render(float_6);                                                                      4
	 * this.leg4.render(float_6);                                                                      5
	 * GlStateManager.popMatrix();
	 * } else {
	 * this.head.render(float_6);                                                                      6
	 * this.body.render(float_6);                                                                      7
	 * this.leg1.render(float_6);                                                                      8
	 * this.leg2.render(float_6);                                                                      9
	 * this.leg3.render(float_6);                                                                      10
	 * this.leg4.render(float_6);                                                                      11
	 * }
	 * (Numbers added for clarity by me)
	 * We need to ensure that only the wools body gets enlarged, so we only catch the render call on ordinance 7
	 *
	 * @param arguments
	 * @param entity
	 * @param float_1
	 * @param float_2
	 * @param float_3
	 * @param float_4
	 * @param float_5
	 * @param scale
	 */
	@ModifyArgs(method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/model/Cuboid;render(F)V",
					ordinal = 7))
	private void ChangeSheepWoolFeatureBodyRendererScale(Args arguments, Entity entity, float float_1, float float_2, float float_3, float float_4, float float_5, float scale) {
		if (entity instanceof SheepEntity) {
			bigSheep_Resizable resizableSheep = (bigSheep_Resizable) entity; // this will work due to the sheepEntity mixin
			float sizeScalar = resizableSheep.bigSheep_getSize();
			if (sizeScalar < 2) { // if the sheep is normal sized or sheared, don't scale
				return;
			}
			GlStateManager.pushMatrix();
			GlStateManager.scalef(1.5F * sizeScalar, 1.5F * sizeScalar, 1.2F * sizeScalar);
			// double the size of all that is drawn
			GlStateManager.translatef(0F * scale * sizeScalar, -2.0F * scale * sizeScalar, 2.0F * scale * sizeScalar);
		}
	}

	// After setting the scale for the body we have to reset it and ensure that the matrix is popped from the GL state
	@ModifyArgs(method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/model/Cuboid;render(F)V",
					ordinal = 8))
	private void ChangeSheepWoolFeatureBodyRendererScaleBack(Args arguments, Entity entity, float float_1, float float_2, float float_3, float float_4, float float_5, float scale) {
		if (entity instanceof SheepEntity) {
//			arguments.set(0, ((float)arguments.get(0)) * 2.0F);
			bigSheep_Resizable resizableSheep = (bigSheep_Resizable) entity; // this will work due to the sheepEntity mixin
			float sizeScalar = resizableSheep.bigSheep_getSize();
			if (sizeScalar < 2) { // if the sheep is normal sized or sheared, don't scale, and also don't pop the matrix
				return;
			}
			GlStateManager.popMatrix();
		}
	}
}
