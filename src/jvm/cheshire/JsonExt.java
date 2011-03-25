package clj_json;

import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonGenerator;
import java.math.BigInteger;
import clojure.lang.ISeq;
import clojure.lang.IPersistentMap;
import clojure.lang.IPersistentVector;
import clojure.lang.IMapEntry;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.PersistentVector;
import clojure.lang.ITransientMap;
import clojure.lang.IPersistentList;
import clojure.lang.ITransientCollection;
import clojure.lang.Seqable;

public class JsonExt {
  public static void generate(JsonGenerator jg, Object obj) throws Exception {
    if (obj instanceof IPersistentMap) {
      IPersistentMap map = (IPersistentMap) obj;
      ISeq mSeq = map.seq();
      jg.writeStartObject();
      while (mSeq != null) {
        IMapEntry me = (IMapEntry) mSeq.first();
        Object key = me.key();
        if (key instanceof Keyword) {
          jg.writeFieldName(((Keyword) key).getName());
        } else if (key instanceof Integer) {
          jg.writeFieldName(((Integer) key).toString());
        } else if (key instanceof BigInteger) {
          jg.writeFieldName(((BigInteger) key).toString());
        } else if (key instanceof Long) {
          jg.writeFieldName(((Long) key).toString());
        } else {
          jg.writeFieldName((String) key);
        }
        generate(jg, me.val());
        mSeq = mSeq.next();
      }
      jg.writeEndObject();
    
    } else if (obj instanceof IPersistentVector) {
      IPersistentVector vec = (IPersistentVector) obj;
      jg.writeStartArray();
      for (int i = 0; i < vec.count(); i++) {
        generate(jg, vec.nth(i));
      }
      jg.writeEndArray();
    
    } else if ((obj instanceof ISeq) || (obj instanceof IPersistentList)) {
      ISeq lSeq = ((Seqable) obj).seq();
      jg.writeStartArray();
      while (lSeq != null) {
        generate(jg, lSeq.first());
        lSeq = lSeq.next();
      }
      jg.writeEndArray();
    
    } else if (obj instanceof String) {
      jg.writeString((String) obj);
    
    } else if (obj instanceof Keyword) {
      jg.writeString(((Keyword) obj).getName());
    
    } else if (obj instanceof Integer) {
      jg.writeNumber((Integer) obj);
    
    } else if (obj instanceof Long) {
      jg.writeNumber((Long) obj);
    
    } else if (obj instanceof BigInteger) {
      jg.writeNumber((BigInteger) obj);
    
    } else if (obj instanceof Double) {
      jg.writeNumber((Double) obj);
    
    } else if (obj instanceof Float) {
      jg.writeNumber((Float) obj);
    
    } else if (obj instanceof Boolean) {
      jg.writeBoolean((Boolean) obj);
    
    } else if (obj == null) {
      jg.writeNull();
    
    } else {
      throw new Exception("Cannot generate " + obj);
    }
  }

  public static Object parse(JsonParser jp, boolean first, boolean keywords, Object eofValue) throws Exception {
    if (first) {
      jp.nextToken();
      if (jp.getCurrentToken() == null) {
        return eofValue;
      }
    }
    switch (jp.getCurrentToken()) {
      case START_OBJECT:
        ITransientMap map = PersistentArrayMap.EMPTY.asTransient();
        jp.nextToken();
        while (jp.getCurrentToken() != JsonToken.END_OBJECT) {
          String keyStr = jp.getText();
          jp.nextToken();
          Object key = keywords ? Keyword.intern(keyStr) : keyStr;
          map = map.assoc(key, parse(jp, false, keywords, eofValue));
          jp.nextToken();
        }
        return map.persistent();
    
      case START_ARRAY:
        ITransientCollection vec = PersistentVector.EMPTY.asTransient();
        jp.nextToken();
        while (jp.getCurrentToken() != JsonToken.END_ARRAY) {
          vec = vec.conj(parse(jp, false, keywords, eofValue));
          jp.nextToken();
        }
        return vec.persistent();
    
      case VALUE_STRING:
        return jp.getText();
    
      case VALUE_NUMBER_INT:
        return jp.getNumberValue();
    
      case VALUE_NUMBER_FLOAT:
        return jp.getDoubleValue();
      
      case VALUE_TRUE:
        return Boolean.TRUE;
    
      case VALUE_FALSE:
        return Boolean.FALSE;
      
      case VALUE_NULL:
        return null;
    
      default:
        throw new Exception("Cannot parse " + jp.getCurrentToken());
    }
  }
}
