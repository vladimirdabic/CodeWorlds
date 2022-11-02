package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.*;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldValue implements Value, ValueProperties {
    private final World world;

    public WorldValue(World world) {
        this.world = world;
    }

    @Override
    public String getTypeName() {
        return "World";
    }

    @Override
    public String toStringValue() {
        return "<World '" + world.getName() + "'>";
    }

    public World getBukkitWorld() {
        return world;
    }

    @Override
    public boolean equals(Value other) {
        if(other instanceof WorldValue) {
            WorldValue otherWorld = (WorldValue)other;
            return otherWorld.world.getName().equals(world.getName());
        }

        return false;
    }

    @Override
    public Value getProperty(String propertyName) {

        switch (propertyName) {
            case "name": return new StringValue(world.getName());
            case "spawnLocation": return new LocationValue(world.getSpawnLocation());
            case "getBlockAt": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    if(args.length == 1) {
                        if(!(args[0] instanceof LocationValue))
                            throw interpreter.error(line, "world->getBlockAt expected location argument");

                        LocationValue location = (LocationValue)args[0];
                        return new BlockValue(world.getBlockAt(location.getBukkitLocation()));
                    } else if(args.length == 3) {
                        interpreter.validateArguments("world->getBlockAt", true, line, args, "number", "number", "number");
                        NumberValue x = (NumberValue)args[0];
                        NumberValue y = (NumberValue)args[1];
                        NumberValue z = (NumberValue)args[2];

                        return new BlockValue(world.getBlockAt((int)x.getValue(), (int)y.getValue(), (int)z.getValue()));
                    }

                    throw interpreter.error(line, "world->getBlockAt expected location argument or coordinates");
                }
            };
        }

        return null;
    }

    @Override
    public Value setProperty(String propertyName, Value propertyValue) {
        return null;
    }

    @Override
    public Value copy() {
        return this;
    }
}
