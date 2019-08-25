package com.matjojo.bigsheep.mixin;

import com.matjojo.bigsheep.resizable;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.QuadrupedEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.SheepEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(QuadrupedEntityModel.class)
public abstract class QuadrupedEntityModelMixin extends EntityModel {
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
	 * @param entity
	 * @param float_1
	 * @param float_2
	 * @param float_3
	 * @param float_4
	 * @param float_5
	 * @param scale
	 * @param cbi Mixin CallbackInfo
	 */
	@Inject(method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/model/Cuboid;render(F)V",
					ordinal = 7))
	private void changeSheepWoolFeatureBodyRendererScale(Entity entity, float float_1, float float_2, float float_3, float float_4, float float_5, float scale, CallbackInfo cbi) {
		if (entity instanceof SheepEntity) {
			resizable resizableSheep = (resizable) entity; // this will work due to the sheepEntity mixin
			float size = resizableSheep.getSize();
			if (size < 2) { // if the sheep is normal sized or sheared, don't scale
				return;
			}
			float sizeScalar = size * 0.525F; // when having eaten a second time the scalar is 1.1;3=>1.65
			GlStateManager.pushMatrix();
			GlStateManager.scalef(1.5F * sizeScalar, 1.5F * sizeScalar, 1.2F * sizeScalar);
			GlStateManager.translatef(0F * scale * sizeScalar, -3.0F * scale * sizeScalar, 2.0F * scale * sizeScalar);
		}
	}

	// After setting the scale for the body we have to reset it and ensure that the matrix is popped from the GL state
	@Inject(method = "render(Lnet/minecraft/entity/Entity;FFFFFF)V",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/model/Cuboid;render(F)V",
					ordinal = 8))
	private void changeSheepWoolFeatureBodyRendererScaleBack(Entity entity, float float_1, float float_2, float float_3, float float_4, float float_5, float scale, CallbackInfo cbi) {
		if (entity instanceof SheepEntity) {
			resizable resizableSheep = (resizable) entity; // this will work due to the sheepEntity mixin
			float sizeScalar = resizableSheep.getSize();
			if (sizeScalar < 2) { // if the sheep is normal sized or sheared, don't scale, and also don't pop the matrix
				return;
			}
			GlStateManager.popMatrix();
		}
	}
}
