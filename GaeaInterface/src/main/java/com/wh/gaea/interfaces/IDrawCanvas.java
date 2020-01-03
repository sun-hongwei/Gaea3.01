package com.wh.gaea.interfaces;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.JPopupMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface IDrawCanvas {

	int ANDROID_DPI = 160;
	float LINEWIDTH = 2.5f;
	int LDPI = 120; // 75% 已经被市场淘汰
	int MDPI = 160; // 100% 基本被市场淘汰
	int TVDPI = 213; // 133% Google Nexus 7
	int HDPI = 240; // 150% Sumsong Glaxy S2， Google Nexus
	// S， MOTO Droid
	int XHDPI = 320; // 200% Sumsong Glaxy S3， Sumsong
	// Note2， Google Nexus 4
	int XXHDPI = 480; // 300% 目前市场上各品牌的旗舰机：Sumsong Glaxy S4
	int XXXHDPI = 560; // 350% 4K
	int XX42HDPI = 420; // 263% 非android标准
	int XX44HDPI = 440; // 275% 非android标准
	String[] ALIGNNAMES = { "顶部对齐", "底部对齐", "左部对齐", "右部对齐", "-", "顶部拉齐", "底部拉齐", "左部拉齐", "右部拉齐", "-", "横向居中", "纵向剧中",
			"-", "横向分布", "纵向分布", "-", "等宽", "等高" };
	String[] PAGENAMES = new String[] { "A4【横向】", "A4【纵向】", "A3【横向】", "A3【纵向】", "A2【横向】", "A2【纵向】", "A1【横向】", "A1【纵向】",
			"720p【720 * 1280】", "1080p【1080 * 1920】", "2k【2560 * 1440】", "WXGA+【1440x900】19寸",
			"WSXGA+【1680 * 1050】20-22寸", "WUXGA【1920 * 1200】17-24寸", "WQXGA【2560 * 1600】30寸", "QVGA【320 * 240】",
			"VGA【640 * 480】", "SVGA【800 * 600】", "XGA【1024 * 768】", "qHD【960 * 540】", "WXGA【1366 * 768】",
			"5.0寸，1080 * 1920（手机）", "5.5寸，1080 * 2160（手机）", "5.5寸，1440 * 2560（手机）", "6.0寸，1440 * 2560（手机）",
			"6.0寸，1440 * 2880（手机）", "6.3寸，1440 * 2960（手机）", "7.0寸，800 * 1280（平板）", "7.0寸，1200 * 1920（平板）",
			"8.9寸，2048 * 1536（平板）", "9.9寸，2560 * 1800（平板）", "10.1寸，2560 * 1600（平板）", "自定义" };

	void clearSelect();

	void clearSelect(boolean needPush);

	void setSelected(IDrawNode node);

	void setSelects(Collection<IDrawNode> nodes, boolean fireChange, boolean pushCommand);

	boolean isCtrlPressed();

	boolean isAltPressed();

	IDrawNode nodeConnectToPrevType(IDrawNode node, Class<? extends IDrawNode> c);

	IDrawNode nodeConnectToNextType(IDrawNode node, Class<? extends IDrawNode> c);

	void addNodeMouseMotionListener(MouseMotionListener listener);

	void removeNodeMouseMotionListener(MouseMotionListener listener);

	void addNodeMouseListener(MouseListener listener);

	void removeNodeMouseListener(MouseListener listener);

	void addNodeKeyListener(KeyListener listener);

	void removeNodeKeyListener(KeyListener listener);

	void updateID(String oldid, String newid);

	boolean fixNode(IDrawNode node);

	Rectangle getClipRect();

	Point getRealPoint(Point point);

	Point getVirtualPoint(Point point);

	IDrawNode NodeOfPoint(Point point);

	boolean selectLine(Point pt);

	void keyPressed(KeyEvent e);

	void keyReleased(KeyEvent e);

	void copy(boolean all);

	void copySelectNodeToClipboard(CopyType ct);

	void postFireChange(ChangeType ct);

	void fireChange(Collection<IDrawNode> nodes, ChangeType ct);

	void fireChange(IDrawNode node, ChangeType ct, Object data);

	void changeNodeId(IDrawNode node, String newId) throws IOException;

	List<IDrawNode> paste();

	void fireMouseRelease(MouseEvent e);

	void refreshDrawTree(List<IDrawNode> nodes, boolean autoAdd);

	void removeLink(IPolyLine line);

	boolean linkTo(IDrawNode start, IDrawNode end);

	boolean isMuiltSelecting();

	Rectangle getSelectRect();

	void setFile(File f);

	void setNodes(List<IDrawNode> nodes);

	File getFile();

	void save() throws Exception;

	JSONObject saveToJson() throws Exception;

	void load(ICreateNodeSerializable createUserDataSerializable, IInitPage onInitPage, boolean clear) throws Exception;

	void loadFromJson(JSONObject data, ICreateNodeSerializable createUserDataSerializable, IInitPage onInitPage,
			boolean clear) throws Exception;

	void loadFromNodes(List<IDrawNode> nodes, ICustomLoad onLoad) throws Exception;

	void loadFromNodes(List<IDrawNode> nodes, JSONObject stackTreeInfo, boolean clear, ICustomLoad onLoad)
			throws Exception;

	void loadFromLoadNodeInfo(LoadNodeInfo info, JSONObject stackTreeInfo, boolean clear, ICustomLoad onLoad)
			throws Exception;

	void load(ICreateNodeSerializable createUserDataSerializable, IInitPage onInitPage) throws Exception;

	void clear();

	void fixNodesInPage();

	JSONArray toJson() throws JSONException;

	Rectangle getLocalRect(Rectangle rectangle);

	Rectangle getPageRectangle(Rectangle rect);

	Point getPageLocation(Point pt);

	Point getVirtualLocation(Point pt);

	IDrawNode add(String name, String title, Rectangle rect, Object userData, IDataSerializable dataSerializable);

	void removeAndHint();

	void remove();

	void remove(String id);

	void remove(IDrawNode node);

	void addNode(IDrawNode node, boolean needPush, boolean needNotify);

	void remove(IDrawNode node, boolean needRepaint, boolean needPushCommand, boolean needNotify);

	void remove(IDrawNode[] removedNodes, boolean needRepaint, boolean needPushCommand, boolean needNotify);

	void remove(IDrawNode[] removedNodes, boolean needRepaint, boolean needPushCommand, boolean needNotify,
			boolean removeRef);

	IDrawNode getSelected();

	List<IDrawNode> getSelecteds();

	void setSelecteds(IDrawNode[] nodes);

	IDrawNode getNode(String id);

	List<IDrawNode> getNodes();

	void setEditMode(EditMode mode);

	void setOffset(IDrawNode node, boolean center);

	void setOffset(Point offset);

	Point getOffset();

	Rectangle getViewPortRect();

	float getScreenToDeviceScale();

	float getDeviceToScreenScale();

	int convertCanvasSizeToScreenSize(int canvasSize);

	int convertScreenSizeToCanvasSize(int screenSize);

	IDrawPageConfig getPageConfig();

	void setPageConfig(IDrawPageConfig pageConfig);

	Dimension getPageSize();

	Dimension getConfigPageSize();

	int getPageWidth();

	int getPageHeight();

	Point getMaxOffset();

	void setPopupMenu(JPopupMenu menu);

	void repaint();

	int[] getZOrderMinAndMax();

	void bringToTop();

	void bringToTop(IDrawNode node);

	void sendToBack();

	void sendToBack(IDrawNode node);

	void update(Graphics g);

	BufferedImage saveToImage();

	void paint(Graphics g);

	void paint(Graphics g, int width, int height, boolean isComponent);

	void beginPaint();

	void endPaint();

	void cancelPaint();

	void invalidate(List<IDrawNode> nodes);

	MouseMode getMouseMode();

	void setMouseMode(MouseMode mouseMode);

	Cursor getCurCursor();

	void setCurCursor(Cursor curCursor);

	ICreateNodeSerializable getLastCreateUserDataSerializable();

	void setLastCreateUserDataSerializable(ICreateNodeSerializable lastCreateUserDataSerializable);

	boolean isMTMode();

	int getMTFontSize(int fontSize);

	float getAndroidDpiScale();

	void setOnPageSizeChanged(IOnPageSizeChanged onPageSizeChanged);
	
	void addMouseWheelListener(MouseWheelListener mouseWheelListener);
	
	void setOnScroll(IScroll onScroll);
	
	void setNodeEvent(INode nodeEvent);
	boolean containsNode(String id);
	IActionCommandManager getACM();
	IActionCommandManager getAcm();
	void setAcm(IActionCommandManager acm);

	void clearNodes();
}