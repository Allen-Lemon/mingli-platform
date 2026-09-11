@echo off
REM ============================================================
REM 玄枢 · 盲派八字命理平台 一键启动
REM 用法：
REM   start.bat          使用 MySQL（默认，也是唯一数据源）
REM ============================================================
setlocal

set PROFILE=%1
if "%PROFILE%"=="" set PROFILE=mysql

set JAVA_HOME=D:\zen_v1.0\jdk-8u201
set MAVEN_HOME=D:\developer\apache-maven-3.8.6
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo [1/3] 打包后端 ...
pushd "%~dp0mingli-backend"
call "%MAVEN_HOME%\bin\mvn.cmd" -q -DskipTests package
if errorlevel 1 (
    echo 打包失败，请检查 Maven / JDK 配置
    popd
    pause
    exit /b 1
)
popd

echo [2/3] 启动后端 :8080  profile=%PROFILE%
start "mingli-backend" cmd /c ""%JAVA_HOME%\bin\java" -Xmx512m -Xms128m -jar "%~dp0mingli-backend\target\mingli-backend.jar" --spring.profiles.active=%PROFILE%"

echo [3/3] 启动前端 :5173
pushd "%~dp0mingli-web"
if not exist node_modules (
    echo 首次运行，安装前端依赖 ...
    call npm install
)
start "mingli-web" cmd /c "npm run dev"
popd

echo.
echo 启动完成，请访问 http://localhost:5173
echo 后端地址 http://localhost:8080
pause
