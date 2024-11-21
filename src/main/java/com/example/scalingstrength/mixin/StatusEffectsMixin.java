
package com.example.scalingstrength.mixin;

import com.example.scalingstrength.ScalingStrengthMod;
import net.minecraft.entity.effect.DamageModifierStatusEffect;
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

@Mixin( StatusEffects.class )
public class StatusEffectsMixin
{
    // Inject into the register method to modify the StatusEffect before it gets registered
    @Inject( method = "register", at = @At( "HEAD" ), cancellable = true )
    private static void onRegister( int rawId, String id, StatusEffect entry, CallbackInfoReturnable<StatusEffect> cir )
    {
        try
        {
            // Check if the effect being registered is the STRENGTH effect
            if( id.equals( "strength" ) )
            {
                ScalingStrengthMod.LOGGER.info( "Hooking 'StatusEffects.register' begin" );

                // Access the DamageModifierStatusEffect class via reflection
                Class< ? > damageModifierClass = DamageModifierStatusEffect.class;

                ScalingStrengthMod.LOGGER.info( "Found 'DamageModifierStatusEffect' class" );

                // Access the constructor of DamageModifierStatusEffect
                Constructor< ? > constructor = damageModifierClass.getDeclaredConstructor( StatusEffectCategory.class, int.class, double.class );

                ScalingStrengthMod.LOGGER.info( "Setting 'DamageModifierStatusEffect' constructor accessible" );

                constructor.setAccessible( true );  // Make the constructor accessible

                ScalingStrengthMod.LOGGER.info( "Creating Strength Effect Override" );

                // Instantiate DamageModifierStatusEffect using the constructor
                StatusEffect strengthEffectOverride = (StatusEffect) constructor.newInstance(
                        StatusEffectCategory.BENEFICIAL,
                        16762624,
                        0.2 // 20% increase per level
                );

                // Add a custom attribute modifier to scale the damage based on potion level
                strengthEffectOverride.addAttributeModifier(
                        EntityAttributes.GENERIC_ATTACK_DAMAGE,
                        "648D7064-6A60-4F59-8ABE-C2C23A6DD7A9", // Same as vanilla Strength
                        1.0,
                        EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                );

                ScalingStrengthMod.LOGGER.info( "Registering Strength Effect Override" );

                // Register the modified STRENGTH effect instead of the original
                Registry.register( Registries.STATUS_EFFECT, rawId, id, strengthEffectOverride );

                // Return the modified effect instead of the original one
                cir.setReturnValue( strengthEffectOverride );

                // Cancel the original registration to avoid registering the unmodified STRENGTH effect
                cir.cancel();

                ScalingStrengthMod.LOGGER.info( "Hooking 'StatusEffects.register' complete" );
            }
        }
        catch( Exception e )
        {
            ScalingStrengthMod.LOGGER.error( "Error while hooking 'StatusEffects.register'", e );
        }
    }
}