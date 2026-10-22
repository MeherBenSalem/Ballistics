package com.nightbeam.remnants.ballistics.registry;

/**
 * Loads every registry class so its entries are created. Order matters on Fabric, which registers immediately: blocks
 * before the items that use them, items before the creative tabs that list them.
 */
public final class ModRegistries {
    private ModRegistries() {}

    public static void init() {
        ModBlocks.init();
        ModItems.init();
        ModMenus.init();
        ModSounds.init();
        ModParticles.init();
        ModEntities.init();
        ModRecipes.init();
        ModLootConditions.init();
        ModCreativeTabs.init();
    }
}
