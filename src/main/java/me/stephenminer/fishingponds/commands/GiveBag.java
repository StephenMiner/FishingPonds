package me.stephenminer.fishingponds.commands;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.items.Backpack;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class GiveBag implements CommandExecutor, TabCompleter {
    private final FishingPonds plugin;
    public GiveBag(FishingPonds plugin){
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (!sender.hasPermission("fp.commands.givebag")){
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            return false;
        }
        int length = args.length;
        if (length < 3){
            sender.sendMessage(ChatColor.RED + "You must specify the player you want to send the bag to, the size, and the type of the bag. Optionally, you can include a material and name for the bag, but a default one will be provided based on your previous arguments");
            return false;
        }
        Player player = Bukkit.getPlayerExact(args[0]);
        Backpack.Size size = Backpack.Size.valueOf(args[1]);
        Backpack.BagType type = Backpack.BagType.valueOf(args[2]);
        Backpack backpack = null;
        if (length >= 5){
            Material mat = Material.matchMaterial(args[3]);
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 4; i < length; i++){
                nameBuilder.append(ChatColor.translateAlternateColorCodes('&',args[i]));
            }
            backpack = new Backpack(nameBuilder.toString(), mat, size, type, UUID.randomUUID());
        }else{
            backpack = new Backpack(size, type);
        }
        HashMap<Integer, ItemStack> drop = player.getInventory().addItem(backpack.getItem());
        World world = player.getWorld();
        drop.values().forEach(item->world.dropItem(player.getLocation(),item));
        player.sendMessage(ChatColor.GREEN + "You've gotten a backpack");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args){
        int size = args.length;
        if (size == 1) return players(args[0]);
        if (size == 2) return sizes(args[1]);
        if (size == 3) return types(args[2]);
        if (size == 4) return materials(args[3]);
        return null;
    }



    private List<String> players(String match){
        List<String> players = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        return plugin.filter(players, match);
    }

    private List<String> sizes(String match){
        return plugin.filter(Arrays.stream(Backpack.Size.values()).map(Backpack.Size::name).collect(Collectors.toList()), match);
    }

    private List<String> types(String match){
        return plugin.filter(Arrays.stream(Backpack.BagType.values()).map(Backpack.BagType::name).collect(Collectors.toList()), match);
    }

    private List<String> materials(String match){
        List<String> sMats = Arrays.stream(Material.values()).filter(Material::isItem).map(Material::name).collect(Collectors.toList());
        return plugin.filter(sMats, match);
    }

}
