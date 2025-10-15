# Quick Deploy Guide - 5 Minutes to Production

This is the fastest way to get your Dynamic API Mock Server live on the internet.

## 🚀 Fastest Option: Render.com (Free)

### Prerequisites
- GitHub account
- Render.com account (sign up free at https://render.com)

### Step 1: Prepare Your Code (2 minutes)

1. **Build the backend JAR**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Push to GitHub**
   ```bash
   git init
   git add .
   git commit -m "Ready for deployment"
   git branch -M main
   git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
   git push -u origin main
   ```

### Step 2: Deploy Database (1 minute)

1. Go to https://dashboard.render.com
2. Click **"New +"** → **"PostgreSQL"**
3. Settings:
   - **Name**: `mockserver-db`
   - **Database**: `mockdb`
   - **User**: `mockuser`
   - **Region**: Choose closest to you
   - **Plan**: **Free**
4. Click **"Create Database"**
5. **IMPORTANT**: Copy the **"Internal Database URL"** - you'll need it next!

### Step 3: Deploy Backend (2 minutes)

1. Click **"New +"** → **"Web Service"**
2. Click **"Build and deploy from a Git repository"**
3. Connect your GitHub repository
4. Settings:
   - **Name**: `mockserver-backend`
   - **Region**: Same as database
   - **Branch**: `main`
   - **Runtime**: **Docker**
   - **Plan**: **Free**

5. **Environment Variables** (click "Add Environment Variable"):
   ```
   SPRING_DATASOURCE_URL = <paste the Internal Database URL from Step 2>
   SPRING_DATASOURCE_USERNAME = mockuser
   SPRING_DATASOURCE_PASSWORD = <password from database page>
   SPRING_PROFILES_ACTIVE = prod
   APP_JWT_SECRET = c3ByaW5nLWJvb3QtZGV2LXNlY3JldC1rZXktMzJieXRlc2Jhc2U2NA==
   APP_CORS_ALLOWED_ORIGINS = *
   ```

6. Click **"Create Web Service"**
7. Wait 5-10 minutes for deployment
8. **Copy your backend URL**: `https://mockserver-backend-xxxx.onrender.com`

### Step 4: Deploy Frontend (2 minutes)

1. **Update frontend environment**
   Edit `mock-ui/.env.production`:
   ```
   VITE_API_BASE_URL=https://mockserver-backend-xxxx.onrender.com
   ```
   Commit and push:
   ```bash
   git add mock-ui/.env.production
   git commit -m "Update production API URL"
   git push
   ```

2. Click **"New +"** → **"Static Site"**
3. Connect same GitHub repository
4. Settings:
   - **Name**: `mockserver-frontend`
   - **Branch**: `main`
   - **Root Directory**: `mock-ui`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `dist`
   - **Plan**: **Free**

5. **Environment Variables**:
   ```
   VITE_API_BASE_URL = https://mockserver-backend-xxxx.onrender.com
   ```

6. Click **"Create Static Site"**
7. Wait 3-5 minutes for deployment

### Step 5: Update CORS (30 seconds)

1. Go to your backend service on Render
2. Go to **"Environment"** tab
3. Update `APP_CORS_ALLOWED_ORIGINS`:
   ```
   https://mockserver-frontend-xxxx.onrender.com
   ```
4. Click **"Save Changes"** (service will auto-redeploy)

## 🎉 You're Live!

Your application is now deployed:
- **Frontend**: `https://mockserver-frontend-xxxx.onrender.com`
- **Backend API**: `https://mockserver-backend-xxxx.onrender.com`
- **API Docs**: `https://mockserver-backend-xxxx.onrender.com/swagger-ui/index.html`
- **Health Check**: `https://mockserver-backend-xxxx.onrender.com/actuator/health`

## 🧪 Test Your Deployment

### 1. Check Backend Health
Open in browser:
```
https://mockserver-backend-xxxx.onrender.com/actuator/health
```
Should see: `{"status":"UP"}`

### 2. Test Frontend
1. Open `https://mockserver-frontend-xxxx.onrender.com`
2. Click **"Sign Up"**
3. Create account: username, email, password
4. Login with your credentials
5. Create a mock endpoint:
   - Endpoint: `/test`
   - Method: `GET`
   - Response: `{"message": "Hello World"}`
   - Status: `200`
6. Test it: `https://mockserver-backend-xxxx.onrender.com/mock/test`

## ⚠️ Important Notes

### Free Tier Limitations
- **Render Free**: Services spin down after 15 minutes of inactivity
- **First request** after sleep takes 30-60 seconds to wake up
- **Database**: 1GB storage limit
- **Bandwidth**: 100GB/month

### Upgrade to Paid ($7/month per service)
- No spin down
- Faster performance
- More resources

### Keep Free Tier Active
Use a service like UptimeRobot to ping your app every 10 minutes:
```
https://uptimerobot.com
Add monitor: https://mockserver-backend-xxxx.onrender.com/actuator/health
Interval: 10 minutes
```

## 🔒 Security Recommendations

### 1. Change JWT Secret
Generate a new secret:
```bash
node -e "console.log(require('crypto').randomBytes(32).toString('base64'))"
```
Update `APP_JWT_SECRET` in Render dashboard.

### 2. Restrict CORS
Update `APP_CORS_ALLOWED_ORIGINS` to only your frontend URL:
```
https://mockserver-frontend-xxxx.onrender.com
```

### 3. Enable HTTPS Only
Your app already uses HTTPS by default on Render!

## 🐛 Troubleshooting

### Backend won't start
1. Check logs: Dashboard → Backend Service → Logs
2. Verify database URL is correct
3. Ensure JAR file exists in `target/` directory

### Frontend shows connection error
1. Check `VITE_API_BASE_URL` is correct
2. Verify backend is running (check health endpoint)
3. Check browser console for CORS errors
4. Ensure CORS is configured correctly on backend

### Database connection failed
1. Verify database is running
2. Check database credentials
3. Ensure using **Internal Database URL** (not external)
4. Check if database and backend are in same region

### Build failed
1. Check build logs for specific error
2. Ensure `mvn clean package` works locally
3. Verify all dependencies are in `pom.xml`
4. Check Java version is 17

## 📊 Monitoring

### View Logs
- **Render**: Dashboard → Service → Logs tab
- Real-time logs show all requests and errors

### Metrics
- **Render**: Dashboard → Service → Metrics tab
- Shows CPU, memory, and request stats

### Alerts
Set up email alerts:
1. Dashboard → Service → Settings
2. Enable "Deploy notifications"
3. Enable "Service health notifications"

## 🔄 Auto-Deploy Updates

Render automatically deploys when you push to GitHub:

```bash
# Make changes to your code
git add .
git commit -m "Update feature"
git push

# Render automatically detects and deploys!
```

## 🌐 Custom Domain (Optional)

### Add Your Domain
1. Dashboard → Frontend Service → Settings
2. Click "Custom Domain"
3. Add your domain: `mockserver.yourdomain.com`
4. Update DNS records as shown
5. SSL certificate auto-generated!

## 💡 Pro Tips

1. **Use environment variables** for all configuration
2. **Monitor logs** regularly for errors
3. **Set up UptimeRobot** to keep free tier active
4. **Use Render's preview environments** for testing
5. **Enable auto-deploy** for continuous deployment

## 📚 Next Steps

- [ ] Set up custom domain
- [ ] Configure monitoring and alerts
- [ ] Add more mock endpoints
- [ ] Invite team members
- [ ] Set up CI/CD pipeline
- [ ] Add API analytics
- [ ] Implement rate limiting
- [ ] Add more authentication methods

## 🆘 Need Help?

- **Render Docs**: https://render.com/docs
- **Render Community**: https://community.render.com
- **Project Issues**: Check application logs first
- **Database Issues**: Verify connection string and credentials

---

**Congratulations!** Your Dynamic API Mock Server is now live and accessible worldwide! 🌍
