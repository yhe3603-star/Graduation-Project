# 侗乡医药后端一键部署脚本
# 使用方法: .\deploy.ps1

$SERVER = "root@47.112.111.115"
$REMOTE_PATH = "/opt/dong-medicine/backend"
$JAR_NAME = "dong-medicine-backend-1.0.0.jar"
$REMOTE_JAR_NAME = "dong-medicine-backend.jar"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  侗乡医药后端一键部署脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. 构建项目
Write-Host "`n[1/4] 构建后端项目..." -ForegroundColor Yellow
.\mvnw.cmd clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "构建失败！" -ForegroundColor Red
    exit 1
}
Write-Host "构建完成！" -ForegroundColor Green

# 2. 停止服务
Write-Host "`n[2/4] 停止后端服务..." -ForegroundColor Yellow
ssh $SERVER "systemctl stop dong-medicine; pkill -9 -f 'java.*dong-medicine' 2>/dev/null; sleep 2; echo '服务已停止'"
Write-Host "服务已停止" -ForegroundColor Green

# 3. 上传文件
Write-Host "`n[3/4] 上传JAR文件到服务器..." -ForegroundColor Yellow
scp "target\$JAR_NAME" "${SERVER}:${REMOTE_PATH}/${REMOTE_JAR_NAME}"
if ($LASTEXITCODE -ne 0) {
    Write-Host "上传失败！" -ForegroundColor Red
    exit 1
}
Write-Host "上传完成！" -ForegroundColor Green

# 4. 启动服务
Write-Host "`n[4/4] 启动后端服务..." -ForegroundColor Yellow
ssh $SERVER "systemctl start dong-medicine && systemctl status dong-medicine --no-pager"
if ($LASTEXITCODE -ne 0) {
    Write-Host "启动失败，请检查服务状态！" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  后端部署成功！" -ForegroundColor Green
Write-Host "  API地址: http://47.112.111.115/api/" -ForegroundColor Green
Write-Host "  Swagger文档: http://47.112.111.115/swagger-ui/index.html" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green
