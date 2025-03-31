package net.momirealms.craftengine.bukkit.compatibility.bettermodel;

import net.momirealms.craftengine.core.entity.Entity;
import net.momirealms.craftengine.core.entity.furniture.AbstractExternalModel;

public class BetterModelModel extends AbstractExternalModel {

    public BetterModelModel(String id) {
        super(id);
    }

    @Override
    public String plugin() {
        return "BetterModel";
    }

    @Override
    public void bindModel(Entity entity) {
        org.bukkit.entity.Entity bukkitEntity = (org.bukkit.entity.Entity) entity.literalObject();
        BetterModelUtils.bindModel(bukkitEntity, id());
    }
}
