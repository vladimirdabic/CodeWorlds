package me.shortint.codeworlds;

import me.shortint.codeworlds.commands.CodeWorldsCommand;
import me.shortint.codeworlds.commands.CodeWorldsRegisteredCommand;
import me.shortint.codeworlds.listeners.EventDistributor;
import me.shortint.codeworlds.utils.RegisterInterface;
import me.shortint.codeworlds.utils.DefaultEnvironment;
import me.shortint.cwlang.interpreter.Environment;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.InterpreterError;
import me.shortint.cwlang.lexer.Lexer;
import me.shortint.cwlang.lexer.LexerError;
import me.shortint.cwlang.lexer.Token;
import me.shortint.cwlang.parser.Parser;
import me.shortint.cwlang.parser.ParserError;
import me.shortint.cwlang.parser.Statement;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.help.HelpTopic;
import org.bukkit.help.IndexHelpTopic;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public final class CodeWorldsPlugin extends JavaPlugin {
    private static CodeWorldsPlugin instance;
    private static EventDistributor eventDistributor;
    private static DefaultEnvironment defaultEnvironment;

    private static final Lexer cwLexer = new Lexer();
    private static final Parser cwParser = new Parser();
    private static final Interpreter cwInterpreter = new Interpreter(null, null);
    private static final HashSet<CodeWorldsRegisteredCommand> commands = new HashSet<>();
    private static final HashMap<String,PermissionAttachment> attachments = new HashMap<>();

    public static CodeWorldsPlugin getInstance() {
        return instance;
    }
    public static Environment getDefaultEnvironment() {
        return defaultEnvironment.environment;
    }
    public static EventDistributor getEventDistributor() {
        return eventDistributor;
    }
    public static Lexer getLexer() {
        return cwLexer;
    }
    public static Parser getParser() {
        return cwParser;
    }
    public static Interpreter getInterpreter() {
        return cwInterpreter;
    }
    public static HashSet<CodeWorldsRegisteredCommand> getRegisteredCommands() {
        return commands;
    }
    public static HashMap<String, PermissionAttachment> getAttachments() {
        return attachments;
    }

    public final FileConfiguration config = getConfig();


    @Override
    public void onEnable() {
        instance = this;
        eventDistributor = new EventDistributor();
        defaultEnvironment = new DefaultEnvironment();
        Objects.requireNonNull(getCommand("codeworlds")).setExecutor(new CodeWorldsCommand());
        getServer().getPluginManager().registerEvents(eventDistributor, this);
        getInterpreter().setCodeWorldsInterface(new RegisterInterface());

        if(!getDataFolder().exists()) {
            if (getDataFolder().mkdir())
                getServer().getConsoleSender().sendMessage("§aCodeWorlds directory created");
            else
                getServer().getConsoleSender().sendMessage("§cFailed to create CodeWorlds directory");
        }

        File scriptsFolder = Paths.get(getDataFolder().getAbsolutePath() + "/scripts").toFile();
        File requireFolder = Paths.get(getDataFolder().getAbsolutePath() + "/require").toFile();

        if(!scriptsFolder.exists())
            getServer().getConsoleSender().sendMessage(scriptsFolder.mkdir() ? "§aCodeWorlds scripts directory created" : "§cFailed to create CodeWorlds scripts directory" );

        if(!requireFolder.exists())
            getServer().getConsoleSender().sendMessage(requireFolder.mkdir() ? "§aCodeWorlds require directory created" : "§cFailed to create CodeWorlds require directory" );


        // initialize permission attachments
        for(Player player : Bukkit.getOnlinePlayers())
            getAttachments().put(player.getName(), player.addAttachment(this));


        // execute scripts after all plugins have been loaded
        // this is done to avoid losing other plugin commands when overriding commands with codeworlds
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                executeScripts(getServer().getConsoleSender());
                syncCommands();
            }
        }, 1);
    }

    @Override
    public void onDisable() {

    }


    public static void executeFile(File file, CommandSender sender) {
        String fileName = file.getName();
        if(file.isFile() && fileName.length() > 3 && fileName.endsWith(".cw")) {
            // execute script
            try {
                String data = Files.readString(Paths.get(file.getPath()));
                Token[] tokens;
                Statement tree;

                tokens = getLexer().lex(data, fileName);
                tree = getParser().parse(tokens, fileName);

                getInterpreter().context = fileName.substring(0, fileName.length() - 3);
                getInterpreter().environment = new Environment(getDefaultEnvironment());
                getInterpreter().evaluate(tree);

                if(sender != null) sender.sendMessage(ChatColor.GREEN + "Script '" + fileName + "' executed");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (LexerError | ParserError | InterpreterError e) {
                if(sender != null) sender.sendMessage(ChatColor.RED + e.getMessage());
            }
        }
    }

    public void executeScripts(CommandSender sender) {
        File scriptsFolder = Paths.get(getDataFolder().getAbsolutePath() + "/scripts").toFile();

        if(scriptsFolder.isDirectory()) {
            for(File file : Objects.requireNonNull(scriptsFolder.listFiles()))
                executeFile(file, sender);
        }

        registerIndexes();
    }

    public void registerIndexes() {
        Map<String, Set<HelpTopic>> scriptTopics = new HashMap<>();

        for(CodeWorldsRegisteredCommand command : getRegisteredCommands()) {
            if(!scriptTopics.containsKey(command.commandCategory))
                scriptTopics.put(command.commandCategory, new HashSet<>());

            HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic("/" + command.getLabel());
            scriptTopics.get(command.commandCategory).add(topic);
        }

        for(Map.Entry<String, Set<HelpTopic>> entry : scriptTopics.entrySet())
            Bukkit.getServer().getHelpMap().addTopic(new IndexHelpTopic(entry.getKey(), "All commands for " + entry.getKey(), null, entry.getValue(), "Below is a list of all " + entry.getKey() + " commands:"));
    }

    public void reload(CommandSender sender) {
        getEventDistributor().eventListeners.clear();

        for(CodeWorldsRegisteredCommand command : CodeWorldsPlugin.getRegisteredCommands()) {
            HelpTopic topic = Bukkit.getServer().getHelpMap().getHelpTopic(command.commandCategory);

            // remove category
            if(topic != null)
                Bukkit.getServer().getHelpMap().getHelpTopics().remove(topic);

            command.unregister();
        }

        CodeWorldsPlugin.getRegisteredCommands().clear();
        executeScripts(sender);
        syncCommands();
    }

    public static void syncCommands() {
        try {
            Method m = Bukkit.getServer().getClass().getDeclaredMethod("syncCommands");
            m.setAccessible(true); // just in case
            m.invoke(Bukkit.getServer());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // Well, it's not a newer version so just ignore the error
        }
    }
}
