package dk.mosberg.client.render;

import dk.mosberg.MAM;
import dk.mosberg.entity.SpellProjectileEntity;
import dk.mosberg.spell.SpellSchool;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.util.Identifier;

/**
 * Renderer for spell projectile entities. Note: This is a minimal implementation for Minecraft
 * 1.21.11. Advanced particle effects and custom rendering will be added later.
 */
@Environment(EnvType.CLIENT)
public class SpellProjectileEntityRenderer
        extends EntityRenderer<SpellProjectileEntity, EntityRenderState> {

    private static final Identifier FIRE_TEXTURE =
            Identifier.of(MAM.MOD_ID, "textures/entity/projectile/fire.png");
    private static final Identifier WATER_TEXTURE =
            Identifier.of(MAM.MOD_ID, "textures/entity/projectile/water.png");
    private static final Identifier AIR_TEXTURE =
            Identifier.of(MAM.MOD_ID, "textures/entity/projectile/air.png");
    private static final Identifier EARTH_TEXTURE =
            Identifier.of(MAM.MOD_ID, "textures/entity/projectile/earth.png");

    public SpellProjectileEntityRenderer(EntityRendererFactory.Context context) {
        super(context);
    }

    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    public Identifier getTexture(EntityRenderState state) {
        // Default to fire texture
        // In a full implementation, we'd store school info in the render state
        return FIRE_TEXTURE;
    }

    public Identifier getTextureForSchool(SpellSchool school) {
        return switch (school) {
            case FIRE -> FIRE_TEXTURE;
            case WATER -> WATER_TEXTURE;
            case AIR -> AIR_TEXTURE;
            case EARTH -> EARTH_TEXTURE;
        };
    }
}
