package com.wh.gaea.role;

import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

import org.json.JSONArray;

public class RoleInfos extends ArrayList<RoleInfo>{
	private static final long serialVersionUID = -7985189032255020369L;

	public RoleInfos(){
		super();
	}
	
	public JSONArray toJson(){
		JSONArray result = new JSONArray();
		for (RoleInfo info : this) {
			result.put(info.toJson());
		}
		return result;
	}
	
	public DefaultListModel<RoleInfo> toModel(){
		DefaultListModel<RoleInfo> result = new DefaultListModel<>();
		for (RoleInfo info : this) {
			result.addElement(info);
		}
		
		return result;
	}
	
	public RoleInfos(DefaultListModel<RoleInfo> infos){
		super();
		for (int i = 0; i < infos.getSize(); i++) {
			add(infos.get(i));
		}
	}
	
	public RoleInfos(List<RoleInfo> infos){
		super();
		addAll(infos);
	}
	
	public RoleInfos(JSONArray json){
		super();
		for (int i = 0; i < json.length(); i++) {
			add(new RoleInfo(json.getJSONObject(i)));
		}
	}
	
	public String toString(){
		JSONArray names = new JSONArray();
		for (RoleInfo info : this) {
			names.put(info.toString());
		}
		
		return names.toString();
	}
}