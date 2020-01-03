package com.wh.tools;

import java.io.File;
import java.io.IOException;

public interface ICacheInstance {

	File open(File file) throws IOException;

	void save(File saveFile, byte[] datas, boolean needCopy) throws IOException;

}