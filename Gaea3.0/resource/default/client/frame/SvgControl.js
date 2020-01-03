function Svg_Control() {
    this.canvas = SVG('myDiagramDiv').size('100%', '100%');
    this.data = [];
    this.allowmove = true;
    this.needChilds = false;
    this.drawText = function (textContent, x, y, states) {
        var text = this.canvas.text(function (add) {
            add.tspan(textContent).newLine()
        })
        if (x, y) {
            text.move(x, y);
        }
        text.font({ fill: '#fff', family: 'Inconsolata' })
        var id = "text" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        text.uiid = id;
        text.states = states;
        text.lineData = [];
        text.LinePoint = [];
        var newdata = {
            id: id,
            type: "Text",
            doc: text,
            text: textContent
        }
        this.data.push(newdata);
        return text;
    }
    this.drawRect = function (w, h) {
        w = w == undefined ? 100 : w;
        h = h == undefined ? 40 : h;
        var rect = this.canvas.rect(w, h);
        var id = "rect" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        rect.uiid = id;
        rect.lineData = [];
        rect.LinePoint = [];
        var newdata = {
            id: id,
            type: "Rect",
            doc: rect
        }
        this.data.push(newdata);
        return rect;
    }
    this.drawCricle = function (r) {
        r = r == undefined ? 20 : r;
        var circle = this.canvas.circle(r)
        var id = "circle" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        circle.uiid = id;
        circle.lineData = [];
        circle.LinePoint = [];
        var newdata = {
            id: id,
            type: "Cricle",
            doc: circle
        }
        this.data.push(newdata);
        return circle;
    }
    this.drawRadiusRect = function (w, h, x, y, r) {
        w = w == undefined ? 90 : w;
        h = h == undefined ? 30 : h;
        r = r == undefined ? 15 : r;
        var rect = this.canvas.rect(w, h).radius(r);
        if (x, y) {
            rect.move(x, y)
        }
        rect.draggable();
        var id = "radiusRect" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        rect.uiid = id;
        var newdata = {
            id: id,
            type: "RadiusRect",
            doc: rect
        }
        this.data.push(newdata);
        return rect;
    }
    this.drawEllipse = function () {
        var ellipse = this.canvas.ellipse(200, 100)
        var id = "ellipse" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        var newdata = {
            id: id,
            type: "Ellipse",
            doc: ellipse
        }
        this.data.push(newdata);
        return ellipse;
    }
    this.drawLine = function (x1, y1, x2, y2) {
        var line = this.canvas.line(x1, y1, x2, y2).stroke({ width: 4, color: '#000' })
        var id = "line" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        line.uiid = id;
        var newdata = {
            id: id,
            type: "Line",
            doc: line
        }
        this.data.push(newdata);
        return line;
    }
    this.drawPolyline = function () {
        var polyline = this.canvas.polyline('0,0 100,50 50,100').fill('none').stroke({ width: 1 })
        var id = "rand" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        var newdata = {
            id: id,
            type: "Polyline",
            doc: polyline
        }
        this.data.push(newdata);
        return polyline;
    }
    /**
     * types {array} 元素集合
     * x,y {number} 初始位置
     * allowMove {boolean} 是否可以拖动
     */
    this.drawComposite = function (types, x, y, r, allowMove, states, content, cellback) {
        var id = "comp" + new Date().getTime() + "_" + Math.floor(Math.random() * 1000);
        var docs = [];
        for (var i = 0; i < types.length; i++) {
            param = types[i].param;
            if (types[i].pos) {
                if (types[i].pos.x && types[i].pos.y) {
                    x = x == undefined ? types[i].pos.x : x + types[i].pos.x;
                    y = y == undefined ? types[i].pos.y : y + types[i].pos.y;
                    r = r == undefined ? (types[i].pos.r == undefined ? r == undefined : types[i].pos.r) : r;
                }
            }
            if (x, y) {
                if (r) {
                    param += "," + x + "," + y + "," + r;
                } else {
                    param += "," + x + "," + y;
                }
            } else if (r) {
                param += "," + r;
            }

            eval("var doc = this.draw" + types[i].type + "(" + param + ");docs.push(doc)");
        }
        if (!this.needChilds) {
            for (var i = 0; i < docs.length; i++) {
                for (var k = 0; k < this.data.length; k++) {
                    if (this.data[k].id == docs[i].uiid) {
                        this.data.splice(k, 1);
                    }
                }
            }
        }
        var doc = docs[0];
        doc.uiid = id;
        doc.Childs = docs.splice(1, 1);
        if (allowMove) {
            var _this = this;
            this.startMove(doc, "move", function (doc, x, y) {
                var Childs = doc.Childs;
                for (var i = 0; i < Childs.length; i++) {
                    var c = Childs[i];
                    var box = _this.getDocAttr(doc);
                    if (c.type == "text") {
                        var cw = c.length() / 2;
                        var ch = 10;
                    }
                    var x = box.x + box.w / 2 - cw;
                    var y = box.y + box.h / 2 - ch;
                    c.move(x, y);
                }
                cellback(doc);
                // _this.moveAndRedrawLine(doc);
            })
        }

        if (types[0].pos == undefined) types[0].pos = "center";
        var box = this.getDocAttr(doc);
        switch (types[0].pos) {
            case "center":
                for (var i = 0; i < doc.Childs.length; i++) {
                    var d = doc.Childs[i];
                    if (d.type == "text") {
                        d.move(box.x + box.w / 2 - d.length() / 2, box.y + box.h / 2 - 10);
                    }
                }
                break;
            case "bottom":
                for (var i = 0; i < doc.Childs.length; i++) {
                    var d = doc.Childs[i];
                    if (d.type == "text") {
                        d.move(box.x + box.w / 2 - d.length() / 2, box.y + box.h - 10);
                    }
                }
                break;
            default:
                break;
        }
        var newDoc = {
            id: id,
            doc: doc,
            type: "comp",
            text: content,
            states: states
        }
        this.data.push(newDoc);
        return doc
    }
    this.moveTo = function (doc, x, y) {
        doc.move(x, y);
    }
    this.startMove = function (doc, type, cellback) {
        doc.draggable();
        if (type == "start") {
            doc.dragstart = function (delta, event) {
                // console.log("start", delta.x, delta.y)
                cellback(this, delta.x, delta.y)
            }
        } else if (type == "move") {
            doc.dragmove = function (delta, event) {
                cellback(this, event.offsetX, event.offsetY)
            }
        } else if (type == "end") {
            doc.dragend = function (delta, event) {
                //console.log("end", delta.x, delta.y)
                cellback(this, delta.x, delta.y)
            }
        }
    }
    this.stopMove = function (doc) {
        doc.fixed();
    }
    this.getDocuments = function () {
        var alldata = this.data;
        var docs = [];
        for (var i = 0; i < alldata.length; i++) {
            var node = alldata[i];
            var doc = node.doc;
            docs.push(doc)
        }
        return docs;
    }
    this.getDocumentsByUiid = function (uiid) {
        var docs = this.getDocuments();
        for (var i = 0; i < docs.length; i++) {
            if (docs[i].uiid == uiid) {
                return docs[i];
            }
        }
    }
    this.getDataByid = function (uiid) {
        var datas = this.data;
        for (var i = 0; i < datas.length; i++) {
            if (datas[i].id == uiid) {
                return datas[i];
            }
        }
    }
    this.setAllStopMove = function () {
        var docs = this.getDocuments();
        for (var i = 0; i < docs.length; i++) {
            this.stopMove(docs);
        }
    }
    this.remove = function (doc) {
        if (doc) {
            var data = this.data;
            for (var i = 0; i < data.length; i++) {
                if (data[i].doc.uiid == doc.uiid) {
                    data[i].doc.remove();
                    var Childs = data[i].doc.Childs;
                    if (Childs) {
                        for (var k = 0; k < Childs.length; k++) {
                            Childs[k].remove();
                            this.remove(Childs[k])
                        }
                    }
                    this.data.splice(i, 1);
                }
            }
        }
    }
    this.clear = function () {
        var data = this.data;
        for (var i = 0; i < data.length; i++) {
            data[i].doc.remove();
            var Childs = data[i].doc.Childs;
            if (Childs) {
                for (var k = 0; k < Childs.length; k++) {
                    Childs[k].remove();
                    this.remove(Childs[k])
                }
            }
        }
        this.data = [];
        this.selectedDoc = [];
        this.connectionList = [];
    }
    this.setSelected = function (doc, cellback) {
        var _this = this;
        doc.click(function (e) {
            _this.isMultiSelect(this, cellback);
        });
    }
    this.setDbClick = function (doc, cellback) {
        doc.dblclick(function () {
            cellback();
        })
    }
    this.getDocAttr = function (doc) {
        return {
            x: doc.type == "ellipse" ? doc.attr("cx") - doc.attr("rx") : doc.attr("x"),
            y: doc.type == "ellipse" ? doc.attr("cy") - doc.attr("ry") : doc.attr("y"),
            w: doc.type == "ellipse" ? doc.attr("rx") * 2 : doc.attr("width"),
            h: doc.type == "ellipse" ? doc.attr("ry") * 2 : doc.attr("height"),
        }
    }
    this.selectedDoc = [];
    this.multiSelect = false;//false 单选
    this.isMultiSelect = function (e, cellback) {
        if (e.selected) {
            e.selected = false;
            e.fill({ color: "#000" })
            this.closeSelectDoc();
        } else {
            //暂时单选
            this.pushSelectedDoc(e);
            this.closeSelectDoc();
            e.fill({ color: '#f06' })
            e.selected = true;
        }
        if (cellback) {
            cellback(e)
        }
    }
    this.pushSelectedDoc = function (e) {
        var olddoc = this.selectedDoc;
        if (olddoc.length != 0) {
            for (var i = 0; i < olddoc.length; i++) {
                var doc = olddoc[i];
                var has = false;
                if (doc.uiid == e.uiid) {
                    var has = true;
                    break;
                }
            }
            if (!has) {
                this.selectedDoc.push(e);
            }
        } else if (olddoc.length == 0) {
            this.selectedDoc.push(e);
        }

    }
    this.closeSelectDoc = function () {
        var docs = this.selectedDoc;
        for (var i = 0; i < docs.length; i++) {
            var doc = docs[i];
            if (doc.selected) {
                doc.selected = false;
                doc.fill({ color: "#000" });
            }
        }
    }
    this.setTextDocValue = function (id, value) {
        var data = this.getDataByid(id);
        if (data.type == "Text") {
            var oldDocAttr = data.doc.attr();
            var newtext = this.drawText(value, oldDocAttr.x, oldDocAttr.y, data.states);
            newtext.attr(oldDocAttr);
            newtext.uiid = id;
            data.doc = newtext;
        } else if (data.type == "comp") {
            var Childs = data.doc.Childs;
            for (var i = 0; i < Childs.length; i++) {
                var e = Childs[i];
                if (e.type == "text") {
                    var oldDocAttr = e.attr();
                    var id = e.uiid;
                    if (this.needChilds) {
                        this.remove(e);
                    } else {
                        e.remove();
                    }
                    var newtext = this.drawText(value, oldDocAttr.x, oldDocAttr.y, data.states);
                    if (!this.needChilds) {
                        this.removeDataById(newtext.uiid);
                    }
                    newtext.attr(oldDocAttr);
                    newtext.uiid = id;
                    Childs.splice(i, 1);
                    Childs.push(newtext);
                    data.text = value;
                }
            }
        } else {
            alert("该元素暂无文字可修改！")
        }
    }
    this.removeDataById = function (id) {
        var datas = this.data;
        for (var i = 0; i < datas.length; i++) {
            var e = datas[i];
            if (e.id == id) {
                datas.splice(i, 1);
            }
        }
    }
}
function lineControl() {
    Svg_Control.call(this);
    //线、连线控制
    this.connectionList = [];
    this.connectionAnthorRule = undefined;
    this.connectionSet = function (doc) {
        var data = this.selectedDoc;
        if (data.length != 0) {
            this.connectionDrawLine(doc);
        }
    }
    this.connectionReset = function (endNode, beginNode) {
        this.connectionDrawLine(endNode, beginNode);
    }
    this.connectionSetRule = function (cellback) {
        this.connectionAnthorRule = cellback;
    }
    this.connectionDrawLine = function (doc, beginNode) {
        var hasSelDoc = undefined;
        for (var k = 0; k < this.selectedDoc.length; k++) {
            var hassel = this.selectedDoc[k];
            if (hassel.selected) {
                hasSelDoc = hassel;
                break;
            }
        }
        if (!beginNode) {
            var beginNode = hasSelDoc;
        }
        if (beginNode == undefined) {
            return false;
        }
        var linelist = this.connectionList;
        var hasLine = false;
        for (var i = 0; i < linelist.length; i++) {
            if (beginNode.uiid == linelist[i].begin) {
                if (doc.uiid == linelist[i].end) {
                    hasLine = true;
                }
            } else if (beginNode.uiid == linelist[i].end) {
                if (doc.uiid == linelist[i].begin) {
                    hasLine = true;
                }
            }
        }
        if (this.connectionAnthorRule(beginNode, doc) == false) {
            return false
        } else if (hasLine) {
            alert("已经连线！")
            return false;
        } else {
            var beginNodePos = this.getDocAttr(beginNode);
            var endNodePos = this.getDocAttr(doc);
            var data = this.getLigatureRoute(beginNodePos, endNodePos);
            var newLine = this.drawLine(data.begin.x, data.begin.y, data.end.x, data.end.y);
            var Ipoint = this.drawIntersectionPoint(data, endNodePos);
            this.connectionList.push({ line_id: newLine.uiid, begin: beginNode.uiid, end: doc.uiid, point: Ipoint.uiid })
            Ipoint.back()
            newLine.back()
            this.reMoveLine(newLine, Ipoint);
        }
    }
    this.getLigatureRoute = function (beginNode, endNode) {
        var beginCenterPos = {
            x: beginNode.x + beginNode.w / 2,
            y: beginNode.y + beginNode.h / 2,
        }
        var endCenterPos = {
            x: endNode.x + endNode.w / 2,
            y: endNode.y + endNode.h / 2
        }
        return { begin: beginCenterPos, end: endCenterPos };
    }
    this.drawIntersectionPoint = function (data, endNodePos) {
        var point = this.findIntersectionPoint(data, endNodePos);
        if (point) {
            var Ipoint = this.drawRect(15, 15);
            Ipoint.move(point.x - 5, point.y - 5);
            return Ipoint;
        }
    }
    this.redrawLine = function (doc, data, type) {
        for (var i = 0; i < data.length; i++) {
            var e = data[i];
            var oldLine = this.getDocumentsByUiid(e.line_id);
            this.remove(oldLine);
            var anotherid = type == "end" ? e.begin : e.end;
            var anotherNode = this.getDocumentsByUiid(anotherid);
            var anotherNodeAttr = this.getDocAttr(anotherNode);
            var box = this.getDocAttr(doc);
            var begin = { x: box.x + box.w / 2, y: box.y + box.h / 2 };
            var end = { x: anotherNodeAttr.x + anotherNodeAttr.w / 2, y: anotherNodeAttr.y + anotherNodeAttr.h / 2 };
            var newLine = this.drawLine(begin.x, begin.y, end.x, end.y);
            newLine.uiid = e.line_id;
            var LinePoint = e.point;
            if (LinePoint) {
                var pointDoc = this.getDocumentsByUiid(LinePoint);
                this.remove(pointDoc)
                var pointdata = type == "end" ? this.getLigatureRoute(anotherNodeAttr, box) : this.getLigatureRoute(box, anotherNodeAttr);
                var inBox = type == "end" ? box : anotherNodeAttr;
                var Ipoint = this.drawIntersectionPoint(pointdata, inBox);
                if (Ipoint) {
                    Ipoint.uiid = e.point;
                    Ipoint.back();
                }
            }
            newLine.back()
            this.reMoveLine(newLine, Ipoint);
        }
    }
    this.reMoveLine = function (lineDoc, pointDoc) {
        var lineList = this.connectionList;
        var _this = this;
        lineDoc.dblclick(function () {
            _this.remove(this);
            _this.remove(pointDoc);
            for (var i = 0; i < lineList.length; i++) {
                if (lineDoc.uiid == lineList[i].line_id) {
                    lineList.splice(i, 1);
                }
            }

        })
    }
    this.findIntersectionPoint = function (data, endNodePos) {
        //1 左 2 上 3 右 4 下 
        var Fouredges = [{
            1: {
                x: data.end.x - endNodePos.w / 2,
                y: data.end.y - endNodePos.h / 2
            },
            2: {
                x: data.end.x - endNodePos.w / 2,
                y: data.end.y + endNodePos.h / 2
            }
        }, {
            1: {
                x: data.end.x - endNodePos.w / 2,
                y: data.end.y - endNodePos.h / 2
            },
            2: {
                x: data.end.x + endNodePos.w / 2,
                y: data.end.y - endNodePos.h / 2
            }
        }, {
            1: {
                x: data.end.x + endNodePos.w / 2,
                y: data.end.y - endNodePos.h / 2
            },
            2: {
                x: data.end.x + endNodePos.w / 2,
                y: data.end.y + endNodePos.h / 2
            }
        }, {
            1: {
                x: data.end.x + endNodePos.w / 2,
                y: data.end.y + endNodePos.h / 2
            },
            2: {
                x: data.end.x - endNodePos.w / 2,
                y: data.end.y + endNodePos.h / 2
            }
        }]
        for (var i = 0; i < Fouredges.length; i++) {
            var e = segmentsIntr(data.begin, data.end, Fouredges[i][1], Fouredges[i][2]);
            if (e) {
                return e;
            }
        }
    }
    this.moveAndRedrawLine = function (doc) {
        var lines = this.getLinesByUiid(doc.uiid);
        if (lines.begin.length > 0) {
            this.redrawLine(doc, lines.begin, "begin")
        }
        if (lines.end.length > 0) {
            this.redrawLine(doc, lines.end, "end");
        }
    }
    this.getLinesByUiid = function (uiid) {
        var lines = this.connectionList;
        var result = { begin: [], end: [] };
        for (var i = 0; i < lines.length; i++) {
            var e = lines[i];
            if (e.begin == uiid) {
                result.begin.push(e);
            } else if (e.end == uiid) {
                result.end.push(e);
            }
        }
        return result;
    }
    this.getconnectionRoute = function (beginNode, endNode) {
        var beginCenterPos = {
            x: beginNode.x + beginNode.w / 2,
            y: beginNode.y + beginNode.h / 2,
        }
        var endCenterPos = {
            x: endNode.x + endNode.w / 2,
            y: endNode.y + endNode.h / 2
        }
        return { begin: beginCenterPos, end: endCenterPos };
    }
}
/**  解线性方程组, 求线段交点. **/
function segmentsIntr(a, b, c, d) {
    // 如果分母为0 则平行或共线, 不相交  
    var denominator = (b.y - a.y) * (d.x - c.x) - (a.x - b.x) * (c.y - d.y);
    if (denominator == 0) {
        return false;
    }
    // 线段所在直线的交点坐标 (x , y)      
    var x = ((b.x - a.x) * (d.x - c.x) * (c.y - a.y)
        + (b.y - a.y) * (d.x - c.x) * a.x
        - (d.y - c.y) * (b.x - a.x) * c.x) / denominator;
    var y = -((b.y - a.y) * (d.y - c.y) * (c.x - a.x)
        + (b.x - a.x) * (d.y - c.y) * a.y
        - (d.x - c.x) * (b.y - a.y) * c.y) / denominator;

    /** 2 判断交点是否在两条线段上 **/
    if (
        // 交点在线段1上  
        (x - a.x) * (x - b.x) <= 0 && (y - a.y) * (y - b.y) <= 0
        // 且交点也在线段2上  
        && (x - c.x) * (x - d.x) <= 0 && (y - c.y) * (y - d.y) <= 0
    ) {
        // 返回交点p  
        return {
            x: x,
            y: y
        }
    }
    //否则不相交  
    return false
}
