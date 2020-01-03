var AJAXHTTPTOTOTOMCATACC= "";



/**
 * 返回字符串格�? and condition like '%value%'
 * @param {Object} value
 * @param {Object} condition
 */
function toLikeCondition(value,condition){
	if(value!=null&&value!=""&&value!=undefined){
		return "and "+condition+" like '"+value+"'";
	}
	return "";
}
/**
 * 删除对象属性为空的值
 * deleteObjNullAttr
 */
function deleteObjNullAttr(obj){
    if(obj==undefined||obj==null){
      return "";
    }
    for(var key  in obj){
      if(obj[key]==null||obj[key]==""||obj[key]==undefined){
        delete obj[key]
      }
    }
  }
/**
 * 判断字符串长�?及是否为�? 注：通过返回false  未通过返回true
 * @param {Object} str 字符串主�?
 * @param {Object} length  限制字符串长�?
 * @param {Object} sRet 是否可以为空                                                            
 */
function cherkStrIsNullOrLength(str,length,sRet){
	str = " "+str;
	str = str.substring(1,str.length);
	var a = true;
	if(str!=null||str!=undefined){
		if(str.length<length){
			if(!sRet){
				a=false;			
			}else{
				if(str.length>0){
					a=false;
				}
			}
		}
	}
	return a;
}
function getId(str){
	return str+"_"+new Date().getTime();
}
/**
 * 获取下拉框内�?并填充入下拉框中
 * @param {Object} query 
 * @param {Object} uiInfo
 * @param {Object} id
 */
function getPZ_SjzdList(query,uiInfo,id){
	queryKeys = query.query.split("'")[1];
    getDictionary(uiInfo, id, queryKeys, false);
}
function getPZ_SjzdListTree(query,uiInfo,id){
	queryKeys = JSON.stringify(query);
//	Tools.ajaxTomcatSubmit(url, null, postdata, true, false, succ);
//	ajaxTomcatSubmit: function(url, command, postdata, async, needSignature, succ) 
	Tools.ajaxTomcatSubmit(AJAXHTTPTOTOTOMCATACC + "/ex/sjzd/getSjzdList",null,{querykeys:queryKeys},false,false,function(data){
		var datas = data.data.data;
		var selectData = [];
		for(var i = 0;i<datas.length;i++){
			selectData[i]= {id: datas[i].sjzd_id, text: datas[i].sjzd_xmmc,pid: datas[i].f_sjzd_id};
		}
		var control = getFrameControlByName(uiInfo, id);
		control.loadList(selectData,"id","pid");
	});
}
function isEnglishAndNumber(v) {
            
    var re = new RegExp("^[0-9a-zA-Z\_]+$");
    if (re.test(v)) return true;
    return false;
}
function isNumber(v) {
            
    var re = new RegExp("^[0-9\_]+$");
    if (re.test(v)) return true;
    return false;
}
/*
 * 打开遮罩层

function loading() {
    var mask_bg = document.createElement("div");
    mask_bg.id = "mask_bg";
    mask_bg.className="loading";
    document.body.appendChild(mask_bg);  
    var mask_msg=document.createElement("div");
    mask_msg.innerText="Loading"
    mask_bg.appendChild(mask_msg);   
    var mask_msg1 = document.createElement("div");
    mask_msg1.id="mask_msg1";  
    var mask_absp1 = document.createElement("div");
    mask_absp1.id="mask_absp1";
    var mask_msg2 = document.createElement("div");
    mask_msg2.id="mask_msg2";
    var mask_absp2 = document.createElement("div");
    mask_absp2.id="mask_absp2";
    var mask_msg3 = document.createElement("div");
    mask_msg3.id="mask_msg3";
    var mask_absp3 = document.createElement("div");
    mask_absp3.id="mask_absp3";
    var mask_msg4 = document.createElement("div");
    mask_msg4.id="mask_msg4";
    var mask_absp4 = document.createElement("div");
    mask_absp4.id="mask_absp4";
    var mask_msg5 = document.createElement("div");
    mask_msg5.id="mask_msg5";
    mask_msg.appendChild(mask_msg1);
    mask_msg.appendChild(mask_absp1);
    mask_msg.appendChild(mask_msg2);
    mask_msg.appendChild(mask_absp2);
    mask_msg.appendChild(mask_msg3);
    mask_msg.appendChild(mask_absp3);
    mask_msg.appendChild(mask_msg4);
    mask_msg.appendChild(mask_absp4);
    mask_msg.appendChild(mask_msg5);
    
} */
/*
 * 关闭遮罩层
 *
function loaded() {
    var mask_bg = document.getElementById("mask_bg");
    if (mask_bg != null)
        mask_bg.parentNode.removeChild(mask_bg);
}*/

