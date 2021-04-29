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

            ItemStack itemStack = p.getInventory().getItemInMainHand();

            // must have an item in hand
            // also don't bother players in creative please
            if (itemStack.getType() != Material.AIR && p.getGameMode() != GameMode.CREATIVE)
            {
                e.setCancelled(true);

                // need permission
                if (!p.hasPermission("QuickDeposit.deposit"))
                {
                    p.sendMessage(ChatColor.RED + "You do not have the required permission to perform a quick deposit!");
                    return;
                }

                // check for occluding blocks above the chest segments
                if (CheckForObstruction(c))
                {
                    TellFailure(p,ChatColor.RED + "This chest is obstructed!");
                    return;
                }

                // locked chest?
                if (c.isLocked())
                {
                    // check item name
                    if (!c.getLock().equals(itemStack.getItemMeta().getDisplayName()))
                    {
                        TellFailure(p,ChatColor.RED + "This chest is locked!");
                        return;
                    }
                }

                // single chest
                if (c.getBlockInventory().getHolder() instanceof Chest)
                {
                    if (ItemFitsIntoInventory(itemStack, c.getInventory()))
                    {
                        Deposit(p, itemStack, c);
                    }
                    else
                    {
                        TellFailure(p,ChatColor.RED + "This chest is full!");
                    }
                }
                // double chest
                else if (c.getBlockInventory().getHolder() instanceof DoubleChest)
                {
                    DoubleChest doubleChest = (DoubleChest) c.getInventory().getHolder();
                    Chest leftChest = (Chest) doubleChest.getLeftSide();
                    Chest rightChest = (Chest) doubleChest.getLeftSide();

                    // attempt to add to the left side first
                    if (ItemFitsIntoInventory(itemStack, leftChest.getInventory()))
                    {
                        Deposit(p, itemStack, leftChest);
                    }
                    // no room? try the right
                    else if (ItemFitsIntoInventory(itemStack, rightChest.getInventory()))
                    {
                        Deposit(p, itemStack, rightChest);
                    }
                    else
                    {
                        TellFailure(p,ChatColor.RED + "This chest is full!");
                    }
                }
            }
        }
    }

    // Deposits item from player's hand into a chest
    private void Deposit(Player p, ItemStack itemStack, Chest c)
    {
        c.getBlockInventory().addItem(itemStack);
        p.getInventory().setItemInMainHand(null);
        TellSuccess(p,ChatColor.GREEN + "Deposited "
                + itemStack.getAmount() + "x " + itemStack.getType().toString().toLowerCase() + " into the chest.");
    }

    // Returns the block 1 above the given one
    private Block GetBlockAbove(Block b)
    {
        return b.getLocation().add(0.0,1.0,0.0).getBlock();
    }

    // Checks if a given chest has an occluding block above it
    private boolean CheckForObstruction(Chest c)
    {
        if (c.getBlockInventory().getHolder() instanceof Chest)
        {
            if (GetBlockAbove(c.getBlock()).getType().isOccluding())
            {
                return true;
            }
            return false;
        }
        else if (c.getBlockInventory().getHolder() instanceof DoubleChest)
        {
            DoubleChest doubleChest = (DoubleChest) c.getInventory().getHolder();
            Chest leftChest = (Chest) doubleChest.getLeftSide();
            Chest rightChest = (Chest) doubleChest.getLeftSide();

            if (GetBlockAbove(leftChest.getBlock()).getType().isOccluding() || GetBlockAbove(rightChest.getBlock()).getType().isOccluding())
            {
                return true;
            }
            return false;
        }
        return true;
    }

    // checks whether the item will fit into the target inventory
    private boolean ItemFitsIntoInventory(ItemStack itemStack, Inventory inv)
    {
        // do we have room to add the item?
        int spaceNeeded = itemStack.getAmount();
        for (ItemStack slot : inv.getContents())
        {
            if (slot == null)
            {
                spaceNeeded -= itemStack.getMaxStackSize();
            }
            else
            {
                if (slot.getType() == itemStack.getType())
                {
                    if (slot.getItemMeta().equals(itemStack.getItemMeta()))
                    {
                        spaceNeeded -= itemStack.getMaxStackSize() - slot.getAmount();
                    }
                }
            }
        }
        if (spaceNeeded <= 0)
        {
            return true;
        }
        return false;
    }

    // sends a success message + sound to the player
    private void TellSuccess(Player p, String s)
    {
        p.sendMessage(s);
        p.playSound(p.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
    }

    // send an error message + sound to the player
    private void TellFailure(Player p,String s)
    {
        p.sendMessage(s);
        p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
    }
}
