package wh.excel.model;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.script.ScriptEngineManager;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import wh.excel.template.Config;

public abstract class ExcelModel<T extends Config> {

	/**
	 * 将excel的宽度值转换为像素
	 * 
	 * @param px execl的宽度值
	 * @return 像素数量
	 */
	public static int getPOIWidthByExcelPiex(int px) {
		return (int) (px * Units.EMU_PER_PIXEL);
	}

	public static class RowInfo {
		public int startCellIndex = 0;
		public int lastCellIndex = 0;
		public HashMap<Integer, Object> values = new HashMap<>();
		public CellStyle rowStyle;
		public HashMap<Integer, CellType> cellTypes = new HashMap<>();
		public HashMap<Integer, CellStyle> cellStyles = new HashMap<>();
		public HashMap<Integer, XSSFComment> cellComments = new HashMap<>();
		public HashMap<Integer, XSSFHyperlink> cellLinks = new HashMap<>();
		public HashMap<Integer, String> cellFormulas = new HashMap<>();

		public CellRangeAddress region;
	}

	protected File excelFile;

	protected XSSFWorkbook curBook;

	protected XSSFSheet curSheet;

	protected XSSFRow curRow;

	protected XSSFCell curCell;

	public static int excelToNum(String col) { // "AAA"
		if (col == null)
			return -1;
		char[] chrs = col.toUpperCase().toCharArray(); // 转为大写字母组成的 char数组
		int length = chrs.length;
		int ret = -1;
		for (int i = 0; i < length; i++) {
			ret += (chrs[i] - 'A' + 1) * Math.pow(26, length - i - 1); // 当做26进制来算
																		// AAA=111
																		// 26^2+26^1+26^0
		}
		return ret;// 702; 从0开始的下标
	}

	/**
	 *      数字下标转列     
	 * 
	 * @param index     
	 * @return     
	 */
	public static String NumToExcel(int index) {
		int shang = 0;
		int yu = 0;
		List<Integer> list = new ArrayList<Integer>(); // 10进制转26进制 倒序
		while (true) {
			shang = index / 26;
			yu = index % 26;
			index = shang;
			list.add(yu);
			if (shang == 0)
				break;
		}
		StringBuilder sb = new StringBuilder();
		for (int j = list.size() - 1; j >= 0; j--) {
			// 倒序拼接序号转字符非末位序号减去1
			sb.append((char) (list.get(j) + 'A' - (j > 1 ? 1 : j)));
		}
		return sb.toString();
	}

	public XSSFSheet getSheet() {
		return curSheet;
	}

	boolean needDeleteLast = false;

	public boolean sheetExist(String name) {
		return curBook.getSheet(name) != null;
	}

	public void setSheet(String name) {
		curSheet = curBook.getSheet(name);
		if (curSheet == null) {
			curSheet = curBook.createSheet(name);
		}
		// if (curSheet != null)
		// curBook.setActiveSheet(curBook.getSheetIndex(curSheet));
	}

	public void setSheet(int index) {
		curSheet = curBook.getSheetAt(index);
	}

	public XSSFCell getCell(int col, int row) {
		return curSheet.getRow(row).getCell(col);
	}

	public XSSFRow getRow() {
		return curRow;
	}

	// public XSSFRow newRow(int row){
	// XSSFRow Row = getRow(row);
	// if (Row == null){
	// int startRow = row - 1;
	// while (getRow(startRow--) == null){}
	// for (int i = startRow + 1; i <= row; i++) {
	// curSheet.createRow(i);
	// }
	// }
	// }
	//
	public XSSFRow newRow(int row) {
		XSSFRow Row = getRow(row);
		if (Row == null) {
			Row = curSheet.createRow(row);
		}
		return Row;
	}

	public XSSFRow getRow(int row) {
		return curSheet.getRow(row);
	}

	public XSSFWorkbook getBook() {
		return curBook;
	}

	/**
	 * 追加一个POI的row对象
	 */
	public void appendRow() {
		int row = curSheet.getLastRowNum();
		curRow = curSheet.createRow(row + 1);
	}

	static class CellInfo {
		public XSSFCellStyle style;
		public Short dataFormat;
		public XSSFComment comment;
		public CellType type;
		public XSSFHyperlink link;
		public String formula;
		public Object value;
	}

