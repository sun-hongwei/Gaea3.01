package wh.role.obj;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import wh.role.interfaces.ICustomDataRole;
import wh.role.interfaces.IDataRole;
import wh.role.interfaces.IFunRole;
import wh.role.interfaces.IGroup;
import wh.role.interfaces.IGroupCustomDataRole;
import wh.role.interfaces.IInfos;
import wh.role.interfaces.IRoleService;
import wh.role.interfaces.IUser;

public abstract class RoleServiceObject implements IRoleService {

	public enum DataOperType {
		dtQuery, dtUpdate
	}

	public enum FunRoleType {
		ftUI, ftButton, ftMenu, ftFun, ftTree, ftNone
	}

	public interface ISerializationJson {
		JSONObject toJson();

		void fromJson(JSONObject json);

		String getKey();
	}

	public interface ITraverse<T> {
		void callback(T t);
	}

	public abstract class BaseInfos<T> implements IInfos<T> {
		protected abstract Collection<T> getSource();

		public void traverse(ITraverse<T> traverse) {
			for (T t : getSource()) {
				traverse.callback(t);
			}
		}
	}

	public class Users extends BaseInfos<UserInfo> implements IUser {
		Map<String, UserInfo> users = new ConcurrentHashMap<>();

		@Override
		public void initUser(String userId) throws Exception {
			UserInfo info = queryUserInfo(userId);
			if (info != null) {
				users.put(info.userid, info);
			}
		}

		@Override
		public void init() throws Exception {
			users.clear();

			List<UserInfo> infos = queryUserInfos();
			for (UserInfo userInfo : infos) {
				users.put(userInfo.userid, userInfo);
			}
		}

		/**
		 * 获取用户信息
		 * 
		 * @param userId
		 *            用户id
		 * @return 返回用户信息
		 */
		@Override
		public UserInfo getUser(String userId) {
			return users.containsKey(userId) ? users.get(userId) : null;
		}

		@Override
		protected Collection<UserInfo> getSource() {
			return users.values();
		}

		public UserInfo removeUser(String userid) {
			return users.remove(userid);
		}

		public void addUser(UserInfo user) {
			users.put(user.userid, user);
		}
	}

	public class Groups extends BaseInfos<GroupInfo> implements IGroup {

		/**
		 * key为组id，map为key组包含的所有子组id，map的key为组id，map的value为组信息
		 */
		Map<String, Map<String, GroupInfo>> groupMap = new ConcurrentHashMap<>();

		/**
		 * key为组id，value为组信息
		 */
		Map<String, GroupInfo> groups = new ConcurrentHashMap<>();

		/**
		 * key为用户id，map为此用户的所有组信息（包含所有下级组），map的key为组id，map的value为组信息
		 */
		Map<String, Map<String, GroupInfo>> userMap = new ConcurrentHashMap<>();

		/**
		 * key为用户id，map为此用户的所有直接组信息，map的key为组id，map的value为组信息
		 */
		Map<String, Map<String, GroupInfo>> userSimpleMap = new ConcurrentHashMap<>();

		protected void addTreeRoots(GroupInfo groupInfo, String id, Map<String, Map<String, GroupInfo>> treeGroups) {
			id = (id == null || id.isEmpty()) ? "" : id;
			Map<String, GroupInfo> childs;
			if (id.isEmpty())
				return;

			if (!treeGroups.containsKey(id)) {
				childs = new HashMap<>();
				treeGroups.put(id, childs);
			} else
				childs = treeGroups.get(id);

			childs.put(groupInfo.groupid, groupInfo);

		}

		protected Map<String, Map<String, GroupInfo>> getTreeGroups(Map<String, GroupInfo> groups) {
			Map<String, Map<String, GroupInfo>> treeGroups = new HashMap<>();
			for (GroupInfo groupInfo : groups.values()) {
				addTreeRoots(groupInfo, groupInfo.grouppid, treeGroups);
				addTreeRoots(groupInfo, groupInfo.groupid, treeGroups);
			}

			Map<String, Map<String, GroupInfo>> result = new HashMap<>();
			for (String groupId : treeGroups.keySet()) {
				Map<String, GroupInfo> allgroups = new HashMap<>();
				result.put(groupId, allgroups);
				getTreeGroups(groupId, treeGroups, allgroups);
			}
			return result;
		}

