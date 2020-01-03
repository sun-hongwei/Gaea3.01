package com.wh.gaea.plugin.role;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.ISqlBuilder.LogicalOperation;
import wh.interfaces.ISqlBuilder.Operation;
import wh.interfaces.ISqlBuilder.SqlType;
import wh.role.obj.GroupCustomDataRoleInfo;
import wh.role.obj.GroupCustomDataRoleInfo.RoleInfo;
import wh.role.obj.RoleServiceObject.DataOperType;

public class GroupCustomDataRoleManager {
	Map<String, Map<DataOperType, GroupCustomDataRoleInfo>> roles = new HashMap<>();
	IDBConnection db;

	public GroupCustomDataRoleManager(IDBConnection db) {
		this.db = db;
	}

	public GroupCustomDataRoleInfo get(String groupid, DataOperType operType) {
		if (groupid == null || groupid.isEmpty())
			return null;

		if (roles.containsKey(groupid))
			if (roles.get(groupid).containsKey(operType)) {
				return roles.get(groupid).get(operType);
			}

		try {
			Map<DataOperType, GroupCustomDataRoleInfo> result = Roler.instance().getGroupCustomDataRoles()
					.getRoleInfo(groupid);
			if (result == null)
				return null;
			if (result.containsKey(operType)) {
				Map<DataOperType, GroupCustomDataRoleInfo> map;
				if (roles.containsKey(groupid)) {
					map = roles.get(groupid);
				} else {
					map = new HashMap<>();
					roles.put(groupid, map);
				}
				GroupCustomDataRoleInfo info = result.get(operType);
				map.put(operType, info);
				return info;
			} else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public GroupCustomDataRoleInfo add(String groupid, DataOperType operType) throws Exception {

		GroupCustomDataRoleInfo info = get(groupid, operType);
		if (info != null)
			return info;

		ISqlBuilder mainBuilder = IDBConnection.getSqlBuilder(db);
		mainBuilder.addField("cid");
		mainBuilder.addTable("workflow_group_custom");
		mainBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		IDataset dataset = db.query(mainBuilder);

		String id = UUID.randomUUID().toString();
		if (dataset != null && dataset.getRowCount() > 0)
			id = (String) dataset.getRow(0).getValue(0);

		info = new GroupCustomDataRoleInfo();
		info.id = id;
		info.groupid = groupid;
		info.operType = operType;
		Map<DataOperType, GroupCustomDataRoleInfo> map;
		if (roles.containsKey(info.groupid)) {
			map = roles.get(info.groupid);
		} else {
			map = new HashMap<>();
			roles.put(info.groupid, map);
		}

		map.put(info.operType, info);
		return info;
	}

	public RoleInfo addRoleInfo(GroupCustomDataRoleInfo info, String name) {
		RoleInfo roleInfo = new RoleInfo();
		roleInfo.id = UUID.randomUUID().toString();
		roleInfo.name = name;
		info.roles.put(roleInfo.name, roleInfo);
		return roleInfo;
	}

	public void remove(GroupCustomDataRoleInfo info) throws Exception {
		db.beginTran();
		try {
			ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable("workflow_group_custom_item");
			StringBuilder builder = new StringBuilder(
					"cid in (select id from workflow_group_custom where groupid = '" + info.groupid + "')");
			delBuilder.setRawWhere(builder);
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable("workflow_group_custom");
			delBuilder.addWhere("groupid", Operation.otEqual, new Object[] { info.groupid });
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			db.commitTran();

			Roler.instance().initCustomDataRole(info.groupid);

			roles.remove(info.groupid);
		} catch (Exception e) {
			db.rollbackTran();
			throw e;
		}

	}

	public void remove(GroupCustomDataRoleInfo info, RoleInfo roleInfo) throws Exception {
		db.beginTran();
		try {
			ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable("workflow_group_custom_item");
			StringBuilder builder = new StringBuilder("cid in (select id from workflow_group_custom where groupid = '"
					+ info.groupid + "' and cname='" + roleInfo.name + "')");
			delBuilder.setRawWhere(builder);
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable("workflow_group_custom");
			delBuilder.addWhere("groupid", Operation.otEqual, new Object[] { info.groupid });
			delBuilder.addLogicalOperation(LogicalOperation.otAnd);
			delBuilder.addWhere("cname", Operation.otEqual, new Object[] { roleInfo.name});			
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			db.commitTran();

			Roler.instance().initCustomDataRole(info.groupid);

			roles.remove(info.groupid);
		} catch (Exception e) {
			db.rollbackTran();
			throw e;
		}

	}

	public void save(GroupCustomDataRoleInfo info) throws Exception {

		ISqlBuilder mainBuilder = IDBConnection.getSqlBuilder(db);
		mainBuilder.addField("*");
		mainBuilder.addTable("workflow_group_custom");
		mainBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
		IDataset mainDataset = db.query(mainBuilder);

		ISqlBuilder itemBuilder;
		itemBuilder = IDBConnection.getSqlBuilder(db);
		itemBuilder.addField("*");
		itemBuilder.addTable("workflow_group_custom_item");
		itemBuilder.addWhere("1", Operation.otEqual, new Object[] { 2 });
		IDataset itemDataset = db.query(itemBuilder);

		for (RoleInfo roleInfo : info.roles.values()) {
			IRow row = mainDataset.newRow();
			row.setValue("id", roleInfo.id);
			row.setValue("cid", info.id);
			row.setValue("groupid", info.groupid);
			row.setValue("cname", roleInfo.name);
			row.setValue("roletype", info.operType.name());
			mainDataset.addRow(row);
			for (String item : roleInfo.items.values()) {
				row = itemDataset.newRow();
				row.setValue("cid", roleInfo.id);
				row.setValue("item", item);
				itemDataset.addRow(row);
			}
		}

		db.beginTran();
		try {
			ISqlBuilder delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable("workflow_group_custom_item");
			StringBuilder builder = new StringBuilder(
					"cid in (select id from workflow_group_custom where groupid = '" + info.groupid + "')");
			delBuilder.setRawWhere(builder);
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			delBuilder = IDBConnection.getSqlBuilder(db);
			delBuilder.addTable(mainBuilder.getTableName());
			delBuilder.addWhere("groupid", Operation.otEqual, new Object[] { info.groupid });
			delBuilder.setSqlType(SqlType.stDelete);
			db.execute(delBuilder);

			mainDataset.post(db);
			itemDataset.post(db);

			db.commitTran();

			Roler.instance().initCustomDataRole(info.groupid);
		} catch (Exception e) {
			db.rollbackTran();
			throw e;
		}

	}

	static GroupCustomDataRoleManager manager;

	public static void reset(){
		manager = null;
	}
	
	public static GroupCustomDataRoleManager getManager(IDBConnection db) {
		if (manager == null)
			manager = new GroupCustomDataRoleManager(db);
		return manager;
	}
}
