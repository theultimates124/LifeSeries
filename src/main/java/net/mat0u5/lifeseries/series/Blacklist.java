package net.mat0u5.lifeseries.series;

import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.mat0u5.lifeseries.Main.blacklist;

public abstract class Blacklist {
    public abstract List<Item> getItemBlacklist();
    public abstract List<Block> getBlockBlacklist();

    public ActionResult onBlockUse(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        processItemStack(player, player.getStackInHand(hand));
        BlockPos blockPos = hitResult.getBlockPos();
        BlockState block = world.getBlockState(blockPos);
        if (block.isAir()) return ActionResult.PASS;
        if (getBlockBlacklist().contains(block.getBlock())) {
            world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
    public ActionResult onBlockAttack(ServerPlayerEntity player, World world, BlockPos pos) {
        if (world.isClient()) return ActionResult.PASS;
        BlockState block = world.getBlockState(pos);
        if (block.isAir()) return ActionResult.PASS;
        if (getBlockBlacklist().contains(block.getBlock())) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            return ActionResult.FAIL;
        }
        return ActionResult.PASS;
    }
    public void onCollision(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        processItemStack(player, stack);
    }
    public void onInventoryUpdated(PlayerEntity player, PlayerInventory inventory, CallbackInfo ci) {
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            ItemStack newItem = processItemStack(player, stack);
            inventory.setStack(i, newItem);
            if (stack.equals(newItem)) {
                player.currentScreenHandler.sendContentUpdates();
                player.playerScreenHandler.onContentChanged(player.getInventory());
            }
        }
    }
    public boolean isBlacklistedItem(Item item) {
        return getItemBlacklist().contains(item);
    }

    public ItemStack processItemStack(PlayerEntity player, ItemStack itemStack) {
        if (itemStack.isEmpty()) return itemStack;
        if (itemStack.getItem() == Items.AIR) return itemStack;
        if (isBlacklistedItem(itemStack.getItem())) {
            itemStack.setCount(0);
            player.getInventory().updateItems();
            return ItemStack.EMPTY;
        }
        return furtherItemProcessing(itemStack);
    }
    public ItemStack furtherItemProcessing(ItemStack itemStack) {
        return itemStack;
    }
}
