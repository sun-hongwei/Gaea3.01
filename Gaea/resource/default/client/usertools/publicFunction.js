/**
 * @author:zhangcheng
 * Modify the time: 2019-4-9
 */
var publicFunction = {
	/**
	 * 直接删除 
	 * @param {列表名} listname 
	 * @param {数据库表名} tabname 
	 * @param {dom元素} thisControl 
	 * @param {请求连接} URL 
	 * @param {请求主键} id 
	 */
	deleteListItem: function (listname, tabname, thisControl, URL, id) {
		var grid = new TomcatDataGrid(listname);
		var gridData = grid.grid.getSelecteds();
		if (gridData == null || gridData == undefined || gridData.length == 0) {
			mini.alert('未勾选数据!');
			return false;
		} else {
			mini.confirm("确定删除记录？", "确定？",
				function (action) {
					if (action == "ok") {
						var newdata = [];
						for (var i = 0; i < gridData.length; i++) {
							var rowData = {
								"_state": "removed"
							};
							var t = gridData[i]

							if (id == undefined) {
								rowData[tabname + "_id"] = t[tabname + "_id"];
								newdata[i] = rowData;
							} else {
								rowData[id] = t[id];
								newdata[i] = rowData;
							}

						}
						var postData = new RemoteDataset(tabname); //需要添加表名
						postData.setData(newdata);
						postData.postAll(
							URL, {},
							function (isdo) {
								if (isdo) {
									if (thisControl != null) {
										thisControl.getList();
									}
									grid.removeRow();
									mini.alert("删除成功！");
								} else {
									if (window.INLINE)
										mini.alert("数据更新失败！请检查服务！");
									else
										window.INLINE = true;
								}
							}
						)
					}
				});
		};
	},
	/**
	 * 删除 （不保存）
	 * @param {页面表名} listname 
	 */
	deleteNoSave: function (listname) {
		var grid = new TomcatDataGrid(listname);
		var gridData = grid.grid.getSelecteds();
		if (gridData == null || gridData == undefined || gridData.length == 0) {
			mini.alert('未勾选数据!');
			return false;
		} else {
			mini.confirm("确定删除记录？", "确定？",
				function (action) {
					if (action == "ok") {
						grid.removeRow();
					}
				});
		};
	},
	saveHasData: function (data, tab, URL, cellback) {
		var postData = new RemoteDataset(tab[0]);
		postData.setData(data[0]);
		if (tab.length > 0) {
			for (let i = 1; i < tab.length; i++) {
				postData.pushDataset(data[i], tab[i]);
			}
		}
		postData.postAll(
			AJAXHTTPTOTOTOMCATACC + URL, {},
			function (isdo) {
				if (isdo) {
					cellback();
					mini.alert("更新成功！");
				} else {
					mini.alert("更新失败！");
				}
			}
		)
	},
	/**
	 * 
	 * @param {请求类数据} reqData 
	 * @param {列表类数据} listData 
	 * @param {其他数据} other 
	 */
	saveGrid: function (reqData, listData, other) {
		var maskDialog = new FrameDialog();
		maskDialog.mask('保存中...', "inloading");
		var postData = new RemoteDataset(reqData[0].tab);
		for (let q = 0; q < reqData.length; q++) {
			var reqData = reqData[q];
			var listData = listData[q];
			var grid = new TomcatDataGrid(listData.listname);
			var griddata = grid.getSaveData(); //获取 数据表格中的用户编辑数据
			var newdata = [];
			var reqDataHeader = reqData.header;
			for (var i = 0; i < griddata.length; i++) {
				var row = griddata[i];
				var newrow = {
					"_state": row._state
				}
				for (let k = 0; k < reqDataHeader.length; k++) {
					newrow[reqDataHeader[k]] = row[reqDataHeader[k]];
				}
				newdata[i] = newrow;
			}
			if (q == 0) {
				postData.setData(newdata);
			} else {
				postData.pushDataset(newdata, reqData.tab);
			}
		}
		postData.postAll(
			AJAXHTTPTOTOTOMCATACC + reqData.URL, {},
			function (isdo, data) {
				maskDialog.unMask();
				if (data == "ok" && isdo) {
					//var uiInfo = globalGetUIInfo("gongweiweihu");
					mini.alert("更新成功！");
					try {
						listData.control.getList();
					} catch (error) { }
				} else if (data == "fail") {
					mini.alert("更新失败！数据不可重复添加！");
				} else if (data == "complete") {
					mini.alert("更新失败！请先确定解除(绑定)相应关系！");
					try {
						listData.control.getList();
					} catch (error) {
						throw "该Control无getList方法"
					}
				} else {
					if (window.INLINE)
						mini.alert("数据更新失败！请检查服务！");
					else
						window.INLINE = true;
				}
			}, false, true
		)
	},
	/**
	 * 树形图分页加载
	 * @param {树} tree 
	 * @param {节点url} curl 
	 */
	getTreeChildNode: function (tree, curl) {
		tree.on("nodeclick", function (e) {
			var that = this;
			Tools.ajaxTomcatSubmit(curl, null, {
				query: "and id='" + e.node.id + "'"
			}, false, false, function (data) {
				var data = data.data;
				for (let i = 0; i < data.length; i++) {
					var newNode = data[i];
					that.addNode(newNode, "add", e.node);
				}
			})
		})
	},
	rulesFun: function () {
		return true;
	},
	/**
	 * 
	 * @param {*}验证内容 text 
	 * @param {*}验证类型 type 
	 */
	rules: function (text, type, length, jd) {
		switch (type) {
			case "string":
				if (length != undefined) {
					if (text.length > length) {
						mini.alert("字符串长度不能大于" + length);
						return false;
					}
				}
				break
			case "boolean":
				if (text != "0" && text != "1") {
					if (text != "") {
						mini.alert("只能填写1或0")
						return false;
					}
				}
				break
			case "float":
				if (isNaN(text)) {
					mini.alert("请输入数字类型,并且长度不能大于" + length + "并且保留" + length + "位小数！");
					return false;
				} else {
					if (length != undefined && jd != undefined) {
						var i = Math.pow(10, length) - 1;
						var f = Math.pow(10, jd) - 1;
						var data = parseFloat(i + "." + f);
						if (text > data) {
							mini.alert("不能大于" + data);
							return false;
						} else {
							var testtext = text + "";
							testtext = testtext.split(".");
							if (parseInt(testtext[1]) > f) {
								mini.alert("小数点后不能超过" + jd);
								return false;
							}
						}
					}
				}
				break
			case "int":
				if (isNaN(text)) {
					mini.alert("请输入整数！");
					return false;
				} else {
					if (length != undefined) {
						if (parseInt(text) > Math.pow(10, length) - 1) {
							mini.alert("输入不能大于" + Math.pow(10, length) - 1)
							return false;
						}
					}
				}
				break
		}
		return true;
	},
	now: function (d) {
		if (d)
			var date = new Date(d);
		else
			var date = new Date();
		return date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " " +
			date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
	}
};
/**
 * json 转 字符串
 * @param {json 对象} str 
 */
