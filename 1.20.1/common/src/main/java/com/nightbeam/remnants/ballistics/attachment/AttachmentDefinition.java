package com.nightbeam.remnants.ballistics.attachment;

import com.nightbeam.remnants.ballistics.core.StatModifier;
import com.nightbeam.remnants.ballistics.weapon.ReloadType;
import com.nightbeam.remnants.ballistics.weapon.WeaponCategory;
import com.nightbeam.remnants.ballistics.weapon.WeaponDefinition;
import com.nightbeam.remnants.ballistics.weapon.WeaponRarity;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Immutable description of an attachment: where it mounts, which weapons accept it and what it changes.
 *
 * @param zoom          optic magnification (FOV divisor); 0 for non-optics
 * @param suppressed    gunshots use the suppressed sound and a reduced audible range
 * @param hidesFlash    no muzzle flash is rendered
 * @param skinTexture   for skins: texture suffix; weapon textures {@code item/gun/<weapon>} are swapped for {@code item/gun/<weapon>_<suffix>}
 */
public record AttachmentDefinition(
        String name,
        AttachmentSlot slot,
        List<StatModifier> modifiers,
        Set<WeaponCategory> categories,
        Set<String> excludedWeapons,
        boolean requiresMagazineReload,
        OpticType optic,
        float zoom,
        boolean suppressed,
        boolean hidesFlash,
        String skinTexture,
        WeaponRarity rarity) {

    public boolean isCompatibleWith(WeaponDefinition weapon) {
        if (!weapon.slots().contains(slot)) return false;
        if (!categories.contains(weapon.category())) return false;
        if (excludedWeapons.contains(weapon.name())) return false;
        return !requiresMagazineReload || weapon.reloadType() == ReloadType.MAGAZINE && !weapon.ammo().fullReloadPerItem();
    }

    public static Builder builder(String name, AttachmentSlot slot) {
        return new Builder(name, slot);
    }

    public static final class Builder {
        private final String name;
        private final AttachmentSlot slot;
        private final List<StatModifier> modifiers = new ArrayList<>();
        private final Set<WeaponCategory> categories = EnumSet.allOf(WeaponCategory.class);
        private final Set<String> excluded = new HashSet<>();
        private boolean requiresMagazineReload;
        private OpticType optic = OpticType.IRON;
        private float zoom;
        private boolean suppressed;
        private boolean hidesFlash;
        private String skinTexture = "";
        private WeaponRarity rarity = WeaponRarity.COMMON;

        private Builder(String name, AttachmentSlot slot) {
            this.name = name;
            this.slot = slot;
        }

        public Builder mod(StatModifier modifier) {
            modifiers.add(modifier);
            return this;
        }

        public Builder only(WeaponCategory first, WeaponCategory... rest) {
            categories.clear();
            categories.add(first);
            categories.addAll(List.of(rest));
            return this;
        }

        public Builder except(WeaponCategory... excludedCategories) {
            List.of(excludedCategories).forEach(categories::remove);
            return this;
        }

        public Builder excludeWeapon(String weaponName) {
            excluded.add(weaponName);
            return this;
        }

        public Builder magazineFed() {
            requiresMagazineReload = true;
            return this;
        }

        public Builder optic(OpticType type, float magnification) {
            optic = type;
            zoom = magnification;
            return this;
        }

        public Builder suppressed() {
            suppressed = true;
            hidesFlash = true;
            return this;
        }

        public Builder hidesFlash() {
            hidesFlash = true;
            return this;
        }

        public Builder skin(String texture) {
            skinTexture = texture;
            return this;
        }

        public Builder rarity(WeaponRarity r) {
            rarity = r;
            return this;
        }

        public AttachmentDefinition build() {
            return new AttachmentDefinition(name, slot, List.copyOf(modifiers), Set.copyOf(categories), Set.copyOf(excluded),
                    requiresMagazineReload, optic, zoom, suppressed, hidesFlash, skinTexture, rarity);
        }
    }
}
