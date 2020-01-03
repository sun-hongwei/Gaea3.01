package wh.excel.template;

import java.util.HashMap;

public class CommandRuntime {
	public enum CommandType{
		ctExpr, ctXY, ctInstru
	}
	
	public static class Command {
		public String replaceStr = null;
		public String command = "get";
		public String varName = null;
		public Object value = null;
		public CommandType commandType = CommandType.ctInstru;

		@Override
		public String toString() {
			return "${" + command + "(" + varName + ")}";
		}
	}

	public static class Var {
		public Object value = null;
		public Class<?> type = String.class;
		public String name = "var1";
		public String format = null;
		public int precision = 0;

		@Override
		public String toString() {
			return name;
		}
	}

	public HashMap<String, Var> vars = new HashMap<>();
}
