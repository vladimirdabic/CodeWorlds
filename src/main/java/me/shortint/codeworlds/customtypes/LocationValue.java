package me.shortint.codeworlds.customtypes;

import me.shortint.cwlang.interpreter.types.NumberValue;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueProperties;
import org.bukkit.Location;

import java.util.Objects;

public class LocationValue implements Value, ValueProperties {
    private final Location location;

    public LocationValue(Location location) {
        this.location = location;
    }

    @Override
    public String getTypeName() {
        return "Location";
    }

    @Override
    public String toStringValue() {
        return "<location x=" + location.getX()
                + ", y=" + location.getY()
                + ", z=" + location.getZ()
                + ", pitch=" + location.getPitch()
                + ", yaw=" + location.getYaw()
                + " in " + Objects.requireNonNull(location.getWorld()).getName() + ">";
    }

    public Location getBukkitLocation() {
        return location;
    }

    @Override
    public boolean equals(Value other) {
        if(other instanceof LocationValue) {
            LocationValue otherLoc = (LocationValue)other;
            return otherLoc.location.equals(location);
        }

        return false;
    }

    @Override
    public Value getProperty(String propertyName) {

        switch (propertyName) {
            case "x": return new NumberValue(location.getX());
            case "y": return new NumberValue(location.getY());
            case "z": return new NumberValue(location.getZ());
            case "pitch": return new NumberValue(location.getPitch());
            case "yaw": return new NumberValue(location.getYaw());
            case "world": return new WorldValue(location.getWorld());
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
