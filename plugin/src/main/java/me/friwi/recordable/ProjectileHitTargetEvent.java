package me.friwi.recordable;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitTargetEvent extends ProjectileHitEvent {
    public ProjectileHitTargetEvent(Projectile projectile, Block hitBlock, Entity hitEntity) {
        super(projectile);
        throw new UnsupportedOperationException("This is only a compile time dependency");
    }

    public Block getHitBlock() {
        throw new UnsupportedOperationException("This is only a compile time dependency");
    }

    public Entity getHitEntity() {
        throw new UnsupportedOperationException("This is only a compile time dependency");
    }
}