		protected void getTreeGroups(String groupId, Map<String, Map<String, GroupInfo>> treeGroups,
				Map<String, GroupInfo> result) {
			Collection<GroupInfo> infos = treeGroups.get(groupId).values();
			if (infos == null)
				return;
			for (GroupInfo groupInfo : infos) {
				result.put(groupInfo.groupid, groupInfo);
				if (groupId.equals(groupInfo.groupid))
					continue;

				getTreeGroups(groupInfo.groupid, treeGroups, result);
			}
		}

		public void onlyInitGroup(String[] grouptypes, boolean include) throws Exception {
			groups.clear();
			groupMap.clear();
			userMap.clear();
			userSimpleMap.clear();

			groups = queryGroupInfos(grouptypes, include);
			if (groups == null || groups.size() == 0)
				return;

			groupMap = getTreeGroups(groups);
		}

		public List<GroupInfo> removeUser(String userid) {
			List<GroupInfo> result = new ArrayList<>();
			for (GroupInfo groupInfo : groups.values()) {
				if (groupInfo.users.containsKey(userid)) {
					groupInfo.users.remove(userid);
					result.add(groupInfo);
				}
			}
			return result;
		}

		public GroupInfo removeGroup(String groupid) {
			if (groupid == null || groupid.isEmpty())
				return null;

			GroupInfo groupInfo = groups.remove(groupid);
			if (groupInfo == null)
				return null;

			for (Map<String, GroupInfo> map : userSimpleMap.values()) {
				map.remove(groupid);
			}

			groupMap.remove(groupid);

			for (Map<String, GroupInfo> map : groupMap.values()) {
				map.remove(groupid);
			}

			for (Map<String, GroupInfo> map : userMap.values()) {
				map.remove(groupid);
			}

			return groupInfo;
		}

		protected void addGroupMap(String groupid, GroupInfo info) {
			if (!groupMap.containsKey(groupid))
				groupMap.put(groupid, new HashMap<>());

			Map<String, GroupInfo> map = groupMap.get(groupid);					
			map.put(info.groupid, info);		
			
			GroupInfo curInfo = getGroup(groupid);
			GroupInfo pInfo = getGroup(curInfo.grouppid);
			if (pInfo != null){
				addGroupMap(pInfo.groupid, info);
			}
		}
		@Override
		public synchronized void initGroup(String groupId) throws Exception {
			GroupInfo info = queryGroupInfo(groupId);
			if (info != null) {
				groups.put(info.groupid, info);
				for (Map<String, GroupInfo> map : userSimpleMap.values()) {
					if (map.containsKey(info.groupid))
						map.put(info.groupid, info);
				}

				addGroupMap(info.groupid, info);
				
				for (Map<String, GroupInfo> map : userMap.values()) {
					if (map.containsKey(info.groupid))
						map.put(info.groupid, info);
				}

			}
		}

		@Override
		public void init() throws Exception {
			groups.clear();
			groupMap.clear();
			userMap.clear();
			userSimpleMap.clear();

			Map<String, GroupInfo> infos = queryGroupInfos(null, false);
			if (infos == null || infos.size() == 0)
				return;
			
			Map<String, List<String>> groupUsers = queryGroupUsers();
			for (String groupid: groupUsers.keySet()) {
				if (!infos.containsKey(groupid))
					continue;
				
				for (String userid : groupUsers.get(groupid)) {
					infos.get(groupid).simpleUsers.put(userid, userid);					
				}
			}
			groupMap = getTreeGroups(infos);

			for (GroupInfo groupInfo : infos.values()) {
				if (groupMap.containsKey(groupInfo.groupid)) {
					for (GroupInfo gInfo : groupMap.get(groupInfo.groupid).values()) {
						if (groupUsers.containsKey(gInfo.groupid)) {
							List<String> users = groupUsers.get(gInfo.groupid);
							if (users == null)
								continue;
							for (String userid : users) {
								groupInfo.users.put(userid, userid);
								Map<String, GroupInfo> userGroups;
								if (!userMap.containsKey(userid)) {
									userGroups = new HashMap<>();
									userMap.put(userid, userGroups);
								} else {
									userGroups = userMap.get(userid);
								}
								userGroups.put(gInfo.groupid, gInfo);
							}
						}
					}
				}

				groups.put(groupInfo.groupid, groupInfo);

			}

			for (String groupid : groupUsers.keySet()) {
				if (!groups.containsKey(groupid))
					continue;

				GroupInfo groupInfo = groups.get(groupid);
				for (String userid : groupUsers.get(groupid)) {
					Map<String, GroupInfo> gs;
					if (userSimpleMap.containsKey(userid)) {
						gs = userSimpleMap.get(userid);
					} else {
						gs = new HashMap<>();
						userSimpleMap.put(userid, gs);
					}
					gs.put(groupInfo.groupid, groupInfo);
				}
			}
		}

