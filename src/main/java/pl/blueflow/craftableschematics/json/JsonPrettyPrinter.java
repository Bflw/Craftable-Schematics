package pl.blueflow.craftableschematics.json;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import org.jetbrains.annotations.NotNull;

public final class JsonPrettyPrinter extends DefaultPrettyPrinter {
	
	public JsonPrettyPrinter() {
		super();
		this._objectFieldValueSeparatorWithSpaces = this._separators.getObjectFieldValueSeparator() + " ";
		this._arrayIndenter = new DefaultIndenter("\t", System.lineSeparator());
		this._objectIndenter = new DefaultIndenter("\t", System.lineSeparator());
	}
	
	@Override
	public @NotNull JsonPrettyPrinter createInstance() {
		return new JsonPrettyPrinter();
	}
	
}
