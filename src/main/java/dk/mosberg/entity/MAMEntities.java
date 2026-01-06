package dk.mosberg.entity;

import dk.mosberg.MAM;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

/**
 * Registry for custom entities.
 *
 * TODO: Add SPELL_AURA entity type (visual effect area, damage over time) TODO: Add
 * SUMMONED_FAMILIAR entity type (AI-controlled helper creature) TODO: Add SPELL_TRAP entity type
 * (triggered trap mechanism) TODO: Add BUFF_ORB entity type (floating pickup for mana/buffs) TODO:
 * Add EXPLOSION_EFFECT entity type (custom explosion with school effects) TODO: Implement entity
 * collision settings per entity type TODO: Add pathfinding for summoned creatures TODO: Implement
 * AI behavior trees for spell-summoned entities TODO: Add entity data serialization for persistence
 */
public class MAMEntities {

    public static final EntityType<SpellProjectileEntity> SPELL_PROJECTILE = Registry.register(
            Registries.ENTITY_TYPE, Identifier.of(MAM.MOD_ID, "spell_projectile"),
            EntityType.Builder
                    .<SpellProjectileEntity>create(SpellProjectileEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f).maxTrackingRange(64).trackingTickInterval(10)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE,
                            Identifier.of(MAM.MOD_ID, "spell_projectile"))));

    public static void register() {
        MAM.LOGGER.info("Registered entities");
    }
}