		public void initUser(String userid) throws Exception {
			List<String> userGroups = queryUserGroups(userid);

			Map<String, GroupInfo> all = new HashMap<>();
			Map<String, GroupInfo> simple = new HashMap<>();
			for (String groupId : userGroups) {
				for (GroupInfo groupInfo : getGroups(groupId)) {
					all.put(groupInfo.groupid, groupInfo);
				}
				simple.put(groupId, groups.get(groupId));
			}
			userMap.put(userid, all);
			userSimpleMap.put(userid, simple);
		}

		/**
		 * 获取用户所在的组，仅直接设置的组
		 * 
		 * @param userid
		 *            用户id
		 * @return 返回所有隶属的组列表
		 */
		@Override
		public Map<String, GroupInfo> getUserGroups(String userid) {
			return userSimpleMap.containsKey(userid) ? userSimpleMap.get(userid) : new HashMap<>();
		}

		/**
		 * 获取用户所在的组，包括直接设置及其下级组
		 * 
		 * @param userid
		 *            用户id
		 * @return 返回所有隶属的组列表
		 */
		@Override
		public Map<String, GroupInfo> getUserGroupTree(String userid) {
			return userMap.containsKey(userid) ? userMap.get(userid) : new HashMap<>();
		}

		/**
		 * 获取组id对应的组信息
		 * 
		 * @param groupId
		 *            组id
		 * @return 返回所有隶属的组列表
		 */
		@Override
		public GroupInfo getGroup(String groupId) {
			if (groupId == null || groupId.isEmpty())
				return null;
			
			return groups.get(groupId);
		}

		/**
		 * 获取组的下级组信息列表，包括组id指定的组本身
		 * 
		 * @param group
		 *            组id
		 * @return 返回组列表
		 */
		@Override
		public List<GroupInfo> getGroups(String groupId) {
			if (!groupMap.containsKey(groupId)) {
				return new ArrayList<>();
			}

			List<GroupInfo> result = new ArrayList<>();
			getGroups(groups.get(groupId), result);
			return result;
		}

		protected void getGroups(GroupInfo root, Collection<GroupInfo> result) {
			if (!groupMap.containsKey(root.groupid))
				return;

			result.addAll(groupMap.get(root.groupid).values());
		}

		@Override
		protected Collection<GroupInfo> getSource() {
			return groups.values();
		}
	}

	public class FunRoles extends BaseInfos<FunRoleInfo> implements IFunRole {

		/**
		 * key为groupid，map的key为功能权限类型，value为所有权限信息列表
		 */
		Map<String, Map<FunRoleType, List<FunRoleInfo>>> groupRoleMap = new ConcurrentHashMap<>();
		/**
		 * key为roleid，value为功能权限信息
		 */
		Map<String, FunRoleInfo> roles = new ConcurrentHashMap<>();

		protected FunRoleType toFunRoleType(String typename) {
			switch (typename) {
			case "func":
				return FunRoleType.ftFun;
			case "view":
				return FunRoleType.ftUI;
			case "button":
				return FunRoleType.ftButton;
			case "menu":
				return FunRoleType.ftMenu;
			case "tree":
				return FunRoleType.ftTree;
			default:
				return FunRoleType.ftNone;
			}
		}

		@Override
		public void init() throws Exception {
			groupRoleMap.clear();
			roles.clear();

			List<FunRoleInfo> infos = queryFunRoleInfos();
			for (FunRoleInfo roleInfo : infos) {
				roles.put(roleInfo.roleid, roleInfo);
			}

			Map<String, List<String>> groupRoleIdMap = queryGroupFunRoleInfos();
			for (String groupid : groupRoleIdMap.keySet()) {
				Map<FunRoleType, List<FunRoleInfo>> map;
				if (!groupRoleMap.containsKey(groupid)) {
					map = new HashMap<>();
					groupRoleMap.put(groupid, map);
				} else
					map = groupRoleMap.get(groupid);

				List<String> roleids = groupRoleIdMap.get(groupid);
				for (String roleId : roleids) {
					if (!roles.containsKey(roleId))
						continue;

					FunRoleInfo roleInfo = roles.get(roleId);
					List<FunRoleInfo> mapList;
					FunRoleType rt = toFunRoleType(roleInfo.roletype);
					if (map.containsKey(rt)) {
						mapList = map.get(rt);
					} else {
						mapList = new ArrayList<>();
						map.put(rt, mapList);
					}
					mapList.add(roleInfo);
				}
			}
		}

