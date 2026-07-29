package net.blue.chaoticd.network;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

/** Safe, non-mixin probe for distinguishing attack packets from interactions. */
public final class InteractionPacketInspector {
    private InteractionPacketInspector() {
    }

    public static boolean isAttack(ServerboundInteractPacket packet) {
        AttackProbe probe = new AttackProbe();
        packet.dispatch(probe);
        return probe.attack;
    }

    private static final class AttackProbe implements ServerboundInteractPacket.Handler {
        private boolean attack;

        @Override
        public void onInteraction(InteractionHand hand) {
            // Not an attack.
        }

        @Override
        public void onInteraction(InteractionHand hand, Vec3 position) {
            // Not an attack.
        }

        @Override
        public void onAttack() {
            attack = true;
        }
    }
}
