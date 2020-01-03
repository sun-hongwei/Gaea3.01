<?php
$filename = $_POST['filename'];
$data = $_POST['data'];
$command = $_POST['command'];
$dir = dirname(dirname(__FILE__));
switch ($command) {
    case "run":
    $fileDir = $dir . DIRECTORY_SEPARATOR. "run" . DIRECTORY_SEPARATOR;
    $FileName = $fileDir .$filename.".whr";
    break;
    case "ran":
    $fileDir = $dir . DIRECTORY_SEPARATOR. "ran" . DIRECTORY_SEPARATOR;
    $FileName = $fileDir .$filename.".rwf";
    break;
}
$dirparent = iconv("UTF-8", "GB2312", $fileDir);
        if (!file_exists($dirparent)){
            mkdir ($dirparent,0777,true);
            //echo '创建文件夹logs成功';
        }
            $file = fopen($FileName,"w");
            $restuls = 0;
            try {
                fwrite($file,$data);
            } catch (\Throwable $th) {
                $restuls = -1;
            } 
            //关闭文件
            fclose($file);
            echo '{"ret":true,"data":' . $restuls . '}';  
?>