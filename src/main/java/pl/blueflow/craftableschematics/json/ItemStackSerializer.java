package pl.blueflow.craftableschematics.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Base64;

public final class ItemStackSerializer extends JsonSerializer<ItemStack> {
	
	private static final @NotNull Base64.Encoder ENCODER = Base64.getEncoder();
	
	@Override
	public void serialize(final @NotNull ItemStack value,
	                      final @NotNull JsonGenerator generator,
	                      final @NotNull SerializerProvider provider) throws IOException {
		generator.writeString(serialize(value));
	}
	
	public static @NotNull String serialize(final @NotNull ItemStack item) {
		return ENCODER.encodeToString(item.serializeAsBytes());
	}
	
}
