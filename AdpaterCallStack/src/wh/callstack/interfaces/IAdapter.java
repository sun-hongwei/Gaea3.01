package wh.callstack.interfaces;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IAdapter {
	
	/**
	 * 默认错误返回信息
	 * @return
	 */
	public static JSONObject ERROR_RESULT() {
		JSONObject data = new JSONObject();
		data.put("ret", -1);
		return data;
	}

	/**
	 * pending回调事件
	 * @author hhcwy
	 *
	 */
	public interface IPendingCallback{
		/**
		 * 当pending任务完成回调此函数
		 * @param taskId execute执行时传入的任务id
		 * @param result 任务的完成数据
		 */
		void callback(String taskId, JSONObject result);
	}
	
	public interface IResultNotify{
		void callback(TaskInfo taskInfo, Map<String, Object> callStack);
	}
	
	public enum ExecuteState{
		esComplete, //当前适配器已经处理任务
		esPending, //当前适配器不能完成
		esPendingPart, //当前适配器不能完成，且需要向下传递任务
		esNotSupported, //当前适配器不支持任务
		esLazy, //当前适配器需要更多数据才可以执行
		esPart, //当前适配器已经处理此任务，但后续还有其他任务需要处理
	}
	
	public enum AdapterType{
		atPLC/*PLC采集设备*/, 
		atMachine/*工控或者自动化设备采集*/, 
		atSensor/*传感器或者数字量具采集*/, 
		atERP/*ERP接口*/, 
		atOA/*办公自动化接口*/, 
		atHR/*人力资源系统接口*/, 
		atDB/*以数据库形式提供数据接口*/, 
		atMES/*MES接口*/, 
		atOther/*其他系统接口*/, 
		atNone
	}
	
	/**
	 * 执行任务信息，调用execute方法传入，结果由action写入result
	 * @author hhcwy
	 *
	 */
	public static class TaskInfo{
		
		/**
		 * 是否跟踪此次任务的执行，开启表示每次会执行都会检查任务是否已经执行过，对于一次执行流程中已经完成的节点或者指令不会再次执行；true表示跟踪，其他不跟踪
		 */
		public boolean trace = true;
		/**
		 * 与此任务绑定的提供者，可以为null，不为null则表示此任务与此provider绑定，并有其发送
		 */
		public Object provider;
		
		/**
		 * 是否异步发送任务，仅对于网转类任务有效
		 */
		public boolean async = false;
		
		/**
		 * 指定本次任务要使用的目标设备，一般为配置文件的connection/host/设备id等值，如果为null则表示发送到所有设备执行
		 */
		public Object targe;
		
		/**
		 * 返回的结果信息
		 */
		public JSONObject result = new JSONObject();
		/**
		 * 任务id
		 */
		public String taskId = UUID.randomUUID().toString();
		
		/**
		 * 任务类别代码，用于确认任务归属，此对象仅可以为某一枚举类型的实例，不允许使用字符串或其他常量
		 * 每个adapter都应该检查此属性已确定自己可以处理此任务
		 */
		Map<Object, Object> taskCode = new HashMap<>();
		public Object[] orderTaskCode;
		
		/**
		 * 执行参数
		 */
		public JSONObject input = new JSONObject();
		
		public Object context;
		
		public AtomicBoolean cancel = new AtomicBoolean(false);
		public AtomicBoolean deviceCancel = new AtomicBoolean(false);
		
		/**
		 * 系统保留，用户不能设置
		 */
		public IPendingCallback pendingCallback;
		
		public boolean ordered = true;
		/**
		 * 用户设置的pending状态异步回调过程，如果设置则会在pending调用结束后调用；仅在pending调用方式有效
		 */
		public IResultNotify resultCallback;
		
		public TaskInfo(Object[] taskCode){
			orderTaskCode = Arrays.copyOf(taskCode, taskCode.length);
			for (Object code : taskCode) {
				this.taskCode.put(code, code);
			}
		}
		
		public boolean checkTaskCode(Object taskCode){
			return this.taskCode.containsKey(taskCode);
		}
		
		/**
		 * 当用户调用成功完成，需要调用此方法以传递处理结果，如果未调用此方法，则用户设置的resultCallback将不会触发
		 * @param result
		 */
		public void notifyPending(JSONObject result){
			if (pendingCallback != null)
				pendingCallback.callback(taskId, result);
		}
		
		/**
		 * 将任务信息部分序列化，并不是整个任务信息序列化
		 * 序列化的项目：result、input、context、taskId、targe
		 * @return
		 */
		public JSONObject toJson() {
			JSONObject json = new JSONObject();
			json.put("result", result);
			json.put("input", input);
			json.put("context", context);
			json.put("taskId", taskId);
			if (targe != null) {
				if (targe.getClass().isArray()) {
					JSONArray targeArray = new JSONArray();
					for (int i = 0; i < Array.getLength(targe); i++) {
						targeArray.put(Array.get(targe, i));
					}
					json.put("targe", targeArray);
				}else {
					json.put("targe", targe);					
				}
			}
			return json;
		}

		/**
		 * 从json数据中建立TaskInfo对象，参考toJson方法
		 * @param json 包含任务数据的json对象
		 * @return
		 */
		public static TaskInfo fromJson(JSONObject json) {
			TaskInfo info = new TaskInfo(new Object[] {"*"});
			if (json.has("result"))
				info.result = json.getJSONObject("result");
			if (json.has("input"))
				info.input = json.getJSONObject("input");
			if (json.has("context"))
				info.context = json.get("context");
			if (json.has("taskId"))
				info.taskId = json.getString("taskId");
			if (json.has("targe")) {
				info.targe = json.get("targe");
				if (info.targe instanceof JSONArray) {
					JSONArray targeArray = (JSONArray) info.targe;
					Object[] datas = new Object[targeArray.length()];
					for (int i = 0; i < datas.length; i++) {
						datas[i] = targeArray.get(i);
					}
					info.targe = datas;
				}
				
			}
			
			return info;
		}
	}
	
	/**
	 * 获取此适配器的类型
	 * @return 适配器类型
	 */
	default AdapterType getAdapterType(){
		return AdapterType.atMES;
	}
	
	/**
	 * 获取此适配器的版本信息
	 * @return 版本信息，每个适配器自己确定
	 */
	default String getVersion(){
		return "1.0";
	}
	
	
	/**
	 * 获取此适配器的唯一ID
	 * @return 适配器id
	 */
	default String getId(){
		return UUID.randomUUID().toString();
	}
	
	/**
	 * 同步执行适配器，并返回结果
	 * @param taskInfo 执行的任务信息
	 * @param uncaptureCallback TODO
	 * @return 如果当前适配器已经处理此任务的结果
	 */
	@SuppressWarnings("rawtypes")
	<A, T> ExecuteState execute(TaskInfo taskInfo, Map<String, Object> callStack, IContext context) throws Throwable;

	@SuppressWarnings("rawtypes")
	default <A, T> ExecuteState frame_execute(TaskInfo taskInfo, Map<String, Object> callStack, IContext context) throws Throwable{
		if (!checkTask(taskInfo))
			return ExecuteState.esNotSupported;
		
		return execute(taskInfo, callStack, context);
	}
	
	Object[] getCode();

	default boolean checkCode(Object taskCode) {
		for (Object code: getCode()) {
			if (taskCode.getClass().equals(code.getClass())){
				if (taskCode == code)
					return true;
			}
		}
		return false;
	}
	
	default boolean checkTask(TaskInfo taskInfo) {
		for (Object code: getCode()) {
			if (taskInfo.checkTaskCode(code))
				return true;
		}
		return false;
	}

}
