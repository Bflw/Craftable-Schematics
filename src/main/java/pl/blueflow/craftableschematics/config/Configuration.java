package pl.blueflow.craftableschematics.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public final class Configuration {
	
	@JsonProperty(value = "_version", required = true)
	private final int version = 1;
	
	@JsonProperty(value = "recipes", required = true)
	private final @NotNull List<SchematicRecipe> recipes;
	
	@Getter
	@RequiredArgsConstructor
	public static final class SchematicRecipe {
		
		@JsonProperty(value = "id", required = true)
		private final @NotNull String id;
		
		@JsonProperty(value = "type", required = true)
		private final @NotNull Type type;
		
		@JsonProperty(value = "recipe_shape", required = true)
		private final @Nullable List<String> recipeShape;
		
		@JsonProperty(value = "recipe_items", required = true)
		private final @NotNull Map<Character, ItemStack> recipeItems;
		
		@JsonProperty(value = "recipe_result", required = true)
		private final @NotNull ItemStack recipeResult;
		
		@JsonProperty(value = "schematic_name", required = true)
		private final @NotNull String schematicName;
		
		public enum Type {
			SHAPED,
			SHAPELESS
		}
		
	}
	
}