		public synchronized void initGroupRole(String groupid) throws Exception {
			List<String> roleids = queryGroupFunRoleInfo(groupid);
			Map<FunRoleType, List<FunRoleInfo>> map;
			map = new HashMap<>();
			groupRoleMap.put(groupid, map);

			for (String roleId : roleids) {
				if (!roles.containsKey(roleId))
					continue;

				FunRoleInfo roleInfo = roles.get(roleId);
				List<FunRoleInfo> mapList;
				FunRoleType rt = toFunRoleType(roleInfo.roletype);
				if (map.containsKey(rt)) {
					mapList = map.get(rt);
				} else {
					mapList = new ArrayList<>();
					map.put(rt, mapList);
				}
				mapList.add(roleInfo);
			}
		}

		public void removeGroupRole(String groupid) throws Exception {
			groupRoleMap.remove(groupid);
		}

		/**
		 * 获取组的自身功能权限设置
		 * 
		 * @param groupId
		 *            组id
		 * @return 返回根据权限类型划分的功能权限列表
		 */
		@Override
		public Map<FunRoleType, Map<String, FunRoleInfo>> getSimpleRoles(String groupId) {
			List<GroupInfo> groupInfos = new ArrayList<>();
			GroupInfo groupInfo = groups.getGroup(groupId);
			if (groupInfo == null)
				return new HashMap<>();

			groupInfos.add(groupInfo);
			return getRoles(groupInfos);
		}

		/**
		 * 获取组的自身及其下级的功能权限设置
		 * 
		 * @param groupId
		 *            组id
		 * @return 返回根据权限类型划分的功能权限列表
		 */
		@Override
		public Map<FunRoleType, Map<String, FunRoleInfo>> getRoles(String groupId) {
			return getRoles(groups.getGroups(groupId));
		}

		/**
		 * 获取组列表中每个组的自身功能权限设置的集合
		 * 
		 * @param roleGroups
		 *            组id列表
		 * @return 返回根据权限类型划分的功能权限列表
		 */
		@Override
		public Map<FunRoleType, Map<String, FunRoleInfo>> getRoles(List<GroupInfo> roleGroups) {
			Map<FunRoleType, Map<String, FunRoleInfo>> result = new HashMap<>();
			for (GroupInfo groupInfo : roleGroups) {
				if (groupRoleMap.containsKey(groupInfo.groupid)) {
					Map<FunRoleType, List<FunRoleInfo>> map = groupRoleMap.get(groupInfo.groupid);
					for (FunRoleType funRoleType : map.keySet()) {
						Map<String, FunRoleInfo> roles;
						if (result.containsKey(funRoleType)) {
							roles = result.get(funRoleType);
						} else {
							roles = new HashMap<>();
							result.put(funRoleType, roles);
						}
						for (FunRoleInfo info : map.get(funRoleType)) {
							roles.put(info.roleid, info);
						}
					}
				}
			}
			return result;
		}

		/**
		 * 获取用户的功能权限
		 * 
		 * @param userid
		 *            用户id
		 * @param rt
		 *            查询的权限类型
		 * @return 返回此用户所有的功能权限列表
		 */
		@Override
		public Map<String, FunRoleInfo> getUserRoles(String userid, FunRoleType rt) {
			Collection<GroupInfo> groupInfos = groups.getUserGroupTree(userid).values();
			if (groupInfos == null || groupInfos.size() == 0)
				return new HashMap<>();

			Map<String, FunRoleInfo> result = new HashMap<>();
			for (GroupInfo groupInfo : groupInfos) {
				Map<FunRoleType, Map<String, FunRoleInfo>> map = getRoles(groupInfo.groupid);
				if (map.containsKey(rt)) {
					for (FunRoleInfo info : map.get(rt).values()) {
						result.put(info.roleid, info);
					}
				}
			}

			return result;
		}

		/**
		 * 检查一个用户是否可以访问某个功能
		 * 
		 * @param curUserId
		 *            当前需要请求数据访问的用户id
		 * @param operType
		 *            请求检查的访问类型
		 * @paam destId 请求访问功能的id
		 * @return 可以访问返回true，其他返回false
		 */
		@Override
		public boolean check(String curUserId, FunRoleType roleType, String destId) {
			Map<String, FunRoleInfo> map = getUserRoles(curUserId, roleType);
			if (map.containsKey(destId)) {
				return true;
			}

			return false;
		}

