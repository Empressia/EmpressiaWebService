package jp.empressia.enterprise.ws.jersey.jsonp;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import jp.empressia.enterprise.ws.JSONParam;

/**
 * JSONParamに値を割り当てるProviderです。
 * JerseyとJSON Processingに依存しています。
 */
@Provider
public class JSONParamProvider implements ValueParamProvider {

	private static final String ENTITY_PROPERTY_NAME = JSONParamProvider.class.getName() + ".Entity";

	private JsonParserFactory ParserFactory = Json.createParserFactory(null);

	private static class Null implements JsonValue {
		@Override public ValueType getValueType() { return null; }
	}

	@Override
	public Function<ContainerRequest, ?> getValueProvider(Parameter parameter) {
		if(parameter.isAnnotationPresent(JSONParam.class) == false) { return null; }
		String[] propertyNames = parameter.getAnnotation(JSONParam.class).value();
		return (r) -> {
			JsonValue entity = (JsonValue)r.getProperty(JSONParamProvider.ENTITY_PROPERTY_NAME);
			if(entity == null) {
				r.bufferEntity();
				InputStream in = r.getEntityStream();
				try {
					in.reset();
				} catch(IOException ex) {
					throw new UncheckedIOException(ex);
				}
				if(in != null) {
					JsonParser p = this.ParserFactory.createParser(in, StandardCharsets.UTF_8);
					try {
						switch(p.next()) {
							case START_OBJECT: { entity = p.getObject(); break; }
							case START_ARRAY: { entity = p.getArray(); break; }
							case VALUE_STRING:
							case VALUE_NUMBER:
							case VALUE_NULL:
							case VALUE_FALSE:
							case VALUE_TRUE: { entity = p.getValue(); break; }
							default: {
								throw new IllegalStateException("この分岐は通常通りません。");
							}
						}
					} catch(JsonParsingException ex) {
						// データが存在しない場合も、読み込みに失敗するので後で拾う。
					}
					if(entity == null) {
						try {
							in.reset();
						} catch(IOException ex) {
							throw new UncheckedIOException(ex);
						}
						int d;
						try {
							d = in.read();
						} catch(IOException ex) {
							throw new UncheckedIOException(ex);
						}
						if(d == -1) {
							entity = new Null();
						} else {
							throw new IllegalStateException("妥当でないJSONデータが読み込まれました。");
						}
						System.out.println(r.bufferEntity());
					}
				}
				r.getPropertiesDelegate().setProperty(JSONParamProvider.ENTITY_PROPERTY_NAME, entity);
			}
			if(entity instanceof Null) {
				return null;
			}
			JsonValue value = entity;
			for(String propertyName : propertyNames) {
				if((value instanceof JsonObject) == false) { return null; }
				value = value.asJsonObject().get(propertyName);
				if(value == null) {
					return null;
				}
			}
			Class<?> c = parameter.getRawType();
			if(c.equals(String.class)) {
				JsonString stringValue = (JsonString)value;
				return stringValue.getString();
			} else if(c.equals(int.class)) {
				JsonNumber numberValue = (JsonNumber)value;
				return numberValue.intValueExact();
			} else if(c.equals(Integer.class)) {
				JsonNumber numberValue = (JsonNumber)value;
				return Integer.valueOf(numberValue.intValueExact());
			} else if(c.equals(long.class)) {
				JsonNumber numberValue = (JsonNumber)value;
				return numberValue.longValueExact();
			} else if(c.equals(Long.class)) {
				JsonNumber numberValue = (JsonNumber)value;
				return Long.valueOf(numberValue.longValueExact());
			} else if(c.equals(boolean.class)) {
				switch(value.getValueType()) {
					case TRUE: { return true; }
					case FALSE: { return false; }
					default: { throw new IllegalStateException("指定された値はtrue/falseではありませんでした。"); }
				}
			} else if(c.equals(Boolean.class)) {
				switch(value.getValueType()) {
					case TRUE: { return Boolean.TRUE; }
					case FALSE: { return Boolean.FALSE; }
					default: { throw new IllegalStateException("指定された値はtrue/falseではありませんでした。"); }
				}
			} else if(c.equals(double.class)) {
				JsonNumber numberValue = (JsonNumber)value;
				return numberValue.doubleValue();
			} else if(c.equals(Double.class)) {
				JsonNumber numberValue = (JsonNumber)value;
				return Double.valueOf(numberValue.doubleValue());
			} else if(c.equals(LocalDateTime.class)) {
				JsonString stringValue = (JsonString)value;
				return LocalDateTime.parse(stringValue.getString());
			} else if(c.equals(LocalDate.class)) {
				JsonString stringValue = (JsonString)value;
				return LocalDate.parse(stringValue.getString());
			} else if(JsonValue.class.isAssignableFrom(c)) {
				return value;
			} else {
				return value;
			}
		};
	}

	@Override public Priority getPriority() { return Priority.NORMAL; }

}
