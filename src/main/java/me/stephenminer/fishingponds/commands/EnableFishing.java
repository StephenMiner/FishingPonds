package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Fish;

import java.util.LinkedList;
import java.util.List;

public class EnableFishing implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;

    public EnableFishing(FishingPonds plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("fp.commands.enablefishing")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this commands!");
            return false;
        }
        int size = args.length;
        if (size < 1){
            sender.sendMessage(ChatColor.RED + "You must put a true/false value");
            return false;
        }
        boolean enable = Boolean.parseBoolean(args[0]);
        setEnabled(enable);
        sender.sendMessage(ChatColor.GREEN + "Set natural fishing allowed to " + enable);
        return true;
    }

    private void setEnabled(boolean enable){
        plugin.settings.getConfig().set("settings.disable-nat-fishing",enable);
        plugin.settings.saveConfig();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return bools(args[0]);
        return null;
    }

    private List<String> bools(String match){
        List<String> out = new LinkedList<>();
        out.add("true");
        out.add("false");
        return plugin.filter(out,match);
    }

}
