package pl.blueflow.craftableschematics.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public final class MaterialDeserializer extends JsonDeserializer<Material> {
	
	@Override
	public @NotNull Material deserialize(final @NotNull JsonParser parser,
	                                     final @NotNull DeserializationContext ctx) throws IOException {
		final var node = (JsonNode) parser.getCodec().readTree(parser);
		final var material = Material.matchMaterial(node.asText());
		if(material == null) throw new RuntimeException(String.format("The value '%s' is not a valid Material", node.asText()));
		return material;
	}
	
}
