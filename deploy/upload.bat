@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ==========================================
echo   侗乡医药数字展示平台 - 上传部署文件
echo ==========================================
echo.

set SERVER_IP=47.112.111.115
set SERVER_USER=root
set DEPLOY_DIR=%~dp0
set REMOTE_DIR=/opt/deploy

echo 目标服务器: %SERVER_IP%
echo 远程目录: %REMOTE_DIR%
echo.

set /p SERVER_PASSWORD=请输入服务器SSH密码:

echo 创建远程目录...
echo %SERVER_PASSWORD% | plink -ssh %SERVER_USER%@%SERVER_IP% "mkdir -p %REMOTE_DIR%"

echo 上传后端JAR包...
pscp -pw %SERVER_PASSWORD% "%DEPLOY_DIR%\dong-medicine-backend.jar" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/

echo 上传前端构建产物...
pscp -pw %SERVER_PASSWORD% -r "%DEPLOY_DIR%\frontend-dist" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/

echo 上传静态资源...
pscp -pw %SERVER_PASSWORD% -r "%DEPLOY_DIR%\public" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/

echo 上传配置文件...
pscp -pw %SERVER_PASSWORD% "%DEPLOY_DIR%\.env.production" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/
pscp -pw %SERVER_PASSWORD% "%DEPLOY_DIR%\nginx.conf" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/
pscp -pw %SERVER_PASSWORD% "%DEPLOY_DIR%\dong-medicine-backend.service" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/
pscp -pw %SERVER_PASSWORD% "%DEPLOY_DIR%\deploy.sh" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/
pscp -pw %SERVER_PASSWORD% "%DEPLOY_DIR%\init-database.sql" %SERVER_USER%@%SERVER_IP%:%REMOTE_DIR%/

echo.
echo ==========================================
echo   上传完成！
echo ==========================================
echo.
echo 请SSH登录服务器执行部署:
echo   ssh %SERVER_USER%@%SERVER_IP%
echo   cd /opt/deploy
echo   chmod +x deploy.sh
echo   ./deploy.sh
echo.
pause
