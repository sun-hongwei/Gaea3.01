package com.wh.gaea.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;

import com.wh.gaea.control.EditorEnvironment;
import com.wh.swing.tools.MsgHelper;
import com.wh.tools.FileHelp;
import com.wh.tools.ICacheInstance;
import com.wh.tools.JsonHelp;

public class FileCache {

	static Map<String, File> netDrivers = new ConcurrentHashMap<>();

	static HashMap<File, File> caches = new HashMap<>();

	public static void addNetDriver(File file) {
		netDrivers.put(file.getAbsolutePath().substring(0, 1).toLowerCase(), file);
	}

	public static boolean isNetFile(File file) {
		String start = file.getAbsolutePath().substring(0, 2);
		boolean b = start.equals("\\");
		if (!b) {
			b = start.equals("//");
		}

		if (!b) {
			String driver = start.trim().substring(0, 1).toLowerCase();
			b = netDrivers.containsKey(driver);
		}
		return b;
	}

	public static File open(File file) throws IOException {
		if (isNetFile(file)) {
			// if (!EditorEnvironment.lockFile(file)) {
			// MsgHelper.showMessage("文件【" + file.getAbsolutePath() +
			// "】已经被其他用户锁定，请稍后再试！");
			// return null;
			// }

			synchronized (caches) {
				long localTime = -1;
				long remoteTime = file.exists() ? file.lastModified() : 0;
				if (!caches.containsKey(file)) {
					File cacheFile = EditorEnvironment.getEditorPath(EditorEnvironment.Cache_Path,
							UUID.randomUUID().toString());
					if (!cacheFile.getParentFile().exists())
						cacheFile.getParentFile().mkdirs();
					caches.put(file, cacheFile);
				}

				File localFile = caches.get(file);
				if (!localFile.exists())
					FileHelp.copyFileTo(file, localFile);
				else {
					localTime = localFile.lastModified();

					if (localTime != file.lastModified()) {
						if (localTime < remoteTime)
							if (MsgHelper.showConfirmDialog("当前缓冲文件的时间戳早于服务端，继续将可能导致您的修改丢失，是否继续？",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								FileHelp.copyFileTo(file, localFile);
							}
					}
				}
				return localFile;

			}
		} else {
			return file;
		}
	}

	static Map<File, File> copys = new HashMap<>();

	static class CopyFileThread implements Runnable {
		@Override
		public void run() {
			File destFile = null;
			File sourceFile = null;
			synchronized (copys) {
				Set<File> keys = copys.keySet();
				if (keys == null || keys.isEmpty())
					return;

				destFile = keys.iterator().next();
				sourceFile = copys.remove(destFile);
				if (destFile == null || sourceFile == null) {
					return;
				}

			}

			while (!EditorEnvironment.lockFile(destFile)) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}
			}

			try {
				FileHelp.copyFileTo(sourceFile, destFile);
			} catch (IOException e) {
				e.printStackTrace();
				MsgHelper.showException(e);
			} finally {
				EditorEnvironment.unlockFile(destFile);
			}
		}
	}

	static ExecutorService pools = Executors.newFixedThreadPool(5);

	public static void copyFile(File source, File dest) {
		synchronized (copys) {
			copys.put(dest, source);
			pools.execute(new CopyFileThread());
		}
	}

	public static void save(File saveFile, byte[] datas, boolean share) throws IOException {
		File copyFile = null;
		boolean needCopy = false;
		if (share) {
			synchronized (caches) {
				needCopy = caches.containsKey(saveFile);
				if (needCopy) {
					copyFile = saveFile;
					saveFile = caches.get(saveFile);
				}
			}
		}

		try (FileOutputStream stream = new FileOutputStream(saveFile);) {
			stream.write(datas, 0, datas.length);
			stream.close();

			if (needCopy) {
				copyFile(saveFile, copyFile);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	public static void clear() throws IOException {
		File cachePath = EditorEnvironment.getEditorPath(EditorEnvironment.Cache_Path, null);
		File[] files = cachePath.listFiles();
		if (files != null)
			for (File file : files) {
				if (!file.delete())
					throw new IOException("delete file[" + file.getAbsolutePath() + "] failed!");
			}
	}
	
	static {
		JsonHelp.setCacheInstance(new ICacheInstance() {
			
			@Override
			public void save(File saveFile, byte[] datas, boolean needCopy) throws IOException {
				FileCache.save(saveFile, datas, needCopy);
			}
			
			@Override
			public File open(File file) throws IOException {
				return FileCache.open(file);
			}
		});
	}
}