function setJsonString(str) {
	var s = "";
	for (let i in str) {
		if (i != undefined) {
			if (typeof str[i] == "object") {
				var o = setJsonString(str[i])
				s = s + i + ":" + o + ",";
			} else {
				s = s + i + ":" + str[i] + ",";
			}
		}
	}
	s = "{" + s + "}";
	var a = s.replace(",}", "}");
	return a;
}
/**
 * 判断下拉框
 * @param {下拉框} control 
 */
function getOptionSelected(control) {
	var c = control.getSelected();
	if (c) {
		return c;
	} else {
		return {};
	}
}
/**
 * 获取下拉框内容
 * get方法为获取 .data为最后获取的数据
 */
function ComboBoxData() {
	this.data = [];
	this.control = undefined;
	this.selects = false;
	this.gridname = undefined;
	this.params = undefined;
	this.dataSouseName = undefined;
	this.key = "";
	this.get = function (control) {
		var _this = this;
		this.control = control;
		control.on("click", function () {
			var timer = setInterval(function () {
				if (control.isShowPopup()) {
					clearInterval(timer);
					_this.init();
				}
			}, 1)
		})
		control.on("valuechanged", function (e) {
			var selects = e.sender.grid.multiSelect;
			this.selects = selects;
			var gridid = e.sender.grid.id;
			var data = new TomcatDataGrid(gridid);
			if (selects) {
				var alldata = data.grid.getSelecteds();
				_this.data = alldata;
			} else {
				_this.data = [];
				var alldata = data.grid.getSelected();
				_this.data.push(alldata);
			}
		})
	}
	this.init = function () {
		this.getList(this.params);
	}
	this.getNewParams = function (newdata) {
		var old = this.params;
		for (let i in old) {
			old[i].command = JSON.parse(old[i].command);
			var isJson = true;
			if (typeof (old[i].command.querykeys) != "object") {
				isJson = false;
				old[i].command.querykeys = JSON.parse(old[i].command.querykeys);
			}
			var a = 0;
			for (let k in old[i].command.querykeys) {
				if (a == 1)
					break;
				old[i].command.querykeys[k] = newdata;
				a += 1;
			}
			if (!isJson) {
				old[i].command.querykeys = JSON.stringify(old[i].command.querykeys);
			}
			old[i].command = JSON.stringify(old[i].command);
		}
		return old;
	}
	this.getList = function (params) {
		var control = mini.get(this.gridname[0]);
		// if (this.gridname[2] != undefined) {
		// 	var seachbtn = mini.get(this.gridname[2]);
		// 	var _this = this
		// 	seachbtn.on("click", function () {
		// 		var seachtxt = mini.get(_this.gridname[1]).getValue();
		// 		var params = _this.getNewParams(seachtxt);
		// 		resetDataSourceControl(control, params);
		// 	})
		// }
		if (this.dataSouseName) {
			var params = {};
			params[this.dataSouseName] = {
				command: JSON.stringify({
					start: 0,
					size: 10
				})
			}
		}
		resetDataSourceControl(control, params);
	}
}
function Mover(title) {
	this.obj = title;
	this.startx = 0;
	this.starty;
	this.startLeft;
	this.startTop;
	this.mainDiv = title.parentNode;
	var that = this;
	this.isDown = false;
	this.movedown = function (e) {
		e = e ? e : window.event;
		if (!window.captureEvents) {
			this.setCapture();
		}
		//事件捕获仅支持ie
		//            函数功能：该函数在属于当前线程的指定窗口里设置鼠标捕获。一旦窗口捕获了鼠标，
		//            所有鼠标输入都针对该窗口，无论光标是否在窗口的边界内。同一时刻只能有一个窗口捕获鼠标。
		//            如果鼠标光标在另一个线程创建的窗口上，只有当鼠标键按下时系统才将鼠标输入指向指定的窗口。
		//            非ie浏览器 需要在document上设置事件
		that.isDown = true;
		that.startx = e.clientX;
		that.starty = e.clientY;

		that.startLeft = parseInt(that.mainDiv.style.left);
		that.startTop = parseInt(that.mainDiv.style.top);
	}
	this.move = function (e) {
		e = e ? e : window.event;
		if (that.isDown) {
			that.mainDiv.style.left = e.clientX - (that.startx - that.startLeft) + "px";
			that.mainDiv.style.top = e.clientY - (that.starty - that.startTop) + "px";
		}
	}
	this.moveup = function () {
		that.isDown = false;
		if (!window.captureEvents) {
			this.releaseCapture();
		} //事件捕获仅支持ie
	}
	this.obj.onmousedown = this.movedown;
	this.obj.onmousemove = this.move;
	this.obj.onmouseup = this.moveup;

	//非ie浏览器
	document.addEventListener("mousemove", this.move, true);
}
/**
 * 弹出框 拖动
 */
