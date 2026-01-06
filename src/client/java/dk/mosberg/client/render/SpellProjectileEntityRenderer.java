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
public class SpellProjectileEntityRenderer extends
        EntityRenderer<SpellProjectileEntity, SpellProjectileEntityRenderer.SpellProjectileRenderState> {

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

    @Override
    public SpellProjectileRenderState createRenderState() {
        return new SpellProjectileRenderState();
    }

    @Override
    public void updateRenderState(SpellProjectileEntity entity, SpellProjectileRenderState state,
            float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        state.school = entity.getSchool();
    }

    public Identifier getTexture(SpellProjectileRenderState state) {
        return switch (state.school) {
            case FIRE -> FIRE_TEXTURE;
            case WATER -> WATER_TEXTURE;
            case AIR -> AIR_TEXTURE;
            case EARTH -> EARTH_TEXTURE;
        };
    }

    public static class SpellProjectileRenderState extends EntityRenderState {
        public SpellSchool school = SpellSchool.FIRE;
    }
}
