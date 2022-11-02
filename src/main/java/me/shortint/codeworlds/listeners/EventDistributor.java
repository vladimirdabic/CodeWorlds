package me.shortint.codeworlds.listeners;

import me.shortint.codeworlds.CodeWorldsPlugin;
import me.shortint.codeworlds.customtypes.PlayerObjectValue;
import me.shortint.cwlang.interpreter.Environment;
import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.InterpreterError;
import me.shortint.cwlang.interpreter.types.*;
import me.shortint.cwlang.parser.Statement;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class EventDistributor implements Listener {
    public final HashMap<String, Set<EventData>> eventListeners = new HashMap<>();
    private final CodeWorldsPlugin plugin = CodeWorldsPlugin.getInstance();


    public void registerEventListener(String eventName, String[] eventArguments, Statement body) {
        eventListeners.putIfAbsent(eventName, new HashSet<>());

        EventData data = new EventData(eventName, eventArguments, body, CodeWorldsPlugin.getInterpreter().environment, CodeWorldsPlugin.getInterpreter().context);
        eventListeners.get(eventName).add(data);
    }


    public void executeEvent(String eventName, Value[] args) {
        Set<EventData> data = eventListeners.get(eventName);
        if(data == null) return;

        Interpreter interpreter = CodeWorldsPlugin.getInterpreter();

        for(EventData eventData : data) {
            interpreter.context = eventData.eventContext;
            interpreter.environment = new Environment(eventData.eventEnvironment);

            for(int i = 0; i < Math.min(args.length, eventData.eventArguments.length); ++i)
                interpreter.environment.declare(eventData.eventArguments[i], args[i]);

            try {
                interpreter.evaluate(eventData.eventBody);
            } catch (InterpreterError e) {
                plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + e.getMessage());
            }
        }
    }



    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        ObjectValue objectValue = new ObjectValue();
        objectValue.setProperty("player", new PlayerObjectValue(e.getPlayer()));
        objectValue.setProperty("cancelled", new BooleanValue(false));
        objectValue.setProperty("message", new StringValue(e.getMessage()));

        executeEvent("onPlayerChat", new Value[] { objectValue });

        Value cancelled = objectValue.getProperty("cancelled");
        if(cancelled instanceof BooleanValue)
            e.setCancelled(((BooleanValue)cancelled).getValue());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        CodeWorldsPlugin.getAttachments().put(player.getName(), player.addAttachment(CodeWorldsPlugin.getInstance()));

        ObjectValue objectValue = new ObjectValue();
        objectValue.setProperty("player", new PlayerObjectValue(player));

        executeEvent("onPlayerJoin", new Value[] { objectValue });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        ObjectValue objectValue = new ObjectValue();
        objectValue.setProperty("player", new PlayerObjectValue(player));

        executeEvent("onPlayerLeave", new Value[] { objectValue });

        player.removeAttachment(CodeWorldsPlugin.getAttachments().get(player.getName()));
    }
}
