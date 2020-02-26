package jp.empressia.enterprise.ws.jersey.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import jp.empressia.enterprise.ws.JSONParam;

/**
 * JSONParamに値を割り当てるProviderです。
 * JerseyとJacksonに依存しています。
 */
@Provider
public class JSONParamProvider implements ValueParamProvider {

	private static final String ENTITY_PROPERTY_NAME = JSONParamProvider.class.getName() + ".Entity";

	private ObjectMapper mapper = new ObjectMapper();

	@Override
	public Function<ContainerRequest, ?> getValueProvider(Parameter parameter) {
	    if(parameter.isAnnotationPresent(JSONParam.class) == false) { return null; }
	    String[] propertyNames = parameter.getAnnotation(JSONParam.class).value();
	    return (r) -> {
			JsonNode entity = (JsonNode)r.getProperty(JSONParamProvider.ENTITY_PROPERTY_NAME);
			if(entity == null) {
				r.bufferEntity();
				try {
					InputStream in = r.getEntityStream();
					if(in != null) {
						// 仕様ではnullになるっようなので、来たことないけど、念のため、後ろにチェックは入れておくこと。
						entity = this.mapper.readTree(in);
					}
					if(entity == null) {
						entity = MissingNode.getInstance();
					}
				} catch(IOException ex) {
					throw new UncheckedIOException(ex);
				}
				r.getPropertiesDelegate().setProperty(JSONParamProvider.ENTITY_PROPERTY_NAME, entity);
			}
			if((entity instanceof MissingNode) || (entity instanceof NullNode)) {
				return null;
			}
			JsonNode node = entity;
			if((propertyNames.length == 1) && propertyNames[0].length() == 0) {
			} else {
				for(String propertyName : propertyNames) {
					node = node.get(propertyName);
					if(node == null) {
						return null;
					}
				}
			}
			Class<?> c = parameter.getRawType();
			if(c.isEnum() == false) {
				if(c.equals(String.class)) {
					return node.asText();
				} else if(c.equals(int.class)) {
					return node.asInt();
				} else if(c.equals(Integer.class)) {
					return Integer.valueOf(node.asInt());
				} else if(c.equals(long.class)) {
					return node.asLong();
				} else if(c.equals(Long.class)) {
					return Long.valueOf(node.asLong());
				} else if(c.equals(boolean.class)) {
					return node.asBoolean();
				} else if(c.equals(Boolean.class)) {
					return Boolean.valueOf(node.asBoolean());
				} else if(c.equals(double.class)) {
					return node.asDouble();
				} else if(c.equals(Double.class)) {
					return Double.valueOf(node.asDouble());
				} else if(c.equals(LocalDateTime.class)) {
					return LocalDateTime.parse(node.asText());
				} else if(c.equals(LocalDate.class)) {
					return LocalDate.parse(node.asText());
				} else if(c.equals(JsonNode.class)) {
					return node;
				} else {
					return node;
				}
			} else {
				@SuppressWarnings({"unchecked", "rawtypes"})
				Enum<?> e = Enum.valueOf((Class<? extends Enum>)c, node.asText());
				return e;
			}
	    };
	}

	@Override public Priority getPriority() { return Priority.NORMAL; }

}
