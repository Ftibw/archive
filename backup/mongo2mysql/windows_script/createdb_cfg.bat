@echo off
cd /d %~dp0
set root=%cd:\data=%
set replace_bat=%root%\conf\replace.bat
set createdb_sql=%root%\data\createdb.sql

::findstr /X表示完全匹配得一行 /R表示正则匹配 注意匹配的字符串中空格会导致读取异常 
for /f "tokens=6 delims= " %%1 in ('findstr /XR "CREATE.*;" %root%\data\createdb.sql')do set dbname=%%1
call %replace_bat% %createdb_sql% %dbname% %1 >nul 2>nul