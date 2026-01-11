# API Key Configuration Guide

## Storing Your AlphaVantage API Key Securely

Your AlphaVantage API key should **never** be committed to Git. Here are the recommended approaches:

---

## Option 1: Environment Variable (Recommended for Production)

### macOS/Linux:

```bash
# Set for current terminal session
export ALPHAVANTAGE_API_KEY=your_actual_api_key_here

# Make it permanent by adding to your shell profile
echo 'export ALPHAVANTAGE_API_KEY=your_actual_api_key_here' >> ~/.zshrc
source ~/.zshrc
```

### Windows (PowerShell):

```powershell
# Set for current session
$env:ALPHAVANTAGE_API_KEY="your_actual_api_key_here"

# Make it permanent (System-wide)
[System.Environment]::SetEnvironmentVariable('ALPHAVANTAGE_API_KEY', 'your_actual_api_key_here', 'User')
```

Then run your application normally:
```bash
mvn spring-boot:run -pl pulse-heartbeat
```

---

## Option 2: application-local.yml (Recommended for Development)

1. **Edit the file**: `pulse-heartbeat/src/main/resources/application-local.yml`
2. **Replace** `YOUR_ALPHAVANTAGE_API_KEY_HERE` with your actual API key
3. **Run with the local profile**:

```bash
mvn spring-boot:run -pl pulse-heartbeat -Dspring-boot.run.profiles=local
```

Or set the profile in your IDE:
- **IntelliJ IDEA**: Run Configuration → Environment Variables → `SPRING_PROFILES_ACTIVE=local`
- **VS Code**: Add to `launch.json`: `"env": { "SPRING_PROFILES_ACTIVE": "local" }`

**Note**: The `application-local.yml` file is already added to `.gitignore` and will not be committed.

---

## Option 3: .env File (Alternative)

Create a `.env` file in the project root:

```bash
ALPHAVANTAGE_API_KEY=your_actual_api_key_here
```

Then source it before running:
```bash
source .env
mvn spring-boot:run -pl pulse-heartbeat
```

**Note**: The `.env` file is already added to `.gitignore`.

---

## Verification

To verify your API key is loaded correctly, check the application logs on startup. You should see:
```
Fetching daily adjusted prices for ticker: AAPL
```

If you see `demo` in the API URL, your key is not being loaded properly.

---

## Security Best Practices

✅ **DO**:
- Use environment variables for production deployments
- Use `application-local.yml` for local development
- Keep API keys in secure password managers
- Rotate API keys periodically

❌ **DON'T**:
- Commit API keys to Git
- Share API keys in chat/email
- Hardcode API keys in source code
- Use production keys in development

---

## Getting Your AlphaVantage API Key

If you don't have an API key yet:
1. Visit: https://www.alphavantage.co/support/#api-key
2. Enter your email and get a free API key
3. Free tier: 25 requests/day, 5 requests/minute
