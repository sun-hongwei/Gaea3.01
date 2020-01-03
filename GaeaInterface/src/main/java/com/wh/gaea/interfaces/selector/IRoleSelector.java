package com.wh.gaea.interfaces.selector;

import com.wh.gaea.role.RoleInfos;

import wh.role.obj.GroupInfo;
import wh.role.obj.RoleServiceObject.ITraverse;

public interface IRoleSelector {
	RoleInfos selectRoles(RoleInfos initRoles);
	GroupInfo getGroupInfo(String group);
	void traverseGroups(ITraverse<GroupInfo> onTraverse);
	void initGroup(String[] typenames) throws Exception;
}
