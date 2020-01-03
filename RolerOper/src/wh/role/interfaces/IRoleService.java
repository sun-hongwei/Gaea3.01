package wh.role.interfaces;

public interface IRoleService{

	void init() throws Exception;

	IGroup getGroups();

	IUser getUsers();

	IFunRole getFunRoles();

	IDataRole getDataRoles();
	
	IGroupCustomDataRole getGroupCustomDataRoles();

	ICustomDataRole getCustomDataRoles();

	/**
	 * 仅仅初始化组信息，并不初始其他信息，包括用户，权限信息，这时仅为主数据系统初始数据
	 * @param grouptypes 要初始的组类型，不应该包括“role”类型
	 * @param include grouptypes的类型是包括还是不包括，true是包括，其他不包括
	 * @throws Exception
	 */
	void onlyInitGroup(String[] grouptypes, boolean include) throws Exception;

}