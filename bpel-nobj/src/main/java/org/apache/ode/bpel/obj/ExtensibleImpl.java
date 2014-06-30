package org.apache.ode.bpel.obj;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class ExtensibleImpl  implements Extensible<Object>{
	/** The wrapper wraps fields. Fields can be deleted, added or updated */
	protected Map<String, Object> fieldContainer;
	/** Version of this class*/
	private static String CLASS_VERSION = "classVersion";

	protected ExtensibleImpl() {
		fieldContainer = new LinkedHashMap<String, Object>();
	}
	protected ExtensibleImpl(Map<String, Object> container) {
		fieldContainer = container;
	}
	
	@JsonAnyGetter
	public Map<String, Object> getFieldContainer() {
		return fieldContainer;
	}

	@JsonAnySetter
	public void addField(String name, Object value) {
		fieldContainer.put(name, value);
	}

	public Object getField(String name) {
		return fieldContainer.get(name);
	}

	@Override
	public Object delField(String fieldName) {
		return fieldContainer.remove(fieldName);
	}
	@JsonIgnore
	public int getClassVersion() {
		return (Integer) fieldContainer.get(CLASS_VERSION);
	}

	public void setClassVersion(int version) {
		fieldContainer.put(CLASS_VERSION, version);
	}
}
