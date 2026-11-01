package com.nightbeam.remnants.ballistics.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameplayModelsTest {
    private final WeaponStats rifle = WeaponStats.builder()
            .set(Stat.HIP_SPREAD, 3f).set(Stat.ADS_SPREAD, 0.2f).set(Stat.MOVE_SPREAD, 2f)
            .set(Stat.BLOOM, 0.5f).set(Stat.MAX_BLOOM, 3f).set(Stat.BLOOM_RECOVERY, 10f)
            .set(Stat.RECOIL_VERTICAL, 1f).set(Stat.RECOIL_HORIZONTAL, 0.4f)
            .set(Stat.FALLOFF_START, 20f).set(Stat.FALLOFF_END, 60f).set(Stat.MIN_DAMAGE, 0.6f)
            .build();

    @Test
    void aimingIsMoreAccurateThanHipFire() {
        float hip = SpreadModel.spread(rifle, 0f, SpreadModel.Movement.STILL, 0f, 1f);
        float ads = SpreadModel.spread(rifle, 1f, SpreadModel.Movement.STILL, 0f, 1f);
        assertEquals(3f, hip, 1e-5);
        assertEquals(0.2f, ads, 1e-5);
    }

    @Test
    void movementJumpingAndSprintingHurtAccuracyWhileCrouchingHelps() {
        float still = SpreadModel.spread(rifle, 0f, SpreadModel.Movement.STILL, 0f, 1f);
        float walking = SpreadModel.spread(rifle, 0f, new SpreadModel.Movement(1f, false, false, false), 0f, 1f);
        float sprinting = SpreadModel.spread(rifle, 0f, new SpreadModel.Movement(1.3f, true, false, false), 0f, 1f);
        float airborne = SpreadModel.spread(rifle, 0f, new SpreadModel.Movement(0f, false, false, true), 0f, 1f);
        float crouched = SpreadModel.spread(rifle, 0f, new SpreadModel.Movement(0f, false, true, false), 0f, 1f);
        assertTrue(walking > still);
        assertTrue(sprinting > walking);
        assertTrue(airborne > still);
        assertTrue(crouched < still);
    }

    @Test
    void bloomAccumulatesToACapAndRecovers() {
        float bloom = 0f;
        for (int i = 0; i < 20; i++) bloom = SpreadModel.addBloom(rifle, bloom);
        assertEquals(3f, bloom, 1e-5);
        assertEquals(1f, SpreadModel.recoverBloom(rifle, bloom, 0.2f), 1e-5);
        assertEquals(0f, SpreadModel.recoverBloom(rifle, bloom, 5f), 1e-5);
    }

    @Test
    void falloffIsLinearBetweenStartAndEnd() {
        assertEquals(1f, DamageModel.falloff(rifle, 10), 1e-6);
        assertEquals(0.8f, DamageModel.falloff(rifle, 40), 1e-6);
        assertEquals(0.6f, DamageModel.falloff(rifle, 100), 1e-6);
    }

    @Test
    void recoilKicksUpwardAndScalesWithAim() {
        RecoilModel.Kick hip = RecoilModel.kick(rifle, 0, 0f, false, new ShotRandom(5), 1f);
        RecoilModel.Kick ads = RecoilModel.kick(rifle, 0, 1f, false, new ShotRandom(5), 1f);
        assertTrue(hip.pitch() > 0f);
        assertTrue(ads.pitch() < hip.pitch());
        assertTrue(Math.abs(hip.yaw()) <= 0.4f * 1.35f + 1e-5);
    }

    @Test
    void headshotsLegshotsAndBodyshotsOnAPlayerSizedBox() {
        // Player: 1.8 tall, eyes at 1.62.
        assertEquals(HitZones.Zone.HEAD, HitZones.classify(64 + 1.7, 64, 1.8, 1.62, true));
        assertEquals(HitZones.Zone.BODY, HitZones.classify(64 + 1.1, 64, 1.8, 1.62, true));
        assertEquals(HitZones.Zone.LIMB, HitZones.classify(64 + 0.3, 64, 1.8, 1.62, true));
        assertEquals(HitZones.Zone.BODY, HitZones.classify(64 + 0.3, 64, 1.4, 1.3, false));
    }

    @Test
    void reloadKeepsChamberedRoundOnTacticalReload() {
        assertEquals(31, ReloadMath.reloadCapacity(30, true, 5));
        assertEquals(30, ReloadMath.reloadCapacity(30, true, 0));
        assertEquals(30, ReloadMath.reloadCapacity(30, false, 5));
        assertEquals(26, ReloadMath.roundsToLoad(5, 31, 100));
        assertEquals(7, ReloadMath.roundsToLoad(5, 31, 7));
        assertFalse(ReloadMath.canReload(31, 30, true, 50));
        assertFalse(ReloadMath.canReload(3, 30, true, 0));
        assertTrue(ReloadMath.canReload(0, 30, true, 1));
    }
}
