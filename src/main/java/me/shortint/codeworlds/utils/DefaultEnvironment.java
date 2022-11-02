package me.shortint.codeworlds.utils;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.codeworlds.commands.CodeWorldsRegisteredCommand;
import me.shortint.codeworlds.customtypes.*;
import me.shortint.cwlang.interpreter.Environment;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.*;
import me.shortint.cwlang.interpreter.types.ClassValue;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;

import java.util.ArrayList;

public class DefaultEnvironment {
    public final Environment environment = new Environment();

    public DefaultEnvironment() {
        initialize();
    }

    private void initialize() {
        environment.declare("print", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                if(args.length == 0)
                    return Interpreter.nullValue;

                Bukkit.getServer().getConsoleSender().sendMessage(args[0].toStringValue());
                return Interpreter.nullValue;
            }
        });

        environment.declare("typeof", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                if(args.length == 0)
                    throw interpreter.error(line, "no value passed to typeof function");

                return new StringValue(args[0].getTypeName());
            }
        });

        environment.declare("Location", new LocationClassValue());
        environment.declare("World", new WorldClassValue());
        environment.declare("Player", new PlayerClassValue());
        environment.declare("CommandSender", new CommandSenderClassValue());
        environment.declare("CodeWorlds", codeWorldsObject());
        environment.declare("Block", new BlockClassValue());
        environment.declare("Item", new ItemClassValue());
        environment.declare("Console", new ConsoleClassValue());

        environment.declare("num", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                interpreter.validateArguments("num", true, line, args, "string");
                StringValue stringNumber = (StringValue)args[0];
                double parsed;

                try {
                    parsed = Double.parseDouble(stringNumber.getValue());
                } catch(NumberFormatException ignored) {
                    return Interpreter.nullValue;
                }

                return new NumberValue(parsed);
            }
        });

        environment.declare("str", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                if(args.length < 1)
                    throw interpreter.error(line, "str function expects 1 argument");

                Value value = args[0];
                return new StringValue(value.toStringValue());
            }
        });
    }

    public ObjectValue codeWorldsObject() {
        ObjectValue object = new ObjectValue();

        object.setProperty("getPlayer", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                interpreter.validateArguments("CodeWorlds.getPlayer", true, line, args, "string");
                StringValue playerName = (StringValue)args[0];
                Player player = Bukkit.getPlayer(playerName.getValue());

                return player == null ? Interpreter.nullValue : new PlayerObjectValue(player);
            }
        });

        object.setProperty("broadcast", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                interpreter.validateArguments("CodeWorlds.broadcast", true, line, args, "string");
                StringValue message = (StringValue)args[0];
                Bukkit.broadcastMessage(message.getValue());
                return Interpreter.nullValue;
            }
        });

        object.setProperty("getWorld", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                interpreter.validateArguments("CodeWorlds.getWorld", true, line, args, "string");
                StringValue worldName = (StringValue)args[0];
                World world = Bukkit.getWorld(worldName.getValue());
                return world == null ? Interpreter.nullValue : new WorldValue(world);
            }
        });

        object.setProperty("removeCommand", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                interpreter.validateArguments("CodeWorlds.removeCommand", true, line, args, "string");
                StringValue commandName = (StringValue)args[0];

                for(CodeWorldsRegisteredCommand command : CodeWorldsPlugin.getRegisteredCommands()) {
                    if(command.getName().equals(commandName.getValue())) {
                        HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(command.commandCategory);
                        // remove category
                        if(topic != null) Bukkit.getServer().getHelpMap().getHelpTopics().remove(topic);
                        command.unregister();
                        return new BooleanValue(true);
                    }
                }

                return new BooleanValue(false);
            }
        });

        object.setProperty("addCommand", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {

                if(args.length < 2)
                    throw interpreter.error(line, "Expected two arguments in addCommand function: string, object (command data)");

                if(!(args[0] instanceof StringValue && (args[1] instanceof ObjectValue || args[1] instanceof ClassInstanceValue || args[1] instanceof ClassValue)))
                    throw interpreter.error(line, "Expected two arguments in addCommand function: string, object (command data)");

                StringValue commandName = (StringValue)args[0];
                ValueProperties data = (ValueProperties)args[1];

                Value executor = data.getProperty("executor");

                if(executor == null)
                    throw interpreter.error(line, "Undefined executor for command '" + commandName + "'");

                if(!(executor instanceof ValueCallable))
                    throw interpreter.error(line, "Executor for command '" + commandName + "' must be a callable value");

                Value temp;
                temp = data.getProperty("description");
                String description = temp instanceof NullValue ? "No description provided" : temp.toStringValue();

                temp = data.getProperty("usage");
                String usage = temp instanceof NullValue ? "/" + commandName : temp.toStringValue();

                temp = data.getProperty("category");
                String category = temp instanceof NullValue ? interpreter.context : temp.toStringValue();

                temp = data.getProperty("aliases");
                ArrayList<String> aliasesList = new ArrayList<>();

                if(!(temp instanceof NullValue)) {
                    if (!(temp instanceof ListValue))
                        throw interpreter.error(line, "Aliases for command '" + commandName + "' must be a list of strings");

                    ListValue aliasesValue = (ListValue) temp;

                    for (Value alias : aliasesValue.getValues()) {
                        if (!(alias instanceof StringValue))
                            throw interpreter.error(line, "Aliases for command '" + commandName + "' must be a list of strings");
                        StringValue aliasString = (StringValue) alias;
                        aliasesList.add(aliasString.getValue());
                    }
                }

                CodeWorldsRegisteredCommand command = new CodeWorldsRegisteredCommand(commandName.getValue(), description, usage, category, aliasesList, (ValueCallable)executor, interpreter.context, line, interpreter.environment);
                command.register();
                CodeWorldsPlugin.getRegisteredCommands().add(command);

                return new BooleanValue(true);
            }
        });

        object.setProperty("getConsole", new RunnableValue() {
            @Override
            public Value run(Interpreter interpreter, Value[] args, int line) {
                return new ConsoleValue();
            }
        });

        return object;
    }
}
