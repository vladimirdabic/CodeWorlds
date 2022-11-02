package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.NumberValue;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueClass;
import me.shortint.cwlang.interpreter.types.ValueNewInstance;
import org.bukkit.Location;

public class LocationClassValue implements Value, ValueNewInstance, ValueClass {

    @Override
    public String getTypeName() {
        return "Location";
    }

    @Override
    public String toStringValue() {
        return "Location";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    @Override
    public Value newInstance(Interpreter interpreter, Value[] args, int line) {
        LocationValue value;

        if(args.length == 4) {
            interpreter.validateArguments("location constructor", true, line, args, "World", "number", "number", "number");
            WorldValue world = (WorldValue)args[0];
            NumberValue x = (NumberValue)args[1];
            NumberValue y = (NumberValue)args[2];
            NumberValue z = (NumberValue)args[3];
            value = new LocationValue(new Location(world.getBukkitWorld(), x.getValue(), y.getValue(), z.getValue()));

        } else if(args.length == 6) {
            interpreter.validateArguments("location constructor", true, line, args, "World", "number", "number", "number", "number", "number");
            WorldValue world = (WorldValue)args[0];
            NumberValue x = (NumberValue)args[1];
            NumberValue y = (NumberValue)args[2];
            NumberValue z = (NumberValue)args[3];
            NumberValue pitch = (NumberValue)args[4];
            NumberValue yaw = (NumberValue)args[5];
            value = new LocationValue(new Location(world.getBukkitWorld(), x.getValue(), y.getValue(), z.getValue(), (float)pitch.getValue(), (float)yaw.getValue()));
        } else {
            throw interpreter.error(line, "Invalid parameters passed to location constructor");
        }

        return value;
    }

    @Override
    public boolean valueIsInstance(Value value) {
        return value instanceof LocationValue;
    }

    @Override
    public Value castValueToSelf(Interpreter interpreter, int line, Value value) {
        return null;
    }

    @Override
    public Value copy() {
        return this;
    }
}
