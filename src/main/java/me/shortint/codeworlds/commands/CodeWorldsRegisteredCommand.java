package me.shortint.codeworlds.commands;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.codeworlds.customtypes.CommandSenderValue;
import me.shortint.cwlang.interpreter.Environment;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.InterpreterError;
import me.shortint.cwlang.interpreter.types.StringValue;
import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.interpreter.types.ValueCallable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.help.*;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class CodeWorldsRegisteredCommand extends Command implements PluginIdentifiableCommand {
    public final Environment commandEnvironment;
    public final int commandLine;
    public final String commandContext;
    public final String commandCategory;
    public final ValueCallable executor;

    private HelpTopic helpTopic;
    private Command previous;

    public CodeWorldsRegisteredCommand(String name, String description, String usageMessage, String category, List<String> aliases, ValueCallable executor, String commandContext, int commandLine, Environment commandEnvironment) {
        super(name, description, usageMessage, aliases);
        this.executor = executor;
        this.commandContext = commandContext;
        this.commandCategory = category;
        this.commandLine = commandLine;
        this.commandEnvironment = commandEnvironment;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        Interpreter interpreter = CodeWorldsPlugin.getInterpreter();

        interpreter.context = commandContext;
        interpreter.environment = new Environment(commandEnvironment);

        final ArrayList<Value> valueArgs = new ArrayList<>();
        valueArgs.add(new CommandSenderValue(sender));
        for(String arg : args)
            valueArgs.add(new StringValue(arg));

        try {
            executor.run(interpreter, valueArgs.toArray(new Value[0]), commandLine);
        } catch (InterpreterError e) {
            sender.sendMessage((sender.isOp() || sender.hasPermission("codeworlds.debug")) ? ChatColor.RED + e.getMessage() : ChatColor.RED + "An internal error occurred.");
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public void register() {
        try {
            final Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            final SimpleCommandMap commandMap = (SimpleCommandMap) f.get(Bukkit.getPluginManager());

            final Field knownCmds = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCmds.setAccessible(true);

            final HashMap<String, Command> knownCommands = (HashMap<String, Command>) knownCmds.get(commandMap);
            previous = knownCommands.put(getName(), this);
            knownCommands.put("codeworlds:" + commandCategory.toLowerCase() + ":" + getName(), this);

            for (String alias : getAliases()) {
                knownCommands.put("codeworlds:" + commandCategory.toLowerCase() + ":" + alias, this);
                if(!knownCommands.containsKey(alias)) {
                    knownCommands.put(alias, this);
                }
            }

            //commandMap.register("codeworlds", this);
            register(commandMap);
            registerHelp();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public void unregister() {
        try {
            final Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            final SimpleCommandMap commandMap = (SimpleCommandMap) f.get(Bukkit.getPluginManager());

            final Field knownCmds = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCmds.setAccessible(true);

            final HashMap<String, Command> knownCommands = (HashMap<String, Command>) knownCmds.get(commandMap);
            knownCommands.remove(getName());
            knownCommands.remove("codeworlds:" + commandCategory.toLowerCase() + ":" + getName());
            this.unregister(commandMap);

            resetToDefault(knownCommands);

            for (String alias : getAliases()){
                if(knownCommands.containsKey(alias) && knownCommands.get(alias).toString().contains(CodeWorldsPlugin.getInstance().getName())) {
                    knownCommands.remove(alias);
                    knownCommands.remove("codeworlds:" + commandCategory.toLowerCase() + ":" + alias);
                }
            }

            unregisterHelp();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static void unregisterPrefixes(String prefix) {
        try {
            final Field f = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            f.setAccessible(true);
            final SimpleCommandMap commandMap = (SimpleCommandMap) f.get(Bukkit.getPluginManager());

            final Field knownCmds = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCmds.setAccessible(true);

            final HashMap<String, Command> knownCommands = (HashMap<String, Command>) knownCmds.get(commandMap);

            for(Map.Entry<String, Command> commandEntry : knownCommands.entrySet()) {
                if(commandEntry.getKey().startsWith(prefix)) {
                    knownCommands.remove(commandEntry.getKey());
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void resetToDefault(HashMap<String, Command> knownCommands) {
        if(previous != null) {
            if(previous instanceof CodeWorldsRegisteredCommand)
                ((CodeWorldsRegisteredCommand)previous).resetToDefault(knownCommands);
            else
                knownCommands.put(previous.getLabel(), previous);
        }
    }


    public void registerHelp() {
        final HelpMap helpMap = Bukkit.getHelpMap();
        final HelpTopic topic = new GenericCommandHelpTopic(this);
        helpTopic = topic;
        helpMap.addTopic(topic);
    }

    public void unregisterHelp() {
        Bukkit.getHelpMap().getHelpTopics().remove(helpTopic);
    }

    @Override
    public Plugin getPlugin() {
        return CodeWorldsPlugin.getInstance();
    }

}
