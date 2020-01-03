package wh.role.interfaces;

import java.util.Map;

import wh.role.obj.GroupCustomDataRoleInfo;
import wh.role.obj.RoleServiceObject.DataOperType;

public interface IGroupCustomDataRole {

	void initGroup(String groupid) throws Exception;

	Map<DataOperType, GroupCustomDataRoleInfo> getRoleInfo(String groupid) throws Exception;

	void init() throws Exception;

	Map<DataOperType, Map<String, GroupCustomDataRoleInfo>> getUserRole(String userid) throws Exception;

}