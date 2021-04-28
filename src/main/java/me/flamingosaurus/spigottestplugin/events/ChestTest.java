package me.flamingosaurus.spigottestplugin.events;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.DoubleChestInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.swing.*;

public class ChestTest implements Listener {

    @EventHandler
    public void onClick(PlayerInteractEvent e)
    {
        Player p = e.getPlayer();
        Action a = e.getAction();
        if (a == Action.RIGHT_CLICK_BLOCK && p.isSneaking() && e.getClickedBlock().getType() == Material.CHEST)
        {
            Block b = e.getClickedBlock();
            Chest c = (Chest) b.getState();

            ItemStack i = p.getInventory().getItemInMainHand();

            // must have an item in hand
            // also don't bother players in creative please
            if (i.getType() != Material.AIR && p.getGameMode() != GameMode.CREATIVE)
            {
                boolean blocked = false;

                // locked chest?
                if (c.isLocked())
                {
                    // check item name
                    if (!c.getLock().equals(i.getItemMeta().getDisplayName()))
                    {
                        blocked = true;
                    }
                }

                // check for occluding blocks above the chest segments
                if (c.getInventory() instanceof DoubleChestInventory)
                {
                    //System.out.println("you found a double chest!");

                    DoubleChest doubleChest = (DoubleChest) c.getInventory().getHolder();
                    Chest leftChest = (Chest) doubleChest.getLeftSide();
                    Chest rightChest = (Chest) doubleChest.getLeftSide();

                    Location loc1 = leftChest.getLocation();
                    loc1.add(0.0, 1.0,0.0);
                    Location loc2 = rightChest.getLocation();
                    loc2.add(0.0,1.0,0.0);

                    if (loc1.getBlock().getType().isOccluding() || loc2.getBlock().getType().isOccluding())
                    {
                        blocked = true;
                    }
                }
                else
                {
                    // single chest
                    Location loc = c.getLocation();
                    loc.add(0.0,1.0,0.0);
                    if (loc.getBlock().getType().isOccluding())
                    {
                        blocked = true;
                    }
                }

                // need permission
                if (!p.hasPermission("QuickDeposit.use"))
                {
                    p.sendMessage(ChatColor.RED + "You do not have the required permission to perform a quick deposit!");
                    e.setCancelled(true);
                }
                else
                {
                    if (blocked)
                    {
                        p.sendMessage(ChatColor.RED + "This chest is locked!");
                        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                        e.setCancelled(true);
                    }
                    else
                    {
                        // do we have room to add the item?
                        int spaceNeeded = i.getAmount();
                        for (ItemStack slot : c.getInventory().getContents())
                        {
                            if (slot == null)
                            {
                                spaceNeeded -= i.getMaxStackSize();
                            }
                            else
                            {
                                if (slot.getType() == i.getType())
                                {
                                    if (slot.getItemMeta().equals(i.getItemMeta()))
                                    {
                                        spaceNeeded -= i.getMaxStackSize() - slot.getAmount();
                                    }
                                }
                            }
                        }

                        // do we have room to add?
                        if (spaceNeeded <= 0)
                        {
                            c.getBlockInventory().addItem(i);

                            p.getInventory().setItemInMainHand(null);

                            p.sendMessage(ChatColor.GREEN + "Deposited your held item into the chest.");
                            p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
                        }
                        else
                        {
                            p.sendMessage(ChatColor.RED + "Sorry, there's not enough room to quick deposit your item!");
                            p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
                            e.setCancelled(true);
                        }

                    }
                }

            }
        }

    }

}
