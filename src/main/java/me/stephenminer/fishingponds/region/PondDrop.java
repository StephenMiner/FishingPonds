package me.stephenminer.fishingponds.region;

import org.bukkit.inventory.ItemStack;

public class PondDrop{
    private ItemStack item;
    private int weight;
    private DropType type;
    public PondDrop(ItemStack item, int chance, DropType type) {
        if (chance == -1) chance = 50;
        this.item = item;
        this.weight = chance;
        this.type = type;
    }


    public ItemStack item(){ return item; }
    public DropType type(){ return type; }
    public int weight(){ return weight; }


    public void setItem(ItemStack item){ this.item = item; }
    public void setType(DropType type){ this.type = type; }
    public void setWeight(int weight){ this.weight = weight; }
}
