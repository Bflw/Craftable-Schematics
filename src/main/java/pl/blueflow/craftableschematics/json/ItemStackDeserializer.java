package pl.blueflow.craftableschematics.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Base64;

public final class ItemStackDeserializer extends JsonDeserializer<ItemStack> {
	
	private static final @NotNull Base64.Decoder DECODER = Base64.getDecoder();
	
	@Override
	public @NotNull ItemStack deserialize(final @NotNull JsonParser parser,
	                                      final @NotNull DeserializationContext context) throws IOException {
		final var node = (JsonNode) parser.getCodec().readTree(parser);
		return deserialize(node.asText());
	}
	
	public static @NotNull ItemStack deserialize(final @NotNull String base64) {
		return ItemStack.deserializeBytes(DECODER.decode(base64));
	}
	
}
