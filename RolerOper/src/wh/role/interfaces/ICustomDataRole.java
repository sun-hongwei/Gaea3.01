package wh.role.interfaces;

import java.util.List;
import java.util.Map;

import wh.role.obj.CustomDataRoleInfo;

public interface ICustomDataRole {

	List<CustomDataRoleInfo> getRoles();

	CustomDataRoleInfo getRole(String id);

	void initRole(String name) throws Exception;

	void init();

	Map<String, CustomDataRoleInfo> getRoleMap();

}