package pl.blueflow.craftableschematics;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import pl.blueflow.craftableschematics.command.ItemSerializerCommand;
import pl.blueflow.craftableschematics.config.Configuration;
import pl.blueflow.craftableschematics.listener.SchematicPlaceListener;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public final class CraftableSchematics extends JavaPlugin {
	
	@Getter
	private final @NotNull Configuration configuration;
	
	@Getter
	private final @NotNull Path schematicsPath;
	
	@Getter
	private final @NotNull Map<String, String> recipeSchematics = new HashMap<>();
	
	@Getter
	private final @NotNull NamespacedKey recipeIdKey = new NamespacedKey(this, "recipe-id");
	
	@Getter
	private WorldEdit worldEdit;
	
	@Override
	public void onEnable() {
		this.getServer().getCommandMap().register("craftable-schematics", new ItemSerializerCommand(this.getSLF4JLogger()));
		this.getServer().getPluginManager().registerEvents(new SchematicPlaceListener(this), this);
		
		final var worldEdit = (WorldEditPlugin) this.getServer().getPluginManager().getPlugin("WorldEdit");
		if(worldEdit == null) throw new IllegalStateException("This plugin requires WorldEdit to work.");
		this.worldEdit = worldEdit.getWorldEdit();
		
		for(final var recipe : this.configuration.getRecipes()) {
			if(this.recipeSchematics.containsKey(recipe.getId())) {
				throw new IllegalStateException(String.format("Recipe %s duplicates another recipe ID.", recipe.getId()));
			}
			this.recipeSchematics.put(recipe.getId(), recipe.getSchematicName());
			
			final var recipeKey = NamespacedKey.fromString(String.format("schematic-recipe/%s", recipe.getId()), this);
			if(recipeKey == null) {
				throw new IllegalStateException(
					String.format("Recipe %s has invalid ID. Must be lowercase letters, numbers, dots, hyphens, underscores and forward slashes only.",
					              recipe.getId())
				);
			}
			
			final var result = recipe.getRecipeResult();
			result.editMeta(meta -> meta.getPersistentDataContainer().set(this.recipeIdKey, PersistentDataType.STRING, recipe.getId()));
			
			switch(recipe.getType()) {
				case SHAPED -> {
					if(recipe.getRecipeShape() == null || recipe.getRecipeShape().size() != 3) {
						throw new IllegalStateException(String.format("Recipe %s has an invalid shape. Must be exactly 3 rows.", recipe.getId()));
					}
					final var shape = recipe.getRecipeShape().toArray(new String[0]);
					
					final var schemRecipe = new ShapedRecipe(recipeKey, result);
					schemRecipe.setCategory(CraftingBookCategory.MISC);
					schemRecipe.shape(shape);
					for(final var items : recipe.getRecipeItems().entrySet()) {
						schemRecipe.setIngredient(items.getKey(), items.getValue());
					}
					
					this.getServer().addRecipe(schemRecipe);
				}
				case SHAPELESS -> {
					final var schemRecipe = new ShapelessRecipe(recipeKey, result);
					schemRecipe.setCategory(CraftingBookCategory.MISC);
					recipe.getRecipeItems().values().forEach(schemRecipe::addIngredient);
					
					this.getServer().addRecipe(schemRecipe);
				}
				default -> throw new IllegalStateException(String.format("Unknown recipe type: %s", recipe.getType().name()));
			}
			
			this.getSLF4JLogger().info("Registered recipe {}", recipe.getId());
		}
	}
	
}