		@Override
		protected Collection<FunRoleInfo> getSource() {
			return roles.values();
		}
	}

	public class CustomDataRoles extends BaseInfos<CustomDataRoleInfo> implements ICustomDataRole {
		Map<String, CustomDataRoleInfo> roles = new ConcurrentHashMap<>();

		@Override
		public List<CustomDataRoleInfo> getRoles() {
			return new ArrayList<>(roles.values());
		}

		@Override
		public Map<String, CustomDataRoleInfo> getRoleMap() {
			return new HashMap<>(roles);
		}

		@Override
		public CustomDataRoleInfo getRole(String id) {
			if (roles.containsKey(id))
				return roles.get(id);
			else
				return null;
		}

		@Override
		public void initRole(String name) throws Exception {
			CustomDataRoleInfo info = queryCustomDataRoleInfo(name);
			if (info == null)
				throw new Exception("init customdata[" + name + "] failed!");
		}

		@Override
		public void init() {
			try {
				roles = queryCustomDataRoleInfos();
			} catch (Exception e) {
				e.printStackTrace();
				roles.clear();
			}
		}

		@Override
		protected Collection<CustomDataRoleInfo> getSource() {
			return roles.values();
		}

	}

	public class GroupCustomDataRoles extends BaseInfos<GroupCustomDataRoleInfo> implements IGroupCustomDataRole {

		/**
		 * key为自定义数据权限id
		 */
		Map<String, GroupCustomDataRoleInfo> roles = new ConcurrentHashMap<>();

		/**
		 * key为组id
		 */
		Map<String, Map<DataOperType, GroupCustomDataRoleInfo>> groupRoleMap = new ConcurrentHashMap<>();

		@Override
		protected Collection<GroupCustomDataRoleInfo> getSource() {
			return roles.values();
		}

		protected void putGroupMap(GroupCustomDataRoleInfo info) {
			Map<DataOperType, GroupCustomDataRoleInfo> map;
			if (groupRoleMap.containsKey(info.groupid)) {
				map = groupRoleMap.get(info.groupid);
			} else {
				map = new HashMap<>();
				groupRoleMap.put(info.groupid, map);
			}

			map.put(info.operType, info);
		}

		@Override
		public void initGroup(String groupid) throws Exception {
			if (groupRoleMap.containsKey(groupid)) {
				Collection<GroupCustomDataRoleInfo> infos = groupRoleMap.get(groupid).values();
				if (infos != null) {
					for (GroupCustomDataRoleInfo info : groupRoleMap.get(groupid).values()) {
						roles.remove(info.id);
					}
				}
				groupRoleMap.remove(groupid);
			}

			Collection<GroupCustomDataRoleInfo> infos = queryGroupCustomDataRoleInfo(groupid);
			if (infos == null)
				return;

			for (GroupCustomDataRoleInfo info : infos) {
				roles.put(info.id, info);
				putGroupMap(info);
			}
		}

		@Override
		public Map<DataOperType, GroupCustomDataRoleInfo> getRoleInfo(String groupid) throws Exception {
			return groupRoleMap.containsKey(groupid) ? groupRoleMap.get(groupid) : null;
		}

		@Override
		public void init() throws Exception {
			roles = queryGroupCustomDataRoleInfos();
			groupRoleMap.clear();
			for (GroupCustomDataRoleInfo info : roles.values()) {
				putGroupMap(info);
			}
		}

		@Override
		public Map<DataOperType, Map<String, GroupCustomDataRoleInfo>> getUserRole(String userid) throws Exception {
			Collection<GroupInfo> userGroups = groups.getUserGroupTree(userid).values();
			if (userGroups == null || userGroups.size() == 0)
				return new HashMap<>();

			Map<DataOperType, Map<String, GroupCustomDataRoleInfo>> result = new HashMap<>();
			for (GroupInfo groupInfo : userGroups) {
				Map<DataOperType, GroupCustomDataRoleInfo> map = getRoleInfo(groupInfo.groupid);
				if (map == null)
					continue;

				for (GroupCustomDataRoleInfo info : map.values()) {
					Map<String, GroupCustomDataRoleInfo> infoMap;
					if (result.containsKey(info.operType)) {
						infoMap = result.get(info.operType);
					} else {
						infoMap = new HashMap<>();
						result.put(info.operType, new HashMap<>());
					}

					infoMap.put(info.groupid, info);
				}
			}
			return result;
		}
	}

