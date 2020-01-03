package com.wh.swing.tools.dialog.input.grid;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import com.wh.swing.tools.KeyValue;
import com.wh.swing.tools.MsgHelper;
import com.wh.swing.tools.dialog.input.grid.GridCellEditor.ActionResult;
import com.wh.swing.tools.dialog.input.grid.GridCellEditor.ButtonActionListener;

public class DefaultKeyValueSelector extends JDialog {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTable table;

	enum DialogResult {
		drOk, drCancel, drNull
	}

	enum SelectType {
		stOne, stAll
	}

	public interface IActionListener {
		ActionResult onAction(javax.swing.table.TableModel model, String key, Object value, int row, int col,
				List<Object> selects);
	}

	public interface ICheckValue {
		public boolean onCheck(Object[][] originalData, JTable table);
	}

	public interface IEditRow {
		public boolean deleteRow(JTable table, Vector<?> row);

		public Object[] addRow(JTable table);

		public void updateRow(JTable table, Vector<?> row);
	}

	SelectType sType = SelectType.stAll;

	ICheckValue onCheckValue;

	DialogResult dr = DialogResult.drCancel;

	IActionListener actionListener;

	boolean fireOnCheck() {
		if (onCheckValue != null) {
			return onCheckValue.onCheck(originalData, table);
		} else
			return true;
	}

