package tk.wurst_client.commands;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.client.C10PacketCreativeInventoryAction;
import net.minecraft.util.ResourceLocation;
import tk.wurst_client.WurstClient;
import tk.wurst_client.commands.Cmd.Info;
import tk.wurst_client.utils.MiscUtils;

@Info(help = "Gives you an item with custom NBT data in creative.",
name = "give",
syntax = {"<item name> <amount> <metadata> <NBT data>"})
public class GiveCmd extends Cmd {

	@Override
	public void execute(String[] args) throws Error {
		EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
		if (!player.capabilities.isCreativeMode) {
			WurstClient.INSTANCE.chat.error("Creative mode only.");
			return;
		}
		
		if (args.length < 1)
			syntaxError();
	
		ResourceLocation loc = new ResourceLocation(args[0]);
		Item item = (Item)Item.itemRegistry.getObject(loc);
		
		if (item == null)
			WurstClient.INSTANCE.chat.error("This item does not exist.");
		else {
			int amount = item.getItemStackLimit();
			int metadata = 0;
			
			if (args.length >= 2) {
				if (!MiscUtils.isInteger(args[1]))
					syntaxError();
				
				amount = Integer.valueOf(args[1]);
				
				if (amount < 1) {
					WurstClient.INSTANCE.chat.error("Amount has to be at least 1.");
					return;
				}
				if (amount > item.getItemStackLimit()) {
					WurstClient.INSTANCE.chat.error("Amount is larger than the maximum stack size.");
					return;
				}
			}
			
			if (args.length >= 3) {
				if (!MiscUtils.isInteger(args[2]))
					syntaxError();
				
				metadata = Integer.valueOf(args[2]);
			}
			
			ItemStack out = new ItemStack(item, amount, metadata);
			
			if (args.length >= 4) {
				try {
					String nbt = CommandBase.getChatComponentFromNthArg(null, args, 3).getUnformattedText();
					out.setTagCompound(JsonToNBT.func_180713_a(nbt));
				} catch (CommandException | NBTException e) {
					WurstClient.INSTANCE.chat.error("An error occurred while parsing NBT data.");
					e.printStackTrace();
					return;
				}
			}
			
			for (int i = 0; i<9; i++) {
				if (player.inventory.getStackInSlot(i) == null) {
					Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(
							new C10PacketCreativeInventoryAction(36+i, out));
					WurstClient.INSTANCE.chat.info("Your specified item has been placed into your hotbar.");
					return;
				}
			}
			
			WurstClient.INSTANCE.chat.error("Please clear a slot of your hotbar to use this command.");
		}
	}

}
