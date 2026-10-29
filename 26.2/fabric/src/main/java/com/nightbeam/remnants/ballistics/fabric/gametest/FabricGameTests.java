package com.nightbeam.remnants.ballistics.fabric.gametest;

import com.nightbeam.remnants.ballistics.gametest.BallisticsGameTests;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

/**
 * Registers the shared {@link BallisticsGameTests} with Fabric's GameTest API (entrypoint {@code fabric-gametest}). Run
 * with {@code ./gradlew :fabric:runGameTestServer}.
 */
public final class FabricGameTests {
    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 200)
    public void hitscanDamagesTarget(GameTestHelper helper) {
        BallisticsGameTests.hitscanDamagesTarget(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 200)
    public void bulletsBreakGlass(GameTestHelper helper) {
        BallisticsGameTests.bulletsBreakGlass(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 200)
    public void fireRateIsEnforced(GameTestHelper helper) {
        BallisticsGameTests.fireRateIsEnforced(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 100)
    public void drawDelayBlocksFiring(GameTestHelper helper) {
        BallisticsGameTests.drawDelayBlocksFiring(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 200)
    public void emptyWeaponDoesNotFire(GameTestHelper helper) {
        BallisticsGameTests.emptyWeaponDoesNotFire(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 200)
    public void voidcasterLaunchesOrb(GameTestHelper helper) {
        BallisticsGameTests.voidcasterLaunchesOrb(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 400)
    public void magazineReloadUsesReserve(GameTestHelper helper) {
        BallisticsGameTests.magazineReloadUsesReserve(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 600)
    public void singleLoadReloadInsertsRounds(GameTestHelper helper) {
        BallisticsGameTests.singleLoadReloadInsertsRounds(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 100)
    public void ammoPressRecipeMatches(GameTestHelper helper) {
        BallisticsGameTests.ammoPressRecipeMatches(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 100)
    public void everyWeaponIsCraftable(GameTestHelper helper) {
        BallisticsGameTests.everyWeaponIsCraftable(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 100)
    public void lootAndDamageDataLoaded(GameTestHelper helper) {
        BallisticsGameTests.lootAndDamageDataLoaded(helper);
    }

    @GameTest(structure = BallisticsGameTests.RANGE, maxTicks = 100)
    public void structureLootIsInjected(GameTestHelper helper) {
        BallisticsGameTests.structureLootIsInjected(helper);
    }
}
