package pl.blueflow.craftableschematics;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import pl.blueflow.craftableschematics.config.Configuration;
import pl.blueflow.craftableschematics.json.JsonPrettyPrinter;
import pl.blueflow.craftableschematics.json.ItemStackDeserializer;
import pl.blueflow.craftableschematics.json.ItemStackSerializer;
import pl.blueflow.craftableschematics.json.MaterialDeserializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApiStatus.Internal
public final class CraftableSchematicsBootstrapper implements PluginBootstrap {
	
	@Override
	public void bootstrap(final @NotNull PluginProviderContext context) {
	}
	
	@Override
	public @NotNull JavaPlugin createPlugin(final @NotNull PluginProviderContext context) {
		context.getLogger().info("Plugin directory is {}", context.getDataDirectory().toAbsolutePath());
		final var schematicsPath = context.getDataDirectory().resolve("schematics/");
		final var config = requireConfig(context.getDataDirectory(), schematicsPath);
		return new CraftableSchematics(config, schematicsPath);
	}
	
	public static @NotNull Configuration requireConfig(final @NotNull Path pluginPath, final @NotNull Path schematicsPath) {
		final var configPath = pluginPath.resolve("config.json");
		
		if(Files.notExists(configPath)) {
			try {
				Files.createDirectories(configPath.getParent());
				try(final var writer = Files.newBufferedWriter(configPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
					createJsonMapper().writerWithDefaultPrettyPrinter().writeValue(writer, createExampleConfiguration());
				}
			} catch(final IOException ex) {
				throw new RuntimeException(
					String.format("Failed to write default configuration to %s", configPath.toAbsolutePath()), ex);
			}
			
			try(final var exampleSchem1 = CraftableSchematics.class.getClassLoader().getResourceAsStream("circle-example.schem");
			    final var exampleSchem2 = CraftableSchematics.class.getClassLoader().getResourceAsStream("stick-example.schem")) {
				Files.createDirectories(schematicsPath);
				
				final var exampleSchem1Path = schematicsPath.resolve("circle-example.schem");
				if(exampleSchem1 != null && Files.notExists(exampleSchem1Path)) {
					Files.copy(exampleSchem1, exampleSchem1Path);
				}
				
				final var exampleSchem2Path = schematicsPath.resolve("stick-example.schem");
				if(exampleSchem2 != null && Files.notExists(exampleSchem2Path)) {
					Files.copy(exampleSchem2, exampleSchem2Path);
				}
			} catch(final IOException ex) {
				throw new RuntimeException(
					String.format("Failed to write default schematics to %s", schematicsPath.toAbsolutePath()));
			}
		}
		
		try(final var reader = Files.newBufferedReader(configPath)) {
			return createJsonMapper().readValue(reader, Configuration.class);
		} catch(final Exception ex) {
			throw new RuntimeException(
				String.format("Failed to read configuration file at %s. Ensure your config is valid JSON and read below for possible cause(s)",
				              configPath.toAbsolutePath()),
				ex);
		}
	}
	
	public static @NotNull JsonMapper createJsonMapper() {
		return JsonMapper
			.builder()
			.enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
			.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
			.enable(SerializationFeature.INDENT_OUTPUT)
			.defaultPrettyPrinter(new JsonPrettyPrinter())
			.addModule(new SimpleModule()
				           .addSerializer(ItemStack.class, new ItemStackSerializer())
				           .addDeserializer(ItemStack.class, new ItemStackDeserializer())
				           .addDeserializer(Material.class, new MaterialDeserializer()))
			.build();
	}
	
	public static @NotNull Configuration createExampleConfiguration() {
		final var recipes = new ArrayList<Configuration.SchematicRecipe>();
		final var slimeBall = new ItemStack(Material.SLIME_BALL);
		slimeBall.editMeta(meta -> meta.displayName(Component.text("Circle", NamedTextColor.BLUE, TextDecoration.ITALIC)));
		recipes.add(new Configuration.SchematicRecipe(
			"circle-example", Configuration.SchematicRecipe.Type.SHAPED,
			List.of(" o ",
			        "o o",
			        " o "),
			Map.of('o', new ItemStack(Material.CLAY_BALL)),
			slimeBall,
			"circle-example.schem"
		));
		
		final var stick = new ItemStack(Material.STICK);
		stick.editMeta(meta -> meta.displayName(Component.text("Almighty Stick", NamedTextColor.DARK_RED, TextDecoration.BOLD)));
		recipes.add(new Configuration.SchematicRecipe(
			"stick-example", Configuration.SchematicRecipe.Type.SHAPELESS,
			null,
			Map.of('0', new ItemStack(Material.OAK_PLANKS),
			       '1', new ItemStack(Material.OAK_PLANKS),
			       '2', new ItemStack(Material.OAK_PLANKS)),
			stick,
			"stick-example.schem"
		));
		
		return new Configuration(recipes);
	}
	
}
