import math
import os
from PIL import Image, ImageDraw, ImageFont, ImageFilter

def create_branding_assets():
    os.makedirs("docs/branding", exist_ok=True)
    os.makedirs("app/src/main/res/drawable", exist_ok=True)
    os.makedirs("app/src/main/res/drawable-night", exist_ok=True)
    
    # -------------------------------------------------------------
    # 1. Android Vector Drawable XML Definitions
    # -------------------------------------------------------------
    # Coordinates in 108x108 space, scaled to fit within safe zone r <= 32dp centered at (54, 54)
    #
    # BPlayer Logo Symbol Geometry:
    # Stem: x=[33..42], y=[29..79], rx=4.5
    # Upper loop: x=[38..71], y=[29..52]
    # Upper counter cutout: x=[42..59], y=[36..46]
    # Lower loop: x=[38..76], y=[50..79]
    # Lower counter: Play Triangle ▶ with apex at (67, 65.5), base at x=[48], y=[57..74]
    
    # LIGHT MODE VECTOR DRAWABLE (app/src/main/res/drawable/ic_bplayer_logo.xml)
    # Deep Charcoal #121316 body with Emerald #10B981 Play Accent
    vd_light = """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- BPlayer Official Symbol (Light Mode) -->
    <!-- Vertical Sound Stem -->
    <path
        android:fillColor="#121316"
        android:pathData="M33,33.5 A4.5,4.5 0 0 1 42,33.5 L42,74.5 A4.5,4.5 0 0 1 33,74.5 Z" />
    <!-- Upper Acoustic Loop -->
    <path
        android:fillColor="#121316"
        android:pathData="M38,29 L56,29 C64.28,29 71,34.15 71,40.5 C71,46.85 64.28,52 56,52 L38,52 Z" />
    <!-- Upper Counter Cutout (negative space) -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M42,35.5 L55,35.5 C58.87,35.5 62,37.74 62,40.5 C62,43.26 58.87,45.5 55,45.5 L42,45.5 Z" />
    <!-- Lower Acoustic Loop -->
    <path
        android:fillColor="#121316"
        android:pathData="M38,50 L58,50 C67.94,50 76,56.49 76,64.5 C76,72.51 67.94,79 58,79 L38,79 Z" />
    <!-- Play Triangle Head (Emerald Accent) -->
    <path
        android:fillColor="#10B981"
        android:pathData="M48,57.5 C48,56.6 49.0,56.0 49.8,56.5 L64.8,64.5 C65.6,64.9 65.6,66.1 64.8,66.5 L49.8,74.5 C49.0,75.0 48,74.4 48,73.5 Z" />
</vector>
"""

    # DARK MODE VECTOR DRAWABLE (app/src/main/res/drawable-night/ic_bplayer_logo.xml)
    # Crisp Platinum White #FFFFFF body with Vibrant Emerald #34D399 Play Accent
    vd_dark = """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp"
    android:height="108dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- BPlayer Official Symbol (Dark Mode) -->
    <!-- Vertical Sound Stem -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M33,33.5 A4.5,4.5 0 0 1 42,33.5 L42,74.5 A4.5,4.5 0 0 1 33,74.5 Z" />
    <!-- Upper Acoustic Loop -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M38,29 L56,29 C64.28,29 71,34.15 71,40.5 C71,46.85 64.28,52 56,52 L38,52 Z" />
    <!-- Upper Counter Cutout (negative space matching dark background #0E1013) -->
    <path
        android:fillColor="#0E1013"
        android:pathData="M42,35.5 L55,35.5 C58.87,35.5 62,37.74 62,40.5 C62,43.26 58.87,45.5 55,45.5 L42,45.5 Z" />
    <!-- Lower Acoustic Loop -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M38,50 L58,50 C67.94,50 76,56.49 76,64.5 C76,72.51 67.94,79 58,79 L38,79 Z" />
    <!-- Play Triangle Head (Vibrant Emerald Accent) -->
    <path
        android:fillColor="#34D399"
        android:pathData="M48,57.5 C48,56.6 49.0,56.0 49.8,56.5 L64.8,64.5 C65.6,64.9 65.6,66.1 64.8,66.5 L49.8,74.5 C49.0,75.0 48,74.4 48,73.5 Z" />
</vector>
"""

    # MONOCHROME SINGLE-PATH VECTOR DRAWABLE (app/src/main/res/drawable/ic_bplayer_logo_mono.xml)
    # Uses evenOdd fill so it works seamlessly on any tint or Material You themed icon!
    vd_mono = """<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="108"
    android:viewportHeight="108">
    <!-- BPlayer Monochrome / Themed Icon Vector (evenOdd fill) -->
    <path
        android:fillColor="#FFFFFFFF"
        android:fillType="evenOdd"
        android:pathData="M33,33.5 A4.5,4.5 0 0 1 42,33.5 L42,29 L56,29 C64.28,29 71,34.15 71,40.5 C71,46.85 64.28,52 56,52 L58,52 C67.94,52 76,57.49 76,65.5 C76,73.51 67.94,79 58,79 L42,79 L42,74.5 A4.5,4.5 0 0 1 33,74.5 Z M42,35.5 L55,35.5 C58.87,35.5 62,37.74 62,40.5 C62,43.26 58.87,45.5 55,45.5 L42,45.5 Z M48,57.5 C48,56.6 49.0,56.0 49.8,56.5 L64.8,64.5 C65.6,64.9 65.6,66.1 64.8,66.5 L49.8,74.5 C49.0,75.0 48,74.4 48,73.5 Z" />
</vector>
"""

    # Write vector drawables
    with open("app/src/main/res/drawable/ic_bplayer_logo.xml", "w", encoding="utf-8") as f:
        f.write(vd_light)
    with open("app/src/main/res/drawable-night/ic_bplayer_logo.xml", "w", encoding="utf-8") as f:
        f.write(vd_dark)
    with open("app/src/main/res/drawable/ic_bplayer_logo_mono.xml", "w", encoding="utf-8") as f:
        f.write(vd_mono)
        
    # Also update ic_launcher_foreground.xml in drawable (light) and drawable-night (dark)
    with open("app/src/main/res/drawable/ic_launcher_foreground.xml", "w", encoding="utf-8") as f:
        f.write(vd_light)
    with open("app/src/main/res/drawable-night/ic_launcher_foreground.xml", "w", encoding="utf-8") as f:
        f.write(vd_dark)
        
    # Update ic_launcher_logo.xml (used by in-app and about screens)
    with open("app/src/main/res/drawable/ic_launcher_logo.xml", "w", encoding="utf-8") as f:
        f.write(vd_light)
    with open("app/src/main/res/drawable-night/ic_launcher_logo.xml", "w", encoding="utf-8") as f:
        f.write(vd_dark)

    print("Vector drawables successfully written.")

create_branding_assets()
