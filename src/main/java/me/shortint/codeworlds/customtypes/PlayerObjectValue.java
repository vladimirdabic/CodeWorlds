package me.shortint.codeworlds.customtypes;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

public class PlayerObjectValue implements Value, ValueProperties {
    private final Player player;

    public PlayerObjectValue(Player player) {
        this.player = player;
    }

    @Override
    public String getTypeName() {
        return "Player";
    }

    @Override
    public String toStringValue() {
        return "<player_instance>";
    }

    @Override
    public boolean equals(Value other) {
        return other == this;
    }

    public Player getBukkitPlayer() {
        return player;
    }

    @Override
    public Value getProperty(String propertyName) {

        switch (propertyName) {
            case "name": return new StringValue(player.getName());
            case "displayName": return new StringValue(player.getDisplayName());
            case "canFly": return new BooleanValue(player.getAllowFlight());
            case "isFlying": return new BooleanValue(player.isFlying());
            case "isSneaking": return new BooleanValue(player.isSneaking());
            case "isSprinting": return new BooleanValue(player.isSprinting());
            case "world": return new WorldValue(player.getWorld());
            case "location": return new LocationValue(player.getLocation());

            case "hasPermission": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->hasPermission", true, line, args, "string");
                    return new BooleanValue(player.isOp() || player.hasPermission(args[0].toStringValue()));
                }
            };

            case "givePermission": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->givePermission", true, line, args, "string");

                    StringValue permission = (StringValue)args[0];

                    PermissionAttachment attachment = CodeWorldsPlugin.getAttachments().get(player.getName());
                    attachment.setPermission(permission.getValue(),true);

                    return Interpreter.nullValue;
                }
            };

            case "removePermission": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->givePermission", true, line, args, "string");

                    StringValue permission = (StringValue)args[0];

                    PermissionAttachment attachment = CodeWorldsPlugin.getAttachments().get(player.getName());
                    attachment.setPermission(permission.getValue(),false);

                    return Interpreter.nullValue;
                }
            };

            case "setPermission": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->givePermission", true, line, args, "string", "boolean");

                    StringValue permission = (StringValue)args[0];
                    BooleanValue value = (BooleanValue)args[1];

                    PermissionAttachment attachment = CodeWorldsPlugin.getAttachments().get(player.getName());
                    attachment.setPermission(permission.getValue(), value.getValue());

                    return Interpreter.nullValue;
                }
            };

            case "teleport": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->teleport", true, line, args, "Location");
                    LocationValue locationValue = (LocationValue)args[0];
                    Bukkit.getScheduler().runTask(CodeWorldsPlugin.getInstance(), () -> player.teleport(locationValue.getBukkitLocation()));
                    return Interpreter.nullValue;
                }
            };

            case "sendMessage": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    if(args.length < 1)
                        throw interpreter.error(line, "Too few arguments passed to player->sendMessage");

                    player.sendMessage(args[0].toStringValue());
                    return Interpreter.nullValue;
                }
            };

            case "setFlying": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->setFlying", true, line, args, "boolean");
                    BooleanValue flyMode = (BooleanValue)args[0];
                    player.setFlying(flyMode.getValue());
                    return Interpreter.nullValue;
                }
            };

            case "setAllowFlight": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->setAllowFlight", true, line, args, "boolean");
                    BooleanValue allowFlight = (BooleanValue)args[0];
                    player.setAllowFlight(allowFlight.getValue());
                    return Interpreter.nullValue;
                }
            };

            case "kick": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->kick", true, line, args, "string");
                    StringValue message = (StringValue)args[0];
                    Bukkit.getScheduler().runTask(CodeWorldsPlugin.getInstance(), () -> player.kickPlayer(message.getValue()));
                    return Interpreter.nullValue;
                }
            };

            case "give": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    interpreter.validateArguments("player->give", true, line, args, "Item");
                    ItemValue itemValue = (ItemValue)args[0];
                    player.getInventory().addItem(itemValue.getBukkitItem());
                    return itemValue;
                }
            };

            case "execute": return new RunnableValue() {
                @Override
                public Value run(Interpreter interpreter, Value[] args, int line) {
                    for(Value value : args)
                        Bukkit.dispatchCommand(player, value.toStringValue());

                    return Interpreter.nullValue;
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
