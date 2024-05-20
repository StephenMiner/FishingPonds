package me.stephenminer.fishingponds.region;

import me.stephenminer.fishingponds.items.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class RegionHelper {
    public static HashMap<UUID, RegionHelper> helpers = new HashMap<>();
    private final Region region;
    private final Player player;

    private List<PondDrop> tempDrops;
    private InvType current;
    private Inventory inv;

    private boolean listenChat;
    private ItemStack listenItem;

    public RegionHelper(Player player, Region region){
        this.player = player;
        this.region = region;
        player.openInventory(mainMenu());
        tempDrops = new ArrayList<>();
    }


    public Inventory mainMenu(){
        current = InvType.MAIN;
        Inventory inv = Bukkit.createInventory(player,9, ChatColor.BLUE + "Main Menu");
        Items items = new Items();
        for (int i = 0; i < 9; i++){
            inv.setItem(i, items.filler());
        }
        inv.setItem(5, fishIcon());
        inv.setItem(6, junkIcon());
        inv.setItem(7, treasureIcon());
        this.inv = inv;
        return inv;
    }

    private Inventory dropMenu(Set<PondDrop> drops, String name){
        Items items = new Items();
        Inventory inv = Bukkit.createInventory(player,54, name);
        for (int i = 45; i < 54; i++){
            inv.setItem(i, items.filler());
        }
        int index = 0;
        for (PondDrop drop : drops){
            if (index >= 45) break;
            inv.setItem(index, drop.item());
            index++;
        }
        this.inv = inv;
        return inv;
    }
    public Inventory fishMenu(){
        current = InvType.FISH;
        return dropMenu(region.pond().fishDrops(), ChatColor.BLUE + "Fish Drops");
    }
    public Inventory junkMenu(){
        current = InvType.JUNK;
        return dropMenu(region.pond().junkDrops(), ChatColor.BLUE + "Junk Drops");
    }
    public Inventory treasureMenu(){
        current = InvType.TREASURE;
        return dropMenu(region.pond().treasureDrops(), ChatColor.BLUE + "Treasure Drops");
    }

    public void chance(ItemStack item){
        current = InvType.CHANCE;
        listenChat = true;
        listenItem = item;
        player.sendMessage(ChatColor.GOLD + "Type out the weight you want the item to have to drop");
        double current = region.pond().getPotentialDrops().get(item).weight();
        player.sendMessage(ChatColor.GOLD + "Current weight: " + current);
        inv = null;

    }

    public void onClose(){
        if (current == InvType.CHANCE){
            ItemStack item = inv.getItem(0);
            PondDrop drop = fromItem(item);
            if (drop == null) {
                //System.out.println("OOF");
                return;
            }
            int chance = Integer.parseInt(((AnvilInventory) inv).getRenameText());
            drop.setWeight(chance);
        }else if (current != InvType.MAIN){
            List<PondDrop> toRemove = new ArrayList<>();
            DropType checkFor = fromCurrentInv();
            if (checkFor == null){
                //System.out.println("OOF 2");
                return;
            }
            Collection<PondDrop> copyDrops = region.pond().getPotentialDrops().values();
            for (PondDrop drop : copyDrops){
                if (drop.type() == checkFor && !containsItem(drop.item())) toRemove.add(drop);
            }
            toRemove.forEach((PondDrop drop)-> region.pond().removeDrop(drop.item()));
            ItemStack[] contents = inv.getContents();
            for (int i = 0; i < 45; i++){
                ItemStack item = contents[i];
                if (item == null || item.getType().isAir()) continue;
                if (region.pond().getPotentialDrops().containsKey(item)) continue;
                else region.pond().addDrop(item, 50, checkFor);
            }
        }
        current = InvType.MAIN;
    }

    private boolean containsItem(ItemStack item){
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < 45; i++){
            ItemStack content = contents[i];
            if (content.isSimilar(item)) return true;
        }
        return false;
    }



    public void writeDrops(){
        
    }

    public void onChat(int weight){
        if (current == InvType.CHANCE){
            if (listenChat && listenItem != null){
                PondDrop drop = region.pond().getPotentialDrops().get(listenItem);
                if (drop != null) drop.setWeight(weight);
                current = InvType.MAIN;
            }
        }
    }


    public PondDrop fromItem(ItemStack item){
        for (PondDrop drop : region.pond().getPotentialDrops().values()){
            if (drop.item().equals(item)) return drop;
        }
        return null;
    }

    public DropType fromCurrentInv(){
        DropType type = null;
        switch (current){
            case FISH:
                type = DropType.FISH;
                break;
            case JUNK:
                type = DropType.JUNK;
                break;
            case TREASURE:
                type = DropType.TREASURE;
                break;
        }
        return type;
    }







    public ItemStack fishIcon(){
        ItemStack item = new ItemStack(Material.COD);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Set Fishing Drops");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.ITALIC + "Fish drops decreasd slightly");
        lore.add(ChatColor.ITALIC + "by luck of the sea but like normal,");
        lore.add(ChatColor.ITALIC + "fish drops have a higher chance of being selected");
        lore.add(ChatColor.BLACK + "fp:fish-icon");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack junkIcon(){
        ItemStack item = new ItemStack(Material.LEATHER_BOOTS);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Set Junk Drops");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.ITALIC + "Junk drops are decreased");
        lore.add(ChatColor.ITALIC + "by luck of the sea");
        lore.add(ChatColor.BLACK + "fp:junk-icon");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack treasureIcon(){
        ItemStack item = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Set Treasure Drops");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.ITALIC + "Treasure drops are increased by ");
        lore.add(ChatColor.ITALIC + "luck of the sea");
        lore.add(ChatColor.BLACK + "fp:treasure-icon");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public Inventory getCurrentInv(){ return inv; }
    public InvType getStatus(){ return current; }


    public void setStatus(InvType status){ this.current = status; }
    public void setInv(Inventory inv){ this.inv = inv; }

    public boolean listenChat(){
        return listenChat;
    }
    public void setListenChat(boolean listenChat){
        this.listenChat = listenChat;
    }





    public enum InvType{
        MAIN,
        FISH,
        JUNK,
        TREASURE,
        CHANCE
    }

}
