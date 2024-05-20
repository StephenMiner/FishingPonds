package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.items.Items;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveWand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (sender instanceof Player){
            Player player = (Player) sender;
            if (!player.hasPermission("fp.commands.wand")){
                player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
                return false;
            }
            Items items = new Items();
            player.getInventory().addItem(items.pondWand());
            player.sendMessage(ChatColor.GREEN + "You've received your item!");
            return true;
        }else sender.sendMessage(ChatColor.RED + "You need to be a player to use this command!");
        return false;
    }
}
