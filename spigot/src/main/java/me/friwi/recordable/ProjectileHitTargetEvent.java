package me.friwi.recordable;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitTargetEvent extends ProjectileHitEvent {
    private Block hitBlock;
    private Entity hitEntity;

    public ProjectileHitTargetEvent(Projectile projectile, Block hitBlock, Entity hitEntity) {
        super(projectile);
        this.hitBlock = hitBlock;
        this.hitEntity = hitEntity;
    }

    public Block getHitBlock() {
        return hitBlock;
    }

    public Entity getHitEntity() {
        return hitEntity;
    }
}
