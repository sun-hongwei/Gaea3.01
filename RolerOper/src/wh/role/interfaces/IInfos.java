package wh.role.interfaces;

import wh.role.obj.RoleServiceObject.ITraverse;

public interface IInfos<T> {
	void traverse(ITraverse<T> traverse);
}