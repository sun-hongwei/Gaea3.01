package com.wh.gaea.control;

import org.json.JSONObject;

public class KeyValue<K, V>{
	public K key;
	public V value;
	public KeyValue(){
		
	}
	public KeyValue(K key, V value){
		this.key = key;
		this.value = value;
	}
	
	public String toString() {
		if (key == null)
			return "";
		
		return key.toString().trim();
	}
	
	public JSONObject toJson(){
		JSONObject object = new JSONObject();
		object.put("key", key);
		object.put("value", value);
		return object;
	}
	
	@SuppressWarnings("unchecked")
	public KeyValue(JSONObject json){
		if (json == null)
			return;
		
		if (json.has("key"))
			key = (K)json.get("key");
		if (json.has("value"))
			value = (V)json.get("value");
	}
}