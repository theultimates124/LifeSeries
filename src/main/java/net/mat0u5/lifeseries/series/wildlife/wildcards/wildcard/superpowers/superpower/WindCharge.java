package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.ToggleableSuperpower;
import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.UnbreakableComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class WindCharge extends ToggleableSuperpower {

    public WindCharge(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.WIND_CHARGE;
    }

    @Override
    public void activate() {
        super.activate();
        giveMace();
        giveWindCharge();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ServerPlayerEntity player = getPlayer();
        if (player != null) {
            player.getInventory().markDirty();
        }
    }

    private void giveWindCharge() {
        ServerPlayerEntity player = getPlayer();
        if (player != null) {
            if (!player.getInventory().containsAny(Set.of(Items.WIND_CHARGE))) {
                ItemStack windCharge = new ItemStack(Items.WIND_CHARGE, 4);
                player.getInventory().insertStack(windCharge);
            }
        }
    }

    private void giveMace() {
        ServerPlayerEntity player = getPlayer();
        if (player != null) {
            ItemStack mace = new ItemStack(Items.MACE);
            mace.addEnchantment(ItemStackUtils.getEnchantmentEntry(Enchantments.VANISHING_CURSE), 1);
            mace.addEnchantment(ItemStackUtils.getEnchantmentEntry(Enchantments.WIND_BURST), 3);
            mace.set(DataComponentTypes.UNBREAKABLE, new UnbreakableComponent(true));
            mace.set(DataComponentTypes.MAX_DAMAGE, 1);
            mace.set(DataComponentTypes.DAMAGE, 1);
            ItemStackUtils.setCustomComponentBoolean(mace, "IgnoreBlacklist", true);
            ItemStackUtils.setCustomComponentBoolean(mace, "FromSuperpower", true);
            ItemStackUtils.setCustomComponentBoolean(mace, "WindChargeSuperpower", true);
            player.getInventory().insertStack(mace);
        }
    }
}
