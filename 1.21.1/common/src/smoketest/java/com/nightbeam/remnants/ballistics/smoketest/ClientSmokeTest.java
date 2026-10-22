package com.nightbeam.remnants.ballistics.smoketest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.InputConstants;
import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.ammo.AmmoType;
import com.nightbeam.remnants.ballistics.attachment.AttachmentDefinition;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.Attachments;
import com.nightbeam.remnants.ballistics.attachment.GunAttachments;
import com.nightbeam.remnants.ballistics.client.ClientWeaponState;
import com.nightbeam.remnants.ballistics.client.Keybinds;
import com.nightbeam.remnants.ballistics.client.anim.GunAnimations;
import com.nightbeam.remnants.ballistics.client.render.GunModels;
import com.nightbeam.remnants.ballistics.client.render.GunVisualsLoader;
import com.nightbeam.remnants.ballistics.registry.ModBlocks;
import com.nightbeam.remnants.ballistics.registry.ModItems;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import com.nightbeam.remnants.ballistics.weapon.GunData;
import com.nightbeam.remnants.ballistics.weapon.GunItem;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.Weapons;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Scripted client run: boots to the title screen, checks that every client asset resolved, creates a flat creative
 * world, then for every weapon draws, aims, fires, reloads and inspects it in first and third person, opens both
 * station screens and the inventory, and exits. Screenshots go to {@code screenshots/}, the verdict to
 * {@code smoketest/result.json}. Any exception or failed check marks the run FAIL; a crash fails it by exiting
 * without a result file.
 */
public final class ClientSmokeTest {
    private static final Logger LOG = LoggerFactory.getLogger("BallisticsSmokeTest");
    /** Hard limit for the whole run, after which it fails rather than hanging CI. */
    private static final long TIMEOUT_NANOS = 15L * 60L * 1_000_000_000L;

    private enum Phase { BOOT, WORLD, SCRIPT, DONE }

    private static Phase phase = Phase.BOOT;
    private static final long startNanos = System.nanoTime();
    private static int bootTicks;
    private static int worldTicks;
    private static final Deque<Step> steps = new ArrayDeque<>();
    private static int waitTicks;
    private static BooleanSupplier waitUntil;
    private static int waitUntilLimit;
    private static String waitUntilLabel;
    private static final List<String> failures = new ArrayList<>();
    private static final List<String> screenshots = new ArrayList<>();
    private static final List<String> notes = new ArrayList<>();
    /** A screenshot to take after the next rendered frame that satisfies the condition. */
    private static String pendingShot;
    private static BooleanSupplier pendingShotCondition;
    private static int pendingShotFrames;

    private record Step(String label, Runnable action) {}

    private ClientSmokeTest() {}

    // ------------------------------------------------------------------------------------------------------------
    // Driver
    // ------------------------------------------------------------------------------------------------------------

    public static void tick() {
        if (phase == Phase.DONE) return;
        try {
            if (System.nanoTime() - startNanos > TIMEOUT_NANOS) {
                fail("timed out in phase " + phase + (waitUntilLabel != null ? " waiting for " + waitUntilLabel : ""));
                finish();
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof PauseScreen && phase != Phase.BOOT) mc.setScreen(null);
            switch (phase) {
                case BOOT -> boot(mc);
                case WORLD -> waitForWorld(mc);
                case SCRIPT -> runScript();
                default -> {}
            }
        } catch (Throwable t) {
            LOG.error("Smoke test step failed", t);
            fail("exception: " + t);
            finish();
        }
    }

    /** Screenshots are taken right after a frame is rendered, so they show that frame rather than the previous one. */
    public static void afterFrame() {
        if (pendingShot == null) return;
        pendingShotFrames++;
        if (pendingShotCondition != null && !pendingShotCondition.getAsBoolean() && pendingShotFrames < 400) return;
        Minecraft mc = Minecraft.getInstance();
        String name = pendingShot + ".png";
        pendingShot = null;
        pendingShotCondition = null;
        Screenshot.grab(mc.gameDirectory, name, mc.getMainRenderTarget(), message -> {});
        screenshots.add(name);
    }

