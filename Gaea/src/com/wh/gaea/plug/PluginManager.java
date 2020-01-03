package com.wh.gaea.plug;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JMenu;

import com.wh.gaea.GlobalInstance;
import com.wh.gaea.interfaces.selector.IDataSourceSelector;
import com.wh.gaea.interfaces.selector.IExcelSelector;
import com.wh.gaea.interfaces.selector.IRoleSelector;
import com.wh.gaea.interfaces.selector.IWorkflowSelector;
import com.wh.gaea.plugin.IGaeaDBPlugin;
import com.wh.gaea.plugin.IGaeaPlugin;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.FileHelp;

public class PluginManager {
	Map<IGaeaPlugin, File> plugins = new ConcurrentHashMap<>();

	IGaeaDBPlugin dbPlugin;

	public static URLClassLoader getExtendClassLoader(File dir) throws Exception {
		List<URL> urls = new ArrayList<URL>();
		for (File file : dir.listFiles()) {
			String name = file.getName();
			if (name.equalsIgnoreCase(".") || name.equalsIgnoreCase(".."))
				continue;

			if (file.isDirectory())
				continue;

			if (!file.getName().trim().toLowerCase().endsWith("jar"))
				continue;

			// loadAdapterJar(file);
			urls.add(file.toURI().toURL());
		}

		return new URLClassLoader(urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());

	}

	public void load(JMenu root) throws Exception {
		File path = FileHelp.GetPath("plugins");

		URLClassLoader urlClassLoader = getExtendClassLoader(path);
		File[] plugFiles = path.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isFile() && FileHelp.GetExt(pathname.getName()).equalsIgnoreCase("jar");
			}
		});

		if (plugFiles == null || plugFiles.length == 0)
			return;

		TreeMap<Integer, IGaeaPlugin> pluginSortMap = new TreeMap<>();
		for (File file : plugFiles) {
			String name = file.getName();
			int index = name.lastIndexOf("-");
			if (index != -1)
				name = name.substring(0, index);
			IGaeaPlugin plugin = (IGaeaPlugin) urlClassLoader.loadClass("com.wh.gaea.plugin." + name).newInstance();
			plugins.put(plugin, file);

			switch (plugin.getType()) {
			case ptDataSource:
				if (plugin instanceof IDataSourceSelector) {
					GlobalInstance.instance().setDataSourceSelector((IDataSourceSelector) plugin);
				}
				break;
			case ptDb:
				if (plugin instanceof IGaeaDBPlugin) {
					dbPlugin = (IGaeaDBPlugin) plugin;
				}
				break;
			case ptExcel:
				if (plugin instanceof IExcelSelector) {
					GlobalInstance.instance().setExcelSelector((IExcelSelector) plugin);
				}
				break;
			case ptRole:
				if (plugin instanceof IRoleSelector) {
					GlobalInstance.instance().setRoleSelector((IRoleSelector) plugin);
				}
				break;
			case ptWorkflow:
				if (plugin instanceof IWorkflowSelector) {
					GlobalInstance.instance().setWorkflowSelector((IWorkflowSelector) plugin);
				}
				break;
			case ptFunction:
			default:
				break;
			}

			int sort = plugin.getLoadOrder();
			if (pluginSortMap.containsKey(sort))
				sort = pluginSortMap.lastKey() + 1;
			pluginSortMap.put(sort, plugin);
		}
		
		for (IGaeaPlugin plugin : pluginSortMap.values()) {
			plugin.setMenu(root);			
		}
	}

	public void setDb() {
		if (dbPlugin != null)
			GlobalInstance.instance().getMainControl().setDB(dbPlugin.getDB());
	}

	static PluginManager pluginManager = new PluginManager();

	public static void init(JMenu root) {
		try {
			pluginManager.load(root);
		} catch (Exception e) {
			e.printStackTrace();
			MsgHelper.showException(e);
		}
	}
	
	public static void reset() {
		pluginManager.setDb();
		for (IGaeaPlugin gaeaPlugin : pluginManager.plugins.keySet()) {
			gaeaPlugin.reset();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getPlugin(Class<T> t){
		for (IGaeaPlugin plugin : pluginManager.plugins.keySet()) {
			if (t == plugin.getClass())
				return (T) plugin;
		}
		
		return null;
	}
	
}
