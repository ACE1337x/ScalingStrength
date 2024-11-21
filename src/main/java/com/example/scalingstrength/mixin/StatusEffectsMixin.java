
package com.example.scalingstrength.mixin;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Constructor;

@Mixin(StatusEffects.class)
public class StatusEffectsMixin {

    // Inject into the register method to modify the StatusEffect before it gets registered
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void onRegister(int rawId, String id, StatusEffect entry, CallbackInfoReturnable<StatusEffect> cir) {
        try {
            // Check if the effect being registered is the STRENGTH effect
            if (id.equals("strength")) {

                // Access the DamageModifierStatusEffect class via reflection
                Class<?> damageModifierClass = Class.forName("net.minecraft.entity.effect.DamageModifierStatusEffect");

                // Access the constructor of DamageModifierStatusEffect
                Constructor<?> constructor = damageModifierClass.getDeclaredConstructor(StatusEffectCategory.class, int.class, double.class);
                constructor.setAccessible(true);  // Make the constructor accessible

                // Instantiate DamageModifierStatusEffect using the constructor
                StatusEffect modifiedStrengthEffect = (StatusEffect) constructor.newInstance(
                        StatusEffectCategory.BENEFICIAL, 16762624, 0.2
                );

                // Add a custom attribute modifier to scale the damage based on potion level
                modifiedStrengthEffect.addAttributeModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9",
                        1.0,  // 20% increase per level
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL);

                // Register the modified STRENGTH effect instead of the original
                Registry.register(Registries.STATUS_EFFECT, rawId, id, modifiedStrengthEffect);

                // Return the modified effect instead of the original one
                cir.setReturnValue(modifiedStrengthEffect);

                // Cancel the original registration to avoid registering the unmodified STRENGTH effect
                cir.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}