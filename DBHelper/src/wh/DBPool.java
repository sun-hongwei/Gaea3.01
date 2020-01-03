package wh;

import java.util.List;
import java.util.Vector;

import org.json.JSONObject;

import wh.interfaces.IDBConnection;
import wh.interfaces.IDBConnection.DBConnectionInfo;

/**
 * @author hhcwy
 *
 */
public class DBPool {
	List<IDBConnection> dbs = new Vector<>();
	
	JSONObject dbConn;
	public DBPool(JSONObject connectionInfo){
		this.dbConn = connectionInfo;
	}
	
    protected IDBConnection initDB() {
    	IDBConnection db = new DBConnection();
		try {			
			String host = dbConn.getString("host");
			String port = dbConn.getString("port");
			String dbname = dbConn.getString("dbname");
			String user = dbConn.getString("user");
			String pwd = dbConn.getString("pwd");
			DBConnectionInfo connectionInfo = IDBConnection.getMSSQLServerConnectionString(host, port, dbname, user, pwd);
			db.open(connectionInfo);
			return db;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
    
	public IDBConnection lock(){
		synchronized (this) {
			if (dbs.size() > 0){
				return dbs.remove(0);
			}else
				return initDB();
		}
	}
	
	public void free(IDBConnection db){
		synchronized (this) {
			dbs.add(db);
		}
	}
}