    private static void runScript() {
        if (pendingShot != null) return;
        if (waitUntil != null) {
            if (waitUntil.getAsBoolean()) {
                waitUntil = null;
                waitUntilLabel = null;
            } else if (--waitUntilLimit <= 0) {
                fail("gave up waiting for " + waitUntilLabel);
                waitUntil = null;
                waitUntilLabel = null;
            } else {
                return;
            }
        }
        if (waitTicks > 0) {
            waitTicks--;
            return;
        }
        Step step = steps.poll();
        if (step == null) {
            finish();
            return;
        }
        LOG.info("Smoke test: {}", step.label());
        step.action().run();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Boot and world
    // ------------------------------------------------------------------------------------------------------------

    private static void boot(Minecraft mc) {
        if (mc.getOverlay() != null) return;
        if (!(mc.screen instanceof TitleScreen)) {
            // First launch shows the accessibility onboarding screen; anything else in front of the title is skipped too.
            if (mc.screen != null) {
                mc.options.onboardAccessibility = false;
                mc.options.save();
                mc.setScreen(new TitleScreen());
            }
            return;
        }
        if (++bootTicks == 1) {
            notes.add("title screen after " + seconds() + " s");
            checkClientAssets(mc);
            pendingShot = "00_title";
            return;
        }
        if (pendingShot != null || bootTicks < 5) return;
        String name = "smoketest-" + System.currentTimeMillis();
        LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(),
                WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(name, settings, new WorldOptions(20261022L, false, false),
                registries -> registries.lookupOrThrow(Registries.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                new TitleScreen());
        phase = Phase.WORLD;
    }

    private static void waitForWorld(Minecraft mc) {
        if (mc.level == null || mc.player == null || mc.screen != null || mc.getSingleplayerServer() == null) return;
        if (++worldTicks < 60) return;
        notes.add("in world after " + seconds() + " s");
        onServer(ClientSmokeTest::buildRange);
        mc.options.setCameraType(CameraType.FIRST_PERSON);
        mc.options.hideGui = false;
        queueScript();
        phase = Phase.SCRIPT;
        waitTicks = 20;
    }

    /** Clears the weather, freezes noon, and builds a target wall, a glass row, the stations and an armour stand. */
    private static void buildRange(MinecraftServer server, ServerPlayer player) {
        ServerLevel level = server.overworld();
        server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
        level.setDayTime(6000);
        level.setWeatherParameters(12000, 0, false, false);
        BlockPos base = player.blockPosition();
        for (int x = -4; x <= 4; x++) {
            for (int y = 0; y <= 5; y++) {
                BlockState state = (x + y) % 3 == 0 ? Blocks.TARGET.defaultBlockState() : Blocks.SMOOTH_STONE.defaultBlockState();
                level.setBlockAndUpdate(base.offset(x, y, -16), state);
            }
            level.setBlockAndUpdate(base.offset(x, 1, -10), Blocks.GLASS_PANE.defaultBlockState());
        }
        level.setBlockAndUpdate(base.offset(3, 0, -3), ModBlocks.GUNSMITH_WORKBENCH.get().defaultBlockState());
        level.setBlockAndUpdate(base.offset(-3, 0, -3), ModBlocks.AMMO_PRESS.get().defaultBlockState());
        BlockPos stand = base.offset(1, 0, -7);
        ArmorStand armorStand = EntityType.ARMOR_STAND.create(level);
        if (armorStand != null) {
            armorStand.moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 0f, 0f);
            level.addFreshEntity(armorStand);
        }
        // Face the wall (north, -Z) with the stations just out of view.
        player.teleportTo(level, base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 180f, 0f);
        player.getInventory().clearContent();
        int slot = 9;
        for (AmmoType type : AmmoType.values()) {
            Item ammo = ModItems.ammo(type);
            if (ammo != null && slot < 36) player.getInventory().setItem(slot++, new ItemStack(ammo, 64));
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // Script
    // ------------------------------------------------------------------------------------------------------------

    private static void queueScript() {
        for (WeaponDefinition def : Weapons.all()) queueWeapon(def);
        queueStations();
        step("inventory screen", () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new InventoryScreen(mc.player));
            waitTicks = 10;
        });
        shot("90_inventory");
        step("close inventory", () -> Minecraft.getInstance().setScreen(null));
    }

    private static void queueWeapon(WeaponDefinition def) {
        String n = def.name();
        int index = Weapons.all().indexOf(def) + 1;
        String prefix = String.format("%02d_%s", index, n);
        step(n + ": equip", () -> {
            GunItem gun = ModItems.gun(def);
            onServer((server, player) -> {
                ItemStack stack = GunData.loaded(gun);
                GunAttachments showcase = showcaseBuild(def);
                if (showcase != null) GunData.setAttachments(stack, showcase);
                player.getInventory().setItem(0, stack);
                player.getInventory().selected = 0;
            });
            Minecraft.getInstance().player.getInventory().selected = 0;
            waitUntil(n + " in hand", () -> Minecraft.getInstance().player.getMainHandItem().getItem() == gun, 100);
            waitTicks = 30;
        });
        shot(prefix + "_1_hip");
        step(n + ": aim", () -> {
            setKey(Minecraft.getInstance().options.keyUse, true);
            waitTicks = 20;
        });
        shot(prefix + "_2_ads");
        int[] before = new int[1];
        long[] previousShot = new long[1];
        step(n + ": fire", () -> {
            Minecraft mc = Minecraft.getInstance();
            before[0] = GunData.ammo(mc.player.getMainHandItem());
            previousShot[0] = ClientWeaponState.INSTANCE.lastShotNanos;
            KeyMapping.click(boundKey(mc.options.keyAttack));
            setKey(mc.options.keyAttack, true);
            // Charged weapons fire once the charge completes, so the trigger stays held until the shot is seen.
            pendingShot = prefix + "_3_fire";
            pendingShotFrames = 0;
            pendingShotCondition = () -> ClientWeaponState.INSTANCE.lastShotNanos != previousShot[0]
                    && System.nanoTime() - ClientWeaponState.INSTANCE.lastShotNanos < 40_000_000L;
        });
        step(n + ": release trigger", () -> {
            setKey(Minecraft.getInstance().options.keyAttack, false);
            waitTicks = 12;
        });
        step(n + ": check the shot registered", () -> {
            int after = GunData.ammo(Minecraft.getInstance().player.getMainHandItem());
            if (after >= before[0]) fail(n + ": ammo did not decrease after firing (" + before[0] + " -> " + after + ")");
            setKey(Minecraft.getInstance().options.keyUse, false);
            waitTicks = 8;
        });
        step(n + ": reload", () -> {
            KeyMapping.click(boundKey(Keybinds.RELOAD));
            waitTicks = 10;
        });
        shot(prefix + "_4_reload");
        step(n + ": wait for the reload", () -> waitTicks = 90);
        step(n + ": inspect", () -> {
            KeyMapping.click(boundKey(Keybinds.INSPECT));
            waitTicks = 18;
        });
        shot(prefix + "_5_inspect");
        step(n + ": third person", () -> {
            Minecraft mc = Minecraft.getInstance();
            mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            setKey(mc.options.keyUse, true);
            waitTicks = 25;
        });
        shot(prefix + "_6_third_person");
        step(n + ": back to first person", () -> {
            Minecraft mc = Minecraft.getInstance();
            setKey(mc.options.keyUse, false);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            waitTicks = 5;
        });
    }

    private static void queueStations() {
        step("gunsmith workbench", () -> onServer((server, player) -> {
            BlockPos pos = player.blockPosition().offset(3, 0, -3);
            BlockState state = server.overworld().getBlockState(pos);
            player.openMenu(state.getMenuProvider(server.overworld(), pos));
            // Put the last weapon on the bench so the screen shows its slots and stats.
            ItemStack gun = player.getInventory().getItem(0).copy();
            if (player.containerMenu.slots.size() > 0 && !gun.isEmpty()) {
                player.containerMenu.getSlot(0).set(gun);
                player.getInventory().setItem(0, ItemStack.EMPTY);
            }
        }));
        step("wait for the gunsmith screen", () -> {
            waitUntil("gunsmith screen", () -> Minecraft.getInstance().screen != null, 100);
            waitTicks = 15;
        });
        shot("80_gunsmith");
        step("close the gunsmith", () -> onServer((server, player) -> player.closeContainer()));
        step("ammo press", () -> {
            waitTicks = 10;
            onServer((server, player) -> {
                BlockPos pos = player.blockPosition().offset(-3, 0, -3);
                player.openMenu(server.overworld().getBlockState(pos).getMenuProvider(server.overworld(), pos));
            });
        });
        step("wait for the ammo press screen", () -> {
            waitUntil("ammo press screen", () -> Minecraft.getInstance().screen != null, 100);
            waitTicks = 15;
        });
        shot("81_ammo_press");
        step("close the ammo press", () -> onServer((server, player) -> player.closeContainer()));
        step("settle", () -> waitTicks = 10);
    }

    /** A representative build per weapon so attachment models get rendered too. */
    private static GunAttachments showcaseBuild(WeaponDefinition def) {
        GunAttachments build = GunAttachments.EMPTY;
        boolean any = false;
        for (AttachmentDefinition att : Attachments.all()) {
            if (!def.slots().contains(att.slot()) || att.slot() == AttachmentSlot.MAGAZINE) continue;
            // Every other weapon gets a build, so plain models are covered as well.
            if (Weapons.all().indexOf(def) % 2 == 1) break;
            if (build.get(att.slot()).isPresent()) continue;
            if (!att.isCompatibleWith(def)) continue;
            build = build.with(att.slot(), att.name());
            any = true;
        }
        return any ? build : null;
    }

    // ------------------------------------------------------------------------------------------------------------
    // Asset checks (the client-side half of tools/assets/check_assets.py: what actually resolved at runtime)
    // ------------------------------------------------------------------------------------------------------------

    private static void checkClientAssets(Minecraft mc) {
        int models = 0;
        for (WeaponDefinition def : Weapons.all()) {
            for (String part : GunModels.PARTS) models += checkModel(mc, GunModels.part(def, part));
            if (!exists(mc, GunModels.icon(def))) fail(def.name() + ": icon texture " + GunModels.icon(def) + " missing");
            if (!GunVisualsLoader.INSTANCE.has(def)) fail(def.name() + ": no gun_visuals loaded");
            ResourceLocation set = GunVisualsLoader.INSTANCE.get(def).animations();
            if (!GunAnimations.INSTANCE.hasSet(set)) fail(def.name() + ": animation set " + set + " not loaded");
            else for (String clip : new String[] {"idle", "equip", "fire", "reload", "inspect"}) {
                if (GunAnimations.INSTANCE.get(set, clip) == null) fail(def.name() + ": animation clip " + clip + " missing");
            }
        }
        for (AttachmentDefinition att : Attachments.all()) {
            if (att.slot() == AttachmentSlot.SKIN) {
                for (WeaponDefinition def : Weapons.all()) {
                    if (!def.slots().contains(AttachmentSlot.SKIN)) continue;
                    ResourceLocation tex = RemnantsBallistics.id("textures/item/gun/" + def.name() + "_" + att.skinTexture() + ".png");
                    if (!GunModels.INSTANCE.hasTexture(tex)) fail("skin texture " + tex + " missing");
                }
            } else {
                models += checkModel(mc, GunModels.attachment(att.name()));
            }
        }
        for (var holder : ModSounds.SOUNDS.getEntries()) {
            if (mc.getSoundManager().getSoundEvent(holder.getId()) == null) fail("sound event " + holder.getId() + " has no sounds.json entry");
        }
        for (var holder : ModItems.ITEMS.getEntries()) {
            String key = holder.get().getDescriptionId();
            if (!I18n.exists(key)) fail("missing translation " + key);
        }
        for (KeyMapping key : Keybinds.ALL) {
            if (!I18n.exists(key.getName())) fail("missing translation " + key.getName());
        }
        if (!I18n.exists(Keybinds.CATEGORY)) fail("missing translation " + Keybinds.CATEGORY);
        notes.add(models + " weapon and attachment models resolved");
    }

    /** A model must have loaded, and every texture it names must exist (a missing one renders as the checkerboard). */
    private static int checkModel(Minecraft mc, ResourceLocation id) {
        if (!GunModels.INSTANCE.has(id)) {
            fail("model " + id + " did not load");
            return 0;
        }
        for (GunModels.Layer layer : GunModels.INSTANCE.model(id).layers()) {
            if (!exists(mc, layer.texture())) {
                fail("model " + id + " uses missing texture " + layer.texture());
                break;
            }
        }
        return 1;
    }

    private static boolean exists(Minecraft mc, ResourceLocation resource) {
        return mc.getResourceManager().getResource(resource).isPresent();
    }

    // ------------------------------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------------------------------

    private static void step(String label, Runnable action) {
        steps.add(new Step(label, action));
    }

    private static void shot(String name) {
        step("screenshot " + name, () -> {
            pendingShot = name;
            pendingShotCondition = null;
            pendingShotFrames = 0;
        });
    }

    private static void waitUntil(String label, BooleanSupplier condition, int limitTicks) {
        waitUntil = condition;
        waitUntilLabel = label;
        waitUntilLimit = limitTicks;
    }

    private static void setKey(KeyMapping key, boolean down) {
        KeyMapping.set(boundKey(key), down);
        key.setDown(down);
    }

    /** The key a mapping is bound to (vanilla keeps the field private; its saved name resolves back to it). */
    private static InputConstants.Key boundKey(KeyMapping key) {
        return InputConstants.getKey(key.saveString());
    }

    private interface ServerAction {
        void run(MinecraftServer server, ServerPlayer player);
    }

    private static void onServer(ServerAction action) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) {
            fail("no integrated server");
            return;
        }
        var uuid = mc.player.getUUID();
        server.execute(() -> {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player == null) {
                fail("server player missing");
                return;
            }
            try {
                action.run(server, player);
            } catch (Throwable t) {
                LOG.error("Server-side smoke test action failed", t);
                fail("server exception: " + t);
            }
        });
    }

    private static synchronized void fail(String message) {
        LOG.error("SMOKE TEST FAILURE: {}", message);
        failures.add(message);
    }

    private static long seconds() {
        return (System.nanoTime() - startNanos) / 1_000_000_000L;
    }

    private static void finish() {
        if (phase == Phase.DONE) return;
        phase = Phase.DONE;
        Minecraft mc = Minecraft.getInstance();
        KeyMapping.releaseAll();
        JsonObject result = new JsonObject();
        result.addProperty("status", failures.isEmpty() ? "PASS" : "FAIL");
        result.addProperty("seconds", seconds());
        JsonArray f = new JsonArray();
        failures.forEach(f::add);
        result.add("failures", f);
        JsonArray s = new JsonArray();
        screenshots.forEach(s::add);
        result.add("screenshots", s);
        JsonArray n = new JsonArray();
        notes.forEach(n::add);
        result.add("notes", n);
        Path out = mc.gameDirectory.toPath().resolve("smoketest/result.json");
        try {
            Files.createDirectories(out.getParent());
            Files.writeString(out, new GsonBuilder().setPrettyPrinting().create().toJson(result), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Could not write {}", out, e);
        }
        LOG.info("SMOKE TEST {}: {} failures, {} screenshots in {} s", failures.isEmpty() ? "PASSED" : "FAILED", failures.size(),
                screenshots.size(), seconds());
        mc.stop();
    }
}
