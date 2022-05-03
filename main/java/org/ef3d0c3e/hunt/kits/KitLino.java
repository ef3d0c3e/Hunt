package org.ef3d0c3e.hunt.kits;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.achievements.HuntAchievement;
import org.ef3d0c3e.hunt.game.Game;
import org.ef3d0c3e.hunt.items.HuntItems;
import org.ef3d0c3e.hunt.player.HuntPlayer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Lino's kit
 */
public class KitLino extends Kit
{
    HashSet<Arrow> arrows;

    @Override
    public String getName() { return "lino"; }
    @Override
    public String getDisplayName() { return "Lino"; }
    @Override
    public ItemStack getDisplayItem()
    {
        return HuntItems.createGuiItem(Material.CROSSBOW, 0, Kit.itemColor + getDisplayName(),
                Kit.itemLoreColor + "╸ Échange de place avec les",
                Kit.itemLoreColor + " entitées touchées à l'arbalette",
                Kit.itemLoreColor + "╸ A une chance qu'un creeper donne",
                Kit.itemLoreColor + " une TNT en mourrant"
        );
    }
    @Override
    public String[][] getDescription()
    {
        String[][] desc = {
                {
                        "§c ╸ §bÉchange de place avec les entitées touchées à l'arbalette. Après avoir changé de place, elles obtiennent slowness, et vous speed.",
                        "§c ╸ §bTuer un creeper a 4/5 chance de donner une TNT.",
                }
        };

        return desc;
    }

    @Override
    public KitID getID() { return KitID.LINO; }

    /**
     * Registers event listener
     */
    public void start()
    {
        Bukkit.getServer().getPluginManager().registerEvents(new KitLinoEvents(), Game.getPlugin());
    }
    /**
     * Awards achievements for playing with this kit
     * @param hp Player
     */
    public void onStart(HuntPlayer hp)
    {
        HuntAchievement.PLAY_LINO.award(hp.getPlayer(), 1);
    }
    /**
     * Awards achievements for winning with this kit
     * @param hp Player
     */
    public void onWin(HuntPlayer hp)
    {
        HuntAchievement.WIN_LINO.award(hp.getPlayer(), 1);
    }
    /**
     * Clears list of ownter arrows
     * @param hp Player that died or lost his kit
     */
    @Override
    public void onDeath(HuntPlayer hp)
    {
        arrows.clear();
    }

    @Override
    public Kit makeCopy()
    {
        return new KitLino();
    }

    public KitLino()
    {
        arrows = new HashSet<>();
    }
}
