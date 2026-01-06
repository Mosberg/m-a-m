package dk.mosberg.datagen;

import dk.mosberg.MAM;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class MAMDataGenerator implements DataGeneratorEntrypoint {
	@SuppressWarnings("null")
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		fabricDataGenerator.createPack();
		MAM.LOGGER.info("Initialized data generator pack (no providers registered yet)");
	}
}
