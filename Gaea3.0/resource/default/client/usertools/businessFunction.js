var business = {
    getParameterList: function (query) {
        var uiInfo = globalGetUIInfo("wuliaozubangdingcanshu");
        var control = getFrameControlByName(uiInfo, "wlzbdcs_txtseach");
        var MP_Name = getFrameControlValue(control);
        query.start = 0;
        query.size = 0;
        query.param_name = MP_Name;
        var params = {
            "参数列表": {
                command: JSON.stringify(query)
            }
        }
        var control = getFrameControlByName(uiInfo, "wlzbdcs_dglist");
        resetDataSourceControl(control, params);
    },
    getValuesData: function (values, type, length, precision) {
        length = parseInt(length);
        precision = parseInt(precision);
        var values = values.split("，");
        var newvalues = [];
        for (let i = 0; i < values.length; i++) {
            if (type == "int") {
                if (parseInt(values[i]) > Math.pow(10, length)) {
                    mini.alert("字符集int类型不能超过" + length + "位数！");
                    return false;
                }
            } else if (type == "string") {
                if (values[i].length > length) {
                    mini.alert("字符集String类型不能超过" + length + "位！");
                    return false;
                }
            } else if (type == "float") {
                for (let i = 0; i < values.length; i++) {
                    const value = values[i];
                    var num = (value + "").split(".");
                    var max = parseFloat(precision + "0");
                    if (isNaN(parseInt(num))) {
                        mini.alert("值集合输入类型有误！");
                        return false;
                    } else if (parseInt(num[1]) > Math.pow(10, length)) {
                        mini.alert("字符集float类型不能超过" + length + "位数！");
                        return false;
                    } else {
                        if (parseFloat(value) - max > 0) {
                            mini.alert("字符集float类型不能超过" + max + "！");
                            return false;
                        }
                    }
                }

            }
            var str = {
                id: i,
                text: values[i]
            }
            newvalues.push(str)
        }
        if (values == "" || values == undefined) {
            return true;
        } else {
            return JSON.stringify(newvalues);
        }
    }
}//对内方法
/**
 * @param {表名称} listname 
 * @param {是否为修改状态} edit 
 * @param {修改数据或 保存时外部数据} gridData
 * @param {是否直接保存} save
 */
