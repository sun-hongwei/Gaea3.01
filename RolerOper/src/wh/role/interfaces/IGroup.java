package wh.role.interfaces;

import java.util.List;
import java.util.Map;

import wh.role.obj.GroupInfo;

public interface IGroup{

	void init() throws Exception;

	/**
	 * 获取用户所在的组，仅直接设置的组
	 * @param userid 用户id
	 * @return 返回所有隶属的组列表
	 * */
	Map<String, GroupInfo> getUserGroups(String userid);

	/**
	 * 获取用户所在的组，包括直接设置及其下级组
	 * @param userid 用户id
	 * @return 返回所有隶属的组列表
	 * */
	Map<String, GroupInfo> getUserGroupTree(String userid);

	/**
	 * 获取组id对应的组信息
	 * @param groupId 组id
	 * @return 返回所有隶属的组列表
	 * */
	GroupInfo getGroup(String groupId);

	/**
	 * 获取组的下级组信息列表，包括组id指定的组本身
	 * @param group 组id
	 * @return 返回组列表
	 * */
	List<GroupInfo> getGroups(String groupId);

	void initGroup(String groupId) throws Exception;

}