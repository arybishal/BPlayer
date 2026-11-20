import math
from PIL import Image, ImageDraw, ImageFont

def render_logo_svg_and_png():
    # Design 1: Modern Audiophile B with Integrated Play Head
    # Viewport 108x108 (Android Adaptive Icon standard)
    # Safe zone: Circle of radius 33 at (54, 54) -> fits in [21, 21, 87, 87]
    
    # We will generate:
    # 1. Light Mode Logo (Dark Obsidian / Slate + Warm Amber/Emerald accent on light background)
    # 2. Dark Mode Logo (Crisp Platinum White + Radiant Amber/Emerald accent on dark background)
    # 3. Vector XML for Android
    # 4. SVG for docs/branding
    
    # Let's create high-res PIL images (1080x1080) for crisp preview
    scale = 10
    size = 108 * scale
    
    # Light mode preview: Soft porcelain background
    img_light = Image.new("RGBA", (size, size), (255, 255, 255, 255))
    draw_l = ImageDraw.Draw(img_light)
    
    # Dark mode preview: Deep obsidian background
    img_dark = Image.new("RGBA", (size, size), (16, 15, 15, 255))
    draw_d = ImageDraw.Draw(img_dark)
    
    # Let's define the geometry in 108-scale:
    # Stem: x from 28 to 38, y from 26 to 82. (width 10, height 56)
    # Upper loop: x: 38 to 68, y: 26 to 52
    # Lower loop: x: 38 to 74, y: 50 to 82
    # Play triangle in lower counter or central counter:
    
    print("Generating logo variations...")

render_logo_svg_and_png()
