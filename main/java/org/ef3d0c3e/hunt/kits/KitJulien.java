package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.util.Arrays;

/**
 * Julien's kit
 */
public class KitJulien extends Kit
{
    static ItemStack milkItem;

    @Override
    public String getName() { return "julien"; }
    @Override
    public String getDisplayName() { return "Julien"; }
    @Override
    public ItemStack getDisplayItem()
    {
        return HuntItems.createGuiItem(Material.COBBLESTONE, 0, Kit.itemColor + getDisplayName(),
                Kit.itemLoreColor + "╸ Consomme de la cobblestone lorsqu'il",
                Kit.itemLoreColor + " tape, ce qui augmente ses dégâts",
                Kit.itemLoreColor + "╸ Ne peux pas avoir mieux que l'Épée en Pierre",
                Kit.itemLoreColor + "╸ Peut se mettre en sécurité en échange de 64 Cobblestone",
                Kit.itemLoreColor + "╸ Casse instantanément la Stone (avec une pioche)"
        );
    }
    @Override
    public String[][] getDescription()
    {
        String[][] desc = {
                {
                    "§c ╸ §bVous tapez plus fort à l'épée en échange de cobblestone.::§a↑ +30% de dégâts et donne\n Slowness II (3s) contre\n 4+2×<Niveau de sharpness> cobblestone\n§c↓ -25% de dégâts si pas de cobblestone",
                    "§c ╸ §bVous ne pouvez qu'utiliser des épées en pierre et en bois.",
                    "§c ╸ §bLes Golems de Fer ne vous attaquent pas.",
                    "§c ╸ §bSi vous droppez de la Cobblestone en sneak (et que vous avez plus de 64 Cobblestone dans votre inventaire), une cage apparaît et vous protège des dégâts de chute.",
                    "§c ╸ §bVous cassez la stone et la Stone plus rapidement.",
                    "§c ╸ §bLes autres joueurs peuvent vous milk::§c↓ Ils vous infligent ½❤ de dégâts.",
                }
        };

        return desc;
    }

    @Override
    public KitID getID() { return KitID.JULIEN; }

    /**
     * Registers milk item & event listener
     */
    @Override
    public void start()
    {
        milkItem = new ItemStack(Material.MILK_BUCKET, 1);
        {
            ItemMeta meta = milkItem.getItemMeta();
            meta.setDisplayName("§6Lait Magique");
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            milkItem.setItemMeta(meta);
        }
        Bukkit.getServer().getPluginManager().registerEvents(new KitJulienEvents(), Game.getPlugin());
    }
    /**
     * Awards achievements for playing with this kit
     * @param hp Player
     */
    @Override
    public void onStart(HuntPlayer hp)
    {
        HuntAchievement.PLAY_JULIEN.award(hp.getPlayer(), 1);
    }
    /**
     * Awards achievements for winning with this kit
     * @param hp Player
     */
    @Override
    public void onWin(HuntPlayer hp)
    {
        HuntAchievement.WIN_JULIEN.award(hp.getPlayer(), 1);
    }

    @Override
    public ItemStack[] getItems()
    {
        return new ItemStack[] { milkItem };
    }

    @Override
    public Kit makeCopy()
    {
        return new KitJulien();
    }

    public KitJulien()
    {
    }
}