	public class DataRoles extends BaseInfos<DataRoleInfo> implements IDataRole {
		/**
		 * key为roleid
		 */
		Map<String, DataRoleInfo> roles = new ConcurrentHashMap<>();
		/**
		 * 这个对象仅当roleid指定的DataRoleInfo信息的roletype为groups类型时有效
		 * key为roleid，map为归属于此roleid的所有组id，map的key及value都为组id
		 */
		Map<String, Map<String, String>> roleGroupMap = new ConcurrentHashMap<>();

		/**
		 * key为groupid，Map的key为opertype的值，value为roleid
		 */
		Map<String, Map<String, String>> groupRoleMap = new ConcurrentHashMap<>();

		public void removeGroupDataRole(String groupid) throws Exception {
			groupRoleMap.remove(groupid);
			for (Map<String, String> map : groupRoleMap.values()) {
				for (String roleId : map.values()) {
					if (!roleGroupMap.containsKey(roleId))
						continue;
					roleGroupMap.get(roleId).remove(groupid);

				}
			}
		}

		protected void initRole(DataRoleInfo roleInfo) {
			Map<String, String> map;
			if (roleGroupMap.containsKey(roleInfo.id)) {
				roleInfo.groups = roleGroupMap.get(roleInfo.id).values();
			}
			if (groupRoleMap.containsKey(roleInfo.groupid)) {
				map = groupRoleMap.get(roleInfo.groupid);
			} else {
				map = new HashMap<>();
				groupRoleMap.put(roleInfo.groupid, map);
			}
			map.put(roleInfo.opertype, roleInfo.id);
			roles.put(roleInfo.id, roleInfo);

		}

		@Override
		public void init() throws Exception {
			roles.clear();
			roleGroupMap = queryDataRoleGroups();

			List<DataRoleInfo> infos = queryDataRoleInfos();
			for (DataRoleInfo roleInfo : infos) {
				initRole(roleInfo);
			}
		}

		@Override
		public void initGroup(String groupId) throws Exception {
			if (groupRoleMap.containsKey(groupId)) {
				Map<String, String> groupRoles = groupRoleMap.remove(groupId);
				for (String roleid : groupRoles.values()) {
					if (roleGroupMap.containsKey(roleid)) {
						roleGroupMap.remove(roleid);
					}

					if (roles.containsKey(roleid)) {
						roles.remove(roleid);
					}
				}
			}

			Map<String, DataRoleInfo> groupRoles = queryDataRoleInfo(groupId);
			Map<String, Map<String, String>> roleGroups = queryDataRoleGroup(groupId);

			if (roleGroups != null && roleGroups.size() > 0)
				for (String roleid : roleGroups.keySet()) {
					roleGroupMap.put(roleid, roleGroups.get(roleid));
				}

			for (DataRoleInfo info : groupRoles.values()) {
				initRole(info);
			}
		}

		/**
		 * 检查指定roleid下是否存在groupid指定的组权限，仅用于roleid表示的功能权限的roletype为groups类型
		 * 
		 * @param roleid
		 *            功能权限id
		 * @param groupid
		 *            检查的组id
		 * @return 如果此groupid包含在roleid指定的权限的组集合下面，返回true，其他返回false
		 */
		@Override
		public boolean check(String roleid, String groupId) {
			return roleGroupMap.containsKey(roleid) ? roleGroupMap.get(roleid).containsKey(groupId) : false;
		}

		/**
		 * 获取指定组的数据访问权限设置
		 * 
		 * @param groupId
		 *            组id
		 * @return 返回此组的数据权限列表，根据操作类型分别存储（query、update）
		 */
		@Override
		public Map<String, DataRoleInfo> getRole(String groupId) {
			Map<String, DataRoleInfo> result = new HashMap<>();
			if (groupRoleMap.containsKey(groupId)) {
				Map<String, String> map = groupRoleMap.get(groupId);
				for (String roleid : map.values()) {
					if (roles.containsKey(roleid)) {
						DataRoleInfo info = roles.get(roleid);
						result.put(info.opertype, info);
					}
				}
			}
			return result;
		}

		/**
		 * 检查一个用户是否可以访问另一个用户的数据
		 * 
		 * @param curUserId
		 *            当前需要请求数据访问的用户id
		 * @param operType
		 *            请求检查的访问类型
		 * @paam destUserId 请求访问数据的所有者id
		 * @return 可以访问返回true，其他返回false
		 */
		@Override
		public boolean check(String curUserId, DataOperType operType, String destUserId) {
			Map<DataOperType, Map<String, UserInfo>> map = getUserRole(curUserId);
			if (map.containsKey(operType)) {
				if (map.get(operType).containsKey(destUserId))
					return true;
			}

			return false;
		}

