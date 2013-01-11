package net.machinemuse.powersuits.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.machinemuse.general.MuseMathUtils;
import net.machinemuse.powersuits.powermodule.ModuleManager;
import net.machinemuse.powersuits.powermodule.PowerModule;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemUtils {
	public static final String NBTPREFIX = "mmmpsmod";

	public static List<PowerModule> getValidModulesForItem(
			EntityPlayer player, ItemStack stack) {
		List<PowerModule> validModules = new ArrayList();
		for (PowerModule module : ModuleManager.getAllModules().values()) {
			if (module.isValidForSlot(getAsModular(stack.getItem())
					.getItemType().ordinal())) {
				validModules.add(module);
			}
		}
		return validModules;
	}

	public static boolean tagHasModule(NBTTagCompound tag, String moduleName) {
		if (tag.hasKey(moduleName)) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean itemHasModule(ItemStack stack, String moduleName) {
		return tagHasModule(getMuseItemTag(stack), moduleName);
	}

	public static void tagAddModule(NBTTagCompound tag, PowerModule module) {
		tag.setCompoundTag(module.getName(), module.getNewTag());
	}

	public static void itemAddModule(ItemStack stack, PowerModule module) {
		tagAddModule(getMuseItemTag(stack), module);
	}

	public static boolean removeModule(NBTTagCompound tag, String moduleName) {
		if (tag.hasKey(moduleName)) {
			tag.removeTag(moduleName);
			return true;
		} else {
			return false;
		}
	}

	public static boolean removeModule(ItemStack stack, String moduleName) {
		return removeModule(getMuseItemTag(stack), moduleName);
	}

	/**
	 * Gets or creates stack.getTagCompound().getTag(NBTPREFIX)
	 * 
	 * @param stack
	 * @return an NBTTagCompound, may be newly created. If stack is null,
	 *         returns null.
	 */
	public static NBTTagCompound getMuseItemTag(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		NBTTagCompound stackTag;
		if (stack.hasTagCompound()) {
			stackTag = stack.getTagCompound();
		} else {
			stackTag = new NBTTagCompound();
			stack.setTagCompound(stackTag);
		}

		NBTTagCompound properties;
		if (stackTag.hasKey(NBTPREFIX)) {
			properties = stackTag
					.getCompoundTag(NBTPREFIX);
		} else {
			properties = new NBTTagCompound();
			stackTag.setCompoundTag(NBTPREFIX,
					properties);
		}
		return properties;
	}

	/**
	 * Scans a specified inventory for modular items.
	 * 
	 * @param inv
	 *            IInventory to scan.
	 * @return A List of ItemStacks in the inventory which implement
	 *         IModularItem
	 */
	public static List<ItemStack> getModularItemsInInventory(IInventory inv) {
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof IModularItem) {
				stacks.add(stack);
			}
		}
		return stacks;
	}

	public static List<ItemStack> getModularItemsInInventory(EntityPlayer player) {
		return getModularItemsInInventory(player.inventory);
	}

	/**
	 * Scans a specified inventory for modular items.
	 * 
	 * @param inv
	 *            IInventory to scan.
	 * @return A List of inventory slots containing an IModularItem
	 */
	public static List<Integer> getModularItemSlotsInInventory(IInventory inv) {
		ArrayList<Integer> slots = new ArrayList<Integer>();

		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && stack.getItem() instanceof IModularItem) {
				slots.add(i);
			}
		}
		return slots;
	}

	/**
	 * Attempts to cast an item to IModularItem, returns null if fails
	 */
	public static IModularItem getAsModular(Item item) {
		if (item instanceof IModularItem) {
			return (IModularItem) item;
		} else {
			return null;
		}
	}

	/**
	 * Checks if the player has a copy of all of the items in
	 * workingUpgradeCost.
	 * 
	 * @param workingUpgradeCost
	 * @param inventory
	 * @return
	 */
	public static boolean hasInInventory(List<ItemStack> workingUpgradeCost,
			InventoryPlayer inventory) {
		for (ItemStack stackInCost : workingUpgradeCost) {
			int found = 0;
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				ItemStack stackInInventory = inventory.getStackInSlot(i);
				if (isSameItem(stackInInventory, stackInCost)) {
					found += stackInInventory.stackSize;
				}
			}
			if (found < stackInCost.stackSize) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param workingUpgradeCost
	 * @param inventory
	 */
	public static List<Integer> deleteFromInventory(List<ItemStack> cost,
			InventoryPlayer inventory) {
		List<Integer> slots = new LinkedList<Integer>();
		for (ItemStack stackInCost : cost) {
			int remaining = stackInCost.stackSize;
			for (int i = 0; i < inventory.getSizeInventory() && remaining > 0; i++) {
				ItemStack stackInInventory = inventory.getStackInSlot(i);
				if (isSameItem(stackInInventory, stackInCost)) {
					int numToTake = Math.min(stackInInventory.stackSize,
							remaining);
					stackInInventory.stackSize -= numToTake;
					remaining -= numToTake;
					if (stackInInventory.stackSize == 0) {
						inventory.setInventorySlotContents(i, null);
					}
					slots.add(i);
				}
			}
		}
		return slots;
	}

	public static List<Integer> findInInventoryForCost(List<ItemStack> workingUpgradeCost,
			InventoryPlayer inventory) {
		List<Integer> slots = new LinkedList<Integer>();
		for (ItemStack stackInCost : workingUpgradeCost) {
			int found = 0;
			for (int i = 0; i < inventory.getSizeInventory() && found < stackInCost.stackSize; i++) {
				ItemStack stackInInventory = inventory.getStackInSlot(i);
				if (isSameItem(stackInInventory, stackInCost)) {
					found += stackInInventory.stackSize;
					slots.add(i);
				}
			}
		}
		return slots;
	}

	/**
	 * Checks the given NBTTag and returns the value if it exists, otherwise 0.
	 */
	public static double getDoubleOrZero(NBTTagCompound itemProperties,
			String string) {
		double value = 0;
		if (itemProperties != null) {
			if (itemProperties.hasKey(string)) {
				value = itemProperties.getDouble(string);
			}
		}
		return value;
	}

	/**
	 * Bouncer for succinctness. Checks the item's modular properties and
	 * returns the value if it exists, otherwise 0.
	 */
	public static double getDoubleOrZero(ItemStack stack, String string) {
		return getDoubleOrZero(getMuseItemTag(stack), string);
	}

	/**
	 * Sets the value of the given nbt tag, or removes it if the value would be
	 * zero.
	 */
	public static void setDoubleOrRemove(NBTTagCompound itemProperties,
			String string,
			double value) {
		if (itemProperties != null) {
			if (value == 0) {
				itemProperties.removeTag(string);
			} else {
				itemProperties.setDouble(string, value);
			}
		}
	}

	/**
	 * Sets the given itemstack's modular property, or removes it if the value
	 * would be zero.
	 */
	public static void setDoubleOrRemove(ItemStack stack, String string,
			double value) {
		setDoubleOrRemove(getMuseItemTag(stack), string, value);
	}

	public static String getStringOrNull(NBTTagCompound itemProperties,
			String key) {
		String value = null;
		if (itemProperties != null) {
			if (itemProperties.hasKey(key)) {
				value = itemProperties.getString(key);
			}
		}
		return value;
	}

	public static String getStringOrNull(ItemStack stack, String key) {
		return getStringOrNull(getMuseItemTag(stack), key);
	}

	public static void setStringOrNull(NBTTagCompound itemProperties,
			String key, String value) {
		if (itemProperties != null) {
			if (value.equals("")) {
				itemProperties.removeTag(key);
			} else {
				itemProperties.setString(key, value);
			}
		}
	}

	public static void setStringOrNull(ItemStack stack, String key, String value) {
		setStringOrNull(getMuseItemTag(stack),
				key, value);
	}

	public static double getPlayerEnergy(EntityPlayer player) {
		double avail = 0;
		for (ItemStack stack : modularItemsEquipped(player)) {
			avail += ((IModularItem) stack.getItem()).getJoules(stack);
		}
		return avail;
	}

	public static List<ItemStack> modularItemsEquipped(EntityPlayer player) {
		List<ItemStack> modulars = new ArrayList(5);
		ItemStack[] equipped = itemsEquipped(player);
		for (ItemStack stack : equipped) {
			if (stack != null && stack.getItem() instanceof IModularItem) {
				modulars.add(stack);
			}
		}
		return modulars;
	}

	public static ItemStack[] itemsEquipped(EntityPlayer player) {
		ItemStack[] equipped = {
				player.inventory.armorInventory[0],
				player.inventory.armorInventory[1],
				player.inventory.armorInventory[2],
				player.inventory.armorInventory[3],
				player.inventory.getCurrentItem() };
		return equipped;
	}

	public static void drainPlayerEnergy(EntityPlayer player, double drainAmount) {
		for (ItemStack stack : modularItemsEquipped(player)) {
			if (stack != null) {
				IModularItem item = getAsModular(stack.getItem());
				double joules = item.getJoules(stack);
				if (joules > drainAmount) {
					item.onUse(drainAmount, stack);
					break;
				} else {
					drainAmount -= joules;
					item.onUse(joules, stack);
				}
			}
		}
	}

	public static double getMaxEnergy(EntityClientPlayerMP player) {
		double max = 0;
		for (ItemStack stack : getModularItemsInInventory(player.inventory)) {
			max += ((IModularItem) stack.getItem()).getMaxJoules(stack);
		}
		return max;
	}

	public static boolean canStackTogether(ItemStack stack1, ItemStack stack2) {
		if (!isSameItem(stack1, stack2)) {
			return false;
		} else if (!stack1.isStackable()) {
			return false;
		} else if (stack1.stackSize >= stack1.getMaxStackSize()) {
			return false;
		} else {
			return true;
		}
	}

	public static boolean isSameItem(ItemStack stack1, ItemStack stack2) {
		if (stack1 == null || stack2 == null) {
			return false;
		} else if (stack1.itemID != stack2.itemID) {
			return false;
		} else if ((!stack1.isItemStackDamageable())
				&& (stack1.getItemDamage() != stack2.getItemDamage())) {
			return false;
		} else {
			return true;
		}
	}

	public static void transferStackWithChance(ItemStack itemsToGive,
			ItemStack destinationStack, double chanceOfSuccess) {
		int maxSize = destinationStack.getMaxStackSize();
		while (itemsToGive.stackSize > 0
				&& destinationStack.stackSize < maxSize) {
			itemsToGive.stackSize -= 1;
			if (MuseMathUtils.nextDouble() < chanceOfSuccess) {
				destinationStack.stackSize += 1;
			}
		}
	}

	public static Set<Integer> giveOrDropItems(ItemStack itemsToGive,
			EntityPlayerMP player) {
		return giveOrDropItemWithChance(itemsToGive, player, 1.0);
	}

	public static Set<Integer> giveOrDropItemWithChance(ItemStack itemsToGive,
			EntityPlayerMP player, double chanceOfSuccess) {
		Set<Integer> slots = new HashSet<Integer>();

		// First try to add the items to existing stacks
		for (int i = 0; i < player.inventory.getSizeInventory()
				&& itemsToGive.stackSize > 0; i++) {
			ItemStack currentStack = player.inventory.getStackInSlot(i);
			if (canStackTogether(currentStack, itemsToGive)) {
				slots.add(i);
				transferStackWithChance(itemsToGive, currentStack,
						chanceOfSuccess);
			}
		}
		// Then try to add the items to empty slots
		for (int i = 0; i < player.inventory.getSizeInventory()
				&& itemsToGive.stackSize > 0; i++) {
			if (player.inventory.getStackInSlot(i) == null) {
				ItemStack destination = new ItemStack(itemsToGive.itemID,
						0, itemsToGive.getItemDamage());
				transferStackWithChance(itemsToGive, destination,
						chanceOfSuccess);
				if (destination.stackSize > 0) {
					player.inventory.setInventorySlotContents(i,
							destination);
					slots.add(i);
				}
			}
		}
		// Finally spawn the items in the world.
		if (itemsToGive.stackSize > 0) {
			for (int i = 0; i < itemsToGive.stackSize; i++) {
				if (MuseMathUtils.nextDouble() < chanceOfSuccess) {
					ItemStack copyStack = itemsToGive.copy();
					copyStack.stackSize = 1;
					player.dropPlayerItem(copyStack);
				}
			}
		}

		return slots;
	}

	public static double getPlayerWeight(EntityPlayer player) {
		double weight = 0;
		for (ItemStack stack : modularItemsEquipped(player)) {
			weight += ModuleManager.computeModularProperty(stack, ModularCommon.WEIGHT);
		}
		return weight;
	}
}