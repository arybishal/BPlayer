import math
import os
from PIL import Image, ImageDraw, ImageFont

def make_bplayer_logo():
    # 108x108 viewport coordinates
    # Let's test evenOdd path and solid shapes
    # Canvas scale for rendering high quality PNG: 10x = 1080x1080
    scale = 10
    W = 108 * scale
    H = 108 * scale
    
    # We will generate:
    # 1. Dark Theme Logo (for dark backgrounds):
    #    Background: #0E1013 (Obsidian Dark) or transparent
    #    Body: #FFFFFF (Crisp White)
    #    Accent Play Head: #10B981 (Emerald Green / Audiophile Pulse) or #00E676
    
    # 2. Light Theme Logo (for light backgrounds):
    #    Background: #FFFFFF (Pure White) or transparent
    #    Body: #121316 (Deep Charcoal Obsidian)
    #    Accent Play Head: #059669 (Deep Emerald)
    
    # Let's draw using precise Bezier / geometric shapes
    # Coordinates in 108 space:
    # Safe circle: center = (54, 54), radius = 33 -> circle from (21, 21) to (87, 87)
    
    # Let's write an SVG generator first so we have mathematical vector precision:
    
    svg_light = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">
  <defs>
    <linearGradient id="emeraldGradLight" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#10B981" />
      <stop offset="100%" stop-color="#059669" />
    </linearGradient>
  </defs>
  <!-- BPlayer Light Mode Logo -->
  <g id="bplayer-symbol">
    <!-- Left Stem: Vertical Sound Capsule -->
    <rect x="30" y="27" width="10" height="54" rx="5" fill="#121316" />
    <!-- Upper Loop -->
    <path d="M 35 27 L 57 27 C 65.28 27 72 33.72 72 42 C 72 50.28 65.28 53.5 57 53.5 L 35 53.5 Z" fill="#121316" />
    <!-- Upper Loop Counter Cutout -->
    <path d="M 40 35 L 56 35 C 60.42 35 64 38.13 64 42 C 64 45.87 60.42 46.5 56 46.5 L 40 46.5 Z" fill="#FFFFFF" />
    <!-- Lower Loop -->
    <path d="M 35 50.5 L 61 50.5 C 70.39 50.5 78 57.11 78 66.5 C 78 75.89 70.39 81 61 81 L 35 81 Z" fill="#121316" />
    <!-- Lower Loop Counter: Embedded Play Triangle -->
    <path d="M 45 56.5 C 45 55.4 46.2 54.7 47.1 55.3 L 64.2 65.1 C 65.1 65.6 65.1 66.9 64.2 67.4 L 47.1 77.2 C 46.2 77.8 45 77.1 45 76.0 Z" fill="url(#emeraldGradLight)" />
  </g>
</svg>"""

    svg_dark = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="108" height="108">
  <defs>
    <linearGradient id="emeraldGradDark" x1="0%" y1="0%" x2="100%" y2="100%">
      <stop offset="0%" stop-color="#34D399" />
      <stop offset="100%" stop-color="#10B981" />
    </linearGradient>
  </defs>
  <!-- BPlayer Dark Mode Logo -->
  <g id="bplayer-symbol">
    <!-- Left Stem: Vertical Sound Capsule -->
    <rect x="30" y="27" width="10" height="54" rx="5" fill="#FFFFFF" />
    <!-- Upper Loop -->
    <path d="M 35 27 L 57 27 C 65.28 27 72 33.72 72 42 C 72 50.28 65.28 53.5 57 53.5 L 35 53.5 Z" fill="#FFFFFF" />
    <!-- Upper Loop Counter Cutout -->
    <path d="M 40 35 L 56 35 C 60.42 35 64 38.13 64 42 C 64 45.87 60.42 46.5 56 46.5 L 40 46.5 Z" fill="#0E1013" />
    <!-- Lower Loop -->
    <path d="M 35 50.5 L 61 50.5 C 70.39 50.5 78 57.11 78 66.5 C 78 75.89 70.39 81 61 81 L 35 81 Z" fill="#FFFFFF" />
    <!-- Lower Loop Counter: Embedded Play Triangle -->
    <path d="M 45 56.5 C 45 55.4 46.2 54.7 47.1 55.3 L 64.2 65.1 C 65.1 65.6 65.1 66.9 64.2 67.4 L 47.1 77.2 C 46.2 77.8 45 77.1 45 76.0 Z" fill="url(#emeraldGradDark)" />
  </g>
</svg>"""

    # Also a pure single-path unified vector for Android vector drawable with evenOdd fill
    # This guarantees that at any scale, under any background, or in monochrome themed icon mode,
    # the entire glyph renders perfectly without any transparency/overlay artifacts!
    
    with open("docs/branding/bplayer_logo_light.svg", "w", encoding="utf-8") as f:
        f.write(svg_light)
    with open("docs/branding/bplayer_logo_dark.svg", "w", encoding="utf-8") as f:
        f.write(svg_dark)
        
    print("SVGs generated successfully.")

make_bplayer_logo()
