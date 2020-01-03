package wh.role.interfaces;

import wh.role.obj.UserInfo;

public interface IUser{

	void initUser(String userid) throws Exception;
	void init() throws Exception;
	/**
	 * 获取用户信息
	 * @param userId 用户id
	 * @return 返回用户信息
	 * */
	UserInfo getUser(String userId);

}