		/**
		 * 获取指定用户可以访问那些用户数据，按照权限类型不同，表示此用户可以访问那些用户的数据
		 * 
		 * @param userid
		 *            用户id
		 * @return 返回此用户的所有数据权限列表，DataRoleType为操作类型（query、update），value的Map的key为用户id，value为用户信息对象
		 */
		@Override
		public Map<DataOperType, Map<String, UserInfo>> getUserRole(String userid) {
			Map<DataOperType, Map<String, UserInfo>> result = new HashMap<>();
			if (users.getUser(userid) == null)
				return result;

			Collection<GroupInfo> userSimpleGroups = groups.getUserGroups(userid).values();
			if (userSimpleGroups == null || userSimpleGroups.size() == 0)
				return result;

			Collection<GroupInfo> userGroups = groups.getUserGroupTree(userid).values();
			if (userGroups == null || userGroups.size() == 0)
				return result;

			for (GroupInfo groupInfo : userGroups) {
				Map<String, String> map = groupRoleMap.get(groupInfo.groupid);
				if (map == null)
					continue;

				for (String rid : map.values()) {
					if (!roles.containsKey(rid))
						continue;
					DataRoleInfo roleInfo = roles.get(rid);
					Map<String, UserInfo> roleUsers = new HashMap<>();
					roleUsers.put(userid, users.getUser(userid));
					switch (roleInfo.roletype) {
					case "self":
						break;
					case "group":
						for (GroupInfo gInfo : userSimpleGroups) {
							for (String uid : gInfo.users.values()) {
								roleUsers.put(uid, users.getUser(uid));
							}
						}
						break;
					case "groups":
						if (roleGroupMap.containsKey(roleInfo.id)) {
							Collection<String> roleGroups = roleGroupMap.get(roleInfo.id).values();
							for (String gid : roleGroups) {
								List<GroupInfo> gInfos = groups.getGroups(gid);
								if (gInfos == null || gInfos.size() == 0)
									continue;

								for (GroupInfo gInfo : gInfos) {
									for (String uid : gInfo.users.values()) {
										roleUsers.put(uid, users.getUser(uid));
									}
								}
							}
						}
						break;
					}

					DataOperType rType = DataOperType.dtQuery;
					switch (roleInfo.opertype) {
					case "query":
						rType = DataOperType.dtQuery;
						break;
					case "update":
						rType = DataOperType.dtUpdate;
						break;
					}

					Map<String, UserInfo> resultUsers;
					if (result.containsKey(rType)) {
						resultUsers = result.get(rType);
					} else {
						resultUsers = new HashMap<>();
						result.put(rType, resultUsers);
					}
					for (UserInfo info : roleUsers.values()) {
						resultUsers.put(info.userid, info);
					}
				}
			}

			return result;
		}

		/**
		 * 获取指定组及其下属组的数据权限信息
		 * 
		 * @param userid
		 *            用户id
		 * @return 返回此组的所有数据权限列表，DataRoleType为操作类型（query、update），value的Map的key为用户id，value为数据权限对象
		 */
		@Override
		public Map<String, Map<String, DataRoleInfo>> getRoles(String groupid) {
			if (groups.getGroup(groupid) == null)
				return new HashMap<>();

			Collection<GroupInfo> userGroups = groups.getGroups(groupid);
			if (userGroups == null || userGroups.size() == 0)
				return new HashMap<>();

			Map<String, Map<String, DataRoleInfo>> result = new HashMap<>();
			for (GroupInfo groupInfo : userGroups) {
				Map<String, DataRoleInfo> map = getRole(groupInfo.groupid);
				for (String operType : map.keySet()) {
					Map<String, DataRoleInfo> infos;
					if (result.containsKey(operType)) {
						infos = result.get(operType);
					} else {
						infos = new HashMap<>();
						result.put(operType, infos);
					}
					DataRoleInfo info = map.get(operType);
					infos.put(info.id, info);
				}
			}

			return result;
		}

		@Override
		protected Collection<DataRoleInfo> getSource() {
			return roles.values();
		}
	}

	/**
	 * 返回所有用户信息
	 * 
	 * @return 返回用户信息对象列表
	 */
	protected abstract List<UserInfo> queryUserInfos() throws Exception;

	/**
	 * 返回用户信息
	 * 
	 * @param userid
	 *            要查询的用户id
	 * @return 返回用户信息对象列表
	 */
	protected UserInfo queryUserInfo(String userid) throws Exception {
		return null;
	}

