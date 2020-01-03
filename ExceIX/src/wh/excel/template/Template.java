package wh.excel.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Template<T extends Config> {
	public static final String DATA_MAP_NAME = "data_sheet";
	public static final String DATA_SHEET_START_Y = "start_y";
	public static final String DATA_SHEET_MASTER_DATASET = "master_dataset";
	public static final String DATA_SHEET_VAR_DEFINE = "vardefine";
	public static final String DATA_SHEET_TEMPLATE = "template";

	public int startY = 0;

	public String masterDatasetId;

	public CommandRuntime commandRuntime = new CommandRuntime();

	HashMap<String, Config> configMap = new HashMap<>();

	public HashMap<String, ConfigItemTemplate> configTemplates = new HashMap<>();
	public List<Config> configs = new ArrayList<>();

	public void add(Config config) {
		configMap.put(config.id, config);
		configs.add(config);
	}

	public void sort() {

	}

	public Config get(String id) {
		if (configMap.containsKey(id))
			return configMap.get(id);
		else {
			return null;
		}
	}

	public Config get(int index) {
		return configs.get(index);
	}

	public int count() {
		return configs.size();
	}

	public Iterable<Config> getIterable() {
		return configs;
	}

	public void clear() {
		configMap.clear();
		configs.clear();
		startY = 0;
	}

}
