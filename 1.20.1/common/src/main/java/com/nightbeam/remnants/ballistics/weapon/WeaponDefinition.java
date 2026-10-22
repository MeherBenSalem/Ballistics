package com.nightbeam.remnants.ballistics.weapon;

import com.nightbeam.remnants.ballistics.ammo.AmmoType;
import com.nightbeam.remnants.ballistics.attachment.AttachmentSlot;
import com.nightbeam.remnants.ballistics.attachment.OpticType;
import com.nightbeam.remnants.ballistics.core.Stat;
import com.nightbeam.remnants.ballistics.core.WeaponStats;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Immutable, code-defined description of a firearm. Balance values can be overridden per weapon in the server
 * config; everything else (mechanics, slots, sounds) is fixed here.
 *
 * @param name               registry path of the weapon item
 * @param burstCount         rounds per burst for {@link FireMode#BURST}
 * @param burstCooldownTicks pause enforced after a burst completes
 * @param chamber            closed-bolt weapons hold an extra round after a tactical reload
 * @param integratedOptic    optic built into the weapon (used when no optic attachment is fitted)
 * @param durability         shots before the weapon needs repair
 * @param tier               progression tier (1 = starter, 5 = endgame)
 * @param defaultAttachments attachments present on freshly crafted weapons, by attachment name
 */
public record WeaponDefinition(
        String name,
        WeaponCategory category,
        AmmoType ammo,
        WeaponStats baseStats,
        List<FireMode> fireModes,
        int burstCount,
        int burstCooldownTicks,
        ActionType action,
        ReloadType reloadType,
        ShotType shotType,
        boolean chamber,
        OpticType integratedOptic,
        SoundProfile sound,
        WeaponRarity rarity,
        int durability,
        int tier,
        Set<AttachmentSlot> slots,
        Map<AttachmentSlot, String> defaultAttachments) {

    public FireMode defaultFireMode() {
        return fireModes.getFirst();
    }

    public int baseMagazine() {
        return baseStats.getInt(Stat.MAGAZINE);
    }

    public boolean isCharged() {
        return baseStats.get(Stat.CHARGE_TIME) > 0f;
    }

    public static Builder builder(String name, WeaponCategory category, AmmoType ammo) {
        return new Builder(name, category, ammo);
    }

    public static final class Builder {
        private final String name;
        private final WeaponCategory category;
        private final AmmoType ammo;
        private final WeaponStats.Builder stats = WeaponStats.builder();
        private List<FireMode> fireModes = List.of(FireMode.SEMI);
        private int burstCount = 3;
        private int burstCooldownTicks = 5;
        private ActionType action = ActionType.SELF_LOADING;
        private ReloadType reloadType = ReloadType.MAGAZINE;
        private ShotType shotType = ShotType.HITSCAN;
        private boolean chamber = true;
        private OpticType integratedOptic = OpticType.IRON;
        private SoundProfile sound = SoundProfile.PISTOL;
        private WeaponRarity rarity = WeaponRarity.COMMON;
        private int durability = 1500;
        private int tier = 1;
        private final Set<AttachmentSlot> slots = EnumSet.of(AttachmentSlot.SKIN);
        private final Map<AttachmentSlot, String> defaults = new EnumMap<>(AttachmentSlot.class);

        private Builder(String name, WeaponCategory category, AmmoType ammo) {
            this.name = name;
            this.category = category;
            this.ammo = ammo;
        }

        public Builder stat(Stat stat, float value) {
            stats.set(stat, value);
            return this;
        }

        public Builder damage(float damage) {
            return stat(Stat.DAMAGE, damage);
        }

        public Builder rpm(int rpm) {
            return stat(Stat.FIRE_RATE, rpm);
        }

        public Builder magazine(int rounds) {
            return stat(Stat.MAGAZINE, rounds);
        }

        /** Tactical/empty reload in ticks (or per-round insert / overhead ticks for single-load weapons). */
        public Builder reload(int tactical, int empty) {
            return stat(Stat.RELOAD_TIME, tactical).stat(Stat.EMPTY_RELOAD_TIME, empty);
        }

        public Builder falloff(float start, float end, float minMultiplier, float maxRange) {
            return stat(Stat.FALLOFF_START, start).stat(Stat.FALLOFF_END, end).stat(Stat.MIN_DAMAGE, minMultiplier).stat(Stat.MAX_RANGE, maxRange);
        }

        public Builder spread(float hip, float ads, float move) {
            return stat(Stat.HIP_SPREAD, hip).stat(Stat.ADS_SPREAD, ads).stat(Stat.MOVE_SPREAD, move);
        }

        public Builder bloom(float perShot, float max, float recoveryPerSecond) {
            return stat(Stat.BLOOM, perShot).stat(Stat.MAX_BLOOM, max).stat(Stat.BLOOM_RECOVERY, recoveryPerSecond);
        }

        public Builder recoil(float vertical, float horizontal, float kick) {
            return stat(Stat.RECOIL_VERTICAL, vertical).stat(Stat.RECOIL_HORIZONTAL, horizontal).stat(Stat.KICK, kick);
        }

        public Builder handling(int adsTicks, float zoom, float moveSpeed, float adsMoveSpeed) {
            return stat(Stat.ADS_TIME, adsTicks).stat(Stat.ZOOM, zoom).stat(Stat.MOVE_SPEED, moveSpeed).stat(Stat.ADS_MOVE_SPEED, adsMoveSpeed);
        }

        public Builder ballistics(float velocity, float gravity, float penetration) {
            return stat(Stat.MUZZLE_VELOCITY, velocity).stat(Stat.GRAVITY, gravity).stat(Stat.PENETRATION, penetration);
        }

        public Builder modes(FireMode... modes) {
            this.fireModes = List.of(modes);
            return this;
        }

        public Builder burst(int count, int cooldownTicks) {
            this.burstCount = count;
            this.burstCooldownTicks = cooldownTicks;
            return this;
        }

        public Builder action(ActionType action) {
            this.action = action;
            return this;
        }

        public Builder singleLoad() {
            this.reloadType = ReloadType.SINGLE;
            this.chamber = false;
            return this;
        }

        public Builder projectile() {
            this.shotType = ShotType.PROJECTILE;
            return this;
        }

        public Builder openBolt() {
            this.chamber = false;
            return this;
        }

        public Builder integratedOptic(OpticType optic) {
            this.integratedOptic = optic;
            return this;
        }

        public Builder sound(SoundProfile profile) {
            this.sound = profile;
            return this;
        }

        public Builder rarity(WeaponRarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public Builder durability(int shots) {
            this.durability = shots;
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public Builder slots(AttachmentSlot... accepted) {
            slots.addAll(List.of(accepted));
            return this;
        }

        public Builder defaultAttachment(AttachmentSlot slot, String attachmentName) {
            defaults.put(slot, attachmentName);
            return this;
        }

        public WeaponDefinition build() {
            if (fireModes.isEmpty()) throw new IllegalStateException(name + " has no fire modes");
            return new WeaponDefinition(name, category, ammo, stats.build(), fireModes, burstCount, burstCooldownTicks, action,
                    reloadType, shotType, chamber, integratedOptic, sound, rarity, durability, tier, Set.copyOf(slots), Map.copyOf(defaults));
        }
    }
}
