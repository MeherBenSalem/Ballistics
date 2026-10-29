package com.nightbeam.remnants.ballistics.neoforge.gametest;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.gametest.BallisticsGameTests;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

/**
 * Registers the shared {@link BallisticsGameTests} with NeoForge: one test function per test, and one test instance per
 * function in template {@code remnants_ballistics:range}, with the timeout from the shared test's javadoc. Run with
 * {@code ./gradlew :neoforge:runGameTestServer}.
 */
public final class NeoForgeGameTests {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION,
            RemnantsBallistics.MODID);

    private record TestEntry(String name, int maxTicks, Consumer<GameTestHelper> function) {}

    private static final TestEntry[] TESTS = {
            new TestEntry("hitscan_damages_target", 200, BallisticsGameTests::hitscanDamagesTarget),
            new TestEntry("bullets_break_glass", 200, BallisticsGameTests::bulletsBreakGlass),
            new TestEntry("fire_rate_is_enforced", 200, BallisticsGameTests::fireRateIsEnforced),
            new TestEntry("draw_delay_blocks_firing", 100, BallisticsGameTests::drawDelayBlocksFiring),
            new TestEntry("empty_weapon_does_not_fire", 200, BallisticsGameTests::emptyWeaponDoesNotFire),
            new TestEntry("voidcaster_launches_orb", 200, BallisticsGameTests::voidcasterLaunchesOrb),
            new TestEntry("magazine_reload_uses_reserve", 400, BallisticsGameTests::magazineReloadUsesReserve),
            new TestEntry("single_load_reload_inserts_rounds", 600, BallisticsGameTests::singleLoadReloadInsertsRounds),
            new TestEntry("ammo_press_recipe_matches", 100, BallisticsGameTests::ammoPressRecipeMatches),
            new TestEntry("every_weapon_is_craftable", 100, BallisticsGameTests::everyWeaponIsCraftable),
            new TestEntry("loot_and_damage_data_loaded", 100, BallisticsGameTests::lootAndDamageDataLoaded),
            new TestEntry("structure_loot_is_injected", 100, BallisticsGameTests::structureLootIsInjected),
    };

    static {
        for (TestEntry test : TESTS) FUNCTIONS.register(test.name(), () -> test.function());
    }

    private NeoForgeGameTests() {}

    public static void register(IEventBus modBus) {
        FUNCTIONS.register(modBus);
        modBus.addListener(NeoForgeGameTests::registerInstances);
    }

    private static void registerInstances(RegisterGameTestsEvent event) {
        Holder<TestEnvironmentDefinition<?>> environment = event.registerEnvironment(RemnantsBallistics.id("default"),
                new TestEnvironmentDefinition.AllOf());
        Identifier range = Identifier.parse(BallisticsGameTests.RANGE);
        for (TestEntry test : TESTS) {
            Identifier id = RemnantsBallistics.id(test.name());
            event.registerTest(id, new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id),
                    new TestData<>(environment, range, test.maxTicks(), 0, true)));
        }
    }
}
