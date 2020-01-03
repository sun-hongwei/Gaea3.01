echo off

set one=%1

x:
echo compile AdpaterCallStack
cd X:\Git\Gaea3.0\AdpaterCallStack
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile Tools
cd X:\Git\Gaea3.0\Tools
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile ParallelComputing
cd X:\Git\Gaea3.0\ParallelComputing
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile SwingTools
cd X:\Git\Gaea3.0\SwingTools
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile DBInterface
cd X:\Git\Gaea3.0\DBInterface
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile DBHelper
cd X:\Git\Gaea3.0\DBHelper
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile ExceIX
cd X:\Git\Gaea3.0\ExceIX
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile RolerOper
cd X:\Git\Gaea3.0\RolerOper
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaInterface
cd X:\Git\Gaea3.0\GaeaInterface
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaPlugin
cd X:\Git\Gaea3.0\GaeaPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaDBPlugin
cd X:\Git\Gaea3.0\GaeaDBPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaRolePlugin
cd X:\Git\Gaea3.0\GaeaRolePlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaExcelPlugin
cd X:\Git\Gaea3.0\GaeaExcelPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaDataSourcePlugin
cd X:\Git\Gaea3.0\GaeaDataSourcePlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaWorkflowPlugin
cd X:\Git\Gaea3.0\GaeaWorkflowPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile Gaea
cd X:\Git\Gaea3.0\Gaea
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (
	goto error
) else ( 
	goto ok
)

:error
echo 编译失败
if "%one%" NEQ "nowait" (pause 按任意键退出！)
exit /b 2

:ok
echo 编译成功
if "%one%" NEQ "nowait" (pause 按任意键退出！)
exit /b 0