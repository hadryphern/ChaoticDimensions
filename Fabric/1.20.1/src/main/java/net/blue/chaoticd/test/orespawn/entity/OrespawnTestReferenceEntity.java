package net.blue.chaoticd.test.orespawn.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.Level;

/**
 * Original pig-based proxy for validating server/client entity plumbing.
 *
 * <p>It intentionally uses vanilla attributes, AI, model, texture and sounds
 * and does not represent or imitate a legacy Orespawn mob.</p>
 */
public final class OrespawnTestReferenceEntity extends Pig {
    public OrespawnTestReferenceEntity(
        EntityType<? extends OrespawnTestReferenceEntity> entityType,
        Level level
    ) {
        super(entityType, level);
    }
}
