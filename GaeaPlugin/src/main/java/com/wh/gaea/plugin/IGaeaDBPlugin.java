package com.wh.gaea.plugin;

import wh.interfaces.IDBConnection;

public interface IGaeaDBPlugin extends IGaeaPlugin{
	IDBConnection getDB();
}
