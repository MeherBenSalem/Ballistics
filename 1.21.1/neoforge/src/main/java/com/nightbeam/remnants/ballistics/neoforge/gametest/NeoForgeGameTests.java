package com.nightbeam.remnants.ballistics.neoforge.gametest;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.gametest.BallisticsGameTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Registers the shared {@link BallisticsGameTests} with NeoForge (template {@code remnants_ballistics:range}). Run with
 * {@code ./gradlew :neoforge:runGameTestServer}.
 */
@GameTestHolder(RemnantsBallistics.MODID)
@PrefixGameTestTemplate(false)
public final class NeoForgeGameTests {
    private static final String RANGE = "range";

    private NeoForgeGameTests() {}

    @GameTest(template = RANGE, timeoutTicks = 200)
    public static void hitscanDamagesTarget(GameTestHelper helper) {
        BallisticsGameTests.hitscanDamagesTarget(helper);
    }

    @GameTest(template = RANGE, timeoutTicks = 200)
    public static void bulletsBreakGlass(GameTestHelper helper) {
        BallisticsGameTests.bulletsBreakGlass(helper);
    }

    @GameTest(template = RANGE, timeoutTicks = 200)
    public static void fireRateIsEnforced(GameTestHelper helper) {
        BallisticsGameTests.fireRateIsEnforced(helper);
    }

    @GameTest(template = RANGE, timeoutTicks = 100)
    public static void drawDelayBlocksFiring(GameTestHelper helper) {
        BallisticsGameTests.drawDelayBlocksFiring(helper);
    }

    @GameTest(template = RANGE, timeoutTicks = 200)
    public static void emptyWeaponDoesNotFire(GameTestHelper helper) {
        BallisticsGameTests.emptyWeaponDoesNotFire(helper);
    }

    @GameTest(template = RANGE, timeoutTicks = 200)
    public static void voidcasterLaunchesOrb(GameTestHelper helper) {
        BallisticsGameTests.voidcasterLaunchesOrb(helper);
    }

    @GameTest(template = RANGE, timeoutTicks = 400)
    public static void magazineReloadUsesReserve(GameTestHelper helper) {
        BallisticsGameTests.magazineReloadUsesReserve(helper);
    }

    @GameTest(template = RANGE, timeoutTicks = 600)
    public static void singleLoadReloadInsertsRounds(GameTestHelper helper) {
        BallisticsGameTests.singleLoadReloadInsertsRounds(helper);
    }

    @GameTest(template = RANGE)
    public static void ammoPressRecipeMatches(GameTestHelper helper) {
        BallisticsGameTests.ammoPressRecipeMatches(helper);
    }

    @GameTest(template = RANGE)
    public static void everyWeaponIsCraftable(GameTestHelper helper) {
        BallisticsGameTests.everyWeaponIsCraftable(helper);
    }

    @GameTest(template = RANGE)
    public static void lootAndDamageDataLoaded(GameTestHelper helper) {
        BallisticsGameTests.lootAndDamageDataLoaded(helper);
    }

    @GameTest(template = RANGE)
    public static void structureLootIsInjected(GameTestHelper helper) {
        BallisticsGameTests.structureLootIsInjected(helper);
    }
}
