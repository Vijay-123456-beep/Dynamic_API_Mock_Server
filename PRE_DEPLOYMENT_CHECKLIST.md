# Pre-Deployment Checklist

Complete this checklist before deploying to production.

## ✅ Code Preparation

### Backend
- [ ] All tests pass: `mvn test`
- [ ] Application builds successfully: `mvn clean package`
- [ ] JAR file created in `target/` directory
- [ ] No hardcoded credentials in code
- [ ] All sensitive data uses environment variables
- [ ] Database migrations are tested
- [ ] API endpoints are documented (Swagger)
- [ ] Error handling is implemented
- [ ] Logging is configured properly

### Frontend
- [ ] Build succeeds: `cd mock-ui && npm run build`
- [ ] No console errors in production build
- [ ] Environment variables are configured
- [ ] API base URL is parameterized
- [ ] All routes work correctly
- [ ] Forms have validation
- [ ] Loading states are implemented
- [ ] Error messages are user-friendly

## 🔒 Security

- [ ] JWT secret is changed from default
- [ ] Database password is strong and unique
- [ ] CORS is configured for production domains only
- [ ] SQL injection prevention is in place (JPA handles this)
- [ ] XSS prevention is implemented
- [ ] Rate limiting is configured (if needed)
- [ ] HTTPS is enforced
- [ ] Sensitive endpoints require authentication
- [ ] Input validation is implemented
- [ ] No secrets in Git repository

## 🗄️ Database

- [ ] Database schema is finalized
- [ ] Indexes are created for performance
- [ ] Database backup strategy is planned
- [ ] Connection pooling is configured
- [ ] Database timezone is set correctly
- [ ] Migration scripts are tested
- [ ] Data seeding is optional (not required)

## 🌐 Infrastructure

- [ ] Domain name is registered (optional)
- [ ] DNS is configured (if using custom domain)
- [ ] SSL certificate is ready (auto-generated on most platforms)
- [ ] CDN is configured (optional)
- [ ] Monitoring is set up
- [ ] Logging is configured
- [ ] Backup strategy is in place

## 📝 Configuration

### Environment Variables Checklist

#### Backend Required
```
✅ SPRING_DATASOURCE_URL
✅ SPRING_DATASOURCE_USERNAME
✅ SPRING_DATASOURCE_PASSWORD
✅ SPRING_PROFILES_ACTIVE=prod
✅ APP_JWT_SECRET
✅ APP_CORS_ALLOWED_ORIGINS
```

#### Frontend Required
```
✅ VITE_API_BASE_URL
```

#### Optional
```
⬜ PORT (defaults to 8088)
⬜ JAVA_OPTS (for JVM tuning)
⬜ SPRING_JPA_HIBERNATE_DDL_AUTO (update/validate)
```

## 🧪 Testing

### Local Testing
- [ ] Application runs locally with production config
- [ ] Database connection works
- [ ] Authentication flow works (register, login, logout)
- [ ] Mock endpoint CRUD operations work
- [ ] Dynamic mock serving works
- [ ] WebSocket connections work (if used)
- [ ] File upload/download works (if applicable)
- [ ] All API endpoints return correct responses

### Integration Testing
- [ ] Frontend connects to backend successfully
- [ ] CORS is configured correctly
- [ ] API calls work from frontend
- [ ] Error handling works end-to-end
- [ ] Session management works
- [ ] Token refresh works (if implemented)

## 📊 Performance

- [ ] Database queries are optimized
- [ ] Indexes are in place
- [ ] Connection pooling is configured
- [ ] Response compression is enabled
- [ ] Static assets are optimized
- [ ] Images are compressed
- [ ] Bundle size is reasonable (< 500KB for frontend)
- [ ] API response times are acceptable (< 500ms)

## 📚 Documentation

- [ ] README.md is up to date
- [ ] API documentation is complete (Swagger)
- [ ] Deployment guide is written
- [ ] Environment variables are documented
- [ ] Architecture diagram is created (optional)
- [ ] User guide is written (optional)
- [ ] Troubleshooting guide is available

## 🔄 Deployment Process

- [ ] Git repository is clean (no uncommitted changes)
- [ ] Code is pushed to main branch
- [ ] Version is tagged (optional): `git tag v1.0.0`
- [ ] Deployment platform account is created
- [ ] Database is provisioned
- [ ] Backend service is configured
- [ ] Frontend service is configured
- [ ] Environment variables are set
- [ ] Custom domain is configured (optional)

## 🚀 Post-Deployment

- [ ] Health check endpoint responds: `/actuator/health`
- [ ] Frontend loads successfully
- [ ] Can register a new user
- [ ] Can login with credentials
- [ ] Can create a mock endpoint
- [ ] Can test the mock endpoint
- [ ] API documentation is accessible: `/swagger-ui/index.html`
- [ ] Logs are accessible and readable
- [ ] Monitoring is working
- [ ] Alerts are configured (optional)

## 🐛 Troubleshooting Preparation

- [ ] Know how to access logs
- [ ] Know how to restart services
- [ ] Know how to rollback deployment
- [ ] Have database backup/restore procedure
- [ ] Have emergency contact list (if team)
- [ ] Have runbook for common issues

## 📈 Monitoring & Maintenance

- [ ] Set up uptime monitoring (UptimeRobot, etc.)
- [ ] Configure error tracking (optional)
- [ ] Set up performance monitoring (optional)
- [ ] Plan for regular updates
- [ ] Plan for security patches
- [ ] Plan for database backups
- [ ] Plan for scaling (if needed)

## 💰 Cost Management

- [ ] Understand pricing model
- [ ] Set up billing alerts
- [ ] Monitor resource usage
- [ ] Plan for scaling costs
- [ ] Optimize resource allocation

## 🎯 Launch Checklist

### Day Before Launch
- [ ] Run full test suite
- [ ] Review all configurations
- [ ] Backup current database (if applicable)
- [ ] Notify team of deployment time
- [ ] Prepare rollback plan

### Launch Day
- [ ] Deploy during low-traffic time
- [ ] Monitor logs during deployment
- [ ] Test all critical paths
- [ ] Verify database migrations
- [ ] Check all integrations
- [ ] Monitor error rates
- [ ] Monitor performance metrics

### After Launch
- [ ] Monitor for 24 hours
- [ ] Check error logs
- [ ] Verify user registrations work
- [ ] Test from different locations
- [ ] Get user feedback
- [ ] Document any issues
- [ ] Plan for improvements

## 🎉 Ready to Deploy?

If you've checked all the boxes above, you're ready to deploy!

### Quick Deploy Commands

1. **Build Backend**
   ```bash
   mvn clean package -DskipTests
   ```

2. **Test Backend**
   ```bash
   mvn test
   ```

3. **Build Frontend**
   ```bash
   cd mock-ui
   npm install
   npm run build
   ```

4. **Push to Git**
   ```bash
   git add .
   git commit -m "Ready for production deployment"
   git push origin main
   ```

5. **Follow Deployment Guide**
   - See `QUICK_DEPLOY.md` for step-by-step instructions
   - See `DEPLOYMENT_GUIDE.md` for detailed options

## 📞 Support

If you encounter issues:
1. Check logs first
2. Review environment variables
3. Verify database connection
4. Check CORS configuration
5. Review deployment guide
6. Check platform documentation

---

**Good luck with your deployment!** 🚀
