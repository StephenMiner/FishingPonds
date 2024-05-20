package me.stephenminer.fishingponds.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Items {
    public ItemStack pondWand(){
        ItemStack item = new ItemStack(Material.IRON_AXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Pond-Wand");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Left-Click: set pos1");
        lore.add(ChatColor.YELLOW + "Right-Click: set pos2");
        lore.add(ChatColor.BLACK + "fp:pond-wand");
        meta.setLore(lore);
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        return item;
    }


    public ItemStack filler(){
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(" ");
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack goldenFish(){
        ItemStack item = new ItemStack( Material.TROPICAL_FISH);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Shiny Fish");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.ITALIC + "Just how shiny is it?");
        lore.add(ChatColor.ITALIC + "Nobody knows");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
