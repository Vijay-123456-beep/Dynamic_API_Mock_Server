# Build Script for Deployment
# This script prepares your application for deployment

Write-Host "🚀 Building Dynamic API Mock Server for Deployment" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""

# Check if Maven is installed
Write-Host "📦 Checking Maven installation..." -ForegroundColor Yellow
try {
    $mavenVersion = mvn -version 2>&1 | Select-String "Apache Maven"
    Write-Host "✅ Maven found: $mavenVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Maven not found! Please install Maven first." -ForegroundColor Red
    Write-Host "Download from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    exit 1
}

# Check if Node.js is installed
Write-Host "📦 Checking Node.js installation..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js found: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js not found! Please install Node.js first." -ForegroundColor Red
    Write-Host "Download from: https://nodejs.org/" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "🔨 Building Backend..." -ForegroundColor Yellow
Write-Host "Running: mvn clean package -DskipTests" -ForegroundColor Gray

try {
    mvn clean package -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Backend build successful!" -ForegroundColor Green
        
        # Check if JAR was created
        $jarFile = Get-ChildItem -Path "target" -Filter "*.jar" -Recurse | Select-Object -First 1
        if ($jarFile) {
            Write-Host "✅ JAR file created: $($jarFile.Name)" -ForegroundColor Green
            Write-Host "   Size: $([math]::Round($jarFile.Length / 1MB, 2)) MB" -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ Backend build failed!" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Backend build failed with error: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎨 Building Frontend..." -ForegroundColor Yellow
Write-Host "Running: npm install && npm run build" -ForegroundColor Gray

try {
    Push-Location "mock-ui"
    
    # Install dependencies
    Write-Host "Installing dependencies..." -ForegroundColor Gray
    npm install
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Dependencies installed!" -ForegroundColor Green
        
        # Build frontend
        Write-Host "Building frontend..." -ForegroundColor Gray
        npm run build
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✅ Frontend build successful!" -ForegroundColor Green
            
            # Check if dist folder was created
            if (Test-Path "dist") {
                $distSize = (Get-ChildItem -Path "dist" -Recurse | Measure-Object -Property Length -Sum).Sum
                Write-Host "✅ Build output created in: mock-ui/dist" -ForegroundColor Green
                Write-Host "   Size: $([math]::Round($distSize / 1MB, 2)) MB" -ForegroundColor Gray
            }
        } else {
            Write-Host "❌ Frontend build failed!" -ForegroundColor Red
            Pop-Location
            exit 1
        }
    } else {
        Write-Host "❌ npm install failed!" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    
    Pop-Location
} catch {
    Write-Host "❌ Frontend build failed with error: $_" -ForegroundColor Red
    Pop-Location
    exit 1
}

Write-Host ""
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host "✅ Build Complete!" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📦 Build Artifacts:" -ForegroundColor Yellow
Write-Host "   Backend JAR: target/dynamic-api-mock-server-0.0.1-SNAPSHOT.jar" -ForegroundColor Gray
Write-Host "   Frontend: mock-ui/dist/" -ForegroundColor Gray
Write-Host ""
Write-Host "🚀 Next Steps:" -ForegroundColor Yellow
Write-Host "   1. Push your code to GitHub" -ForegroundColor Gray
Write-Host "   2. Follow QUICK_DEPLOY.md for deployment instructions" -ForegroundColor Gray
Write-Host "   3. Or use docker-compose for local testing:" -ForegroundColor Gray
Write-Host "      docker-compose up --build" -ForegroundColor Cyan
Write-Host ""
Write-Host "📚 Documentation:" -ForegroundColor Yellow
Write-Host "   - QUICK_DEPLOY.md - Fast deployment guide" -ForegroundColor Gray
Write-Host "   - DEPLOYMENT_GUIDE.md - Detailed deployment options" -ForegroundColor Gray
Write-Host "   - DEPLOYMENT_OPTIONS.md - Platform comparison" -ForegroundColor Gray
Write-Host "   - PRE_DEPLOYMENT_CHECKLIST.md - Pre-deployment checklist" -ForegroundColor Gray
Write-Host ""
Write-Host "✨ Happy Deploying!" -ForegroundColor Green