	/**
	 * 返回所有组信息
	 * 
	 * @param grouptypes
	 *            要获取的权限类型列表，如果为null则忽略include参数，并查询所有信息
	 * @param include
	 *            true，表示查询的是包含grouptypes的数据，否则为不包含grouptypes的数据
	 * @return 返回用户组对象列表
	 */
	protected abstract Map<String, GroupInfo> queryGroupInfos(String[] grouptypes, boolean include) throws Exception;

	/**
	 * 返回组信息
	 * 
	 * @param grouptypes
	 *            要获取的权限类型列表，如果为null则忽略include参数，并查询所有信息
	 * @param include
	 *            true，表示查询的是包含grouptypes的数据，否则为不包含grouptypes的数据
	 * @return 返回用户组对象列表
	 */
	protected GroupInfo queryGroupInfo(String groupId) throws Exception {
		return null;
	}

	/**
	 * 返回所有数据权限信息
	 * 
	 * @return 返回数据权限对象列表
	 */
	protected abstract List<DataRoleInfo> queryDataRoleInfos() throws Exception;

	protected Map<String, DataRoleInfo> queryDataRoleInfo(String groupId) throws Exception {
		return null;
	}

	/**
	 * 返回数据权限类型为groups的数据权限包含的所有组id信息
	 * 
	 * @return 返回所有包含的组id列表，key为数据权限id，map的key、value都为归属于此数据权限的组id
	 */
	protected abstract Map<String, Map<String, String>> queryDataRoleGroups() throws Exception;

	protected Map<String, Map<String, String>> queryDataRoleGroup(String groupId) throws Exception {
		return null;
	}

	/**
	 * 返回所有功能权限信息
	 * 
	 * @return 返回功能权限对象列表
	 */
	protected abstract List<FunRoleInfo> queryFunRoleInfos() throws Exception;

	/**
	 * 返回所有组的所有功能权限信息
	 * 
	 * @return 返回组id与权限id列表，key为组id，list为归属于此组的所有功能权限id列表
	 */
	protected abstract Map<String, List<String>> queryGroupFunRoleInfos() throws Exception;

	/**
	 * 返回组的所有功能权限信息
	 * 
	 * @return 返回归属于此组的所有功能权限id列表
	 */
	protected List<String> queryGroupFunRoleInfo(String groupId) throws Exception {
		return null;
	}

	/**
	 * 返回所有组用户信息
	 * 
	 * @return 返回组用户信息列表，key为组id，list为归属于此组的所有用户id
	 * @throws Exception
	 */
	protected abstract Map<String, List<String>> queryGroupUsers() throws Exception;

	protected List<String> queryUserGroups(String userid) throws Exception {
		return null;
	}

	protected Groups groups = new Groups();
	protected Users users = new Users();
	protected FunRoles funRoles = new FunRoles();
	protected DataRoles dataRoles = new DataRoles();
	protected GroupCustomDataRoles groupCustomDataRoles = new GroupCustomDataRoles();
	protected CustomDataRoles customDataRoles = new CustomDataRoles();

	@Override
	public void init() throws Exception {
		users.init();
		groups.init();
		funRoles.init();
		dataRoles.init();
		customDataRoles.init();
		groupCustomDataRoles.init();
	}

	@Override
	public void onlyInitGroup(String[] grouptypes, boolean include) throws Exception {
		groups.onlyInitGroup(grouptypes, include);
	}

	@Override
	public IGroup getGroups() {
		return groups;
	}

	@Override
	public IUser getUsers() {
		return users;
	}

	@Override
	public IFunRole getFunRoles() {
		return funRoles;
	}

	@Override
	public IDataRole getDataRoles() {
		return dataRoles;
	}

	@Override
	public ICustomDataRole getCustomDataRoles() {
		return customDataRoles;
	}

	@Override
	public IGroupCustomDataRole getGroupCustomDataRoles() {
		return groupCustomDataRoles;
	}

	protected abstract List<GroupCustomDataRoleInfo> queryGroupCustomDataRoleInfo(String groupid) throws Exception;

	/**
	 * key为id
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract Map<String, GroupCustomDataRoleInfo> queryGroupCustomDataRoleInfos() throws Exception;

	protected abstract CustomDataRoleInfo queryCustomDataRoleInfo(String id) throws Exception;

	/**
	 * key为id
	 * 
	 * @return
	 * @throws Exception
	 */
	protected abstract Map<String, CustomDataRoleInfo> queryCustomDataRoleInfos() throws Exception;
}
