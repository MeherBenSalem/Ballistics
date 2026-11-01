package com.nightbeam.remnants.ballistics.menu;

import com.nightbeam.remnants.ballistics.recipe.AmmoPressInput;
import com.nightbeam.remnants.ballistics.recipe.AmmoPressRecipe;
import com.nightbeam.remnants.ballistics.registry.ModBlocks;
import com.nightbeam.remnants.ballistics.registry.ModMenus;
import com.nightbeam.remnants.ballistics.registry.ModRecipes;
import com.nightbeam.remnants.ballistics.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Three ingredient slots and a result slot; recipes are matched server-side and the result slot is synced. */
public class AmmoPressMenu extends AbstractContainerMenu {
    public static final int INPUT_START = 0;
    public static final int RESULT_SLOT = 3;
    private static final int INV_START = 4;
    private static final int INV_END = INV_START + 36;

    private final ContainerLevelAccess access;
    private final Player player;
    private final SimpleContainer inputs = new SimpleContainer(AmmoPressRecipe.SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            AmmoPressMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer result = new ResultContainer();
    private AmmoPressRecipe current;

    /** Client constructor. */
    public AmmoPressMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public AmmoPressMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenus.AMMO_PRESS.get(), id);
        this.access = access;
        this.player = inventory.player;
        addSlot(new Slot(inputs, 0, 30, 17));
        addSlot(new Slot(inputs, 1, 30, 35));
        addSlot(new Slot(inputs, 2, 30, 53));
        addSlot(new Slot(result, 0, 124, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player p) {
                // The recipe is only known server-side; the client predicts the pickup and is corrected by slot sync.
                return hasItem();
            }

            @Override
            public void onTake(Player p, ItemStack stack) {
                craftOnce();
                super.onTake(p, stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
        }
        for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, col, 8 + col * 18, 142));
    }

    private AmmoPressInput input() {
        List<ItemStack> items = new ArrayList<>(AmmoPressRecipe.SLOTS);
        for (int i = 0; i < AmmoPressRecipe.SLOTS; i++) items.add(inputs.getItem(i));
        return new AmmoPressInput(items);
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        access.execute((level, pos) -> refreshResult(level));
    }

    private void refreshResult(Level level) {
        Optional<AmmoPressRecipe> recipe = level.getRecipeManager().getRecipeFor(ModRecipes.AMMO_PRESS_TYPE.get(), input(), level);
        current = recipe.orElse(null);
        result.setItem(0, recipe.map(r -> r.assemble(input(), level.registryAccess())).orElse(ItemStack.EMPTY));
        broadcastChanges();
    }

    /** Consumes one craft's worth of ingredients. Server-authoritative: the client only mirrors via slot sync. */
    private void craftOnce() {
        if (current == null) return;
        int[] slots = current.assignment(input());
        if (slots == null) return;
        for (int i = 0; i < slots.length; i++) {
            inputs.removeItem(slots[i], current.ingredients().get(i).count());
        }
        access.execute((level, pos) -> level.playSound(null, pos, ModSounds.AMMO_PRESS_USE.get(), SoundSource.BLOCKS, 0.8f, 0.9f + level.random.nextFloat() * 0.2f));
        inputs.setChanged();
    }

    @Override
    public ItemStack quickMoveStack(Player p, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == RESULT_SLOT) {
            // Shift-click crafts repeatedly until the inputs run out or the inventory is full.
            int guard = 64;
            while (current != null && slot.hasItem() && guard-- > 0) {
                ItemStack made = slot.getItem().copy();
                if (!moveItemStackTo(made, INV_START, INV_END, true)) break;
                slot.onTake(p, made);
                // Anything that did not fit goes back to the player (or is dropped), never lost.
                if (!made.isEmpty()) p.getInventory().placeItemBackInInventory(made);
            }
            return ItemStack.EMPTY;
        } else if (index < RESULT_SLOT) {
            if (!moveItemStackTo(stack, INV_START, INV_END, true)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, INPUT_START, RESULT_SLOT, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(p, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player p) {
        return stillValid(access, p, ModBlocks.AMMO_PRESS.get());
    }

    @Override
    public void removed(Player p) {
        super.removed(p);
        access.execute((level, pos) -> clearContainer(p, inputs));
    }

    public Player player() {
        return player;
    }
}
