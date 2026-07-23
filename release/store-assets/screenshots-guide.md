# Google Play Store Screenshots Guide
## Snooker Scorekeeper v1.0.0

### Screenshot Requirements
- **Size**: 1080 x 1920 px (portrait phone)
- **Format**: PNG or JPEG
- **Count**: 2-8 screenshots recommended

### Recommended Screenshots (5 total)
1. **Scoreboard** - Main game screen with player scores and ball buttons
2. **Setup** - Player name entry screen  
3. **Foul Sheet** - Foul penalty selection dialog
4. **End Game** - End game confirmation with winner display
5. **History** - Match history list

### Feature Graphic
- **Size**: 1024 x 500 px
- Created as SVG: `release/store-assets/feature-graphic.svg`

### How to Generate
Run the app on emulator/device and take screenshots via:
1. Press Power + Volume Down (physical device)
2. Or use: `adb shell screencap /sdcard/screenshot.png && adb pull /sdcard/screenshot.png`

### Store Icon
- Use the existing 512x512 PNG or the adaptive icon
- Convert the iOS 1024x1024 icon if needed