	static class RowReserveInfo {
		public XSSFCellStyle rowStyle;
		public TreeMap<Integer, CellInfo> cellInfos = new TreeMap<>();
	}

	static class ReserveInfos {
		public TreeMap<Integer, RowReserveInfo> rows = new TreeMap<>();
		public List<CellRangeAddress> merges = new ArrayList<>();

		public void clear() {
			rows.clear();
			merges.clear();
		}
	}

	ReserveInfos reserveMap = new ReserveInfos();

	protected void reserve(int startRow, int endRow) {
		reserveMap.clear();

		if (startRow > endRow)
			return;

		for (int i = startRow; i <= endRow; i++) {
			RowReserveInfo rowData = new RowReserveInfo();
			XSSFRow row = getRow(i);
			rowData.rowStyle = row.getRowStyle();
			for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
				XSSFCell cell = row.getCell(j);
				CellInfo info = new CellInfo();
				info.comment = cell.getCellComment();
				info.type = cell.getCellType();
				if (info.type == CellType.FORMULA)
					info.formula = cell.getCellFormula();
				else
					info.formula = null;
				info.style = cell.getCellStyle();
				info.link = cell.getHyperlink();
				info.value = getCellValue(cell);
				info.dataFormat = cell.getCellStyle().getDataFormat();
				rowData.cellInfos.put(j, info);
			}
			reserveMap.rows.put(i, rowData);
		}

