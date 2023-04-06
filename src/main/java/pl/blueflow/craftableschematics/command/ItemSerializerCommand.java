package pl.blueflow.craftableschematics.command;

import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import pl.blueflow.craftableschematics.json.ItemStackDeserializer;
import pl.blueflow.craftableschematics.json.ItemStackSerializer;

import java.util.Collections;
import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.event.ClickEvent.copyToClipboard;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.Style.style;
import static net.kyori.adventure.text.format.TextDecoration.BOLD;

public final class ItemSerializerCommand extends Command {
	
	private final @NotNull Logger logger;
	
	public ItemSerializerCommand(final @NotNull Logger logger) {
		super("item-serializer",
		      "Serialize and deserialize items to Base64.",
		      "<red>/<command> <serialize | <deserialize <base64>>",
		      Collections.emptyList());
		this.setPermission("craftable-schematics.command.item-serializer");
		this.logger = logger;
	}
	
	@Override
	public boolean execute(final @NotNull CommandSender sender,
	                       final @NotNull String label,
	                       final @NotNull String[] args) {
		if(!(sender instanceof final Player player)) return true;
		if(!this.testPermission(sender)) return true;
		if(args.length < 1) {
			this.sendUsage(sender, label);
			return false;
		}
		
		switch(args[0].toLowerCase()) {
			case "serialize" -> {
				final var hand = player.getInventory().getItemInMainHand();
				if(hand.getType().isAir()) {
					player.sendMessage(text("Cannot serialize air.", RED));
					return true;
				}
				
				final var serialized = ItemStackSerializer.serialize(hand);
				player.sendMessage(
					text()
						.append(text("Serialized item in hand: ", AQUA, BOLD))
						.append(
							text(serialized)
								.clickEvent(copyToClipboard(serialized))
								.hoverEvent(showText(
									text("Click to copy to clipboard", style(BOLD))
								))
						)
				);
			}
			case "deserialize" -> {
				if(args.length < 2) {
					this.sendUsage(sender, label);
					return false;
				}
				
				final ItemStack item;
				try {
					item = ItemStackDeserializer.deserialize(args[1]);
				} catch(final Exception ex) {
					sender.sendMessage(text("Failed to deserialize.", RED));
					this.logger.warn("Failed to deserialize player input, most likely due to malformed Base64: {}", ex.getMessage(), ex);
					return true;
				}
				
				final var failed = player.getInventory().addItem(item);
				failed.forEach((x, failedItem) -> player.getWorld().dropItem(player.getLocation(), failedItem));
			}
			default -> {
				this.sendUsage(sender, label);
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public @NotNull List<String> tabComplete(final @NotNull CommandSender sender,
	                                         final @NotNull String alias,
	                                         final @NotNull String[] args) throws IllegalArgumentException {
		return switch(args.length) {
			case 1 -> List.of("serialize", "deserialize");
			case 2 -> {
				if(args[0].equalsIgnoreCase("deserialize")) yield List.of("base64");
				yield Collections.emptyList();
			}
			default -> Collections.emptyList();
		};
	}
	
	private void sendUsage(final @NotNull CommandSender sender, final @NotNull String label) {
		sender.sendMessage(MiniMessage.miniMessage().deserialize(
			this.getUsage(),
			Placeholder.unparsed("command", label)
		));
	}
	
}
