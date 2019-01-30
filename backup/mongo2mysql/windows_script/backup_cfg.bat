@echo off
cd /d %~dp0
set root=%cd:\data=%
set replace_bat=%root%\conf\replace.bat
set backup_sql=%root%\data\backup.sql

::findstr /X表示完全匹配得一行 /R表示正则匹配 注意匹配的字符串中空格会导致读取异常 
for /f "tokens=6 delims= " %%1 in ('findstr /XR "CREATE.*;" %root%\data\backup.sql')do set dbname=%%1
call %replace_bat% %backup_sql% %dbname% %1 >nul 2>nul

for /f "tokens=5 delims= " %%1 in ('findstr /XR "load.*" %root%\data\backup.sql')do set bakfilepath=%%1
set bakfilepath=%bakfilepath:'=%
call %replace_bat% %backup_sql% %bakfilepath% %2 >nul 2>nul

for /f "tokens=3 delims= " %%1 in ('findstr /XR "into.*" %root%\data\backup.sql')do set tablename=%%1
set tablename=%tablename:`=%
call %replace_bat% %backup_sql% %tablename% %3 >nul 2>nul
