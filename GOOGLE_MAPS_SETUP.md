# Google Maps API Key Setup

To use the StartRunActivity with Google Maps, you need to set up a Google Maps API key:

## Steps:

1. **Get a Google Maps API Key:**
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select an existing one
   - Enable the "Maps SDK for Android" API
   - Create credentials (API Key)
   - Restrict the API key to your app's package name and SHA-1 certificate fingerprint

2. **Add the API Key to your project:**
   - Open `app/src/main/AndroidManifest.xml`
   - Find the line: `<meta-data android:name="com.google.android.geo.API_KEY" android:value="YOUR_API_KEY_HERE" />`
   - Replace `YOUR_API_KEY_HERE` with your actual API key

3. **Get your SHA-1 fingerprint:**
   ```bash
   # For debug keystore (default):
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # For release keystore (if you have one):
   keytool -list -v -keystore your-release-keystore.jks -alias your-alias
   ```

## Note:
- Google Maps has a free tier with generous limits
- The app will work without the API key, but the map will not display
- Make sure to restrict your API key in Google Cloud Console for security

