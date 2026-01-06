package dk.mosberg.client.render;

import dk.mosberg.entity.SpellProjectileEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;

/**
 * Renderer for spell projectile entities using FlyingItemEntityRenderer.
 * This renderer displays the spell projectile using the item model system,
 * allowing each school (Fire, Water, Air, Earth) to have distinct visual appearances
 * based on their corresponding projectile item models.
 */
@Environment(EnvType.CLIENT)
public class SpellProjectileEntityRenderer extends FlyingItemEntityRenderer<SpellProjectileEntity> {

    public SpellProjectileEntityRenderer(EntityRendererFactory.Context context) {
        super(context, 1.0F, true); // scale=1.0, lit=true (always bright)
    }
}