	/**
	 * Create the frame.
	 */
	public DefaultKeyValueSelector(JComponent parent) {
		setResizable(false);
		setModal(true);
		setBounds(100, 100, 611, 501);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				setVisible(false);
			}

		});
		buttons = new JPanel();
		contentPane.add(buttons, BorderLayout.SOUTH);

		JButton okButton = new JButton("确定");
		okButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.isEditing()) {
					if (table.getEditorComponent() instanceof JComboBox) {
						int row = table.getSelectedRow();
						int col = table.getSelectedColumn();
						Object value = table.getValueAt(row, col);
						table.getCellEditor().cancelCellEditing();
						table.setValueAt(value, row, col);
					} else
						table.getCellEditor().stopCellEditing();
				}

				if (sType == SelectType.stOne && table.getSelectedRow() == -1) {
					MsgHelper.showMessage(DefaultKeyValueSelector.this, "请先选择一项数据！", "提示",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}

				if (!fireOnCheck())
					return;

				dr = DialogResult.drOk;
				dispose();
			}
		});

		btn_add = new JButton("添加");
		btn_add.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btn_add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				if (iEditRow == null)
					model.setRowCount(model.getRowCount() + 1);
				else {
					Object[] row = iEditRow.addRow(table);
					if (row != null) {
						addRow(row);
					}
				}
			}
		});
		buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		buttons.add(btn_add);

		btn_del = new JButton("删除");
		btn_del.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		btn_del.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() == -1)
					return;

				if (MsgHelper.showConfirmDialog("是否删除选定的条目？", "", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
					return;
				DefaultTableModel model = (DefaultTableModel) table.getModel();

				int index = table.getSelectedRow();
				Vector<?> row = (Vector<?>) model.getDataVector().get(index);
				if (iEditRow != null) {
					if (!iEditRow.deleteRow(table, row))
						return;
				}
				model.removeRow(index);
			}
		});
		buttons.add(btn_del);

		btn_delString = new JButton("删除字符值");
		btn_delString.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (table.getSelectedRow() != -1) {
					int col = table.getSelectedColumn();
					Object value = table.getValueAt(table.getSelectedRow(), col);
					if (value instanceof String || value instanceof KeyValue) {
						table.setValueAt(null, table.getSelectedRow(), col);
					}
				}
			}
		});
		btn_delString.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttons.add(btn_delString);

		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		separator.setPreferredSize(new Dimension(0, 10));
		buttons.add(separator);
		buttons.add(okButton);

		JButton cancelButton = new JButton("取消");
		cancelButton.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dr = DialogResult.drCancel;
				dispose();
			}
		});
		buttons.add(cancelButton);

		btnNull = new JButton("null返回");
		btnNull.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sType != SelectType.stOne) {
					MsgHelper.showMessage("非单选模式不可以null返回！");
					return;
				}

				if (!fireOnCheck())
					return;

				dr = DialogResult.drNull;
				dispose();
			}
		});
		btnNull.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		buttons.add(btnNull);

		JScrollPane scrollPane = new JScrollPane();
		contentPane.add(scrollPane);

		table = new JTable();
		table.setRowHeight(40);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSurrendersFocusOnKeystroke(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);

		setLocationRelativeTo(null);
	}

	class TableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		Object[] columns;
		HashMap<Integer, Integer> readOnlyColumns = new HashMap<>();

		public TableModel(Object[] columns, int[] readOnlyColumns) {
			super(new Object[][] {}, columns);
			this.columns = columns;
			if (readOnlyColumns != null)
				for (int i = 0; i < readOnlyColumns.length; i++) {
					this.readOnlyColumns.put(readOnlyColumns[i], readOnlyColumns[i]);
				}
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return !readOnlyColumns.containsKey(columnIndex);
		}

		public void setValueAt(Object aValue, int row, int column) {
			Object object = getValueAt(row, column);
			if (object != aValue || (object != null && aValue != null && !object.equals(aValue)))
				super.setValueAt(aValue, row, column);
		}
	}

	/***
	 * 
	 * @param columns           列名称列表
	 * @param multipleSelection 表格是否可以多选
	 * @param readOnlyColumns   固定列的索引列表
	 * @param columnTypes       参见KeyValueGridRender的columnType参数说明
	 * 
	 */
	void init(Object[] columns, boolean multipleSelection, int[] readOnlyColumns, Object[] columnTypes) {
		table.setSelectionMode(multipleSelection ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
				: ListSelectionModel.SINGLE_SELECTION);
		DefaultTableModel tableModel = new TableModel(columns, readOnlyColumns);
		tableModel.addTableModelListener(new TableModelListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void tableChanged(TableModelEvent e) {
				TableModelListener[] listeners = tableModel.getTableModelListeners();
				for (TableModelListener listener : listeners) {
					tableModel.removeTableModelListener(listener);
				}
				try {
					switch (e.getType()) {
					case TableModelEvent.INSERT:
						break;
					case TableModelEvent.UPDATE:
						int index = e.getLastRow();
						DefaultTableModel model = ((DefaultTableModel) table.getModel());
						if (index >= model.getRowCount() || index < 0)
							return;

						Vector<Object> rows = (Vector<Object>) model.getDataVector();
						if (rows.size() == 0)
							return;
						Vector<Object> row = (Vector<Object>) rows.get(index);
						if (iEditRow != null) {
							iEditRow.updateRow(table, row);
						}
						break;
					case TableModelEvent.DELETE:
						break;
					}
				} finally {
					for (TableModelListener listener : listeners) {
						tableModel.addTableModelListener(listener);
					}
				}
			}
		});

		table.setModel(tableModel);
		Map<Integer, Integer> readColumns = new HashMap<>();
		if (readOnlyColumns != null)
			for (int index : readOnlyColumns) {
				readColumns.put(index, index);
			}

		for (int i = 0; i < columns.length; i++) {
			if (!readColumns.containsKey(i)) {
				Object columnType = columnTypes == null ? null : columnTypes[i];
				table.getColumnModel().getColumn(i).setCellEditor(
						new GridCellEditor(table.getColumnName(i), i, "编辑", columnType, new ButtonActionListener() {
							@Override
							public ActionResult actionPerformed(ActionEvent e, String columnName, int columnIndex) {
								if (actionListener != null) {

									int row = table.convertRowIndexToModel(table.getSelectedRow());
									int col = table.convertColumnIndexToModel(table.getSelectedColumn());
									Object key = table.getValueAt(row, 0);
									if (key != null)
										key = key.toString();
									else
										key = null;
									return actionListener.onAction(table.getModel(), (String) key,
											table.getValueAt(row, columnIndex), row, col, null);
								}
								return new ActionResult();
							}
						}, new GridCellEditor.UpdateValueListener() {

							@Override
							public void onUpdateValue(Object value, String columnName, int columnIndex) {
								tableModel.setValueAt(value, table.getSelectedRow(), columnIndex);
							}
						}, new GridCellEditor.InitComboBoxListener() {

							@Override
							public void init(String columnName, int columnIndex, JComboBox<Object> comboBox) {
								if (actionListener != null) {
									List<Object> list = new ArrayList<>();
									int row = table.convertRowIndexToModel(table.getSelectedRow());
									int col = table.convertColumnIndexToModel(table.getSelectedColumn());
									Object key = table.getValueAt(row, 0);
									actionListener.onAction(table.getModel(), key == null ? null : key.toString(),
											table.getValueAt(row, columnIndex), row, col, list);

									DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) comboBox
											.getModel();
									model.removeAllElements();
									for (Object object : list) {
										comboBox.addItem(object);
									}
								}
							}
						}));
			}
		}

		setLocationRelativeTo(null);
	}

	Object[][] originalData;

	void addRow(Object[] row) {
		GridCellEditor.VectorEx data = new GridCellEditor.VectorEx();
		data.addAll(Arrays.asList(row));
		data.ordData = row;
		((DefaultTableModel) table.getModel()).addRow(data);
	}

	void init(Object[][] datas, Object[] columns, Object[] columnType, boolean multipleSelection,
			int[] readOnlyColumns) {
		originalData = datas;

		init(columns, multipleSelection, readOnlyColumns, columnType);
		TableModel tableModel = (TableModel) table.getModel();
		if (datas != null)
			for (int i = 0; i < datas.length; i++) {
				addRow(datas[i]);
			}
		tableModel.fireTableDataChanged();
	}

	Object[] getResult() {
		return (Object[]) getResult(null);
	}

	Object getResult(Integer index) {
		DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
		if (dr != DialogResult.drOk)
			return null;

		if (index == null) {
			Object obj = tableModel.getDataVector().get(table.getSelectedRow());
			if (obj instanceof Vector<?>) {
				Vector<?> vector = (Vector<?>) obj;
				return vector.toArray();
			} else
				return obj;
		} else
			return tableModel.getValueAt(table.getSelectedRow(), index);
	}

	IEditRow iEditRow;

	static IActionListener defaultActionListener;
	private JPanel buttons;
	private JButton btnNull;
	private JButton btn_del;
	private JButton btn_add;
	private JButton btn_delString;

	public static abstract class Result {
		public boolean isok = false;
	}

	public static class ModelResult extends Result {
		public DefaultTableModel model;

		public ModelResult(DefaultTableModel model, boolean isok) {
			this.model = model;
			this.isok = isok;
		}
	}

	public static class RowResult extends Result {
		public Object[] row;

		public RowResult(Object[] row, boolean isok) {
			this.row = row;
			this.isok = isok;
		}
	}

	public interface IInstanceKeyValueSelector {
		public DefaultKeyValueSelector instance(JComponent parent);
	}

	/**
	 * 显示表格数据编辑框
	 * 
	 * @param parent                   父组件
	 * @param onCheckValue             当按【确定】按钮时候调用，返回true本次确认操作有效，其他无效
	 * @param iEditRow                 当编辑行数据时触发
	 * @param datas                    行数据集合，格式为[行号][列值]，行号从0开始；如[0]["列1值","列n值"]，表示第1行有2列数据；
	 *                                 如果单行数据数目大于列数，表示从后到前的编辑框要求： 比如表格有2列,
	 *                                 1、[0]["a1","a2", new ArrayList(), new
	 *                                 JButton()]，前2项为值，0行0列使用下拉列表编辑，0行1列使用按钮编辑
	 *                                 2、[0]["a1","a2", new
	 *                                 JButton()]，前2项为值，0行0列使用默认编辑器，0行1列使用按钮编辑
	 * @param columns                  列定义集合，使用toString()方法显示列头
	 * @param columnTypes              列的编辑类型，如果datas也设置了，优先使用datas设置，列的编辑器类型定义如下：
	 *                                 1、columnTypes的对应列值为null或者不存在，则根据对应单元格的数据类型设置编辑器：
	 *                                 boolean：JComboBox控件（true|false），
	 *                                 Number：JSpinner控件 Date：JDatePicker控件
	 *                                 其他：JTextField控件
	 * 
	 *                                 2、columnTypes为数组且对应列值不为null
	 *                                 String：使用按钮，columnType[i].isEmpty()函数返回false，使用其值作为按钮名称，否则按钮名称使用defaultButtonCaption设置，
	 *                                 List<E>：使用下拉列表方式，size不为0，下拉项目为E.toString()值列表，否则调用initComboBoxListener方法设置下拉项目
	 *                                 3、columnType为Class类型 根据columnType的值类型使用不同组件：
	 *                                 list则使用下拉列表，其他为按钮
	 * @param readOnlyColumns          只读列的定义集合
	 * @param actionListener           单击及下拉列表项目初始事件，如果datas传入的列表为0会调用此事件初始列表
	 * @param multipleSelection        是否多选
	 * @param onlyReturnRow            是否按行返回，true按行返回，其他按model返回
	 * @param instanceKeyValueSelector 建立AbstractKeyValueSelector对象
	 * @return
	 */
	public static Result showDialog(JComponent parent, ICheckValue onCheckValue, IEditRow iEditRow, Object[][] datas,
			Object[] columns, Object[] columnTypes, int[] readOnlyColumns, IActionListener actionListener,
			boolean multipleSelection, boolean onlyReturnRow, IInstanceKeyValueSelector instanceKeyValueSelector) {

		DefaultKeyValueSelector tableDialog = instanceKeyValueSelector.instance(parent);
		tableDialog.actionListener = actionListener;
		tableDialog.sType = onlyReturnRow ? SelectType.stOne : SelectType.stAll;
		if (!onlyReturnRow)
			tableDialog.buttons.remove(tableDialog.btnNull);
		tableDialog.init(datas, columns, columnTypes, multipleSelection, readOnlyColumns);
		tableDialog.onCheckValue = onCheckValue;
		tableDialog.iEditRow = iEditRow;
		tableDialog.setModal(true);
		tableDialog.setVisible(true);

		if (tableDialog.dr != DialogResult.drOk) {
			tableDialog.dispose();
			if (!onlyReturnRow)
				return new ModelResult(null, false);
			else
				return new RowResult(null, tableDialog.dr == DialogResult.drNull);
		}

		if (!onlyReturnRow) {
			DefaultTableModel model = (DefaultTableModel) tableDialog.table.getModel();
			tableDialog.dispose();
			return new ModelResult(model, true);
		} else {
			Object result = tableDialog.getResult(null);
			tableDialog.dispose();
			return new RowResult((Object[]) result, true);
		}
	}

	public interface IEditor {
		void onEdited(Result result);
	}

	public static void kvEditor(ICheckValue onCheckValue, IEditRow iEditRow, Object[][] datas, Object[] columnTypes,
			IEditor onEditor) {
		Result result = showDialog(null, onCheckValue, iEditRow, datas, new Object[] { "项目", "值" }, columnTypes, null,
				null, false, false, new IInstanceKeyValueSelector() {

					@Override
					public DefaultKeyValueSelector instance(JComponent parent) {
						return new DefaultKeyValueSelector(null);
					}
				});
		if (onEditor != null) {
			onEditor.onEdited(result);
		}
	}

	public static void kvEditor(ICheckValue onCheckValue, IEditRow iEditRow, Object[][] datas, IEditor onEditor) {
		kvEditor(onCheckValue, iEditRow, datas, null, onEditor);
	}

	public static void kvEditor(ICheckValue onCheckValue, IEditRow iEditRow, IEditor onEditor) {
		kvEditor(onCheckValue, iEditRow, null, onEditor);
	}

	public static void kvEditor(IEditor onEditor) {
		kvEditor(null, null, null, onEditor);
	}
}
