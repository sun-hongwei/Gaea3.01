package com.wh.gaea.plugin.role;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.wh.parallel.computing.ParallelComputingExecutor;
import com.wh.parallel.computing.interfaces.ISimpleActionComputer;
import com.wh.swing.tools.MsgHelper;

import wh.interfaces.IConnectionFactory;
import wh.interfaces.IDBConnection;
import wh.interfaces.IDataset;
import wh.interfaces.IDataset.IRow;
import wh.interfaces.ISqlBuilder;
import wh.interfaces.IDBConnection.DBConnectionInfo;
import wh.interfaces.ISqlBuilder.Operation;
import wh.role.obj.CustomDataRoleInfo;
import wh.role.obj.CustomDataRoleInfo.UseType;
import wh.role.obj.DataRoleInfo;
import wh.role.obj.FunRoleInfo;
import wh.role.obj.GroupCustomDataRoleInfo;
import wh.role.obj.GroupCustomDataRoleInfo.RoleInfo;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject;
import wh.role.obj.UserInfo;

public class Roler extends RoleServiceObject {
	DBConnectionInfo connectionInfo;
	ThreadLocal<IDBConnection> db = new ThreadLocal<IDBConnection>() {
	    @Override
		protected IDBConnection initialValue() {
	        try {
				return IConnectionFactory.getConnection(connectionInfo);
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
	    }
	};

	protected IDBConnection getDB() {
		return db.get();
	}
	
	public Roler(IDBConnection db) {
		connectionInfo = db.getConnectionInfo();
	}

	protected Map<String, GroupCustomDataRoleInfo> internalQueryCustomDataRoleInfos(String groupid) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;
		
		ISqlBuilder sqlBuilder;
		try {
			sqlBuilder = IDBConnection.getSqlBuilder(db);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
			return null;
		}
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_group_custom_item i left join workflow_group_custom j on i.cid =j.id");
		if (groupid != null && !groupid.isEmpty())
			sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupid });
		IDataset dataset = db.query(sqlBuilder);

		Map<String, GroupCustomDataRoleInfo> result = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			String id = (String) row.getValue("cid");
			String name = (String) row.getValue("cname");
			GroupCustomDataRoleInfo info;
			if (result.containsKey(id)) {
				info = result.get(id);
			} else {
				info = new GroupCustomDataRoleInfo();
				info.id = id;
				info.groupid = (String) row.getValue("groupid");
				info.operType = DataOperType.valueOf((String) row.getValue("roletype"));
				result.put(info.id, info);
			}
			RoleInfo roleInfo;
			if (info.roles.containsKey(name)) {
				roleInfo = info.roles.get(name);
			} else {
				roleInfo = new RoleInfo();
				roleInfo.name = name;
				roleInfo.id = (String) row.getValue("id");
				info.roles.put(roleInfo.name, roleInfo);
			}

			String item = (String) row.getValue("item");
			roleInfo.items.put(item, item);
		}

		return result;
	}

	@Override
	protected List<GroupCustomDataRoleInfo> queryGroupCustomDataRoleInfo(String groupid) throws Exception {
		Map<String, GroupCustomDataRoleInfo> result = internalQueryCustomDataRoleInfos(groupid);
		return result.size() == 0 ? null : new ArrayList<>(result.values());
	}

	@Override
	protected Map<String, GroupCustomDataRoleInfo> queryGroupCustomDataRoleInfos() throws Exception {
		return internalQueryCustomDataRoleInfos(null);
	}

	@Override
	protected UserInfo queryUserInfo(String userid) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_user");
		sqlBuilder.addWhere("userid", Operation.otEqual, new Object[] { userid });
		IDataset dataset = db.query(sqlBuilder);

		if (dataset == null || dataset.getRows() == null || dataset.getRows().size() == 0)
			return null;

		IRow row = dataset.getRow(0);
		UserInfo userInfo = new UserInfo();
		userInfo.userid = (String) row.getValue("userid");
		userInfo.username = (String) row.getValue("username");
		userInfo.password = (String) row.getValue("password");
		userInfo.superbutton = (int) row.getValue("superbutton") == 1;
		userInfo.superdata = (int) row.getValue("superdata") == 1;
		userInfo.supermenu = (int) row.getValue("supermenu") == 1;
		userInfo.superview = (int) row.getValue("superview") == 1;
		userInfo.lasttime = (Date) row.getValue("lasttime");
		userInfo.registertime = (Date) row.getValue("registertime");

		return userInfo;
	}

	@Override
	protected List<UserInfo> queryUserInfos() throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_user");
		IDataset dataset = db.query(sqlBuilder);

		List<UserInfo> datas = new ArrayList<>();
		for (IRow row : dataset.getRows()) {
			UserInfo userInfo = new UserInfo();
			userInfo.userid = (String) row.getValue("userid");
			userInfo.username = (String) row.getValue("username");
			userInfo.password = (String) row.getValue("password");
			userInfo.superbutton = (int) row.getValue("superbutton") == 1;
			userInfo.superdata = (int) row.getValue("superdata") == 1;
			userInfo.supermenu = (int) row.getValue("supermenu") == 1;
			userInfo.superview = (int) row.getValue("superview") == 1;
			userInfo.lasttime = (Date) row.getValue("lasttime");
			userInfo.registertime = (Date) row.getValue("registertime");
			datas.add(userInfo);
		}
		return datas;
	}

	@Override
	protected GroupInfo queryGroupInfo(String groupId) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_group");
		IDataset dataset;
		sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupId });

		dataset = db.query(sqlBuilder);

		if (dataset == null || dataset.getRows() == null || dataset.getRows().size() == 0)
			return null;

		IRow row = dataset.getRow(0);
		GroupInfo groupInfo = new GroupInfo();
		groupInfo.groupid = (String) row.getValue("groupid");
		groupInfo.grouppid = (String) row.getValue("grouppid");
		groupInfo.groupname = (String) row.getValue("groupname");
		groupInfo.grouptype = (String) row.getValue("grouptype");
		groupInfo.groupmemo = (String) row.getValue("groupmemo");

		return groupInfo;
	}

	@Override
	protected Map<String, GroupInfo> queryGroupInfos(String[] grouptypes, boolean include) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_group");
		IDataset dataset;
		if (grouptypes != null) {
			sqlBuilder.addWhere("grouptype", include ? Operation.otIn : Operation.otNotIn, grouptypes);
		}
		dataset = db.query(sqlBuilder);

		Map<String, GroupInfo> datas = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			GroupInfo groupInfo = new GroupInfo();
			groupInfo.groupid = (String) row.getValue("groupid");
			groupInfo.grouppid = (String) row.getValue("grouppid");
			groupInfo.groupname = (String) row.getValue("groupname");
			groupInfo.grouptype = (String) row.getValue("grouptype");
			groupInfo.groupmemo = (String) row.getValue("groupmemo");
			datas.put(groupInfo.groupid, groupInfo);
		}
		return datas;
	}

	protected List<String> queryUserGroups(String userid) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder detailBuilder;
		detailBuilder = IDBConnection.getSqlBuilder(db);
		detailBuilder.addField("i.*");
		detailBuilder.addTable("workflow_group_user i left join workflow_user j on i.userid = j.userid");
		detailBuilder.addWhere("i.userid", Operation.otEqual, new Object[] { userid });
		IDataset dataset = db.query(detailBuilder);
		List<String> groups = new ArrayList<>();
		for (IRow row : dataset.getRows()) {
			String groupid = (String) row.getValue("groupid");
			groups.add(groupid);
		}
		return groups;
	}

	@Override
	protected Map<String, List<String>> queryGroupUsers() throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder detailBuilder;
		detailBuilder = IDBConnection.getSqlBuilder(db);
		detailBuilder.addField("i.*");
		detailBuilder.addTable("workflow_group_user i left join workflow_group j on i.groupid = j.groupid");
		IDataset dataset = db.query(detailBuilder);
		Map<String, List<String>> result = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			String uid = (String) row.getValue("userid");
			String groupid = (String) row.getValue("groupid");
			List<String> groups;
			if (result.containsKey(groupid)) {
				groups = result.get(groupid);
			} else {
				groups = new ArrayList<>();
				result.put(groupid, groups);
			}
			groups.add(uid);
		}
		return result;
	}

	@Override
	protected List<FunRoleInfo> queryFunRoleInfos() throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder roleBuilder = IDBConnection.getSqlBuilder(db);
		roleBuilder.addField("*");
		roleBuilder.addTable("workflow_role");
		IDataset roleDataset = db.query(roleBuilder);

		List<FunRoleInfo> result = new ArrayList<>();
		for (IRow roleRow : roleDataset.getRows()) {
			FunRoleInfo info = new FunRoleInfo();
			info.roleid = (String) roleRow.getValue("roleid");
			info.roletext = (String) roleRow.getValue("roletext");
			info.rolepid = (String) roleRow.getValue("rolepid");
			info.rolememo = (String) roleRow.getValue("rolememo");
			info.roletype = (String) roleRow.getValue("roletype");
			result.add(info);
		}
		return result;
	}

	protected List<String> queryGroupFunRoleInfo(String groupId) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder detailBuilder;
		detailBuilder = IDBConnection.getSqlBuilder(db);
		detailBuilder.addField("*");
		detailBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupId });
		detailBuilder.addTable("workflow_group_role");
		IDataset dataset = db.query(detailBuilder);
		List<String> groups = new ArrayList<>();
		for (IRow row : dataset.getRows()) {
			String roleid = (String) row.getValue("roleid");
			groups.add(roleid);
		}
		return groups;
	}

	@Override
	protected Map<String, List<String>> queryGroupFunRoleInfos() throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder detailBuilder;
		detailBuilder = IDBConnection.getSqlBuilder(db);
		detailBuilder.addField("*");
		detailBuilder.addTable("workflow_group_role");
		IDataset dataset = db.query(detailBuilder);
		Map<String, List<String>> result = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			String roleid = (String) row.getValue("roleid");
			String groupid = (String) row.getValue("groupid");
			List<String> groups;
			if (result.containsKey(groupid)) {
				groups = result.get(groupid);
			} else {
				groups = new ArrayList<>();
				result.put(groupid, groups);
			}
			groups.add(roleid);
		}
		return result;
	}

	@Override
	protected List<DataRoleInfo> queryDataRoleInfos() throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder;
		sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("id, groupid, opertype, roletype");
		sqlBuilder.addTable("workflow_role_data");
		IDataset dataset = db.query(sqlBuilder);

		List<DataRoleInfo> result = new ArrayList<>();
		for (IRow row : dataset.getRows()) {
			DataRoleInfo info = new DataRoleInfo();
			info.id = (String) row.getValue("id");
			info.groupid = (String) row.getValue("groupid");
			info.opertype = (String) row.getValue("opertype");
			info.roletype = (String) row.getValue("roletype");
			result.add(info);
		}
		return result;
	}

	@Override
	protected Map<String, DataRoleInfo> queryDataRoleInfo(String groupId) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder;
		sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("id, groupid, opertype, roletype");
		sqlBuilder.addWhere("groupid", Operation.otEqual, new Object[] { groupId });
		sqlBuilder.addTable("workflow_role_data");
		IDataset dataset = db.query(sqlBuilder);

		Map<String, DataRoleInfo> result = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			DataRoleInfo info = new DataRoleInfo();
			info.id = (String) row.getValue("id");
			info.groupid = (String) row.getValue("groupid");
			info.opertype = (String) row.getValue("opertype");
			info.roletype = (String) row.getValue("roletype");
			result.put(info.opertype, info);
		}
		return result;
	}

	protected Map<String, Map<String, String>> setDataRoleGroups(IDataset dataset) {
		Map<String, Map<String, String>> result = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			String roleid = (String) row.getValue("id");
			String groupid = (String) row.getValue("groupid");
			Map<String, String> groups;
			if (result.containsKey(roleid)) {
				groups = result.get(roleid);
			} else {
				groups = new HashMap<>();
				result.put(roleid, groups);
			}
			groups.put(groupid, groupid);
		}
		return result;

	}

	@Override
	protected Map<String, Map<String, String>> queryDataRoleGroups() throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder detailBuilder;
		detailBuilder = IDBConnection.getSqlBuilder(db);
		detailBuilder.addField("*");
		detailBuilder.addTable("workflow_role_data_group");
		IDataset dataset = db.query(detailBuilder);

		return setDataRoleGroups(dataset);
	}

	@Override
	protected Map<String, Map<String, String>> queryDataRoleGroup(String groupId) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder detailBuilder;
		detailBuilder = IDBConnection.getSqlBuilder(db);
		detailBuilder.addField("i.*");
		detailBuilder.addTable("workflow_role_data_group i left join workflow_role_data j on i.id =j.id");
		detailBuilder.addWhere("j.groupid", Operation.otEqual, new Object[] { groupId });
		IDataset dataset = db.query(detailBuilder);
		return setDataRoleGroups(dataset);
	}

	public static String listToString(Collection<String> list) {
		JSONArray data = new JSONArray();
		for (String str : list) {
			data.put(str);
		}

		return data.toString();
	}

	public static void showRealRole(String[] groupids, Roler roler) {
		showRealRole(groupids, roler, true);
	}

	public void initCustomDataRole(String groupid) throws Exception {
		if (db == null)
			return;

		groupCustomDataRoles.initGroup(groupid);
	}

	public void initUser(String userid) throws Exception {
		if (db == null)
			return;

		users.initUser(userid);
		groups.initUser(userid);
	}

	public void initGroup(String groupId) throws Exception {
		if (db == null)
			return;

		groups.initGroup(groupId);
		funRoles.initGroupRole(groupId);
	}

	public void removeUser(String userId) {
		if (db == null)
			return;

		users.removeUser(userId);
		groups.removeUser(userId);
	}

	public void removeGroup(String groupId) throws Exception {
		if (db == null)
			return;

		groups.removeGroup(groupId);
		funRoles.removeGroupRole(groupId);
		dataRoles.removeGroupDataRole(groupId);
	}

	public static void showRealRole(String[] groupids, Roler roler, boolean showDataRole) {
		Map<String, String> groupNames = new HashMap<>();
		Map<FunRoleType, Map<String, String>> funRoles = new HashMap<>();
		Map<String, Map<String, String>> dataRoles = new HashMap<>();
		for (String groupid : groupids) {
			if (groupid == null || groupid.isEmpty())
				continue;

			GroupInfo curInfo = roler.getGroups().getGroup(groupid);
			if (curInfo == null)
				continue;

			List<GroupInfo> groups = roler.getGroups().getGroups(groupid);

			if (groups == null) {
				continue;
			}

			for (GroupInfo groupInfo : groups) {
				groupNames.put(groupInfo.groupid, groupInfo.groupname);
			}

			Map<FunRoleType, Map<String, FunRoleInfo>> roless = roler.getFunRoles().getRoles(groups);
			if (roless == null)
				continue;

			for (FunRoleType funRoleType : roless.keySet()) {
				Map<String, String> roles;
				if (funRoles.containsKey(funRoleType)) {
					roles = funRoles.get(funRoleType);
				} else {
					roles = new HashMap<>();
					funRoles.put(funRoleType, roles);
				}
				for (FunRoleInfo info : roless.get(funRoleType).values()) {
					roles.put(info.roleid, info.roletext);
				}

			}

			if (showDataRole) {
				Map<String, Map<String, DataRoleInfo>> map = roler.getDataRoles().getRoles(groupid);
				for (Map<String, DataRoleInfo> entry : map.values()) {
					for (DataRoleInfo info : entry.values()) {
						Map<String, String> roles;
						if (dataRoles.containsKey(info.opertype)) {
							roles = dataRoles.get(info.opertype);
						} else {
							roles = new HashMap<>();
							dataRoles.put(info.opertype, roles);
						}

						switch (info.roletype) {
						case "self":
							roles.put("self", "self");
							break;
						case "group":
							GroupInfo groupInfo = roler.getGroups().getGroup(info.groupid);
							if (groupInfo != null)
								roles.put(info.groupid, groupInfo.groupname);
							break;
						case "groups":
							for (String gid : info.groups) {
								groupInfo = roler.getGroups().getGroup(gid);
								if (groupInfo == null)
									continue;

								roles.put(gid, groupInfo.groupname);
							}
							break;
						}
					}
				}
			}
		}

		MsgHelper.showMessage("所在的组：" + listToString(groupNames.values()));
		for (FunRoleType funRoleType : funRoles.keySet()) {
			String title;
			switch (funRoleType) {
			case ftButton:
				title = "按钮权限";
				break;
			case ftFun:
				title = "功能权限";
				break;
			case ftMenu:
				title = "菜单权限";
				break;
			case ftTree:
				title = "导航权限";
				break;
			case ftUI:
				title = "界面权限";
				break;
			default:
				title = "未定义权限";
				break;
			}
			MsgHelper.showMessage(title + "信息：" + listToString(funRoles.get(funRoleType).values()));
		}
		for (String opertype : dataRoles.keySet()) {
			MsgHelper.showMessage("数据权限信息[" + opertype + "]：" + listToString(dataRoles.get(opertype).values()));
		}
	}

	static Roler roler;

	public static Roler instance() {
		return roler;
	}

	public static void reset(IDBConnection db) {
		roler = null;
		
		if (db == null)
			return;

		try {
			roler = new Roler(db);
			roler.init();
		} catch (Exception e) {
			e.printStackTrace();
			roler = null;
		}
	}

	@Override
	public void init() throws Exception {
		List<String> commands = new ArrayList<>();
		commands.add("users");
		commands.add("groups");
		commands.add("funRoles");
		commands.add("dataRoles");
		commands.add("customDataRoles");
		commands.add("groupCustomDataRoles");
		ParallelComputingExecutor<String> executor = new ParallelComputingExecutor<String>(commands, 2);
		executor.execute(new ISimpleActionComputer<String>() {
			
			@Override
			public void compute(String t1) throws Exception {
				switch (t1) {
				case "users":
					users.init();
					break;
				case "groups":
					groups.init();
					break;
				case "funRoles":
					funRoles.init();
					break;
				case "dataRoles":
					dataRoles.init();
					break;
				case "customDataRoles":
					customDataRoles.init();
					break;
				case "groupCustomDataRoles":
					groupCustomDataRoles.init();
					break;
				}
			}
		});
	}
	
	@Override
	protected CustomDataRoleInfo queryCustomDataRoleInfo(String name) throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder;
		sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_customrole");
		sqlBuilder.addWhere("name", Operation.otEqual, new Object[] { name });
		IDataset dataset = db.query(sqlBuilder);

		if (dataset.getRowCount() == 0)
			return null;

		IRow row = dataset.getRow(0);
		CustomDataRoleInfo info = new CustomDataRoleInfo((String) row.getValue("name"));
		info.field = (String) row.getValue("field");
		info.tablename = (String) row.getValue("tablename");
		info.useType = UseType.valueOf((String) row.getValue("usetype"));
		info.sqlInfo.fromJson(new JSONObject((String) row.getValue("sqlinfo")));
		info.listInfo.fromJson(new JSONObject((String) row.getValue("listinfo")));
		return info;

	}

	@Override
	protected Map<String, CustomDataRoleInfo> queryCustomDataRoleInfos() throws Exception {
		IDBConnection db = getDB();
		if (db == null)
			return null;

		ISqlBuilder sqlBuilder;
		sqlBuilder = IDBConnection.getSqlBuilder(db);
		sqlBuilder.addField("*");
		sqlBuilder.addTable("workflow_customrole");
		IDataset dataset = db.query(sqlBuilder);

		Map<String, CustomDataRoleInfo> roles = new HashMap<>();
		for (IRow row : dataset.getRows()) {
			CustomDataRoleInfo info = new CustomDataRoleInfo((String) row.getValue("name"));
			info.field = (String) row.getValue("field");
			info.tablename = (String) row.getValue("tablename");
			info.useType = UseType.valueOf((String) row.getValue("usetype"));
			info.sqlInfo.fromJson(new JSONObject((String) row.getValue("sqlinfo")));
			info.listInfo.fromJson(new JSONObject((String) row.getValue("listinfo")));
			roles.put(info.name, info);
		}

		return roles;
	}

}
