@if "%DEBUG%" == "" @echo off
if "%OS%"=="Windows_NT" setlocal
if "%HUTOOL_PATH%"=="" goto envFail

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%..

for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi
set DEFAULT_JVM_OPTS=
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
chcp 936 > nul
set CLASSPATH=%APP_HOME%\hutool.jar
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %HUTOOL_OPTS%  -classpath "%CLASSPATH%" org.code4everything.hutool.Hutool "--work-dir" %cd% %*

:end
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
if  not "" == "%HUTOOL_EXIT_CONSOLE%" exit 1
exit /b 1

:envFail
echo environment 'HUTOOL_PATH' not found!
goto fail

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
