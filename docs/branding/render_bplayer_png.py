import math
import os
from PIL import Image, ImageDraw, ImageFont

def render_bplayer_png(is_dark=False, size=512, background_color=None, is_round=False):
    # Render at 4x oversampling for pristine anti-aliasing
    scale = 4
    canvas_size = size * scale
    s = canvas_size / 108.0
    
    # Body and accent colors
    if is_dark:
        body_color = (255, 255, 255, 255) # Crisp White
        accent_color = (52, 211, 153, 255) # Vibrant Emerald #34D399
        bg_color = (14, 16, 19, 255) if background_color is None else background_color
        counter_cutout_color = bg_color
    else:
        body_color = (18, 19, 22, 255) # Deep Charcoal #121316
        accent_color = (16, 185, 129, 255) # Emerald #10B981
        bg_color = (255, 255, 255, 255) if background_color is None else background_color
        counter_cutout_color = bg_color

    img = Image.new("RGBA", (canvas_size, canvas_size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    if bg_color and bg_color[3] > 0:
        if is_round:
            draw.ellipse([0, 0, canvas_size, canvas_size], fill=bg_color)
        else:
            # Rounded squircle for launcher icons, or pure rect
            draw.rectangle([0, 0, canvas_size, canvas_size], fill=bg_color)

    # 1. Lower Loop Outer
    # Bounding box of ellipse for lower outer curve:
    # Center (58, 64.5), rx=18, ry=14.5 -> x:[40..76], y:[50..79]
    draw.chord([40 * s, 50 * s, 76 * s, 79 * s], start=-90, end=90, fill=body_color)
    draw.rectangle([38 * s, 50 * s, 58 * s, 79 * s], fill=body_color)

    # 2. Upper Loop Outer
    # Center (56, 40.5), rx=15, ry=11.5 -> x:[41..71], y:[29..52]
    draw.chord([41 * s, 29 * s, 71 * s, 52 * s], start=-90, end=90, fill=body_color)
    draw.rectangle([38 * s, 29 * s, 56 * s, 52 * s], fill=body_color)

    # 3. Vertical Stem: x=[33..42], y=[29..79], rounded ends rx=4.5
    draw.rounded_rectangle([33 * s, 29 * s, 42 * s, 79 * s], radius=4.5 * s, fill=body_color)

    # 4. Upper Counter Cutout (matching background color)
    # Center (55, 40.5), rx=7, ry=5 -> x:[48..62], y:[35.5..45.5]
    draw.chord([48 * s, 35.5 * s, 62 * s, 45.5 * s], start=-90, end=90, fill=counter_cutout_color)
    draw.rectangle([42 * s, 35.5 * s, 55 * s, 45.5 * s], fill=counter_cutout_color)

    # 5. Play Triangle Head (Emerald Accent)
    # Apex at (64.8, 65.5), base top (48, 57.5), base bottom (48, 73.5)
    triangle_points = [
        (48 * s, 57.5 * s),
        (64.8 * s, 65.5 * s),
        (48 * s, 73.5 * s)
    ]
    draw.polygon(triangle_points, fill=accent_color)
    # Soften apex corner
    apex_r = 1.8 * s
    draw.ellipse([64.8 * s - apex_r, 65.5 * s - apex_r, 64.8 * s + apex_r, 65.5 * s + apex_r], fill=accent_color)
    draw.ellipse([48 * s - apex_r, 57.5 * s - apex_r, 48 * s + apex_r, 57.5 * s + apex_r], fill=accent_color)
    draw.ellipse([48 * s - apex_r, 73.5 * s - apex_r, 48 * s + apex_r, 73.5 * s + apex_r], fill=accent_color)

    # Downsample to target size with LANCZOS
    final_img = img.resize((size, size), Image.Resampling.LANCZOS)
    return final_img

def render_wordmark(is_dark=False, width=640, height=180):
    # Render symbol + "BPlayer" wordmark text
    scale = 3
    W = width * scale
    H = height * scale
    
    bg_color = (14, 16, 19, 255) if is_dark else (255, 255, 255, 255)
    text_color = (255, 255, 255, 255) if is_dark else (18, 19, 22, 255)
    
    img = Image.new("RGBA", (W, H), bg_color)
    
    # Render logo symbol at height H * 0.72
    symbol_size = int(H * 0.72)
    symbol_img = render_bplayer_png(is_dark=is_dark, size=symbol_size, background_color=(0, 0, 0, 0))
    
    # Paste symbol on left
    offset_x = int(W * 0.08)
    offset_y = int((H - symbol_size) / 2)
    img.alpha_composite(symbol_img, (offset_x, offset_y))
    
    # Draw wordmark text: try to find a clean sans font or draw bold geometric type
    draw = ImageDraw.Draw(img)
    text = "BPlayer"
    font = None
    # Check system fonts for modern sans (Segoe UI, Roboto, Arial, etc.)
    font_candidates = [
        "C:/Windows/Fonts/segoeuib.ttf",
        "C:/Windows/Fonts/arialbd.ttf",
        "C:/Windows/Fonts/calibrib.ttf",
        "C:/Windows/Fonts/seguisb.ttf",
    ]
    for fc in font_candidates:
        if os.path.exists(fc):
            try:
                font = ImageFont.truetype(fc, int(H * 0.42))
                break
            except Exception:
                pass
                
    text_x = offset_x + symbol_size + int(W * 0.04)
    text_y = int(H * 0.28)
    if font:
        draw.text((text_x, text_y), text, font=font, fill=text_color)
    else:
        draw.text((text_x, text_y), text, fill=text_color)
        
    final_img = img.resize((width, height), Image.Resampling.LANCZOS)
    return final_img

# Test render
img_l = render_bplayer_png(is_dark=False, size=512)
img_l.save("docs/branding/bplayer_logo_light_512.png")

img_d = render_bplayer_png(is_dark=True, size=512)
img_d.save("docs/branding/bplayer_logo_dark_512.png")

wm_l = render_wordmark(is_dark=False)
wm_l.save("docs/branding/bplayer_wordmark_light.png")

wm_d = render_wordmark(is_dark=True)
wm_d.save("docs/branding/bplayer_wordmark_dark.png")

print("Rendered 512px logos and wordmarks successfully.")
