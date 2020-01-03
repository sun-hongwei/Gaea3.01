package com.wh.gaea.plugin.role;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;
import wh.role.obj.CustomDataRoleInfo;

public class CustomDataRoleManager {
	Map<String, CustomDataRoleInfo> roles = new HashMap<>();
	IDBConnection db;

	public CustomDataRoleManager(IDBConnection db) {
		this.db = db;
	}

	public List<CustomDataRoleInfo>  refresh() {
		roles.clear();
		return getRoles();
	}

	public CustomDataRoleInfo get(String name) {
		if (roles.containsKey(name))
			return roles.get(name);
		else
			return null;
	}

	public void remove(CustomDataRoleInfo info) throws Exception {
		if (info == null)
			return;

		remove(info.name);
	}

	public void remove(String name) throws Exception {
		if (name != null && !name.isEmpty()) {
			ISqlBuilder sqlBuilder;
			try {
				sqlBuilder = IDBConnection.getSqlBuilder(db);
				sqlBuilder.addTable("workflow_customrole");
				sqlBuilder.addWhere("name", Operation.otEqual, new Object[] { name });
				sqlBuilder.setSqlType(SqlType.stDelete);
			} catch (Exception e1) {
				e1.printStackTrace();
				throw e1;
			}
		}
	}

	public void save(CustomDataRoleInfo info) throws Exception {
		if (info == null)
			return;

		ISqlBuilder sqlBuilder;
		sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_customrole");
		sqlBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
		IDataset dataset = db.query(sqlBuilder);

		IRow row = dataset.newRow();
		row.setValue("name", info.name);
		row.setValue("tablename", info.tablename);
		row.setValue("field", info.field);
		row.setValue("usetype", info.useType.name());
		row.setValue("sqlinfo", info.sqlInfo.toJson().toString());
		row.setValue("listinfo", info.listInfo.toJson().toString());
		dataset.addRow(row);

		db.beginTran();
		try {
			ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable("workflow_customrole");
			delBuilder.addWhere("name", Operation.otEqual, new Object[] { info.name });
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			dataset.post(db);

			db.commitTran();
			
		} catch (Exception e) {
			db.rollbackTran();
			throw e;
		}
	}

	public List<CustomDataRoleInfo> getRoles() {
		if (roles.size() == 0){
			roles = Roler.instance().getCustomDataRoles().getRoleMap();
		}
		
		if (roles.size() == 0)
			return new ArrayList<>();
		
		return new ArrayList<>(roles.values());
	}

	static CustomDataRoleManager manager;

	public static void reset(){
		manager = null;
	}
	
	public static CustomDataRoleManager getManager(IDBConnection db) {
		if (manager == null)
			manager = new CustomDataRoleManager(db);

		return manager;
	}
}
