package com.nightbeam.remnants.ballistics.network;

import com.nightbeam.remnants.ballistics.RemnantsBallistics;
import com.nightbeam.remnants.ballistics.combat.BulletTracer;
import com.nightbeam.remnants.ballistics.combat.ImpactType;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Server → nearby players (excluding the shooter, who predicts locally): everything needed to render one shot:
 * muzzle flash, tracer polylines and impacts. Positions after the origin are float offsets to keep packets small.
 */
public record ShotEffectsPayload(int shooterId, int weaponIndex, Vec3 origin, int flags, List<Trace> traces) implements CustomPacketPayload {
    public static final Type<ShotEffectsPayload> TYPE = new Type<>(RemnantsBallistics.id("shot_effects"));
    public static final StreamCodec<FriendlyByteBuf, ShotEffectsPayload> STREAM_CODEC = StreamCodec.ofMember(ShotEffectsPayload::write, ShotEffectsPayload::read);

    public static final int FLAG_SUPPRESSED = 1;
    public static final int FLAG_HIDE_FLASH = 2;
    public static final int FLAG_NO_TRACER = 4;

    private static final int MAX_TRACES = 32;
    private static final int MAX_POINTS = 64;
    private static final int MAX_IMPACTS = 32;

    public record Impact(Vec3 pos, Direction face, ImpactType type, int blockStateId, boolean ricochet) {
        public static Impact of(BulletTracer.Impact i) {
            return new Impact(i.pos(), i.face(), i.type(), i.blockStateId(), i.ricochet());
        }
    }

    public record Trace(List<Vec3> points, List<Impact> impacts) {
        public static Trace of(BulletTracer.Result r) {
            List<Impact> impacts = new ArrayList<>(r.impacts().size());
            for (BulletTracer.Impact i : r.impacts()) impacts.add(Impact.of(i));
            return new Trace(r.points(), impacts);
        }
    }

    public boolean has(int flag) {
        return (flags & flag) != 0;
    }

    private void write(FriendlyByteBuf buf) {
        buf.writeVarInt(shooterId);
        buf.writeVarInt(weaponIndex);
        buf.writeDouble(origin.x);
        buf.writeDouble(origin.y);
        buf.writeDouble(origin.z);
        buf.writeByte(flags);
        int traceCount = Math.min(MAX_TRACES, traces.size());
        buf.writeVarInt(traceCount);
        for (int t = 0; t < traceCount; t++) {
            Trace trace = traces.get(t);
            int pointCount = Math.min(MAX_POINTS, trace.points().size());
            buf.writeVarInt(pointCount);
            for (int i = 0; i < pointCount; i++) writeOffset(buf, trace.points().get(i));
            int impactCount = Math.min(MAX_IMPACTS, trace.impacts().size());
            buf.writeVarInt(impactCount);
            for (int i = 0; i < impactCount; i++) {
                Impact impact = trace.impacts().get(i);
                writeOffset(buf, impact.pos());
                buf.writeByte(impact.face().get3DDataValue());
                buf.writeByte(impact.type().ordinal());
                buf.writeVarInt(impact.blockStateId());
                buf.writeBoolean(impact.ricochet());
            }
        }
    }

    private static ShotEffectsPayload read(FriendlyByteBuf buf) {
        int shooter = buf.readVarInt();
        int weapon = buf.readVarInt();
        Vec3 origin = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        int flags = buf.readByte();
        int traceCount = capped(buf.readVarInt(), MAX_TRACES);
        List<Trace> traces = new ArrayList<>(traceCount);
        for (int t = 0; t < traceCount; t++) {
            int pointCount = capped(buf.readVarInt(), MAX_POINTS);
            List<Vec3> points = new ArrayList<>(pointCount);
            for (int i = 0; i < pointCount; i++) points.add(readOffset(buf, origin));
            int impactCount = capped(buf.readVarInt(), MAX_IMPACTS);
            List<Impact> impacts = new ArrayList<>(impactCount);
            for (int i = 0; i < impactCount; i++) {
                Vec3 pos = readOffset(buf, origin);
                Direction face = Direction.from3DDataValue(buf.readByte());
                ImpactType type = ImpactType.byId(buf.readByte());
                int state = buf.readVarInt();
                boolean ricochet = buf.readBoolean();
                impacts.add(new Impact(pos, face, type, state, ricochet));
            }
            traces.add(new Trace(points, impacts));
        }
        return new ShotEffectsPayload(shooter, weapon, origin, flags, traces);
    }

    private void writeOffset(FriendlyByteBuf buf, Vec3 pos) {
        buf.writeFloat((float) (pos.x - origin.x));
        buf.writeFloat((float) (pos.y - origin.y));
        buf.writeFloat((float) (pos.z - origin.z));
    }

    private static Vec3 readOffset(FriendlyByteBuf buf, Vec3 origin) {
        return origin.add(buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    private static int capped(int count, int max) {
        if (count < 0 || count > max) throw new IllegalArgumentException("Shot effect list too large: " + count);
        return count;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
