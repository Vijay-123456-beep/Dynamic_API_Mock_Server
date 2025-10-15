# Deployment Guide - Dynamic API Mock Server

This guide covers deploying your Dynamic API Mock Server to various cloud platforms with free tiers.

## Table of Contents
1. [Option 1: Render.com (Recommended)](#option-1-rendercom-recommended)
2. [Option 2: Railway.app](#option-2-railwayapp)
3. [Option 3: Heroku](#option-3-heroku)
4. [Option 4: Fly.io](#option-4-flyio)
5. [Post-Deployment Configuration](#post-deployment-configuration)

---

## Option 1: Render.com (Recommended)

**Pros**: Free PostgreSQL, easy setup, auto-deploy from Git, free SSL
**Free Tier**: 750 hours/month, PostgreSQL with 1GB storage

### Prerequisites
- GitHub/GitLab account with your code pushed
- Render.com account (free)

### Step-by-Step Deployment

#### A. Deploy Backend (Spring Boot)

1. **Push your code to GitHub**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin <your-github-repo-url>
   git push -u origin main
   ```

2. **Create PostgreSQL Database**
   - Go to [Render Dashboard](https://dashboard.render.com/)
   - Click "New +" → "PostgreSQL"
   - Name: `dynamic-mock-db`
   - Database: `mockdb`
   - User: `mockuser`
   - Region: Choose closest to you
   - Plan: **Free**
   - Click "Create Database"
   - **Save the Internal Database URL** (you'll need this)

3. **Build the JAR file locally first**
   ```bash
   mvn clean package -DskipTests
   ```
   This creates `target/dynamic-api-mock-server-0.0.1-SNAPSHOT.jar`

4. **Deploy Backend Service**
   - Click "New +" → "Web Service"
   - Connect your GitHub repository
   - Name: `dynamic-mock-backend`
   - Region: Same as database
   - Branch: `main`
   - Root Directory: Leave empty
   - Runtime: **Docker**
   - Plan: **Free**
   
5. **Configure Environment Variables**
   Add these in the "Environment" section:
   ```
   SPRING_DATASOURCE_URL=<paste-internal-database-url-from-step-2>
   SPRING_DATASOURCE_USERNAME=mockuser
   SPRING_DATASOURCE_PASSWORD=<database-password-from-step-2>
   SPRING_PROFILES_ACTIVE=prod
   APP_JWT_SECRET=<generate-random-base64-string>
   APP_CORS_ALLOWED_ORIGINS=*
   PORT=8088
   ```

6. **Click "Create Web Service"**
   - Render will build and deploy automatically
   - Wait 5-10 minutes for first deployment
   - Your backend URL: `https://dynamic-mock-backend.onrender.com`

#### B. Deploy Frontend (React)

1. **Update Frontend Environment**
   Create `mock-ui/.env.production`:
   ```
   VITE_API_BASE_URL=https://dynamic-mock-backend.onrender.com
   ```

2. **Deploy Frontend Service**
   - Click "New +" → "Static Site"
   - Connect same GitHub repository
   - Name: `dynamic-mock-frontend`
   - Branch: `main`
   - Root Directory: `mock-ui`
   - Build Command: `npm install && npm run build`
   - Publish Directory: `dist`
   - Plan: **Free**

3. **Configure Environment Variables**
   ```
   VITE_API_BASE_URL=https://dynamic-mock-backend.onrender.com
   ```

4. **Click "Create Static Site"**
   - Your frontend URL: `https://dynamic-mock-frontend.onrender.com`

#### C. Update CORS Settings

After deployment, update backend environment variable:
```
APP_CORS_ALLOWED_ORIGINS=https://dynamic-mock-frontend.onrender.com
```

**Your app is now live!** 🎉

---

## Option 2: Railway.app

**Pros**: Simple setup, automatic HTTPS, good free tier
**Free Tier**: $5 credit/month (enough for small apps)

### Step-by-Step Deployment

1. **Install Railway CLI** (optional)
   ```bash
   npm install -g @railway/cli
   railway login
   ```

2. **Create New Project**
   - Go to [Railway Dashboard](https://railway.app/)
   - Click "New Project"
   - Select "Deploy from GitHub repo"
   - Connect your repository

3. **Add PostgreSQL Database**
   - In your project, click "New"
   - Select "Database" → "PostgreSQL"
   - Railway automatically creates and connects it

4. **Deploy Backend**
   - Click "New" → "GitHub Repo"
   - Select your repository
   - Railway auto-detects Dockerfile
   
5. **Configure Backend Variables**
   Click on the service → "Variables" tab:
   ```
   SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}
   SPRING_PROFILES_ACTIVE=prod
   APP_JWT_SECRET=<generate-random-base64-string>
   APP_CORS_ALLOWED_ORIGINS=*
   PORT=8088
   ```

6. **Generate Domain**
   - Go to "Settings" → "Networking"
   - Click "Generate Domain"
   - Your backend: `https://your-app.up.railway.app`

7. **Deploy Frontend**
   - Create new service from same repo
   - Set Root Directory: `mock-ui`
   - Build Command: `npm install && npm run build`
   - Start Command: `npx vite preview --host 0.0.0.0 --port $PORT`
   
8. **Configure Frontend Variables**
   ```
   VITE_API_BASE_URL=https://your-backend.up.railway.app
   ```

9. **Generate Frontend Domain**
   - Your frontend: `https://your-frontend.up.railway.app`

---

## Option 3: Heroku

**Note**: Heroku no longer offers a free tier, but has affordable options starting at $5/month.

### Deployment Steps

1. **Install Heroku CLI**
   ```bash
   npm install -g heroku
   heroku login
   ```

2. **Create Heroku Apps**
   ```bash
   # Backend
   heroku create dynamic-mock-backend
   
   # Add PostgreSQL
   heroku addons:create heroku-postgresql:mini -a dynamic-mock-backend
   ```

3. **Configure Backend**
   ```bash
   heroku config:set SPRING_PROFILES_ACTIVE=prod -a dynamic-mock-backend
   heroku config:set APP_JWT_SECRET=<your-secret> -a dynamic-mock-backend
   heroku config:set APP_CORS_ALLOWED_ORIGINS=* -a dynamic-mock-backend
   ```

4. **Deploy Backend**
   ```bash
   git push heroku main
   ```

5. **Deploy Frontend**
   ```bash
   cd mock-ui
   heroku create dynamic-mock-frontend
   heroku buildpacks:set heroku/nodejs
   heroku config:set VITE_API_BASE_URL=https://dynamic-mock-backend.herokuapp.com
   git push heroku main
   ```

---

## Option 4: Fly.io

**Pros**: Global edge deployment, generous free tier
**Free Tier**: 3 shared VMs, 3GB storage

### Deployment Steps

1. **Install Fly CLI**
   ```bash
   powershell -Command "iwr https://fly.io/install.ps1 -useb | iex"
   fly auth login
   ```

2. **Create Fly App for Backend**
   ```bash
   fly launch --name dynamic-mock-backend --no-deploy
   ```

3. **Add PostgreSQL**
   ```bash
   fly postgres create --name dynamic-mock-db
   fly postgres attach dynamic-mock-db -a dynamic-mock-backend
   ```

4. **Configure Secrets**
   ```bash
   fly secrets set SPRING_PROFILES_ACTIVE=prod -a dynamic-mock-backend
   fly secrets set APP_JWT_SECRET=<your-secret> -a dynamic-mock-backend
   fly secrets set APP_CORS_ALLOWED_ORIGINS=* -a dynamic-mock-backend
   ```

5. **Deploy Backend**
   ```bash
   fly deploy
   ```

6. **Deploy Frontend**
   ```bash
   cd mock-ui
   fly launch --name dynamic-mock-frontend
   fly secrets set VITE_API_BASE_URL=https://dynamic-mock-backend.fly.dev
   fly deploy
   ```

---

## Post-Deployment Configuration

### 1. Generate JWT Secret

Generate a secure base64 secret:
```bash
# Using Node.js
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"

# Using OpenSSL
openssl rand -base64 32
```

### 2. Test Your Deployment

#### Test Backend Health
```bash
curl https://your-backend-url.com/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

#### Test Backend API
```bash
# Register a user
curl -X POST https://your-backend-url.com/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123","email":"test@example.com"}'

# Login
curl -X POST https://your-backend-url.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'
```

#### Test Frontend
Open `https://your-frontend-url.com` in browser and:
1. Register a new account
2. Login
3. Create a mock endpoint
4. Test the mock endpoint

### 3. Update CORS for Production

After confirming your frontend URL, update backend:
```
APP_CORS_ALLOWED_ORIGINS=https://your-frontend-url.com,https://www.your-frontend-url.com
```

### 4. Monitor Your Application

- **Render**: Dashboard → Your Service → Logs
- **Railway**: Dashboard → Your Service → Deployments → Logs
- **Heroku**: `heroku logs --tail -a your-app-name`
- **Fly.io**: `fly logs -a your-app-name`

### 5. Custom Domain (Optional)

All platforms support custom domains:
- **Render**: Settings → Custom Domain
- **Railway**: Settings → Domains
- **Heroku**: Settings → Domains
- **Fly.io**: `fly certs add your-domain.com`

---

## Troubleshooting

### Backend Won't Start
1. Check logs for database connection errors
2. Verify `SPRING_DATASOURCE_URL` format: `jdbc:postgresql://host:port/database`
3. Ensure PostgreSQL is running and accessible
4. Check if port 8088 is exposed in Dockerfile

### Frontend Can't Connect to Backend
1. Verify `VITE_API_BASE_URL` is set correctly
2. Check CORS settings on backend
3. Ensure backend is deployed and healthy
4. Check browser console for CORS errors

### Database Connection Issues
1. Verify database credentials
2. Check if database is in same region as app
3. Ensure connection string includes SSL mode if required
4. Check database logs for connection attempts

### Build Failures
1. **Backend**: Ensure Java 17 is specified
2. **Frontend**: Check Node.js version compatibility
3. Verify all dependencies are in `pom.xml` and `package.json`
4. Check build logs for specific errors

---

## Cost Estimates (Monthly)

| Platform | Free Tier | Paid Tier |
|----------|-----------|-----------|
| **Render** | 750 hrs web + 1GB PostgreSQL | $7/month web + $7/month DB |
| **Railway** | $5 credit | $5-20/month |
| **Heroku** | None | $5/month dyno + $5/month DB |
| **Fly.io** | 3 VMs + 3GB storage | $5-15/month |

**Recommendation**: Start with **Render.com** for the best free tier experience.

---

## Next Steps

1. ✅ Deploy to your chosen platform
2. ✅ Test all functionality
3. ✅ Set up monitoring and alerts
4. ✅ Configure custom domain (optional)
5. ✅ Set up CI/CD for automatic deployments
6. ✅ Add production logging and error tracking
7. ✅ Implement database backups

---

## Support

- **Render**: https://render.com/docs
- **Railway**: https://docs.railway.app
- **Heroku**: https://devcenter.heroku.com
- **Fly.io**: https://fly.io/docs

For issues with this application, check the logs and ensure all environment variables are correctly set.
