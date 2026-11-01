package com.nightbeam.remnants.ballistics.client.anim;

import com.nightbeam.remnants.ballistics.client.ClientWeaponState;
import com.nightbeam.remnants.ballistics.client.RemoteWeaponStates;

/** Blends the animation layers (idle loop, action clip, fire clip) into a pose for rendering. */
public final class WeaponAnimator {
    private static final PoseState LOCAL = new PoseState();
    private static final PoseState REMOTE = new PoseState();

    private WeaponAnimator() {}

    /** First-person pose for the local player. Idle motion fades out while aiming so sights stay steady. */
    public static PoseState local(float ads) {
        ClientWeaponState s = ClientWeaponState.INSTANCE;
        LOCAL.reset();
        float idleWeight = s.action.isActive() ? 0.3f : 1f;
        s.idle.apply(LOCAL, idleWeight * (1f - 0.85f * ads));
        s.action.apply(LOCAL, 1f);
        s.fire.apply(LOCAL, 1f - 0.5f * ads);
        return LOCAL;
    }

    /** Third-person pose for any player (remote state for others, local state for yourself in F5). */
    public static PoseState thirdPerson(int entityId, boolean local) {
        REMOTE.reset();
        if (local) {
            ClientWeaponState s = ClientWeaponState.INSTANCE;
            s.action.apply(REMOTE, 1f);
            s.fire.apply(REMOTE, 1f);
        } else {
            RemoteWeaponStates.State st = RemoteWeaponStates.peek(entityId);
            if (st != null) {
                st.action.apply(REMOTE, 1f);
                st.fire.apply(REMOTE, 1f);
            }
        }
        return REMOTE;
    }
}
