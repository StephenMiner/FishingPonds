package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class GoldFishCmd implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;

    public GoldFishCmd(FishingPonds plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (!player.hasPermission("fp.commands.goldfish")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            int size = args.length;
            if (size >= 1){
                String sub = args[0];
                if (sub.equalsIgnoreCase("setItem")){
                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (!item.hasItemMeta() || item.getType().isAir()){
                        player.sendMessage(ChatColor.RED + "You must be holding an item to set the gold-fish item!");
                        return false;
                    }
                    setGoldFish(item);
                    player.sendMessage(ChatColor.GREEN + "Set gold-fish item!");
                    return true;
                }
                if (size >= 2){
                    String arg = args[1];
                    if (sub.equalsIgnoreCase("minTime")){
                        setMinTime(Integer.parseInt(arg));
                        player.sendMessage(ChatColor.GREEN + "Set min-time for gold fish");
                        return true;
                    }
                    if (sub.equalsIgnoreCase("maxTime")){
                        setMaxTime(Integer.parseInt(arg));
                        player.sendMessage(ChatColor.GREEN + "Set max-time for gold fish");
                        return true;
                    }
                    if (sub.equalsIgnoreCase("setChance")){
                        setChance(Float.parseFloat(arg));
                        player.sendMessage(ChatColor.GREEN + "Set chance for golden fish");
                        return true;
                    }
                }else player.sendMessage(ChatColor.RED + "You must input a time in ticks or chance for this command!");
            }else player.sendMessage(ChatColor.RED + "You must input a sub-cmd at the very least!");
        }else sender.sendMessage(ChatColor.RED + "Only players can use this command!");
        return false;
    }


    private void setGoldFish(ItemStack item){
        plugin.settings.reloadConfig();
        plugin.settings.getConfig().set("settings.golden-fish.item", item);
        plugin.settings.saveConfig();
    }
    private void setMaxTime(int time){
        plugin.settings.reloadConfig();
        plugin.settings.getConfig().set("settings.golden-fish.max-time",time);
        plugin.settings.saveConfig();
    }
    private void setMinTime(int time){
        plugin.settings.reloadConfig();
        plugin.settings.getConfig().set("settings.golden-fish.min-time",time);
        plugin.settings.saveConfig();
    }
    private void setChance(float chance){
        plugin.settings.reloadConfig();
        plugin.settings.getConfig().set("settings.golden-fish.chance", chance);
        plugin.settings.saveConfig();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return subCmds(args[0]);
        if (size == 2 && !args[0].equalsIgnoreCase("setItem")) return arg();
        return null;
    }


    private List<String> subCmds(String match){
        List<String> subs = new LinkedList<>();
        subs.add("setItem");
        subs.add("minTime");
        subs.add("maxTime");
        subs.add("setChance");
        return plugin.filter(subs, match);
    }
    private List<String> arg(){
        List<String> arg = new LinkedList<>();
        arg.add("[time-in-ticks]");
        return arg;
    }
}
