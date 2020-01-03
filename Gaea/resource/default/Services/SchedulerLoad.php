<?php
$command = $_POST['command'];
$dir = dirname(dirname(__FILE__));
$fileDir = $dir . DIRECTORY_SEPARATOR. "ran" . DIRECTORY_SEPARATOR;
switch ($command) {
    case "getFlowCharts":
    $restuls = array();
    try {
        if (!file_exists($fileDir)){
            echo '{"ret":-1,"msg":"打开目录失败！请检查文件夹是否存在！"}'; 
            break;
        }
        $handler = opendir($fileDir);//当前目录中的文件夹下的文件夹
        while( ($filename = readdir($handler)) !== false ) {
            if($filename != "." && $filename != ".."){
                $restuls[]=$filename;
            }
        }
        closedir($handler);
        $restuls = json_encode($restuls);
        echo '{"ret":0,"data":' . $restuls . '}';  
    } catch (\Throwable $th) {
        //throw $th;
        echo '{"ret":-1,"msg":"打开目录失败！请检查文件夹是否存在！"}'; 
    }
   
    break;
    case "getFlowChart":
    $filename = $_POST['filename'];
    $FileName = $fileDir .$filename.".rwf";
    $dirparent = iconv("UTF-8", "GB2312", $fileDir);
    if (!file_exists($dirparent)){
        echo  '{"ret":-1,"msg":"未找到文件"}';
    }
    $file = fopen($FileName,"r");
    $restuls = 0;
    try {
        $restuls = fread($file,10000000);
        echo '{"ret":0,"data":' . $restuls . '}';  
    } catch (\Throwable $th) {
        echo '{"ret":-1,"msg":"打开文件失败！"}';  
    } 
    //关闭文件
    fclose($file);
    break;
}
?>