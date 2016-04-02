package cheshire.prettyprint;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;

class DynamicIndenter extends DefaultIndenter {
    public DynamicIndenter(int indentation) {
        super(new String(new char[indentation]).replace("\0", " "), "\n");
    }
    public DynamicIndenter(int indentation, String newline) {
        super(new String(new char[indentation]).replace("\0", " "), newline);
    }
}

public class CustomPrettyPrinter extends DefaultPrettyPrinter {
    private Indenter _indenter;
    private int _indentation = 2;
    private boolean _indentObjects = true;
    private boolean _indentArrays = true;
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

    public CustomPrettyPrinter setIndentation(int indentation, boolean indentObjects, boolean indentArrays) {
        this._indentation = indentation;
        this._indentObjects = indentObjects;
        this._indentArrays = indentArrays;
        this._indenter = new DynamicIndenter(indentation);
        if (this._indentArrays) {
            this.indentArraysWith(this._indenter);
        }
        if (this._indentObjects) {
            this.indentObjectsWith(this._indenter);
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
