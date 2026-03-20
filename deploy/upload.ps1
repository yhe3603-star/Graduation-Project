# 侗乡医药数字展示平台 - 一键上传部署文件到服务器
# 使用方法: 在PowerShell中运行此脚本

param(
    [string]$ServerIP = "47.112.111.115",
    [string]$ServerUser = "root",
    [string]$ServerPassword = ""
)

$ErrorActionPreference = "Stop"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  侗乡医药数字展示平台 - 上传部署文件" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

$DeployDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RemoteDir = "/opt/deploy"

Write-Host "目标服务器: $ServerIP" -ForegroundColor Yellow
Write-Host "远程目录: $RemoteDir" -ForegroundColor Yellow
Write-Host ""

if ([string]::IsNullOrEmpty($ServerPassword)) {
    $SecurePassword = Read-Host "请输入服务器SSH密码" -AsSecureString
    $ServerPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [Runtime.InteropServices.Marshal]::SecureStringToBSTR($SecurePassword)
    )
}

$FilesToUpload = @(
    "dong-medicine-backend.jar",
    ".env.production",
    "nginx.conf",
    "dong-medicine-backend.service",
    "deploy.sh",
    "init-database.sql",
    "install-env.sh"
)

$DirsToUpload = @(
    "frontend-dist",
    "public"
)

Write-Host "检查部署文件..." -ForegroundColor Yellow

$MissingFiles = @()
foreach ($file in $FilesToUpload) {
    $filePath = Join-Path $DeployDir $file
    if (-not (Test-Path $filePath)) {
        $MissingFiles += $file
    }
}

foreach ($dir in $DirsToUpload) {
    $dirPath = Join-Path $DeployDir $dir
    if (-not (Test-Path $dirPath)) {
        $MissingFiles += "$dir/"
    }
}

if ($MissingFiles.Count -gt 0) {
    Write-Host "缺少以下文件:" -ForegroundColor Red
    $MissingFiles | ForEach-Object { Write-Host "  - $_" -ForegroundColor Red }
    Write-Host ""
    Write-Host "请先运行 build.bat 构建项目" -ForegroundColor Yellow
    Read-Host "按回车键退出"
    exit 1
}

Write-Host "所有文件准备就绪" -ForegroundColor Green
Write-Host ""

Write-Host "上传方法:" -ForegroundColor Cyan
Write-Host ""
Write-Host "方法1: 使用WinSCP或FileZilla等FTP工具" -ForegroundColor White
Write-Host "  服务器: $ServerIP" -ForegroundColor Gray
Write-Host "  用户名: $ServerUser" -ForegroundColor Gray
Write-Host "  协议: SFTP" -ForegroundColor Gray
Write-Host "  上传目录: $DeployDir" -ForegroundColor Gray
Write-Host "  目标目录: $RemoteDir" -ForegroundColor Gray
Write-Host ""
Write-Host "方法2: 使用scp命令 (需要安装OpenSSH)" -ForegroundColor White
Write-Host "  scp -r `"$DeployDir\*`" ${ServerUser}@${ServerIP}:${RemoteDir}/" -ForegroundColor Gray
Write-Host ""
Write-Host "方法3: 手动上传" -ForegroundColor White
Write-Host "  将deploy目录下的所有文件上传到服务器的 $RemoteDir 目录" -ForegroundColor Gray
Write-Host ""

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  上传后在服务器执行以下命令" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "# 1. 首次部署，安装环境" -ForegroundColor Yellow
Write-Host "chmod +x install-env.sh" -ForegroundColor White
Write-Host "./install-env.sh" -ForegroundColor White
Write-Host ""
Write-Host "# 2. 初始化数据库" -ForegroundColor Yellow
Write-Host "mysql -u root -p < init-database.sql" -ForegroundColor White
Write-Host ""
Write-Host "# 3. 修改环境配置" -ForegroundColor Yellow
Write-Host "vi .env.production" -ForegroundColor White
Write-Host "  # 修改 DB_PASSWORD 为你的MySQL密码" -ForegroundColor Gray
Write-Host "  # 修改 JWT_SECRET 为随机生成的密钥" -ForegroundColor Gray
Write-Host ""
Write-Host "# 4. 执行部署" -ForegroundColor Yellow
Write-Host "chmod +x deploy.sh" -ForegroundColor White
Write-Host "./deploy.sh" -ForegroundColor White
Write-Host ""
Write-Host "# 5. 验证部署" -ForegroundColor Yellow
Write-Host "curl http://localhost:8080/api/health" -ForegroundColor White
Write-Host "curl http://localhost" -ForegroundColor White
Write-Host ""

Read-Host "按回车键退出"
