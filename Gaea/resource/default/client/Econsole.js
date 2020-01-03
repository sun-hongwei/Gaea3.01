var Econsole = {
    clickMap: [],
    ajaxMap: [],
    ajaxConsoleId: 1,
    currentClick: undefined,
    module: undefined,
    username: GlobalSessionObject.getUserName(),
    init: function (element) {
        var str = {
            "用户:": Econsole.username != undefined ? Econsole.username : "暂未登录",
            "点击模块": Econsole.module != undefined ? Econsole.module : "主模块",
            "点击按钮:": element.source.text,
            "按钮id:": element.source.id,
            "点击时间:": Tools.now(),
        };
        Econsole.clickMap.push(str);
        Econsole.currentClick = str;
        console.log("[Econsole]当前点击:", str);
    },
    onError: function (err) {
        var newstr = Econsole.currentClick;
        newstr["页面报错:"] = err;
        Econsole.clickMap[Econsole.clickMap.length - 1] = newstr;
    },
    initAjax: function (data, id) {
        var str = {
            id: id,
            "请求接口:": data.url,
            "请求参数:": data.data,
            "请求时间:": Tools.now(),
        }
        Econsole.ajaxMap.push(str);
    },
    AjaxFinish: function (id, data) {
        if (typeof (data) != "object") {
            data = JSON.parse(data);
        }
        for (var i = 0; i < Econsole.ajaxMap.length; i++) {
            var element = Econsole.ajaxMap[i];
            if (element.id == id) {
                element["请求结果状态:"] = data.ret == 0 ? "请求成功！" : "请求失败！(错误状态:" + data.ret + ")";
                element["请求结果参数:"] = data.data;
                if (element["请求接口:"].split(".do").length>1) {
                    var data = JSON.stringify(element);
                    localStorage.setItem("req",data);
                }
            }
        }
    },
    SubData: function () {
        if (Econsole.clickMap.length > 20 || Econsole.ajaxMap.length > 20) {
            clickLen = Econsole.clickMap.length;
            ajaxLen = Econsole.ajaxMap.length;
            var endpageData = [];
            for (var i = 0; i < clickLen; i++) {
                var e = Econsole.clickMap[i];
                e = JSON.stringify(e);
                endpageData[i] = e;
            }
            var endportData = [];
            for (var i = 0; i < ajaxLen; i++) {
                var e = Econsole.ajaxMap[i];
                e = JSON.stringify(e);
                endportData[i] = e;
            }
            Tools.uploadLog(JSON.stringify(endpageData),clickLen, JSON.stringify(endportData),ajaxLen);
        }
        // console.log("[Econsole]all:",Econsole.clickMap); 
    }
}
function openDeBugger(){

}