function removeBody() {
	var div = document.getElementsByClassName('showDlg')[0];
	div = div.getElementsByClassName('TitleDiv')[0]
	new Mover(div);
}
function loaded() {

}
function getDictionary(uiInfo, tabname, data, gid, id, cellback) {
	if (gid) {
		var URL = "/ex/dictionary/getDictionaryByGid";
	} else {
		var URL = "/ex/dictionary/getDictionaryByFid";
	}
	var reqData = {
		data: data
	}
	if (id) {
		var URL = "/ex/dictionary/getDictionaryByGidAndId";
		reqData.id = id;
	}
	Tools.ajaxTomcatSubmit(AJAXHTTPTOTOTOMCATACC + URL, null, reqData, false, false, function (data) {
		var datas = data.data;
		var selectData = [];
		for (var i = 0; i < datas.length; i++) {
			selectData[i] = { id: datas[i].sjzd_id, text: datas[i].sjzd_xmmc, pid: datas[i].f_sjzd_id };
		}
		var control = getFrameControlByName(uiInfo, tabname);
		if (gid) {
			control.loadList(selectData, "id", "pid");
		} else {
			control.setData(selectData);
		}
		control.on("valuechanged", function (e) {
			var selects = gid ? this.getSelectedNode() : this.getSelected();
			if (cellback)
				cellback(e, selects);
		})
	})
}

