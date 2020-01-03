package wh.role.interfaces;

import java.util.List;
import java.util.Map;

import wh.role.obj.FunRoleInfo;
import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.FunRoleType;

public interface IFunRole{

	void init() throws Exception;

	/**
	 * 获取组的自身功能权限设置
	 * @param groupId 组id
	 * @return 返回根据权限类型划分的功能权限列表
	 * */
	Map<FunRoleType, Map<String, FunRoleInfo>> getSimpleRoles(String groupId);

	/**
	 * 获取组的自身及其下级的功能权限设置
	 * @param groupId 组id
	 * @return 返回根据权限类型划分的功能权限列表
	 * */
	Map<FunRoleType, Map<String, FunRoleInfo>> getRoles(String groupId);

	/**
	 * 获取组列表中每个组的自身功能权限设置的集合
	 * @param roleGroups 组id列表
	 * @return 返回根据权限类型划分的功能权限列表
	 * */
	Map<FunRoleType, Map<String, FunRoleInfo>> getRoles(List<GroupInfo> roleGroups);

	/**
	 * 获取用户的功能权限
	 * @param userid 用户id
	 * @param rt 查询的权限类型
	 * @return 返回此用户所有的功能权限列表
	 * */
	Map<String, FunRoleInfo> getUserRoles(String userid, FunRoleType rt);

	/**
	 * 检查一个用户是否可以访问某个功能
	 * @param curUserId 当前需要请求数据访问的用户id
	 * @param operType 请求检查的访问类型
	 * @paam destId 请求访问功能的id
	 * @return 可以访问返回true，其他返回false
	 * */
	boolean check(String curUserId, FunRoleType roleType, String destId);

}