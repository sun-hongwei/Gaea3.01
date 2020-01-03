package wh.interfaces;

import java.lang.reflect.Constructor;

import wh.interfaces.IDBConnection.DBConnectionInfo;

public interface IConnectionFactory {
	public static IDBConnection getConnection(DBConnectionInfo connectionInfo) throws Exception{
		Class<?> connectionClass = Class.forName("wh.DBConnection");
		Constructor<?> constructor = connectionClass.getDeclaredConstructor();
		constructor.setAccessible(true);
		IDBConnection dbconnection = (IDBConnection)constructor.newInstance();
		dbconnection.open(connectionInfo);
		return dbconnection;
	}
}
