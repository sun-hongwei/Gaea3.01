<?php
//
$pageData = $_POST['pageData'];
$pageLen = $_POST['pageLen'];
$reportdata = $_POST['reportData'];
$reportLen = $_POST['reportLen'];

$dir = dirname(dirname(__FILE__));
$time = date("Y-m-d");
//$hour = date("H:i:s",time());
$uploadDir = $dir . DIRECTORY_SEPARATOR. "logs" . DIRECTORY_SEPARATOR;
$pageFileName = $time."Gaea-web-page"; 
$reportFileName = $time."Gaea-web-report"; 
$isokPage = true;
$isokPort = true;
$FILESIZE = pow(1024, 4);

$dirparent = iconv("UTF-8", "GB2312", $uploadDir);
        if (!file_exists($dirparent)){
            mkdir ($dirparent,0777,true);
            //echo '创建文件夹logs成功';
        } else {
            //echo '不需创建的文件夹logs已经存在';
      
        $pageFile = $uploadDir .$pageFileName.".log";
        $isokPage = wirte_File($pageFile,$pageData,$pageLen);
        $reportFile = $uploadDir .$reportFileName.".log";
        $isokPort = wirte_File($reportFile,$reportdata,$reportLen);
        //关闭文件
        fclose($pageFile);
        fclose($reportFile);
        echo '{"ret":true,"data":{"page":'.$isokPage.',"port":'.$isokPort.'}}';  
    }

/**
 * 实现数据换行 每20个字符串一换行
 *
 * @param $oldstr 老数据
 */
function newline_Write($oldstr){
    $newstr = "";
    $k = 0;
    $len = strlen((string)$oldstr);
    for ($i=0; $i <$len; $i++) { 
        if ($i%20 == 0 && $i != 0) {
            $newstr = $newstr.substr($oldstr,$k,$i)."\r\n";
            $k +=$i;
        }else if ($len <20) {
            return $oldstr;            
        }
    }
    return $newstr;
}
/**
 * 写入文件
 */
$pageIndex = 1;
function wirte_File($filename,$postData,$postDataLen){
        $file_name = $filename.".log";
        $file = fopen($file_name,"w"); 
        $size = filesize($file_name);//当前文件大小
        $postDataSize = strlen($postData);
        $newSize = $size+$postDataSize;//目标新文件大小
        if($size == 0){
            $newdata =getHeader($postDataLen).getFileData($pageData);
            $data = newline_Write($newdata);
            try {
                fwrite($file,$data);
                return 1;
            } catch (\Throwable $th) {
                return 0;
            }
        }else if($newSize>$FILESIZE){
            $a = 2;
            while ($a > 1) {
                $a = wirte_File($filename."_".$pageIndex,$postData,$postDataLen);
                if ($a >1) {
                    $pageIndex +=$pageIndex;
                }else{
                    $pageIndex =1;
                }
            }
            return $a;
        }else{
           $olddata =  getOldFile($file_name);
           $newdata = getHeader((int)$olddata["header"]+(int)$postDataLen).getFileData($olddata["data"].",".getFileData($pageData));
           $data = newline_Write($newdata);
           try {
            fwrite($file,$data);
                return 1;
            } catch (\Throwable $th) {
                return 0;
            }
        }
}
function getHeader($num){
   $l =  strlen((string)$num);
   for ($i=0; $i <6-$l; $i++) { 
       $num ="0".$num;
   }
   return $num;
}
function getFileData($postData){
    return substr($postData,1,strlen($postData)-2);
}
function getOldFile($filename){
    $data =  fread($filename);
    $header = substr($data,0,6);
    $context = substr($data,6,strlen($data));
    $data = [
        'header' => $header,
        'data'  =>  $context     
    ];
    return $data;
}
?>