function getConfigDivContent(listname, save, cellback, gridData, edit) {
    var til = "添加自定义参数";
    if (edit) {
        til = "编辑自定义参数";
    }
    GlobalDialog.showFrameDialog("bianjizidingyicanshu", til, function (body, uiInfo) {
        //				var control = getFrameControlByName(uiInfo, "bjbomcs_txtid");
        //				setFrameControlValue(control,getId("WLCS"));
        getDictionary(uiInfo, "bjbomcs_cmbtype", "cslx");
        var edituiInfo = "";
        if (edit) {
            control = getFrameControlByName(uiInfo, "bjbomcs_txtname");
            setFrameControlValue(control, gridData.param_name);
            control = getFrameControlByName(uiInfo, "bjbomcs_cmbtype");
            setFrameControlValue(control, gridData.param_type);
            if (gridData.param_type == "boolean") {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("boolean", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("boolean")
            } else if (gridData.param_type == "float") {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("float", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("float")
            } else if (gridData.param_type == "string") {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("string", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("string")
            } else {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("stringint", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("stringint")
            }
            control = getFrameControlByName(uiInfo, "bjbomcs_numJD");
            setFrameControlValue(control, gridData.param_precision);
            control = getFrameControlByName(uiInfo, "bjbomcs_numlong");
            setFrameControlValue(control, gridData.param_length);
            control = getFrameControlByName(uiInfo, "bjbomcs_txtdefault");
            setFrameControlValue(control, gridData.param_default);
            control = getFrameControlByName(uiInfo, "bjbomcs_nummax");
            setFrameControlValue(control, gridData.param_maxvalue);
            control = getFrameControlByName(uiInfo, "bjbomcs_nummix");
            setFrameControlValue(control, gridData.param_minvalue);
            control = getFrameControlByName(uiInfo, "bjbomcs_txtcvalue");
            var newvalues = "";
            if (gridData.param_valuelist != "" && gridData.param_valuelist!=undefined) {
                var oldvalues = JSON.parse(gridData.param_valuelist);
                for (let i = 0; i < oldvalues.length; i++) {
                    if (i == oldvalues.length - 1) {
                        newvalues += oldvalues[i].text;
                    } else {
                        newvalues += oldvalues[i].text + "，";
                    }
                }
            }
            setFrameControlValue(control, newvalues);
            control = getFrameControlByName(uiInfo, "bjbomcs_txtvalue");
            setFrameControlValue(control, gridData.param_values);
            control = getFrameControlByName(uiInfo, "bjbomcs_txtcslbbh");
            setFrameControlValue(control, gridData.param_code);
            control = getFrameControlByName(uiInfo, "bjbomcs_cmbmust");
            setFrameControlValue(control, gridData.param_must);
            edituiInfo = uiInfo;
        }
        var uiInfo = globalGetUIInfo("bianjizidingyicanshu");
        control = getFrameControlByName(uiInfo, "bjbomcs_cmbtype");
        control.on("valuechanged", function () {
            var uiInfo = globalGetUIInfo("bianjizidingyicanshu");
            if (getFrameControlValue(this) == "boolean") {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("boolean", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("boolean")
            } else if (getFrameControlValue(this) == "float") {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("float", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("float")
            } else if (getFrameControlValue(this) == "string") {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("string", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("string")
            } else {
                var div = getFrameControlByName(uiInfo, "bjbomcs_divlist");//获取要打开窗体的父div
                createUIForName("stringint", div, false);//在此div上打开窗体
                var uiInfo = globalGetUIInfo("stringint")
            }

            if (getFrameControlValue(this) == "boolean") {
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcvalue");
                setFrameControlValue(control, "否，是");
                control.setEnabled(false);
            } else {
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcvalue");
                setFrameControlValue(control, "");
                control.setEnabled(true);
            }
            ControlHelp.setEvent(uiInfo, "bjbomcs_btnsave", "onClick", function () {
                //					var control = getFrameControlByName(uiInfo, "bjbomcs_txtid");
                //					var MP_Id = getFrameControlValue(control);
                var body_uiInfo = globalGetUIInfo("bianjizidingyicanshu");
                control = getFrameControlByName(body_uiInfo, "bjbomcs_txtname");
                var MP_Name = getFrameControlValue(control);

                control = getFrameControlByName(body_uiInfo, "bjbomcs_cmbtype");
                var MP_Type = getFrameControlValue(control);

                control = getFrameControlByName(uiInfo, "bjbomcs_numJD");
                var MP_ValuePrecision = getFrameControlValue(control);//精度 小数点后

                control = getFrameControlByName(uiInfo, "bjbomcs_numlong");
                var MP_Length = getFrameControlValue(control);//长度 小数点前

                control = getFrameControlByName(uiInfo, "bjbomcs_txtdefault");
                var MP_Defaultvalue = getFrameControlValue(control);//缺省值

                control = getFrameControlByName(uiInfo, "bjbomcs_nummax");
                var MP_MaxValue = getFrameControlValue(control);

                control = getFrameControlByName(uiInfo, "bjbomcs_nummix");
                var MP_MinValue = getFrameControlValue(control);

                control = getFrameControlByName(uiInfo, "bjbomcs_txtcvalue");

                var MP_Values = getFrameControlValue(control);//值集合
                if (MP_Values != ""&&MP_Values != undefined) {
                    var MP_Values = business.getValuesData(MP_Values, MP_Type, MP_Length, MP_ValuePrecision);
                    if (MP_Values == false) {
                        return false;
                    }
                }

                control = getFrameControlByName(uiInfo, "bjbomcs_txtvalue");
                var MP_Value = getFrameControlValue(control);//参数值
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcslbbh");
                var Mp_code = getFrameControlValue(control);//参数值
                control = getFrameControlByName(uiInfo, "bjbomcs_cmbmust");
                var MP_Must = getOptionSelected(control).id;//是否必填
                if (MP_Must == "1") {
                    if (MP_Defaultvalue == "" || MP_Defaultvalue == undefined) {
                        mini.alert("必填参数,必须填写默认值！");
                        return false;
                    }
                }
                var rowData = {
                    "param_name": MP_Name,
                    "param_code": Mp_code,
                    "param_type": MP_Type,
                    "param_precision": MP_ValuePrecision,
                    "param_length": MP_Length,
                    "param_default": MP_Defaultvalue,
                    "param_maxvalue": MP_MaxValue,
                    "param_minvalue": MP_MinValue,
                    "param_valuelist": MP_Values,
                    "param_values": MP_Value,
                    "param_must": MP_Must
                }
                if (rowData.param_name == ""||rowData.param_name == undefined) {
                    mini.alert("参数名称不能为空")
                    return false;
                }
                var grid = new TomcatDataGrid(listname);
                var ggrid = grid.grid;
                rowData.group_type = gridData.group_type;
                if (edit) {
                    grid.editRowValue(rowData);
                } else {
                    if (save) {
                        rowData._state = "added";
                        var data = [rowData];
                        publicFunction.saveHasData([data], ["param"], "/ex/group/saveDisposeContent", cellback)
                    } else {
                        ggrid.addRow(rowData, 0);
                        if (cellback) {
                            cellback();
                        }
                        mini.alert("添加成功");
                    }
                }
                var control = getFrameControlByName(uiInfo, "bjbomcs_txtid");
                setFrameControlValue(control, "");
                control = getFrameControlByName(body_uiInfo, "bjbomcs_txtname");
                setFrameControlValue(control, "");
                control = getFrameControlByName(body_uiInfo, "bjbomcs_cmbtype");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_numJD");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_numlong");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtdefault");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_nummax");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_nummix");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcvalue");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtvalue");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcslbbh");
                setFrameControlValue(control, "");
            });
        })
        if (edit) {
            ControlHelp.setEvent(edituiInfo, "bjbomcs_btnsave", "onClick", function () {
                //					var control = getFrameControlByName(uiInfo, "bjbomcs_txtid");
                //					var MP_Id = getFrameControlValue(control);
                var body_uiInfo = globalGetUIInfo("bianjizidingyicanshu");
                control = getFrameControlByName(body_uiInfo, "bjbomcs_txtname");
                var MP_Name = getFrameControlValue(control);

                control = getFrameControlByName(body_uiInfo, "bjbomcs_cmbtype");
                var MP_Type = getFrameControlValue(control);
                var uiInfo = edituiInfo;
                control = getFrameControlByName(uiInfo, "bjbomcs_numJD");
                var MP_ValuePrecision = getFrameControlValue(control);//精度 小数点后

                control = getFrameControlByName(uiInfo, "bjbomcs_numlong");
                var MP_Length = getFrameControlValue(control);//长度 小数点前

                control = getFrameControlByName(uiInfo, "bjbomcs_txtdefault");
                var MP_Defaultvalue = getFrameControlValue(control);//缺省值

                control = getFrameControlByName(uiInfo, "bjbomcs_nummax");
                var MP_MaxValue = getFrameControlValue(control);

                control = getFrameControlByName(uiInfo, "bjbomcs_nummix");
                var MP_MinValue = getFrameControlValue(control);

                control = getFrameControlByName(uiInfo, "bjbomcs_txtcvalue");

                var MP_Values = getFrameControlValue(control);//值集合
                if (MP_Values != "") {
                    var MP_Values = business.getValuesData(MP_Values, MP_Type, MP_Length, MP_ValuePrecision);
                    if (MP_Values == false) {
                        return false;
                    }
                }
                control = getFrameControlByName(uiInfo, "bjbomcs_txtvalue");
                var MP_Value = getFrameControlValue(control);//参数值
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcslbbh");
                var Mp_code = getFrameControlValue(control);//参数值
                control = getFrameControlByName(uiInfo, "bjbomcs_cmbmust");
                var MP_Must = getOptionSelected(control).id;//是否必填
                if (MP_Must == "1") {
                    if (MP_Defaultvalue == "" || MP_Defaultvalue == undefined) {
                        mini.alert("必填参数,必须填写默认值！");
                        return false;
                    }
                }
                var rowData = {
                    "param_name": MP_Name,
                    "param_code": Mp_code,
                    "param_type": MP_Type,
                    "param_precision": MP_ValuePrecision,
                    "param_length": MP_Length,
                    "param_default": MP_Defaultvalue,
                    "param_maxvalue": MP_MaxValue,
                    "param_minvalue": MP_MinValue,
                    "param_valuelist": MP_Values,
                    "param_values": MP_Value,
                    "param_must": MP_Must
                }
                if (rowData.param_name == ""||rowData.param_name == undefined) {
                    mini.alert("参数名称不能为空")
                    return false;
                }
                var grid = new TomcatDataGrid(listname);
                var ggrid = grid.grid;
                rowData.group_type = gridData.group_type;
                if (edit) {
                    grid.editRowValue(rowData);
                } else {
                    if (save) {
                        rowData._state = "added";
                        var data = [rowData];
                        publicFunction.saveHasData([data], ["param"], "/ex/group/saveDisposeContent", cellback)
                    } else {
                        ggrid.addRow(rowData, 0);
                        if (cellback) {
                            cellback();
                        }
                        mini.alert("添加成功");
                    }
                }
                var control = getFrameControlByName(uiInfo, "bjbomcs_txtid");
                setFrameControlValue(control, "");
                control = getFrameControlByName(body_uiInfo, "bjbomcs_txtname");
                setFrameControlValue(control, "");
                control = getFrameControlByName(body_uiInfo, "bjbomcs_cmbtype");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_numJD");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_numlong");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtdefault");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_nummax");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_nummix");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcvalue");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtvalue");
                setFrameControlValue(control, "");
                control = getFrameControlByName(uiInfo, "bjbomcs_txtcslbbh");
                setFrameControlValue(control, "");
            });
        }

    });
}
/**
 * 
 * @param {标题名} titlename 
 * @param {选择后列表名} listname 
 * @param {加载列表需求} query 一般为json格式{group_type:"device"}
 * @param {*} id 
 * @param {*} control 
 */
