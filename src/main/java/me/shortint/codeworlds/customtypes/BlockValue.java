package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.types.StringValue;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueProperties;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class BlockValue implements Value, ValueProperties {
    private final Block block;

    public BlockValue(final Block block) {
        this.block = block;
    }

    @Override
    public String getTypeName() {
        return "Block";
    }

    @Override
    public String toStringValue() {
        return "<Block>";
    }

    @Override
    public boolean equals(Value other) {
        if(!(other instanceof BlockValue)) return false;
        return block.equals(((BlockValue)other).getBukkitBlock());
    }

    @Override
    public Value getProperty(String propertyName) {
        switch (propertyName) {
            case "world": return new WorldValue(block.getWorld());
            case "location": return new LocationValue(block.getLocation());
            case "id": return new StringValue(block.getBlockData().getMaterial().toString());
        }

        return null;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        switch(propertyName) {
            case "id":
                String id = propertyValue.toStringValue();
                Material material = Material.matchMaterial(id);
                if(material == null) return null;
                block.setType(material);
                return propertyValue;
        }
        return null;
    }

    public Block getBukkitBlock() {
        return block;
    }

    @Override
    public Value copy() {
        return this;
    }
}
