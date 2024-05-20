package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.region.Region;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.stream.Collectors;

public class ShowBorder implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;
    public ShowBorder(FishingPonds plugin){
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (!player.hasPermission("fp.commands.showborder")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            if (args.length < 1){
                player.sendMessage(ChatColor.RED + "You need to input a region id to show the border of!");
                return false;
            }
            String id = args[0];
            Region region = fromId(id);
            if (region == null){
                player.sendMessage(ChatColor.RED + "Region with id " + id + " doesn't exist!");
                return false;
            }
            boolean current = region.isShowing(player);
            if (!current){
                region.showBorder(player);
                player.sendMessage(ChatColor.GREEN + "Showing border for region with id " + id);
                timeoutTimer(player,region,5*20*60);
            }else {
                region.setShowing(player,false);
                player.sendMessage(ChatColor.GREEN + "Stopped showing border for region with id " + id);
            }
            return true;
        }else sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
        return false;
    }

    public Region fromId(String id){
        return Region.regions.stream()
                .filter((region)-> region.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return regionIds(args[0]);
        return null;
    }



    public List<String> regionIds(String match){
        return plugin.filter(Region.regions.stream().map(Region::getId).collect(Collectors.toList()), match);
    }


    private void timeoutTimer(Player player, Region region, int timeout){
        new BukkitRunnable(){
            private int count;
            @Override
            public void run(){
                if (count >= timeout || !region.isShowing(player)){
                    this.cancel();
                    region.setShowing(player,false);
                }
                count++;
            }
        }.runTaskTimer(plugin, 1, 1);
    }
}