function exportFun(uiInfo, listname, filename) {
	var control = getFrameControlByName(uiInfo, listname);
	control = getFrameControl(control);
	control.exportToExcel(filename, filename + ".xlsx");
}
function filesUpload(uiInfo, btnname, fileType) {
	this.fileds1 = [];
	this.fileds2 = [];
	var _this = this;
	ControlHelp.setEvent(uiInfo, btnname, "onGetUserData", function (file) {
		var newName = new Date().getTime() + "";
		var fieldtext = file.name.split(".");
		fieldtext = fieldtext[fieldtext.length - 1];
		var typeOk = false;//类型控制
		if (fileType != undefined) {
			var typeTxt = '';
			for (let i = 0; i < fileType.length; i++) {
				if (fileType[i] == 'image') {
					if (/\.(gif|jpg|jpeg|png|GIF|JPG|PNG)$/.test("." + fieldtext)) {
						typeOk = true;
					}
				} else if (fieldtext == fileType[i]) {
					typeOk = true;
				}
				typeTxt += fileType[i] + ",";
			}

			if (!typeOk) {
				this.allowupload = false;
				mini.alert("上传文件格式为：" + typeTxt);
				return false;
			}
		}
		_this.fileds1[file.name] = newName + "." + fieldtext;
		return { command: "upload", data: { newName: newName } };
	});
	ControlHelp.setEvent(uiInfo, btnname, "onUploadEnd", function (uploadItem, filename) {
		_this.fileds2.push(filename);
	});
	this.getFiles = function () {
		var control = getFrameControlByName(uiInfo, btnname);
		var fileds = control.options.upload.files;
		var newFileds = [];
		for (var i = 0; i < _this.fileds2.length; i++) {
			var oldName = _this.fileds2[i];
			var newName = _this.fileds1[oldName];
			if (fileds[oldName] == undefined) {
				continue;
			}
			var filed = {
				newName: newName,
				oldName: oldName
			}
			newFileds[i] = filed;
		}
		return newFileds;
	}

}
//大数相加
function sumBigNumber(a, b) {
	var res = '', //结果
		temp = 0; //按位加的结果及进位
	a = a.split('');
	b = b.split('');
	while (a.length || b.length || temp) {
		//~~按位非 1.类型转换，转换成数字 2.~~undefined==0 
		temp += ~~a.pop() + ~~b.pop();
		res = (temp % 10) + res;
		temp = temp > 9;
	}
	return res.replace(/^0+/, '');
}
//浮点数加法运算
function FloatAdd(arg1, arg2) {
	var r1, r2, m;
	try { r1 = arg1.toString().split(".")[1].length } catch (e) { r1 = 0 }
	try { r2 = arg2.toString().split(".")[1].length } catch (e) { r2 = 0 }
	m = Math.pow(10, Math.max(r1, r2));
	return parseFloat((arg1 * m + arg2 * m) / m);
}

//浮点数减法运算
function FloatSub(arg1, arg2) {
	var r1, r2, m, n;
	try { r1 = arg1.toString().split(".")[1].length } catch (e) { r1 = 0 }
	try { r2 = arg2.toString().split(".")[1].length } catch (e) { r2 = 0 }
	m = Math.pow(10, Math.max(r1, r2));
	//动态控制精度长度
	n = (r1 = r2) ? r1 : r2;
	return parseFloat(((arg1 * m - arg2 * m) / m).toFixed(n));
}

//浮点数乘法运算
function FloatMul(arg1, arg2) {
	var m = 0, s1 = arg1.toString(), s2 = arg2.toString();
	try { m += s1.split(".")[1].length } catch (e) { }
	try { m += s2.split(".")[1].length } catch (e) { }
	return parseFloat(Number(s1.replace(".", "")) * Number(s2.replace(".", "")) / Math.pow(10, m));
}


//浮点数除法运算
function FloatDiv(arg1, arg2) {
	var t1 = 0, t2 = 0, r1, r2;
	try { t1 = arg1.toString().split(".")[1].length } catch (e) { }
	try { t2 = arg2.toString().split(".")[1].length } catch (e) { }
	with (Math) {
		r1 = Number(arg1.toString().replace(".", ""));
		r2 = Number(arg2.toString().replace(".", ""));
		return parseFloat((r1 / r2) * pow(10, t2 - t1));
	}
}
(function () {
	if (typeof window.CustomEvent === "function") return false;

	function CustomEvent(event, params) {
		params = params || {
			bubbles: false,
			cancelable: false,
			detail: undefined
		};
		var evt = document.createEvent('CustomEvent');
		evt.initCustomEvent(event, params.bubbles, params.cancelable, params.detail);
		return evt;
	}
	CustomEvent.prototype = window.Event.prototype;
	window.CustomEvent = CustomEvent;
})();
(function () {
	$(document).ready(function () {
		$('#particles').particleground({
			dotColor: '#17b5cd',
			lineColor: '#17b5cd'
		});
	});
})();