package wh.excel.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import wh.excel.template.CommandRuntime.Command;

@SuppressWarnings("rawtypes")
public class Config {
	public enum YType{
		ytValue, ytStartY, ytLoop, ytCommand, ytRef
	}
	
	public Template template;

	public ExprType exprType = ExprType.ttExpr;
	public int startX = 0;
	public int startY = -1;
	public Object expr;
	public String id = "config1";
	public Class<?> valueType = String.class;
	public String format;
	public int precision = 2;
	public int row = 0;
	public LoopType loopType = LoopType.ltOne;
	public String ref;
	public String datasetId;
	public String templateId;
	public boolean masterLoop = true;
	public boolean notNull = false;
	public String uniqueGroup;
	
	public boolean executed = false;
	
	public YType yType = YType.ytValue;
	public String ycommand;

	public List<Command> commands = new ArrayList<>();
	public HashMap<String, Command> replaceCommands = new HashMap<>();
	
	public enum LoopType {
		ltOne, ltLoop
	}

	public enum ExprType {
		ttConst, ttKey, ttExpr
	}

	public static class EntryInfo{
		public String replaceKey;
		public String dataKey;
		public Object row;
		
		public EntryInfo(String replaceKey, String dataKey, Object row){
			this.replaceKey = replaceKey;
			this.dataKey = dataKey;
			this.row = row;
		}
	}
	
	public Config(Template template){
		this.template = template;
	}
	
	public Integer getRowIndex(){
		if (ref == null && ref.isEmpty()) {
			return (Integer) startY;
		} else {
			Config next = template.get(ref);
			if (next == null)
				throw new RuntimeException(new NullPointerException("ref[" + ref + "] not found!"));
			
			return next.endRuntimeCellRow;
		}
	}
	
	public Map<String, EntryInfo> localExprs = new HashMap<>();
	public Map<String, EntryInfo> remoteExprs = new HashMap<>();
	public Map<String, EntryInfo> localAtExprs = new HashMap<>();
	public Map<String, Integer> remoteDatasetMap = new HashMap<>();

	public int startRuntimeCellRow;
	public int endRuntimeCellRow;
	
	@Override
	public String toString() {
		return id;
	}
}

