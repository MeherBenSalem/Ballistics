package com.nightbeam.remnants.ballistics.registry;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.ShooterState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** NeoForge data attachments (entity-bound state, unrelated to weapon attachments). */
public final class ModAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, RemnantsBallistics.MODID);

    /** Transient per-player shooting state: never saved, discarded with the entity on death/logout. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ShooterState>> SHOOTER = TYPES.register("shooter",
            () -> AttachmentType.builder(ShooterState::new).build());

    private ModAttachmentTypes() {}
}