		// 先保存原始的合并单元格address集合
		for (int i = curSheet.getNumMergedRegions() - 1; i >= 0; i--) {
			CellRangeAddress cellRangeAddress = curSheet.getMergedRegion(i);
			if (cellRangeAddress.getFirstRow() >= startRow) {
				reserveMap.merges.add(cellRangeAddress);
				curSheet.removeMergedRegion(i);
			}
		}

	}

	protected RowReserveInfo getReserveInfo(int row) {
		if (reserveMap.rows.containsKey(row))
			return reserveMap.rows.get(row);
		else
			return null;
	}

	protected void restore(int offsetRow, int offsetCol) {
		// 恢复被删除的合并区域
		for (CellRangeAddress cellRangeAddress : reserveMap.merges) {
			int firstRow = cellRangeAddress.getFirstRow() + offsetRow;

			CellRangeAddress newCellRangeAddress = new CellRangeAddress(firstRow,
					(firstRow + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())),
					cellRangeAddress.getFirstColumn() + offsetCol, cellRangeAddress.getLastColumn() + offsetCol);
			curSheet.addMergedRegion(newCellRangeAddress);
		}

		for (Integer rowIndex : reserveMap.rows.keySet()) {
			RowReserveInfo rowData = reserveMap.rows.get(rowIndex);
			int realRowIndex = rowIndex + offsetRow;
			XSSFRow row = getRow(realRowIndex);
			for (Integer colIndex : rowData.cellInfos.keySet()) {
				CellInfo info = rowData.cellInfos.get(colIndex);
				int realColIndex = offsetCol + colIndex;
				XSSFCell cell = row.createCell(realColIndex);
				if (info.style != null)
					cell.setCellStyle(info.style);
				if (info.comment != null)
					cell.setCellComment(info.comment);
				if (info.link != null)
					cell.setHyperlink(info.link);
				if (info.type == CellType.FORMULA) {
					cell.setCellFormula(info.formula);
				} else {
					cell.setCellType(CellType.STRING);
					cell.setCellType(info.type);
					setCellValue(cell, info.value);
				}

				cell.getCellStyle().setDataFormat(info.dataFormat);
			}
		}

	}

	/**
	 * 在已有的Excel文件中的当前行插入一行新的数据的入口方法
	 */
	public XSSFRow insertRow() {
		int index = 0;
		if (curRow != null)
			index = curRow.getRowNum();
		return insertRow(index);
	}

	public XSSFRow insertRow(int index) {
		return insertRow(index, true);
	}

	public XSSFRow insertRow(int index, boolean useCurrentRowStyle) {
		reserve(index, curSheet.getLastRowNum());

		curSheet.shiftRows(index, curSheet.getLastRowNum(), 1, false, false);
		XSSFRow newRow = curSheet.createRow(index);
		RowReserveInfo info = getReserveInfo(index);
		if (info != null) {
			for (Integer colIndex : info.cellInfos.keySet()) {
				CellInfo cellInfo = info.cellInfos.get(colIndex);
				XSSFCell cell = newRow.createCell(colIndex, cellInfo.type);
				if (useCurrentRowStyle) {
					cell.setCellStyle(cellInfo.style);
				}
			}
		}

		restore(1, 0);

		return newRow;
	}

	public boolean removeRow(int index) {
		if (index == curSheet.getLastRowNum()) {
			return false;
		}

		reserve(index + 1, curSheet.getLastRowNum());

		for (int i = curSheet.getNumMergedRegions() - 1; i >= 0; i--) {
			CellRangeAddress range = curSheet.getMergedRegion(i);
			if (range.getFirstRow() == index && range.getLastRow() == index) {
				curSheet.removeMergedRegion(i);
			}
		}

		curSheet.shiftRows(index + 1, curSheet.getLastRowNum(), -1, false, false);

		restore(-1, 0);

		return true;
	}

	/**
	 * 创建要当前行中单元格
	 * 
	 * @param row
	 * @return
	 */
	public void addCell() {
		int index = 0;
		if (curCell != null)
			index = curCell.getColumnIndex();
		addCell(index);
	}

	public void addCell(int col) {
		curCell = curRow.createCell(col);
	}

	public XSSFCell getCell() {
		return curCell;
	}

	public XSSFCell getCell(int col) {
		return curRow.getCell(col);
	}

	public void insertSheet(String sheetName, Integer index) {
		if (index == null) {
			if (curSheet != null)
				index = curBook.getSheetIndex(curSheet);
			else
				index = curBook.getNumberOfSheets();
		}

		setSheet(sheetName);
		if (curSheet == null)
			curSheet = curBook.createSheet(sheetName);
		curBook.setSheetOrder(sheetName, index);
	}

	public void insertSheet(String sheetName) {
		insertSheet(sheetName, null);
	}

	public void appendSheet(String sheetName) {
		curSheet = null;
		insertSheet(sheetName, null);
	}

	ScriptEngineManager manager = new ScriptEngineManager();

	protected double roundTo(double d, T config) {
		return roundTo(d, config.precision);
	}

	protected double roundTo(double d, int precision) {
		BigDecimal bg = new BigDecimal(d);
		return bg.setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	protected void setCellType(Class<?> t, XSSFCell cell) {
		cell.setCellType(CellType.STRING);
		if (Number.class.isAssignableFrom(t)) {
			cell.setCellType(CellType.NUMERIC);
		} else if (Boolean.class.isAssignableFrom(t)) {
			cell.setCellType(CellType.BOOLEAN);
		} else if (Date.class.isAssignableFrom(t)) {
			cell.setCellType(CellType.NUMERIC);
		} else {
			cell.setCellType(CellType.STRING);
		}
	}

	protected String getExprString(T config, Object obj) {
		if (Date.class.isAssignableFrom(config.valueType)) {
			return obj == null ? "" : obj.toString();
		}

		if (String.class.isAssignableFrom(config.valueType)) {
			return obj == null ? "" : (String) obj;
		}

		return obj == null ? "" : obj.toString();
	}

	public static boolean isEmpty(String value) {
		boolean b = value == null || value.isEmpty();
		if (!b) {
			value = value.replace(" ", "");
			b = value.isEmpty();
		}
		return b;
	}

	protected boolean rowIsEmpty(XSSFRow row) {
		if (row == null || (row.getLastCellNum() - row.getFirstCellNum() <= 0))
			return true;

		for (Cell cell : row) {
			switch (getCellType((XSSFCell) cell)) {
			case BLANK:
			case ERROR:
			case _NONE:
				continue;
			default:
				break;

			}
			String vString = getCellStringValue((XSSFCell) cell);
			if (vString != null && !vString.trim().isEmpty())
				return false;
		}

		return true;
	}

	protected String getHeader(int row, int col) {
		XSSFCell cell = getCell(col, row);
		if (cell == null)
			return "";

		String value = getCellStringValue(cell);
		if (value == null || value.trim().isEmpty())
			return getHeader(row - 1, col);
		else
			return value;
	}

	/***
	 * 将当前book的内容导出到jsonarray中
	 * 
	 * @param columnRow excel表格中包含列头定义的行索引
	 * @param startRow  导出起始行号
	 * @param startRow  导出终止行号
	 * @return 导出的数据json，格式： (columnRow == -1) => { header:[field1,field2],
	 *         data:[{field:value}] } (columnRow != -1) => [ {field:value} ]
	 */
	public Object exportTo(int columnRow, Integer startRow, Integer endRow) {
		JSONArray dataset = new JSONArray();

		if (curSheet == null)
			throw new RuntimeException("not set current of sheet!");

		if (curSheet.getLastRowNum() - curSheet.getFirstRowNum() <= 0)
			return new JSONObject();

		if (startRow == null)
			startRow = curSheet.getFirstRowNum();

		if (endRow == null)
			endRow = curSheet.getLastRowNum();

		JSONArray headers = new JSONArray();
		if (columnRow != -1)
			for (int i = curSheet.getFirstRowNum(); i < curSheet.getLastRowNum(); i++) {
				XSSFRow row = getRow(i);
				if (i != columnRow) {
					continue;
				}

				for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
					String value = getHeader(i, j);
					headers.put(value);
				}
				break;
			}

		for (int i = startRow; i < endRow; i++) {
			XSSFRow row = getRow(i);
			JSONArray rowdata = new JSONArray();

			for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
				XSSFCell cell = row.getCell(j);
				if (columnRow == -1 || i > columnRow) {
					rowdata.put(getCellValue(cell, false));
				}
			}

			dataset.put(rowdata);
		}

		JSONObject result = new JSONObject();
		if (columnRow != -1) {
			result.put("header", headers);
			result.put("data", dataset);
			return result;
		} else {
			return dataset;
		}
	}

	/**
	 * 通过excel文件装载数据
	 * 
	 * @throws RuntimeException
	 */
	public void load(File file) throws RuntimeException {
		close();

		curBook = null;
		this.excelFile = file;
		if (file == null || !file.exists())
			curBook = new XSSFWorkbook();
		else {
			try (FileInputStream fileInputStream = new FileInputStream(file);) {
				curBook = new XSSFWorkbook(fileInputStream);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void close() {
		try {
			if (curBook != null) {
				try {
					curBook.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		} finally {
			curBook = null;
		}
	}

	protected static CellType getCellType(CellValue cellValue) {
		return cellValue.getCellType();
	}

	public static CellType getCellType(XSSFCell cell) {
		CellType type = cell.getCellType();
		return type;
	}

	public Object getExprValue(XSSFCell cell, boolean asString) {
		XSSFFormulaEvaluator eva = new XSSFFormulaEvaluator(curBook);
		CellValue cellVal = eva.evaluate(cell);
		if (asString)
			return cellVal.formatAsString();

		switch (getCellType(cellVal)) {
		case BOOLEAN:
			return cellVal.getBooleanValue();
		case NUMERIC:
			return cellVal.getNumberValue();
		default:
			return cellVal.getStringValue();
		}
	}

	public String getCellStringValue(XSSFCell cell) {
		String cellValue = "";
		switch (getCellType((XSSFCell) cell)) {
		case BLANK:
			cellValue = " ";
			break;
		case BOOLEAN:
			cellValue = cell.getBooleanCellValue() ? "true" : "false";
			break;
		case ERROR:
			break;
		case FORMULA:
			cellValue = (String) getExprValue(cell, true);
			break;
		case NUMERIC:
			cellValue = String.valueOf(readNumberValue(cell));
			break;
		case STRING:
			try {
				cellValue = cell.getStringCellValue();
			} catch (Exception e) {
				cellValue = String.valueOf(cell.getNumericCellValue());
			}
			if (cellValue.trim().equals("") || cellValue.trim().length() <= 0)
				cellValue = " ";
			break;
		case _NONE:
			break;
		default:
			break;

		}
		return cellValue;
	}

	protected Object getValue(T config, Object value) {
		return getValue(value, config.valueType, config.format, config.precision);
	}

	protected Object getValue(Object value, Class<?> c, String dateFormat, int precision) {
		if (Integer.class.isAssignableFrom(c)) {
			if (value == null)
				return 0;

			if (value instanceof Number) {
				if (value instanceof Double)
					return ((Double) value).intValue();
				else if (value instanceof Float) {
					return ((Float) value).intValue();
				} else
					return (int) value;
			}

			return Integer.parseInt(value.toString());
		}

		if (Double.class.isAssignableFrom(c)) {
			double d = 0;
			if (value != null) {
				if (value instanceof Number) {
					if (value instanceof Integer)
						d = (int) value;
					else if (value instanceof Float)
						d = (float) value;
					else
						d = (double) value;
				} else
					d = Double.parseDouble(value.toString());
			}

			return roundTo(d, precision);
		}

		if (Date.class.isAssignableFrom(c)) {
			SimpleDateFormat formater = new SimpleDateFormat(dateFormat);

			if (value == null)
				return formater.format(new Date());

			try {
				if (value instanceof Number)
					return HSSFDateUtil.getJavaDate((double) value);
				else if (value instanceof Date) {
					return value;
				} else
					return formater.parse(value.toString());
			} catch (ParseException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		if (String.class.isAssignableFrom(c)) {
			if (value == null)
				return "";
			return value.toString();
		}

		if (Boolean.class.isAssignableFrom(c)) {
			if (value == null)
				return false;
			return Boolean.parseBoolean(value.toString());
		}

		return value;
	}

	protected static Object processDateValue(XSSFCell cell, boolean asDate, Object o) {
		if (o instanceof Double) {
			if (asDate) {
				return HSSFDateUtil.getJavaDate((double) o);
			} else if (cell.getCellStyle() != null) {
				if (HSSFDateUtil.isCellDateFormatted(cell))
					return HSSFDateUtil.getJavaDate((double) o);
			}
		}

		return o;
	}

	public Object getCellValue(XSSFCell cell, Class<?> c) {
		Object value = getCellValue(cell, Date.class.isAssignableFrom(c));
		if (Integer.class.isAssignableFrom(c)) {
			if (value instanceof Double)
				return ((Double) value).intValue();
			else if (value instanceof Float)
				return ((Float) value).intValue();
			else if (value instanceof Number)
				return (int) value;
			return value == null ? 0 : Integer.parseInt(value.toString());
		} else if (Double.class.isAssignableFrom(c)) {
			if (value == null || (value instanceof String && value.toString().trim().isEmpty()))
				return 0F;
			
			if (value instanceof Number) {
				Number number = (Number) value;
				return number.doubleValue();
			}
			return value == null ? 0 : Double.parseDouble(value.toString());
		} else if (Date.class.isAssignableFrom(c)) {
			if (value instanceof Date)
				return value;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				return value == null ? null : format.parse(value.toString());
			} catch (ParseException e) {
				e.printStackTrace();
				return null;
			}
		} else if (Boolean.class.isAssignableFrom(c)) {
			if (value instanceof Boolean)
				return value;
			return value == null ? false : Boolean.parseBoolean(value.toString());
		} else {
			if (value instanceof String)
				return value;
			return value == null ? null : value.toString();
		}
	}

	public Object getCellValue(XSSFCell cell, boolean asDate) {
		Object result = getCellValue(cell);
		return processDateValue(cell, asDate, result);
	}

	protected static Object readNumberValue(XSSFCell cell) {
		if (HSSFDateUtil.isCellDateFormatted(cell)) {
			return cell.getDateCellValue();
		} else {
			String format = cell.getCellStyle().getDataFormatString();
			if (format == null || format.isEmpty())
				return cell.getNumericCellValue();
			else {
				HSSFDataFormatter formatter = new HSSFDataFormatter();
				String tmp = formatter.formatCellValue(cell);
				try {
					if (tmp.contains("."))
						return Double.parseDouble(tmp);
					else
						return Integer.parseInt(tmp);

				} catch (Exception e) {
					return tmp;
				}
			}
		}
	}

	public Object getCellValue(XSSFCell cell) {
		switch (getCellType((XSSFCell) cell)) {
		case BOOLEAN:
			return cell.getBooleanCellValue();
		case FORMULA: {
			return getExprValue(cell, false);
		}
		case NUMERIC: {
			return readNumberValue(cell);
		}
		default:
			return getCellStringValue(cell);
		}
	}

	public void setCellValue(XSSFCell cell, Object value) {

		switch (getCellType(cell)) {
		case BOOLEAN:
			boolean b = false;
			if (value != null) {
				if (value instanceof Boolean)
					b = (boolean) value;
				else if (value instanceof String)
					b = Boolean.parseBoolean((String) value);
				else if (value instanceof Integer)
					b = (int) value != 0;
			}
			cell.setCellValue(b);
			break;
		case FORMULA:
			cell.setCellType(CellType.NUMERIC);
			cell.setCellValue(value == null ? "" : value.toString());
			break;
		case NUMERIC:
			if (value != null)
				if (value instanceof Number) {
					if (value instanceof Short || value instanceof Integer || value instanceof Long)
						cell.setCellValue((int) value);
					else
						cell.setCellValue(((Number)value).doubleValue());
				} else if (value instanceof Date) {
					double v = HSSFDateUtil.getExcelDate((Date) value);
					cell.setCellValue(v);
				} else if (value instanceof String) {
					cell.setCellValue(NumberUtils.toDouble((String) value));
				}
			break;
		default:
			cell.setCellValue(value == null ? "" : value.toString());
			break;
		}
	}

	/**
	 * 保存工作薄
	 * 
	 * @param wb
	 */
	public void saveAs(File file) {
		// refreshSheet();
		saveAs(file, curBook);
	}

	public void save() {
		saveAs(excelFile, curBook);
	}

	protected static void saveAs(File file, XSSFWorkbook workbook) {
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			saveAs(outputStream, workbook);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected static void saveAs(OutputStream outputStream, XSSFWorkbook workbook) throws IOException {
		if (workbook == null)
			throw new RuntimeException("not open excel!");

		workbook.write(outputStream);
	}

	protected HashMap<Integer, CellRangeAddress> getOneRowRegions() {
		HashMap<Integer, CellRangeAddress> mergeMap = new HashMap<>();
		for (CellRangeAddress range : curSheet.getMergedRegions()) {
			if (range.getFirstRow() == range.getLastRow()) {
				mergeMap.put(range.getFirstRow(), range);
			}
		}

		return mergeMap;
	}

	/**
	 * 将行的保存信息恢复到目的行，如果行内的单元格未赋值则恢复值
	 * 
	 * @param source 保存待恢复信息的对象
	 * @param dest   要恢复的行对象
	 */
	protected void restoreRow(RowInfo source, XSSFRow dest) {
		dest.setRowStyle(source.rowStyle);
		for (int i = dest.getFirstCellNum(); i <= dest.getLastCellNum(); i++) {
			XSSFCell cell = dest.getCell(i);
			if (cell == null || cell.getCellType() == null)
				continue;

			if (source.cellTypes.containsKey(i) && source.cellTypes.get(i) != null) {
				cell.setCellType(CellType.STRING);
				cell.setCellType(source.cellTypes.get(i));
			}
			if (cell.getCellType() == CellType.FORMULA)
				if (source.cellFormulas.containsKey(i) && source.cellFormulas.get(i) != null)
					cell.setCellFormula(source.cellFormulas.get(i));
			if (source.cellStyles.containsKey(i) && source.cellStyles.get(i) != null)
				cell.setCellStyle(source.cellStyles.get(i));
			if (source.cellComments.containsKey(i) && source.cellComments.get(i) != null)
				cell.setCellComment(source.cellComments.get(i));
			if (source.cellLinks.containsKey(i) && source.cellLinks.get(i) != null)
				cell.setHyperlink(source.cellLinks.get(i));
			if (source.values.containsKey(i)) {
				Object v = getCellValue(cell);
				if (v == null || (v instanceof String && isEmpty((String) v))) {
					Object object = source.values.get(i);
					if (object != null && (object instanceof String && !isEmpty((String) object))) {
						setCellValue(cell, source.values.get(i));
					}
				}
			}
		}

		if (source.region != null) {
			if (dest.getRowNum() != source.region.getFirstRow()) {
				source.region.setFirstRow(dest.getRowNum());
				source.region.setLastRow(dest.getRowNum());
			}

			// 恢复被删除的合并区域
			CellRangeAddress newCellRangeAddress = new CellRangeAddress(source.region.getFirstRow(),
					source.region.getLastRow(), source.region.getFirstColumn(), source.region.getLastColumn());
			try {
				curSheet.addMergedRegion(newCellRangeAddress);
			} catch (Exception e) {
			}
		}

	}

	protected RowInfo saveRow(XSSFRow row, HashMap<Integer, CellRangeAddress> mergeMap) {
		RowInfo rowInfo = new RowInfo();
		rowInfo.rowStyle = row.getRowStyle();
		rowInfo.startCellIndex = row.getFirstCellNum();
		rowInfo.lastCellIndex = row.getLastCellNum();
		for (int j = row.getFirstCellNum(); j < row.getLastCellNum(); j++) {
			XSSFCell cell = row.getCell(j);
			rowInfo.cellComments.put(j, cell.getCellComment());
			if (cell.getCellType() == CellType.FORMULA)
				rowInfo.cellFormulas.put(j, cell.getCellFormula());
			rowInfo.cellLinks.put(j, cell.getHyperlink());
			rowInfo.cellStyles.put(j, cell.getCellStyle());
			rowInfo.cellTypes.put(j, cell.getCellType());
			rowInfo.values.put(j, getCellValue(cell));
		}

		if (mergeMap != null && mergeMap.containsKey(row.getRowNum())) {
			rowInfo.region = mergeMap.get(row.getRowNum());
		}

		return rowInfo;
	}

	// 调用此方法后，如果不是继续使用当前sheet，应该调用close方法关闭此对象
	public void saveSheet(File file) throws IOException, InvalidFormatException {
		if (curSheet == null)
			throw new NullPointerException("not set cur sheet!");

		XSSFWorkbook saveBook = curBook;
		try {
			int index = curBook.getSheetIndex(curSheet);

			for (int i = saveBook.getNumberOfSheets() - 1; i >= 0; i--) {
				if (i == index)
					continue;

				curBook.removeSheetAt(i);
			}

			saveAs(file, saveBook);

		} finally {
			// saveBook.close();
		}
	}

	public byte[] getHttpImage(String imageUrl) throws Exception {
		byte[] buf = new byte[8192];
		int size = 0;
		URL url = new URL(imageUrl);
		HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
		httpUrl.connect();
		try (ByteArrayOutputStream fos = new ByteArrayOutputStream();
				BufferedInputStream bis = new BufferedInputStream(httpUrl.getInputStream());) {
			while ((size = bis.read(buf)) != -1) {
				fos.write(buf, 0, size);
			}
			fos.flush();
			return fos.toByteArray();
		} catch (IOException e) {
			throw e;
		} finally {
			httpUrl.disconnect();
		}

	}

	public void addImageDataUrl(String dataUrl, int startRow, int endRow, int startCol, int endCol) throws Exception {
		byte[] imagedata = DatatypeConverter.parseBase64Binary(dataUrl.substring(dataUrl.indexOf(",") + 1));
		addImage(imagedata, startRow, endRow, startCol, endCol);
	}

	public void addImage(String url, int row, int col) throws Exception {
		addImage(url, row, -1, col, -1);
	}

	public void addImage(String url, int startRow, int endRow, int startCol, int endCol) throws Exception {
		byte[] bytes = getHttpImage(url);
		addImage(bytes, startRow, endRow, startCol, endCol);
	}

	public void addImage(byte[] bytes, int startRow, int endRow, int startCol, int endCol) throws Exception {
		int pictureIdx = curBook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		CreationHelper helper = curBook.getCreationHelper();
		Drawing<XSSFShape> drawing = curSheet.createDrawingPatriarch();
		ClientAnchor anchor = helper.createClientAnchor();
		anchor.setAnchorType(AnchorType.MOVE_AND_RESIZE);
		// 图片插入坐标
		anchor.setCol1(startCol); // 列
		if (endCol != -1)
			anchor.setCol2(endCol);
		anchor.setRow1(startRow); // 行
		if (endRow != -1)
			anchor.setRow2(endRow);

		int leftTop = getPOIWidthByExcelPiex(5);
		anchor.setDx1(leftTop);
		anchor.setDy1(leftTop);

		int right = getPOIWidthByExcelPiex(-5);
		int bottom = getPOIWidthByExcelPiex(-5);
		anchor.setDx2(right);
		anchor.setDy2(bottom);

		// 插入图片
		drawing.createPicture(anchor, pictureIdx);
		// Picture pict = drawing.createPicture(anchor, pictureIdx);
		// pict.resize();
	}

	public void setBorder(CellRangeAddress cellRangeAddress) throws Exception {
		RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress, curSheet);
		RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress, curSheet);
		RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress, curSheet);
		RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress, curSheet);

	}

	public void setBorder(XSSFCell cell) throws Exception {
		XSSFCellStyle cellStyle = cell.getCellStyle();
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);

	}

	public static class ExecuteParam {
		public Object[] paramObj = null;
		public String sheetName = null;
		public Object result = null;
	}

	// 模型执行的方法，子类必须实现此方法用以完成模型的功能
	public abstract void execute(ExecuteParam executeParam) throws Exception;
}
