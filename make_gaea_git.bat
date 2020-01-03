echo off

set one=%1

echo compile AdpaterCallStack
cd .\AdpaterCallStack
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile Tools
cd ..\Tools
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile ParallelComputing
cd ..\ParallelComputing
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile SwingTools
cd ..\SwingTools
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile DBInterface
cd ..\DBInterface
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile DBHelper
cd ..\DBHelper
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile ExceIX
cd ..\ExceIX
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile RolerOper
cd ..\RolerOper
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaInterface
cd ..\GaeaInterface
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaPlugin
cd ..\GaeaPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaDBPlugin
cd ..\GaeaDBPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaRolePlugin
cd ..\GaeaRolePlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaExcelPlugin
cd ..\GaeaExcelPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaDataSourcePlugin
cd ..\GaeaDataSourcePlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile GaeaWorkflowPlugin
cd ..\GaeaWorkflowPlugin
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (goto error)

echo compile Gaea
cd ..\Gaea
call C:\apache-maven-3.6.2\bin\mvn.cmd clean package install -q -Dmaven.test.skip=true
if %errorlevel% NEQ 0 (
	goto error
) else ( 
	goto ok
)

:error
echo ����ʧ��
if "%one%" NEQ "nowait" (pause ��������˳���)
exit /b 2

:ok
echo ����ɹ�
if "%one%" NEQ "nowait" (pause ��������˳���)
exit /b 0