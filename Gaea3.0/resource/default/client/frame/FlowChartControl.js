function ApprovalFlowChart() {
    this.svgdiv = undefined;
    this.nodeTypes = [
        { id: "start", text: "开始节点", shape: [{ type: "RadiusRect", param: "120,35" }] },
        { id: "and", text: "AND", shape: [{ type: "Cricle", param: "35" }] },
        { id: "or", text: "XOR", shape: [{ type: "Cricle", param: "35" }] },
        { id: "states", text: "状态节点", shape: [{ type: "RadiusRect" }], data: { id: "", memo: "", name: "", states: "", title: "", zOrder: "5" } },
        { id: "action", text: "业务节点", shape: [{ type: "Rect" }], data: { id: "", initData: "{}", memo: "", model_id: "", name: "", role: "", title: "", zOrder: "5" } },
        { id: "end", text: "结束节点", shape: [{ type: "RadiusRect", param: "120,35" }] }
    ];
    this.allowConnection = false;//默认不连线
    this.allowmove = true;//允许移动
    this.gridDiv = undefined;//显示数据的grid
    this.uiInfo = undefined;
    this.nodeData = [];
    this.allowRemove = false;//双击删除元素
    this.init = function (uiInfo, gridName) {
        var parent = document.getElementById(uiInfo.workflow.id + "div");
        var doc = document.createElement("div");
        doc.style = "width:100%; white-space:nowrap;";
        var str = `
        <div id="myPaletteDiv"
            style="display: inline-block; vertical-align: top; width:6%;border: solid 1px black; height: 600px">
        </div>
        <div id="myDiagramDiv"
            style="display: inline-block; vertical-align: top; width:69%;border: solid 1px black; height: 600px">
        </div>
        `;
        doc.innerHTML = str;
        parent.appendChild(doc);
        this.svgdiv = new lineControl();
        this.svgdiv.prototype = new Svg_Control();
        this.gridDiv = new TomcatDataGrid(gridName, uiInfo);
        this.gridName = gridName;
        this.uiInfo = uiInfo;
        this.drawLeft();
        this.stopOperation();
        this.setChartConnectionRule();
        this.setAllowMove();
        this.flowChartRightGridRoles();
    }
    this.getNodeTypes = function () {
        return this.nodeTypes;
    }
    this.setNodeTypes = function (nodeTypes) {
        this.nodeTypes = nodeTypes
    }

    this.drawLeft = function () {
        var leftdata = this.nodeTypes;
        if (!this.verdictNodeData(leftdata)) {
            alert("id不能重复！");
            return false;
        }
        var fra = document.createDocumentFragment();
        var leftbody = document.getElementById("myPaletteDiv");
        var w = leftbody.offsetWidth;
        var h = leftbody.offsetHeight * 16 / 100;
        var _this = this;
        for (var i = 0; i < leftdata.length; i++) {
            var id = leftdata[i].id;
            var parent = document.createElement("div");
            parent.style = "width:100%;height:10%;padding:0 auto;";
            var doc = document.createElement("button");
            doc.style.width = w + "px";
            doc.style.height = "100%";
            doc.style.lineHeight = h / 2 + "px";
            doc.style.fontSize = h / 97 + "px";
            doc.className = "flowchart_leftbtn";
            doc.innerText = leftdata[i].text == undefined ? "" : leftdata[i].text;
            doc.setAttribute("nodeType", id);
            doc.setAttribute("nodeTypeSize", leftdata[i]);
            doc.onclick = function (e) {
                var doc = e.target;
                var content = this.innerText;
                var states = doc.getAttribute("nodeType");
                var allowDraw = _this.flowChartLeftBtnRoles(states);
                if (allowDraw != false) {
                    _this.draw(content, states);
                }
            };
            parent.appendChild(doc);
            fra.appendChild(parent);
        }
        leftbody.appendChild(fra);
    }
    this.verdictNodeData = function (data) {
        var arr = [];
        for (var i = 0; i < data.length; i++) {
            arr.push(data[i].id);
        }
        var uniqueLeftdata = Tools.unique(arr);
        if (uniqueLeftdata.length == data.length)
            return true
        else
            return false
    }
    this.getNodeTypeById = function (id) {
        for (var i = 0; i < this.nodeTypes.length; i++) {
            if (this.nodeTypes[i].id == id) {
                return this.nodeTypes[i];
            }
        }
    }
    this.getNodeById = function (id) {
        for (var i = 0; i < this.nodeData.length; i++) {
            var data = this.nodeData[i];
            data = this.getConversiondata(data);
            if (data.id == id) {
                return data;
            }
        }
    }
    this.draw = function (content, states, historyData) {
        var data = this.getNodeTypeById(states);
        if (data.shape == undefined) {
            if (data.text) {
                var doc =  this.svgdiv.drawText(content, data.style, data.id)
            }
        } else if (data.shape.length == 1 && data.text == undefined) {
            if (data.shape[0].style == undefined) {
                eval('var doc = this.svgdiv.draw' + data.shape[0].type + '(' + states + ')')
            } else {
                eval('var doc = this.svgdiv.draw' + data.shape[0].type + '(' + states + ',' + data.shape[0].style + ')')
            }
        } else {
            var docs =[];
            var shapes = data.shape;
            for (var i = 0; i < shapes.length; i++) {
                docs.push(shapes[i]);
            }
            if (content) {
                docs.push({ type: "Text", param: "'" + content + "'" })
            }
            docs[0].states = states;
            var _this = this;
            var doc = this.svgdiv.drawComposite(docs, 0, 0, 0, true, states, content, function (doc) {
                _this.svgdiv.moveAndRedrawLine(doc)
            })
            if (historyData) {
                var data = this.svgdiv.getDataByid(doc.uiid);
                data.id = historyData.id;
                doc.uiid = historyData.id;
                var childs = historyData.childs;
                var oldchilds = doc.Childs;
                for (var i = 0; i < childs.length; i++) {
                    var c = childs[i];
                    for (var k = 0; k < oldchilds.length; k++) {
                        var f = oldchilds[k];
                        if (f.type == c.type) {
                            f.attr(c.style);
                        }
                    }
                }
                doc.attr(historyData.style);
            }
        }
        this.setClick(doc, function () {
            var docData = _this.svgdiv.getDataByid(doc.uiid);
            if (docData.states == "states" || docData.states == "action") {
                var hasData = [];
                for (var k = 0; k < _this.nodeData.length; k++) {
                    var e = _this.nodeData[k];
                    if (e[0].value == doc.uiid) {
                        hasData = e;
                    }
                }
                var newData = [];
                if (hasData.length > 0) {
                    newData = hasData;
                } else {
                    var nodedata = data.data;
                    for (var i in nodedata) {
                        var e = {
                            attr: i,
                            value: nodedata[i]
                        }
                        if (i == "id") {
                            e.value = doc.uiid;
                        }
                        newData.push(e)
                    }
                    _this.nodeData.push(newData);
                }
            }
            var grid = _this.gridDiv;
            grid.grid.setData(newData);
            if (_this.connection) {
                _this.svgdiv.connectionSet(doc);
            }
        })
        this.setDbClick(doc, function () {
            if (_this.allowRemove) {
                var d = _this.svgdiv.getDataByid(doc.uiid);
                if (d.states == "action" || d.states == "states") {
                    for (var i = 0; i < _this.nodeData.length; i++) {
                        var e = _this.nodeData[i];
                        if (e[0].value == doc.uiid) {
                            _this.nodeData.splice(i, 1);
                        }
                    }
                }
                _this.svgdiv.remove(doc);
                var grid = new TomcatDataGrid("flowChartgrid");
                grid.grid.setData([]);
            }
        })
    }
    this.setAllowConnection = function () {
        this.connection = true;
    }
    this.stopOperation = function () {
        this.connection = false;
        this.allowRemove = false;
    }
    this.setAllowMove = function () {
        this.svgdiv.allowmove = this.allowmove
    }
    this.setClick = function (doc, cellback) {
        this.svgdiv.setSelected(doc, cellback);
    }
    this.setDbClick = function (doc, cellback) {
        this.svgdiv.setDbClick(doc, cellback)
    }
    this.setLeftRoles = undefined;
    this.setLeftBtnRoles = function (cellback) {
        this.setLeftRoles = cellback;
    }
    this.flowChartLeftBtnRoles = function (states) {
        var datas = this.svgdiv.data;
        var result = {};
        for (var index = 0; index < datas.length; index++) {
            if (datas[index].states) {
                result[datas[index].states] = result[datas[index].states] == undefined ? 1 : result[datas[index].states] + 1;
            }
        }
        if (states == "start") {
            if (result.start == 1) {
                alert("只能存在一个开始节点")
                return false;
            }
        } else if (states == "end") {
            if (result.end == 1) {
                alert("只能存在一个结束节点")
                return false;
            }
        }
        if (this.setLeftRoles != undefined) {
            return this.setLeftRoles(states, result);
        }
    }
    this.setRightRoles = undefined;
    this.setRightGridRoles = function (cellback) {
        this.setRightRoles = cellback;
    }
    this.flowChartRightGridRoles = function () {
        var _this = this;
        ControlHelp.setEvent(this.uiInfo, this.gridName, "cellendedit", function (e) {
            var grid = new TomcatDataGrid("flowChartgrid", _this.uiInfo)
            var uiid = grid.getValue("value", 0);
            if (e.field == "value") {
                var row = e.row;
                if (row.attr == "name") {
                    _this.svgdiv.setTextDocValue(uiid, e.value);
                }
            }
        })
        if (this.setRightRoles != undefined) {
            return this.setRightRoles(this.uiInfo, this.gridName);
        }
    }
    this.setChartConnectionRule = function (cellback) {
        this.flowChartConnectionRule(function (beginDoc, endDoc) {
            var beginData = this.getDataByid(beginDoc.uiid);
            var endData = this.getDataByid(endDoc.uiid);
            if (beginData.states == endData.states) {
                alert('同样状态节点不能连接！')
                return false;
            } else {
                if (cellback) {
                    return cellback(beginDoc, endDoc)
                }
            }
        });
    }
    this.flowChartConnectionRule = function (cellback) {
        this.svgdiv.connectionSetRule(cellback);
    }
    this.getLastNode = function (id) {
        var result = this.svgdiv.getLinesByUiid(id);
        var linesEnd = result.end;
        var arr = [];
        for (var i = 0; i < linesEnd.length; i++) {
            var data = this.svgdiv.getDataByid(linesEnd[i].begin);
            arr.push(data)
        }
        return arr;
    }
    this.getRelatedNode = function (id) {
        var result = this.svgdiv.getLinesByUiid(id);
        var linesBegin = result.begin;
        var arr = [];
        for (var k = 0; k < linesBegin.length; k++) {
            var data = this.svgdiv.getDataByid(linesBegin[k].end);
            if (data.states != "states") {
                alert("业务节点只能连接状态节点")
                return false;
            } else {
                var nodeData = this.nodeData;
                for (var j = 0; j < nodeData.length; j++) {
                    if (nodeData[j][0].value == linesBegin[k].end) {
                        var e = this.getConversiondata(nodeData[j])
                        arr.push(e);
                    }
                }
            }
        }
        var lastNodes = this.getLastNode(id);
        var states = this.getActionStates(lastNodes);

        var next = [];
        var data = [];
        for (var l = 0; l < arr.length; l++) {
            next.push(arr[l].states);
            data.push(data);
        }
        var result = {};
        result.next = next;
        result.states = states;
        result.data = data;
        return result;
    }
    this.getActionStates = function (lastNodes) {
        var states = [];
        for (var i = 0; i < lastNodes.length; i++) {
            if (lastNodes[i].states == "states") {
                states.push(lastNodes[i]);
            }
        }
        if (states.length > 1) {
            alert("一个业务节点只能被一个状态节点连接！");
            return false;
        }
        if (lastNodes.length > 1) {
            alert("一个业务节点只能被一个状态节点或AND节点连接！")
            return false;
        }
        if (states.length == 1) {
            return states[0];
        } else {
            if (lastNodes[0].states == "and") {
                var lastNodes = this.getLastNode(lastNodes[0].id);
                return this.getActionStates(lastNodes);
            } else {
                alert("一个业务节点只能被一个状态节点或AND节点连接！");
                return false;
            }
        }
    }
    this.getConversiondata = function (e) {
        var conversiondata = {};
        for (var k = 0; k < e.length; k++) {
            conversiondata[e[k].attr] = e[k].value;
        }
        return conversiondata;
    }
    this.release = function () {
        var nodeData = this.nodeData;
        var datas = this.svgdiv.data;
        var lineData = this.svgdiv.connectionList;
        if (datas.length == 0) {
            alert("暂无可发送流程！");
            return false;
        }
        if (nodeData.length == 0) {
            alert("流程中不能没有业务节点和状态节点!\r\n或业务节点和状态节点没设置相关属性！");
            return false;
        }
        if (lineData.length == 0) {
            alert("请连线！");
            return false;
        }
        if (this.ConfirmNodeConnection() == false) {
            return false;
        }
        if (this.ConfirmNodeInfo() == false) {
            return false;
        }

        var process = {};
        var start = "";
        var state = {};
        if (this.getStartStates() == false) {
            return false;
        } else {
            start = this.getStartStates();
        }
        for (var i = 0; i < nodeData.length; i++) {
            var e = nodeData[i];
            var conversiondata = this.getConversiondata(e);
            var docData = this.svgdiv.getDataByid(conversiondata.id);
            if (docData.states == "action") {
                var relatedNode = this.getRelatedNode(conversiondata.id);
                if (!relatedNode) {
                    return false;
                }
                var next = relatedNode.next;
                var states = relatedNode.states.states;
                process[conversiondata.id] = {
                    next: next,
                    role: conversiondata.role,
                    workflow: {
                        value: conversiondata.model_id,
                        key: conversiondata.title
                    },
                    id: conversiondata.id,
                    state: states,
                    initData: conversiondata.initData
                }
            }
            if (docData.states == "states") {
                var statesData = this.getStatesData(conversiondata.id);
                if (statesData) {
                    state[conversiondata.states] = statesData;
                }
            }
        }
        var submitData = {
            process: process,
            start: start,
            state: state
        }
        var fileName = prompt("请输入流程名称：");
        if (fileName == "") {
            alert("流程名不能为空！");
            return false;
        }
        console.log("提交数据", submitData);
        var flowData = this.getFlowChartData();
        Tools.uploadScheduler(fileName, submitData, flowData);
    }
    this.getStatesData = function (id) {
        var action = [];
        var next = "";
        var prev = [];
        var nextNodes = this.getNextNodes(id);
        var lastNodes = this.getLastNode(id);
        for (var l = 0; l < lastNodes.length; l++) {
            var e = lastNodes[l];
            if (e.states == "and") {
                var andlastnode = this.getLastNode(e.id);
                for (var f = 0; f < andlastnode.length; f++) {
                    var aln = andlastnode[f];
                    var states = this.getNodeById(aln.id).states;
                    prev.push(states);
                    var oper = "otAnd";
                }
            }
        }
        for (var i = 0; i < nextNodes.length; i++) {
            var data = this.svgdiv.getDataByid(nextNodes[i].id)
            switch (data.states) {
                case "action":
                    action.push(data.id)
                    break;
                case "and":
                    var andnextdata = this.getNextNodes(data.id);
                    for (var k = 0; k < andnextdata.length; k++) {
                        var e = andnextdata[k];
                        if (e.states == "action") {
                            action.push(e.id)
                            var oper = "otAnd";
                        }
                        if (e.states == "states") {
                            next = this.getNodeById(e.id).states;
                        }
                    }
                    break;
                case "or":
                    return false;
                default:
                    break;
            }
        }
        var result = {};
        if (action.length > 0) {
            result.action = action
        }
        if (next != "") {
            result.next = next;
        }
        if (prev.length > 0) {
            result.prev = prev;
        }
        if (oper) {
            result.oper = oper;
        }
        return result;
    }
    this.getNextNodes = function (id) {
        var lines = this.svgdiv.connectionList;
        var nextNodes = [];
        for (var i = 0; i < lines.length; i++) {
            var l = lines[i];
            if (l.begin == id) {
                var nextdata = this.svgdiv.getDataByid(l.end)
                nextNodes.push(nextdata);
            }
        }
        return nextNodes;
    }
    this.getStartStates = function () {
        var data = this.svgdiv.data;
        for (var i = 0; i < data.length; i++) {
            var e = data[i];
            if (e.states == "start") {
                var line = this.svgdiv.getLinesByUiid(e.id);
                var id = line.begin[0].end;
                var doc = this.svgdiv.getDataByid(id);
                if (doc.states == "states") {
                    var nodes = this.nodeData;
                    for (var k = 0; k < nodes.length; k++) {
                        var e = nodes[k];
                        var d = this.getConversiondata(e);
                        if (d.id == id) {
                            return d.states;
                        }
                    }
                } else {
                    alert("开始节点必须连接状态节点")
                    return false;
                }
            }
        }
    }
    this.ConfirmNodeConnection = function () {
        var datas = this.svgdiv.data;
        var lines = this.svgdiv.connectionList;
        var hasStart = false;
        var hasEnd = false;
        for (var i = 0; i < datas.length; i++) {
            var e = datas[i];
            if (e.type != "comp") {
                continue;
            }
            var has = false;
            for (var k = 0; k < lines.length; k++) {
                var l = lines[k];
                if (l.begin == e.id || l.end == e.id) {
                    has = true;
                    if (e.states == "end" && l.begin == e.uiid) {
                        alert("结束节点不能连接其他节点！");
                        return false;
                    }
                    if (e.states == "begin" && l.end == e.uiid) {
                        alert("开始节点不能被其他节点连接！");
                        return false;
                    }
                }
            }
            if (e.states == "start") {
                hasStart = true;
            }
            if (e.states == "end") {
                hasEnd = true;
            }
            if (!has) {
                alert("请确定所有节点都已连线！");
                return false;
            }
        }
        var hasNecessaryNode = hasStart ? hasEnd : hasStart;
        if (!hasNecessaryNode) {
            alert("必须含有开始和结束节点！")
            return false;
        }
    }
    this.ConfirmNodeInfo = function () {
        var nodes = this.nodeData;
        for (var i = 0; i < nodes.length; i++) {
            var e = nodes[i];
            //name role model_id title states
            e = this.getConversiondata(e);
            if (e.name == "" || e.title == "") {
                alert("name,title不能为空")
                return false;
            }
            if (e.role) {
                if (e.role == "") {
                    alert("role 权限不能为空")
                    return false;
                }
            }
            if (e.model_id) {
                if (e.model_id == "") {
                    alert("model_id 不能为空")
                    return false;
                }
            }
            if (e.states) {
                if (e.states == "") {
                    alert("states 不能为空")
                    return false;
                }
            }
        }
        return true;
    }
    this.getFlowChartData = function () {
        var docdata = this.svgdiv.data;
        var lineData = this.svgdiv.connectionList;
        var nodedata = this.nodeData;
        var postData = {
            doc: [],
            line: [],
            node: nodedata
        }
        for (var i = 0; i < docdata.length; i++) {
            var e = docdata[i];
            var doc = {
                id: e.id,
                childs: [],
                style: this.svgdiv.getDocAttr(e.doc),
                state: e.states,
                type: e.type,
                text: e.text
            }
            if (e.doc.Childs) {
                var Childs = e.doc.Childs;
                for (var k = 0; k < Childs.length; k++) {
                    doc.childs.push({
                        id: Childs[k].uiid,
                        style: this.svgdiv.getDocAttr(Childs[k])
                    })
                }
            }
            if (e.type == "comp") {
                postData.doc.push(doc);
            }
        }
        for (var l = 0; l < lineData.length; l++) {
            var e = lineData[l];
            var str = {
                begin: e.begin,
                end: e.end
            }
            postData.line.push(str);
        }
        return postData;
    }
    this.getHistroy = function (data) {
        var docs = data.doc;
        for (var i = 0; i < docs.length; i++) {
            var e = docs[i];
            if (e.type != "Line") {
                this.draw(e.text, e.state, e);
            }
        }
        var lines = data.line;
        for (var l = 0; l < lines.length; l++) {
            var e = lines[l];
            this.reDrawLine(e);
        }
        this.nodeData = data.node;
    }
    this.reDrawLine = function (data) {
        var beginNode = this.svgdiv.getDataByid(data.begin).doc;
        var endNode = this.svgdiv.getDataByid(data.end).doc;
        this.svgdiv.connectionReset(endNode, beginNode);
    }
    this.isEmpty = function () {
        var datas = this.svgdiv.data;
        if (datas.length > 0) {
            return false;
        } else {
            $("myDiagramDiv").empty();
            return true;
        }
    }
    this.clear = function(){
        this.nodeData = [];
        this.svgdiv.clear();
    }
}