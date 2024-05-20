package me.stephenminer.fishingponds.region;

import me.stephenminer.fishingponds.FishingPonds;
import me.stephenminer.fishingponds.items.Backpack;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class FishHelper {
    public static int RANDOM_CAP = 1000;
    private final Region region;
    private final FishingPonds plugin;
    public FishHelper(Region region){
        this.region = region;
        this.plugin = JavaPlugin.getPlugin(FishingPonds.class);
    }




    public DropType rollCategory(Player player){
        int scalar = RANDOM_CAP/100;
        int luck = getLuckOfTheSea(player.getInventory().getItemInMainHand());
        int treasureChance = (int) Math.round((5 + luck*2.1) *scalar);
        int fishChance = (int) Math.round((85 - luck*0.15) * scalar);
        int junkChance = FishHelper.RANDOM_CAP - (fishChance + treasureChance);
        int roll = ThreadLocalRandom.current().nextInt(RANDOM_CAP);

        int lowerFish = RANDOM_CAP-fishChance;
        int lowerJunk = lowerFish-junkChance;
        DropType type;
        if (roll < lowerJunk) type = DropType.TREASURE;
        else if (roll < lowerFish) type = DropType.JUNK;
        else type = DropType.FISH;
        return type;
    }

    public ItemStack reelIn(Player player){
        DropType rollType = rollCategory(player);
        Set<PondDrop> potential = null;
        switch (rollType){
            case TREASURE:
                potential = region.pond().treasureDrops();
                break;
            case JUNK:
                potential = region.pond().junkDrops();
                break;
            case FISH:
                potential = region.pond().fishDrops();
                break;
        }
        return rollDrop(potential, rollType, 1, player);
    }


    public Set<PondDrop> validateSet(Set<PondDrop> drops, DropType type){
        if (drops.isEmpty()){
            if (type == DropType.FISH) {
                if (region.pond().junkDrops().isEmpty()) return region.pond().treasureDrops();
                else return region.pond().junkDrops();
            }else if (type == DropType.TREASURE){
                if (region.pond().junkDrops().isEmpty()) return region.pond().fishDrops();
                else return region.pond().junkDrops();
            }else if (type == DropType.JUNK){
                if (region.pond().treasureDrops().isEmpty()) return region.pond().fishDrops();
                else return region.pond().treasureDrops();
            }
        }
        return drops;
    }




    public ItemStack rollDrop(Set<PondDrop> dropSet, DropType type, int attempt, Player player){
        dropSet = validateSet(dropSet,type);
        if (attempt > 100) return null;
        Map<Integer, List<PondDrop>> byWeight = byWeight(dropSet);
        List<PondDrop> sortedByChance = dropSet.stream().sorted(Comparator.comparingInt(PondDrop::weight)).collect(Collectors.toList());
        int maxBound = maxBound(sortedByChance);
        List<PondDrop> valid = null;
        for (PondDrop drop : sortedByChance){
            int roll = ThreadLocalRandom.current().nextInt(maxBound);
            int chance = (int) drop.weight();
            if (roll < chance){
                valid = byWeight.get(chance);

            }
        }
        if (valid != null && valid.size() > 0) {
            PondDrop drop = valid.get(ThreadLocalRandom.current().nextInt(valid.size()));
            String required = requiredBait(drop);
            if (required != null) {
                HashMap<String, Pair<ItemStack,Backpack>> baits = playerBaits(player);

                if (baits.containsKey(required)) {
                    Pair<ItemStack,Backpack> data = baits.get(required);
                    ItemStack item = data.entry1;
                    if (data.entry2 == null) item.setAmount(item.getAmount()-1);
                    else data.entry2.setAmount(required,item.getAmount()-1);
                    return drop.item().clone();
                }
            }else return drop.item().clone();
        }
        return rollDrop(dropSet,type,attempt+1,player);
    }

    public HashMap<Integer,List<PondDrop>> byWeight(Set<PondDrop> drops){
        HashMap<Integer, List<PondDrop>> byWeight = new HashMap<>();
        for (PondDrop drop : drops){
            if (byWeight.containsKey(drop.weight())) byWeight.get(drop.weight()).add(drop);
            else {
                List<PondDrop> add = new ArrayList<>();
                add.add(drop);
                byWeight.put(drop.weight(),add);
            }
        }
        return byWeight;
    }
    private int maxBound(List<PondDrop> drops){
        return drops.stream().mapToInt(PondDrop::weight).max().orElse(50);
    }

    public int getLure(ItemStack item){
        return item.getEnchantmentLevel(Enchantment.LURE);
    }

    public int getLuckOfTheSea(ItemStack item){
        if (item == null || !item.hasItemMeta()) return 0;
        return item.getEnchantmentLevel(Enchantment.LUCK);
    }


    public String requiredBait(PondDrop drop){
        ItemStack item = drop.item();
        String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? drop.item().getItemMeta().getDisplayName() : item.getType().name();
        return plugin.fishFile.getConfig().getString("fish." + name);
    }


    public HashMap<String, Pair<ItemStack,Backpack>> playerBaits(Player player){
        HashMap<String, Pair<ItemStack,Backpack>> baits = new HashMap<>();
        for (ItemStack item : player.getInventory().getContents()){
            String id = itemBait(item);
            if (id != null && !baits.containsKey(id)) baits.put(id, new Pair<>(item,null));
        }
        baitInBags(player, baits);
        return baits;
    }

    private void baitInBags(Player player, HashMap<String, Pair<ItemStack,Backpack>> totalBait){
        List<Backpack> bags = baitBags(playerBag(player));
        for (Backpack bag : bags){
            if (bag.getInventory() == null) bag.loadInventory();
            for (ItemStack item : bag.getInventory().getContents()){
                String id = itemBait(item);
                if (id != null && !totalBait.containsKey(id)) totalBait.put(id, new Pair<>(item,bag));
            }
            //bag.resetInventory();
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,()-> bags.forEach(Backpack::resetInventory), 1);
    }


    private boolean isBait(ItemStack item){
        return itemBait(item) != null;
    }

    private String itemBait(ItemStack item){
        Set<String> baits = plugin.baitFile.getConfig().getConfigurationSection("bait").getKeys(false);
        for (String entry : baits){
            if (plugin.checkLore(item,"fp:" + entry)) return entry;
        }
        return null;
    }
    private List<Backpack> baitBags(List<Backpack> bags){
        return bags.stream().filter(bag -> bag.getType() == Backpack.BagType.BAIT).collect(Collectors.toList());
    }
    private List<Backpack> fishbags(List<Backpack> bags){
        return bags.stream().filter(bag-> bag.getType() != Backpack.BagType.BAIT).collect(Collectors.toList());
    }

    private List<Backpack> playerBag(Player player){
        List<Backpack> bags = new ArrayList<>();
        Arrays.stream(player.getInventory().getContents())
                .forEach(item -> {
                    if (item != null && item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.getPersistentDataContainer().has(Backpack.BAG_KEY, PersistentDataType.STRING)) {
                            UUID uuid = UUID.fromString(meta.getPersistentDataContainer().get(Backpack.BAG_KEY, PersistentDataType.STRING));

                            Backpack bag = Backpack.BY_UUID.getOrDefault(uuid, null);
                            if (bag == null) bag = loadBag(item);
                            if (bag != null) bags.add(bag);
                        }
                    }
                });
        return bags;
    }
    private Backpack loadBag(ItemStack item){
        if (item == null || !item.hasItemMeta()) return null;
        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (container.has(Backpack.BAG_KEY, PersistentDataType.STRING)){
            String suuid = container.get(Backpack.BAG_KEY, PersistentDataType.STRING);
            if (suuid == null || suuid.isEmpty()) return null;
            UUID uuid = UUID.fromString(suuid);
            Backpack.Size size = Backpack.Size.valueOf(plugin.bagFile.getConfig().getString("bags." + uuid + ".size"));
            Backpack.BagType type = Backpack.BagType.valueOf(plugin.bagFile.getConfig().getString("bags." + uuid + ".type"));
            return new Backpack(size, type, uuid, item);
        }
        return null;
    }

    /**
     *
     * @param player
     * @return the highest time speed multiplier that a player's bait provides
     */
    public Pair<Float, Pair<ItemStack,Backpack>> highestMultiplier(Player player){
        HashMap<String, Pair<ItemStack,Backpack>> bait = playerBaits(player);
        float max = 1;
        String itemId = null;
        for (String id : bait.keySet()){
            float modifier = baitModifier(id);
            if (modifier > max){
                max = modifier;
                itemId = id;
            }
        }

        return new Pair<>(max, bait.get(itemId));
    }
    private float baitModifier(String id){
        if (plugin.baitFile.getConfig().contains("bait." + id + ".modifier")){
            return (float) plugin.baitFile.getConfig().getDouble("bait." + id + ".modifier");
        }else return 1;
    }

    /**
     *
     * @param player player who is depositing
     * @param item item to deposit
     * @return item if failed to add item to a bag
     */
    public ItemStack depositFish(Player player, ItemStack item){
        List<Backpack> fishBags = fishbags(playerBag(player));
        if (fishBags.isEmpty()) return item;
        List<Backpack> filteredBags = fishBags.stream().filter((bag)->bag.contains(item)).collect(Collectors.toList());
        if (filteredBags.isEmpty()) {
            fishBags.get(0).addItem(item);
        }else{
            filteredBags.get(0).addItem(item);
        }
        return null;
    }


    public class Pair<T,K>{
        private final T entry1;
        private final K entry2;
        public Pair(T entry1, K entry2){
            this.entry1 = entry1;
            this.entry2 = entry2;
        }

        public T entry1(){ return entry1; }
        public K entry2(){ return entry2;}
    }
}
