/**
 * Crafting Dead
 * Copyright (C) 2020  Nexus Node
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.craftingdead.core.inventory.container;

import java.util.function.BiPredicate;
import com.craftingdead.core.capability.ModCapabilities;
import com.craftingdead.core.inventory.CraftingInventorySlotType;
import com.craftingdead.core.inventory.InventorySlotType;
import com.craftingdead.core.item.AttachmentItem;
import com.craftingdead.core.item.HatItem;
import com.craftingdead.core.item.MeleeWeaponItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ModInventoryContainer extends Container {

  private final IItemHandler itemHandler;

  private final CraftResultInventory outputInventory = new CraftResultInventory();
  private final Inventory craftingInventory = new Inventory(4);

  public ModInventoryContainer(int windowId, PlayerInventory playerInventory) {
    super(ModContainerTypes.PLAYER.get(), windowId);
    this.itemHandler = playerInventory.player.getCapability(ModCapabilities.LIVING)
        .orElseThrow(() -> new IllegalStateException("No living capability")).getItemHandler();
    this.craftingInventory.addListener(this::onCraftMatrixChanged);

    final int deltaY = 20;

    for (int i = 0; i < 3; ++i) {
      for (int j = 0; j < 9; ++j) {
        this.addSlot(new Slot(playerInventory, j + (i + 1) * 9, 8 + j * 18, 84 + i * 18 + deltaY));
      }
    }

    for (int i = 0; i < 9; ++i) {
      this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142 + deltaY));
    }

    this
        .addSlot(new PredicateItemHandlerSlot(this.itemHandler, InventorySlotType.MELEE.getIndex(),
            26, 9, (slot, itemStack) -> itemStack.getItem() instanceof MeleeWeaponItem));
    this
        .addSlot(new PredicateItemHandlerSlot(this.itemHandler, InventorySlotType.GUN.getIndex(),
            44, 9, (slot, itemStack) -> itemStack.getCapability(ModCapabilities.GUN).isPresent()));
    this
        .addSlot(new PredicateItemHandlerSlot(this.itemHandler,
            InventorySlotType.CLOTHING.getIndex(), 65, 9,
            (slot, itemStack) -> itemStack.getCapability(ModCapabilities.CLOTHING).isPresent()));
    this
        .addSlot(new PredicateItemHandlerSlot(this.itemHandler, InventorySlotType.HAT.getIndex(),
            65, 30, (slot, itemStack) -> itemStack.getItem() instanceof HatItem));
    this
        .addSlot(new PredicateItemHandlerSlot(this.itemHandler, InventorySlotType.VEST.getIndex(),
            65, 48,
            (slot, itemStack) -> itemStack
                .getCapability(ModCapabilities.STORAGE)
                .map(storage -> storage.isValidForSlot(InventorySlotType.VEST))
                .orElse(false)));

    this.addSlot(new GunCraftSlot(this.outputInventory, 0, 125, 48, this.craftingInventory));

    final BiPredicate<PredicateSlot, ItemStack> attachmentPredicate =
        (slot, itemStack) -> itemStack.getItem() instanceof AttachmentItem
            && ((AttachmentItem) itemStack.getItem()).getInventorySlot().getIndex() == slot
                .getSlotIndex();
    this
        .addSlot(new PredicateSlot(this.craftingInventory,
            CraftingInventorySlotType.MUZZLE_ATTACHMENT.getIndex(), 104, 48, attachmentPredicate));
    this
        .addSlot(new PredicateSlot(this.craftingInventory,
            CraftingInventorySlotType.UNDERBARREL_ATTACHMENT.getIndex(), 125, 69,
            attachmentPredicate));
    this
        .addSlot(new PredicateSlot(this.craftingInventory,
            CraftingInventorySlotType.OVERBARREL_ATTACHMENT.getIndex(), 125, 27,
            attachmentPredicate));
    this
        .addSlot(new PredicateSlot(this.craftingInventory,
            CraftingInventorySlotType.PAINT.getIndex(), 146, 48,
            (slot, itemStack) -> itemStack.getCapability(ModCapabilities.PAINT).isPresent()));
  }

  @Override
  public boolean canInteractWith(PlayerEntity playerEntity) {
    return true;
  }

  @Override
  public void onContainerClosed(PlayerEntity playerEntity) {
    super.onContainerClosed(playerEntity);
    if (!playerEntity.getEntityWorld().isRemote()) {
      this.clearContainer(playerEntity, playerEntity.getEntityWorld(), this.craftingInventory);
      this.clearContainer(playerEntity, playerEntity.getEntityWorld(), this.outputInventory);
    }
  }

  public IItemHandler getItemHandler() {
    return this.itemHandler;
  }

  public ItemStack getGunStack() {
    return this.outputInventory.getStackInSlot(0);
  }

  public boolean isCraftingInventoryEmpty() {
    return this.craftingInventory.isEmpty();
  }

  public boolean isCraftable() {
    return this.getGunStack().getCapability(ModCapabilities.GUN).map(gunController -> {
      for (int i = 0; i < this.craftingInventory.getSizeInventory(); i++) {
        ItemStack itemStack = this.craftingInventory.getStackInSlot(i);
        if (!itemStack.isEmpty() && !gunController.isAcceptedPaintOrAttachment(itemStack)) {
          return false;
        }
      }
      return true;
    }).orElse(false);
  }

  @Override
  public ItemStack transferStackInSlot(PlayerEntity playerEntity, int clickedIndex) {
    Slot clickedSlot = this.inventorySlots.get(clickedIndex);
    if (clickedSlot != null && clickedSlot.getHasStack()) {
      // Shift-clicking gun crafting slots is currently unavailable due to
      // the nature of how itemstacks are merged when shift-clicking.
      // In other words, attachments do not get attached.
      if (clickedSlot instanceof GunCraftSlot) {
        return ItemStack.EMPTY;
      }

      ItemStack clickedStack = clickedSlot.getStack();

      if (clickedIndex < 27) {
        // Pushes the clicked stack to the higher slots in a "negative direction"
        if (!this.mergeItemStack(clickedStack, 27, this.inventorySlots.size(), true)) {
          return ItemStack.EMPTY;
        }
      } else {
        // Pushes the clicked stack to the lower slots in a "positive direction"
        if (!this.mergeItemStack(clickedStack, 0, 27, false)) {
          return ItemStack.EMPTY;
        }
      }

      // From vanilla code. Seems to be mandatory.
      if (clickedStack.isEmpty()) {
        clickedSlot.putStack(ItemStack.EMPTY);
      } else {
        clickedSlot.onSlotChanged();
      }
    }

    return ItemStack.EMPTY;
  }
}
