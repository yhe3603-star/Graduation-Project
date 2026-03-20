# 侗乡医药前端一键部署脚本
# 使用方法: .\deploy.ps1

$SERVER = "root@47.112.111.115"
$REMOTE_PATH = "/opt/dong-medicine/frontend"
$LOCAL_DIST = ".\dist"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  侗乡医药前端一键部署脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. 构建项目
Write-Host "`n[1/3] 构建前端项目..." -ForegroundColor Yellow
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host "构建失败！" -ForegroundColor Red
    exit 1
}
Write-Host "构建完成！" -ForegroundColor Green

# 2. 上传文件
Write-Host "`n[2/3] 上传文件到服务器..." -ForegroundColor Yellow
scp -r "$LOCAL_DIST\*" "${SERVER}:${REMOTE_PATH}/"
if ($LASTEXITCODE -ne 0) {
    Write-Host "上传失败！" -ForegroundColor Red
    exit 1
}
Write-Host "上传完成！" -ForegroundColor Green

# 3. 修复权限
Write-Host "`n[3/3] 修复文件权限..." -ForegroundColor Yellow
ssh $SERVER "chown -R www-data:www-data $REMOTE_PATH && chmod -R 755 $REMOTE_PATH && find $REMOTE_PATH -type f -exec chmod 644 {} +"
if ($LASTEXITCODE -ne 0) {
    Write-Host "权限修复失败！" -ForegroundColor Red
    exit 1
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "  部署成功！" -ForegroundColor Green
Write-Host "  访问地址: http://47.112.111.115/" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
