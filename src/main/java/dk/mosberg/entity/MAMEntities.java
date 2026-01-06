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
