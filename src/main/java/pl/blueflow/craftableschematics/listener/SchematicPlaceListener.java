package pl.blueflow.craftableschematics.listener;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pl.blueflow.craftableschematics.CraftableSchematics;

import java.io.IOException;
import java.nio.file.Files;

import static com.sk89q.worldedit.bukkit.BukkitAdapter.adapt;

public final class SchematicPlaceListener implements Listener {
	
	private final @NotNull CraftableSchematics plugin;
	
	public SchematicPlaceListener(final @NotNull CraftableSchematics plugin) {
		this.plugin = plugin;
	}
	
	private void placeSchematic(final @NotNull Location location, final @NotNull String schematicName) {
		final var schematic = this.plugin.getSchematicsPath().resolve(schematicName);
		final @NotNull Clipboard clipboard;
		final var format = ClipboardFormats.findByFile(schematic.toFile());
		if(format == null) {
			throw new IllegalStateException(String.format("Schematic %s format cannot be detected.", schematic.toAbsolutePath()));
		}
		
		try(final var input = Files.newInputStream(schematic);
		    final var reader = format.getReader(input)) {
			clipboard = reader.read();
		} catch(final IOException ex) {
			throw new RuntimeException(String.format("Failed to read schematic %s", schematic.toAbsolutePath()), ex);
		}
		
		try(final var session = this.plugin.getWorldEdit().newEditSession(adapt(location.getWorld()))) {
			Operations.complete(
				new ClipboardHolder(clipboard)
					.createPaste(session)
					.to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
					.build());
		} catch(final WorldEditException ex) {
			throw new RuntimeException(String.format("WorldEdit failed to paste schematic %s: %s", schematic.toAbsolutePath(), ex.getMessage()), ex);
		}
	}
	
	@EventHandler
	public void on(final @NotNull BlockPlaceEvent event) {
		final var item = event.getItemInHand();
		if(this.notSchematicItem(item)) return;
		event.setCancelled(true);
		this.placeSchematic(event.getBlockPlaced().getLocation(), this.getSchematicName(item));
		item.add(-1);
	}
	
	@EventHandler
	public void on(final @NotNull PlayerInteractEvent event) {
		if(!event.getAction().isRightClick()) return;
		final var block = event.getClickedBlock();
		if(block == null) return;
		final var item = event.getItem();
		if(item == null || this.notSchematicItem(item)) return;
		event.setCancelled(true);
		
		final var location = Tag.REPLACEABLE_PLANTS.isTagged(block.getType())
		                     ? block.getLocation()
		                     : block.getRelative(event.getBlockFace()).getLocation();
		this.placeSchematic(location, this.getSchematicName(item));
		item.add(-1);
	}
	
	private @Nullable String getRecipeId(final @NotNull ItemStack item) {
		return item.getItemMeta().getPersistentDataContainer().get(this.plugin.getRecipeIdKey(), PersistentDataType.STRING);
	}
	
	private boolean notSchematicItem(final @NotNull ItemStack item) {
		final var id = this.getRecipeId(item);
		if(id == null) return true;
		return !this.plugin.getRecipeSchematics().containsKey(id);
	}
	
	private @NotNull String getSchematicName(final @NotNull ItemStack item) {
		final var id = this.getRecipeId(item);
		final var schematic = this.plugin.getRecipeSchematics().get(id);
		if(schematic == null) throw new IllegalStateException();
		return schematic;
	}
	
}