function getBindConfigContent(titlename, listname, query, id, control) {
    GlobalDialog.showFrameDialog("wuliaozubangdingcanshu", titlename, function (body, uiInfo) {
        business.getParameterList(query);
        ControlHelp.setEvent(uiInfo, "wlzbdcs_btnseach", "onClick", function () {
            business.getParameterList(query);
        })
        ControlHelp.setEvent(uiInfo, "wlzbdcs_btnok", "onClick", function () {
            var datagrid = new TomcatDataGrid("wlzbdcs_dglist");
            var csdata = datagrid.grid.getSelecteds();
            if (csdata.length < 1) {
                GlobalDialog.close();
            }
            var newdata = [];
            for (var i = 0; i < csdata.length; i++) {
                var banDingGrid = new TomcatDataGrid(listname);
                var row = banDingGrid.grid.findRow(function (row) {
                    if (row.param_id == csdata[i].param_id) return true;
                });
                if (row == null || row == undefined) {
                    var rowData = csdata[i];
                    var newrow = {
                        "group_id": id,
                        "param_id": rowData.param_id,
                        "_state": "added"
                    }
                    newdata[i] = newrow;
                } else {
                    mini.alert("不能重复绑定！");
                    return false;
                }
            }
            var postData = new RemoteDataset("relation");
            postData.setData(newdata);
            postData.postAll(
                AJAXHTTPTOTOTOMCATACC + "/ex/group/saveDisposeContent", {},
                function (isdo) {
                    if (isdo) {
                        control.getCsList(id);
                        mini.alert("更新成功！");
                    } else {
                        mini.alert("更新失败！");
                    }
                }
            )
            GlobalDialog.close();
        })
    })
}
/**
 * 
 * @param {页面uiinfo name} uiInfo 
 * @param {Header 的 fiedid} key 
 * @param {表名称} listname 
 * @param {数据字典对应数据} data 
 */
