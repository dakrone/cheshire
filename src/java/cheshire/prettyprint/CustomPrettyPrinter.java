package cheshire.prettyprint;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;

public class CustomPrettyPrinter extends DefaultPrettyPrinter {
    private String _beforeArrayValues;
    private String _afterArrayValues;
    private String _objectFieldValueSeparator;

    public CustomPrettyPrinter() {
        super();
    }

    @Override
    public void beforeArrayValues(JsonGenerator gen) throws IOException {
        if (this._beforeArrayValues != null) {
            gen.writeRaw(this._beforeArrayValues);
        } else {
            super.beforeArrayValues(gen);
        }
    }

    @Override
    public void writeEndArray(JsonGenerator gen, int nrOfValues) throws IOException {
        if (this._afterArrayValues != null) {
            gen.writeRaw(this._afterArrayValues + "]");
        } else {
            super.writeEndArray(gen, nrOfValues);
        }
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator gen) throws IOException {
        if (this._objectFieldValueSeparator != null) {
            gen.writeRaw(this._objectFieldValueSeparator);
        } else {
            super.writeObjectFieldValueSeparator(gen);
        }
    }

    public CustomPrettyPrinter setIndentation(String indentation, String lineBreak, boolean indentObjects, boolean indentArrays) {
        Indenter indenter = new DefaultIndenter(indentation, lineBreak);
        if (indentArrays) {
            this.indentArraysWith(indenter);
        }
        if (indentObjects) {
            this.indentObjectsWith(indenter);
        }
        return this;
    }

    public CustomPrettyPrinter setBeforeArrayValues(String beforeArrayValues) {
        this._beforeArrayValues = beforeArrayValues;
        return this;
    }

    public CustomPrettyPrinter setAfterArrayValues(String afterArrayValues) {
        this._afterArrayValues = afterArrayValues;
        return this;
    }

    public CustomPrettyPrinter setObjectFieldValueSeparator(String objectFieldValueSeparator) {
        this._objectFieldValueSeparator = objectFieldValueSeparator;
        return this;
    }

}
