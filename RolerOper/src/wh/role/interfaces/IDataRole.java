package wh.role.interfaces;

import java.util.Map;

import wh.role.obj.DataRoleInfo;
import wh.role.obj.UserInfo;
import wh.role.obj.RoleServiceObject.DataOperType;

public interface IDataRole {

	void init() throws Exception;

	/**
	 * 检查指定roleid下是否存在groupid指定的组权限，仅用于roleid表示的功能权限的roletype为groups类型
	 * @param roleid 功能权限id
	 * @param groupid 检查的组id
	 * @return 如果此groupid包含在roleid指定的权限的组集合下面，返回true，其他返回false
	 * */
	boolean check(String roleid, String groupId);

	/**
	 * 获取指定组的数据访问权限设置
	 * @param groupId 组id
	 * @return 返回此组的数据权限列表，根据操作类型分别存储（query、update）
	 * */
	Map<String, DataRoleInfo> getRole(String groupId);

	/**
	 * 检查一个用户是否可以访问另一个用户的数据
	 * @param curUserId 当前需要请求数据访问的用户id
	 * @param operType 请求检查的访问类型
	 * @paam destUserId 请求访问数据的所有者id
	 * @return 可以访问返回true，其他返回false
	 * */
	boolean check(String curUserId, DataOperType operType, String destUserId);

	/**
	 * 获取指定用户可以访问那些用户数据，按照权限类型不同，表示此用户可以访问那些用户的数据
	 * @param userid 用户id
	 * @return 返回此用户的所有数据权限列表，DataRoleType为操作类型（query、update），value的Map的key为用户id，value为用户信息对象
	 * */
	Map<DataOperType, Map<String, UserInfo>> getUserRole(String userid);

	/**
	 * 获取指定组及其下属组的数据权限信息
	 * @param userid 用户id
	 * @return 返回此组的所有数据权限列表，DataRoleType为操作类型（query、update），value的Map的key为用户id，value为数据权限对象
	 * */
	Map<String, Map<String, DataRoleInfo>> getRoles(String groupid);

	void initGroup(String groupId) throws Exception;

}