function getConfigDowndata(uiInfo, key, listname, data) {
    var URL = "/ex/dictionary/getDictionaryByGidAndId";
    var uiInfo = globalGetUIInfo(uiInfo);
    Tools.ajaxTomcatSubmit(AJAXHTTPTOTOTOMCATACC + URL, null, {
        data: data,
        id:""
    }, false, false, function (data) {
        var datas = data.data;
        var selectData = {
            [key]: {}
        };
        var sData = selectData[key];
        for (var i = 0; i < datas.length; i++) {
            var data = {
                value: datas[i].sjzd_id,
                text: datas[i].sjzd_xmmc,
            };
            sData[datas[i].sjzd_id] = data;
        }
        var control = getFrameControlByName(uiInfo, listname);
        control = getFrameControl(control);
        control.setDownData(selectData);
    })
}
function setProcessData(listname, gridData, key) {
    GlobalDialog.showFrameDialog("bianjigongyicanshu", "编辑工艺参数", function (b, uiInfo) {
        for (let i in gridData) {
            let k = i.replace(key, "gycsbz")
            gridData[k] = gridData[i];
        }
        getDictionary(uiInfo, "bjgycs_txtsrfs", "srfs", false);
        var control = getFrameControlByName(uiInfo, "bjgycs_txtxm");
        control.setEnabled(false);
        setFrameControlValue(control, gridData.param_name);
        control = getFrameControlByName(uiInfo, "bjgycs_txtpdz");
        if (gridData.param_valuelist != "" && gridData.param_valuelist != undefined) {
            var selectData = gridData.param_valuelist;
            control.setData(selectData);
            control.setValueFromSelect(true);
        }
        setFrameControlValue(control, gridData.gycsbz_defaultvalue);
        control = getFrameControlByName(uiInfo, "bjgycs_txtzxz");
        setFrameControlValue(control, gridData.gycsbz_minvalue);
        control = getFrameControlByName(uiInfo, "bjgycs_txtzdz");
        setFrameControlValue(control, gridData.gycsbz_maxvalue);
        control = getFrameControlByName(uiInfo, "bjgycs_txtff");
        setFrameControlValue(control, gridData.gycsbz_ff);
        control = getFrameControlByName(uiInfo, "bjgycs_txtclff");
        setFrameControlValue(control, gridData.gycsbz_ycclff == undefined?gridData.gycsbz_yccl:gridData.gycsbz_ycclff);
        control = getFrameControlByName(uiInfo, "bjgycs_txtsrfs");
        setFrameControlValue(control, gridData.gycsbz_fs_id == undefined? gridData.gycsbz_fs:gridData.gycsbz_fs_id);
        ControlHelp.setEvent(uiInfo, "bjgycs_btnsave", "onClick", function () {
            var control = getFrameControlByName(uiInfo, "bjgycs_txtpdz");
            var pd = getOptionSelected(control).id;
            var pd_text = getOptionSelected(control).text;
            if (pd == undefined) {
                pd = getFrameControlValue(control); //判定值  
                pd_text = pd;
            }

            control = getFrameControlByName(uiInfo, "bjgycs_txtzxz");
            var min = getFrameControlValue(control); //最小值

            control = getFrameControlByName(uiInfo, "bjgycs_txtzdz");
            var max = getFrameControlValue(control); //最大值

            var control = getFrameControlByName(uiInfo, "bjgycs_txtff");
            var ff = getFrameControlValue(control); //方式

            control = getFrameControlByName(uiInfo, "bjgycs_txtclff");
            var clfs = getFrameControlValue(control); //处理方式

            control = getFrameControlByName(uiInfo, "bjgycs_txtsrfs");
            var srfs = getOptionSelected(control).id; //输入方式
            var srfs_text = getOptionSelected(control).text; //输入方式
            var ismax =publicFunction.rules(max,gridData.param_type,gridData.param_length,gridData.param_precision);
            if (!ismax) {
                return false;
            }
            var ismin =publicFunction.rules(min,gridData.param_type,gridData.param_length,gridData.param_precision);
            if (!ismin) {
                return false;
            }

            var rowData = {
                "gycsbz_defaultvalue": pd,
                "defaultvalue_text": pd_text,
                "gycsbz_minvalue": min,
                "gycsbz_maxvalue": max,
                "gycsbz_ff": ff,
                "gycsbz_ycclff": clfs,
                "gycsbz_fs": srfs_text,
                "gycsbz_fs_id": srfs,
                _state: gridData._state
            }
            for (let i in rowData) {
                let k = i.replace("gycsbz", key);
                rowData[k] = rowData[i];
            }
            var grid = new TomcatDataGrid(listname);
            grid.editRowValue(rowData)
            GlobalDialog.close();
        })
    })
}