import os
import math
from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUTPUT_DIR = "docs/branding"
LOGO_PATH = os.path.join(OUTPUT_DIR, "bplayer_logo_dark_512.png")

FONT_BOLD = "C:/Windows/Fonts/segoeuib.ttf"
FONT_REGULAR = "C:/Windows/Fonts/segoeui.ttf"
FONT_SEMIBOLD = "C:/Windows/Fonts/segoeuiz.ttf"

def get_font(path, size):
    try:
        return ImageFont.truetype(path, size)
    except Exception:
        return ImageFont.truetype("C:/Windows/Fonts/arialbd.ttf" if "b" in path else "C:/Windows/Fonts/arial.ttf", size)

def draw_pill(draw, x, y, text, font, bg_color, text_color, border_color=None, padding_x=16, padding_y=8, radius=12):
    bbox = font.getbbox(text)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    w = tw + padding_x * 2
    h = th + padding_y * 2
    
    rect = [x, y, x + w, y + h]
    draw.rounded_rectangle(rect, radius=radius, fill=bg_color, outline=border_color, width=1)
    # text vertical centering
    tx = x + padding_x - bbox[0]
    ty = y + padding_y - bbox[1]
    draw.text((tx, ty), text, font=font, fill=text_color)
    return w, h

def draw_glass_card(draw, rect, radius=16, bg_color=(24, 27, 34, 220), border_color=(255, 255, 255, 28)):
    draw.rounded_rectangle(rect, radius=radius, fill=bg_color, outline=border_color, width=1)

def create_base_canvas(width, height):
    img = Image.new("RGBA", (width, height), (13, 15, 18, 255))
    draw = ImageDraw.Draw(img)
    
    # Warm subtle radial glow at top-left and bottom-right
    # Top-left subtle warm ambient
    glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow)
    glow_draw.ellipse([-150, -150, width // 2, height // 2 + 100], fill=(244, 162, 97, 18)) # Warm Amber
    glow_draw.ellipse([width // 2, height // 3, width + 200, height + 200], fill=(16, 185, 129, 20)) # Emerald
    glow = glow.filter(ImageFilter.GaussianBlur(80))
    img = Image.alpha_composite(img, glow)
    
    # Subtle grid lines / acoustic motif
    grid = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    g_draw = ImageDraw.Draw(grid)
    for x in range(0, width, 40):
        g_draw.line([(x, 0), (x, height)], fill=(255, 255, 255, 4), width=1)
    for y in range(0, height, 40):
        g_draw.line([(0, y), (width, y)], fill=(255, 255, 255, 4), width=1)
    img = Image.alpha_composite(img, grid)
    
    return img

def render_hero_banner():
    width, height = 1280, 640
    img = create_base_canvas(width, height)
    draw = ImageDraw.Draw(img)
    
    # Load Logo
    if os.path.exists(LOGO_PATH):
        logo = Image.open(LOGO_PATH).convert("RGBA").resize((130, 130), Image.Resampling.LANCZOS)
        img.paste(logo, (80, 80), logo)
    
    # Top Header & Product Badge
    font_badge = get_font(FONT_SEMIBOLD, 14)
    draw_pill(draw, 230, 95, "v1.2.0 PRODUCTION RELEASE • AUDIOPHILE ENGINE", font_badge, 
              (40, 30, 20, 240), (244, 162, 97, 255), (244, 162, 97, 120), padding_x=14, padding_y=6, radius=10)
    
    # App Name
    font_title = get_font(FONT_BOLD, 52)
    draw.text((230, 135), "BPlayer", font=font_title, fill=(255, 255, 255, 255))
    
    font_tagline = get_font(FONT_SEMIBOLD, 24)
    draw.text((80, 230), "Audiophile-Grade Music Player for Android", font=font_tagline, fill=(52, 211, 153, 255))
    
    font_sub = get_font(FONT_REGULAR, 17)
    draw.text((80, 270), 
              "Bit-Perfect Precision Audio • 4,000+ AutoEQ Profiles • Offline Audio Transcoding (MP3/FLAC/AAC/OPUS)", 
              font=font_sub, fill=(170, 175, 185, 255))
    
    # Waveform & Frequency Canvas Graphic (Right side)
    draw_glass_card(draw, [740, 70, 1200, 570], radius=24, bg_color=(19, 22, 28, 230), border_color=(255, 255, 255, 25))
    
    # Inner Signal Card
    draw.text((770, 95), "PRECISION SIGNAL PATH AUDIT", font=get_font(FONT_BOLD, 14), fill=(244, 162, 97, 255))
    draw_pill(draw, 1070, 90, "BIT-PERFECT", font=get_font(FONT_BOLD, 12), 
              bg_color=(16, 185, 129, 40), text_color=(52, 211, 153, 255), border_color=(16, 185, 129, 100), padding_x=10, padding_y=4, radius=6)
    
    # Waveform visual
    points = []
    for i in range(770, 1170, 5):
        norm = (i - 770) / 400.0
        # Simulated target curve + response
        y = 220 - math.sin(norm * math.pi * 3.5) * 45 - math.log10(norm * 9 + 1) * 30
        points.append((i, y))
    
    for i in range(len(points) - 1):
        draw.line([points[i], points[i+1]], fill=(52, 211, 153, 255), width=3)
    
    # Second curve (Harman Target)
    target_points = []
    for i in range(770, 1170, 5):
        norm = (i - 770) / 400.0
        y = 220 - math.sin(norm * math.pi * 3.0) * 35 - math.log10(norm * 8 + 1) * 20
        target_points.append((i, y))
    
    for i in range(len(target_points) - 1):
        draw.line([target_points[i], target_points[i+1]], fill=(244, 162, 97, 180), width=2)
    
    # Legend
    draw.line([(770, 280), (800, 280)], fill=(52, 211, 153, 255), width=3)
    draw.text((810, 272), "Compensated AutoEQ (Biquad Filters)", font=get_font(FONT_REGULAR, 12), fill=(220, 225, 230, 255))
    draw.line([(980, 280), (1010, 280)], fill=(244, 162, 97, 180), width=2)
    draw.text((1020, 272), "Harman Target Curve", font=get_font(FONT_REGULAR, 12), fill=(220, 225, 230, 255))
    
    # Audio Specs Grid inside card
    specs = [
        ("Source Stream", "FLAC 24-bit / 96.0 kHz Stereo • 2,822 kbps"),
        ("DSP Engine", "32-bit Float • 10-Band Parametric EQ • Zero Clip"),
        ("Audio HAL Output", "Direct PCM AudioTrack (USB DAC Native)")
    ]
    cur_y = 315
    for title, desc in specs:
        draw_glass_card(draw, [765, cur_y, 1175, cur_y + 68], radius=12, bg_color=(25, 30, 38, 180), border_color=(255, 255, 255, 15))
        draw.text((780, cur_y + 12), title, font=get_font(FONT_BOLD, 13), fill=(52, 211, 153, 255))
        draw.text((780, cur_y + 36), desc, font=get_font(FONT_REGULAR, 12), fill=(180, 185, 195, 255))
        cur_y += 78

    # 4 Feature Action Cards on Left side
    card_w = 300
    card_h = 100
    left_cards = [
        ("💾 Offline Transcoding", "OPUS, MP3 (Shine C), AAC, FLAC\nAuto itag 140 direct stream discovery", 80, 330),
        ("🎧 4,000+ AutoEQ Curves", "Crinacle, Oratory1990, Rtings\nReal-time biquad parameter solver", 400, 330),
        ("🚗 Android Auto Ready", "Full MediaLibrarySession browse tree\nSafe vehicle steering & head unit UI", 80, 450),
        ("📜 Synchronized LRC Lyrics", "Embedded ID3 & local .lrc parser\nTap-to-seek & in-app timing editor", 400, 450),
    ]
    for c_title, c_desc, cx, cy in left_cards:
        draw_glass_card(draw, [cx, cy, cx + card_w, cy + card_h], radius=14, bg_color=(20, 23, 29, 210), border_color=(255, 255, 255, 20))
        draw.text((cx + 18, cy + 14), c_title, font=get_font(FONT_BOLD, 15), fill=(244, 162, 97, 255))
        draw.text((cx + 18, cy + 42), c_desc, font=get_font(FONT_REGULAR, 12), fill=(160, 165, 175, 255))
        
    # Bottom Bar badges
    badges = ["NO BLUE GRADIENTS", "AMOLED BLACK #000000", "KOTLIN 2.0 & MEDIA3", "100% OFFLINE CAPABLE", "OPEN SOURCE GPL-3.0"]
    bx = 80
    for b in badges:
        bw, _ = draw_pill(draw, bx, 580, b, get_font(FONT_BOLD, 11), (25, 28, 35, 200), (140, 145, 155, 255), (255, 255, 255, 18), padding_x=12, padding_y=6, radius=8)
        bx += bw + 12

    out_path = os.path.join(OUTPUT_DIR, "banner_hero_main.png")
    img.save(out_path, "PNG")
    print("Saved:", out_path)

def render_formats_banner():
    width, height = 1200, 500
    img = create_base_canvas(width, height)
    draw = ImageDraw.Draw(img)
    
    # Title
    draw_pill(draw, 70, 45, "OFFLINE MUSIC DOWNLOAD ENGINE", get_font(FONT_BOLD, 12), (40, 30, 20, 240), (244, 162, 97, 255), (244, 162, 97, 100), padding_x=12, padding_y=5, radius=8)
    draw.text((70, 80), "User-Selectable Offline Audio Formats", font=get_font(FONT_BOLD, 36), fill=(255, 255, 255, 255))
    draw.text((70, 130), "Genuine native audio conversion pipeline powered by Shine MP3 C engine, MediaCodec & ExoPlayer", font=get_font(FONT_REGULAR, 16), fill=(160, 165, 175, 255))
    
    # 5 Format Cards across the middle
    formats = [
        ("OPUS", "Default", "Pristine streaming fidelity", "~2.5 MB / song", "Native WebM Remux", (16, 185, 129, 255)),
        ("MP3", "Universal", "Car stereos & legacy DACs", "64k to 320 kbps", "Native Shine C Engine", (244, 162, 97, 255)),
        ("AAC / M4A", "High-Quality", "Apple / MPEG-4 standard", "128k to 256 kbps", "Direct itag 140 Stream", (52, 211, 153, 255)),
        ("FLAC", "Lossless", "Bit-exact audio fidelity", "Lossless container", "MediaCodec Hardware", (224, 122, 95, 255)),
        ("Original", "Bitstream", "Raw source container", "Exact stream bytes", "Direct Bitstream", (170, 175, 185, 255))
    ]
    
    card_w = 200
    card_h = 240
    start_x = 70
    start_y = 175
    
    for i, (fmt, badge, desc, size_text, engine, color) in enumerate(formats):
        cx = start_x + i * (card_w + 15)
        cy = start_y
        draw_glass_card(draw, [cx, cy, cx + card_w, cy + card_h], radius=16, bg_color=(20, 23, 30, 230), border_color=(255, 255, 255, 24))
        
        # Format title & badge
        draw.text((cx + 16, cy + 18), fmt, font=get_font(FONT_BOLD, 22), fill=(255, 255, 255, 255))
        draw_pill(draw, cx + 16, cy + 54, badge, get_font(FONT_BOLD, 10), (color[0], color[1], color[2], 30), color, (color[0], color[1], color[2], 90), padding_x=8, padding_y=3, radius=6)
        
        draw.text((cx + 16, cy + 90), desc, font=get_font(FONT_REGULAR, 12), fill=(180, 185, 195, 255))
        draw.text((cx + 16, cy + 135), "Typical Size:", font=get_font(FONT_BOLD, 11), fill=(130, 135, 145, 255))
        draw.text((cx + 16, cy + 152), size_text, font=get_font(FONT_SEMIBOLD, 13), fill=color)
        
        # Engine tag
        draw_glass_card(draw, [cx + 12, cy + 185, cx + card_w - 12, cy + 225], radius=8, bg_color=(12, 14, 18, 200), border_color=(255, 255, 255, 12))
        draw.text((cx + 20, cy + 196), engine, font=get_font(FONT_BOLD, 10), fill=(200, 205, 215, 255))

    # Bottom notes
    draw.text((70, 440), "⚡ Smart Stream Matching: Direct itag 140 AAC stream downloaded directly when AAC/M4A selected without re-encoding.", font=get_font(FONT_REGULAR, 13), fill=(52, 211, 153, 255))
    draw.text((70, 465), "🔒 Atomic Finalization: Staged in .tmp files, validated, and tagged with JAudioTagger before moving to Music/BPlayer.", font=get_font(FONT_REGULAR, 13), fill=(160, 165, 175, 255))

    out_path = os.path.join(OUTPUT_DIR, "banner_formats_transcoding.png")
    img.save(out_path, "PNG")
    print("Saved:", out_path)

def render_autoeq_banner():
    width, height = 1200, 500
    img = create_base_canvas(width, height)
    draw = ImageDraw.Draw(img)
    
    # Title
    draw_pill(draw, 70, 45, "AUDIOPHILE DSP & CALIBRATION", get_font(FONT_BOLD, 12), (20, 35, 30, 240), (52, 211, 153, 255), (16, 185, 129, 100), padding_x=12, padding_y=5, radius=8)
    draw.text((70, 80), "AutoEQ Headphone Correction & DSP Engine", font=get_font(FONT_BOLD, 36), fill=(255, 255, 255, 255))
    draw.text((70, 130), "Over 4,000 lab measurements, target curves & real-time Levenberg-Marquardt biquad filter solver", font=get_font(FONT_REGULAR, 16), fill=(160, 165, 175, 255))
    
    # Left Side: Features Card
    draw_glass_card(draw, [70, 175, 520, 445], radius=18, bg_color=(19, 22, 28, 230), border_color=(255, 255, 255, 25))
    
    eq_features = [
        ("🎧 4,000+ Headphone Lab Database", "Measurements from Crinacle, Oratory1990, Rtings, InnerFidelity"),
        ("🎯 Target Curve Calibration", "Harman 2020 Over-Ear/In-Ear, IEF Neutral, Optimum HiFi"),
        ("🎛️ 10-Band Parametric EQ Solver", "Real-time biquad calculation with automatic preamp gain protection"),
        ("🔬 Poweramp-Style Signal Inspector", "Audits bit-depth, float32 DSP, and direct AudioTrack HAL routing")
    ]
    ey = 195
    for f_title, f_desc in eq_features:
        draw.text((95, ey), f_title, font=get_font(FONT_BOLD, 15), fill=(244, 162, 97, 255))
        draw.text((95, ey + 24), f_desc, font=get_font(FONT_REGULAR, 12), fill=(170, 175, 185, 255))
        ey += 58

    # Right Side: Interactive Response Visualizer
    draw_glass_card(draw, [550, 175, 1130, 445], radius=18, bg_color=(16, 19, 24, 240), border_color=(255, 255, 255, 25))
    draw.text((580, 200), "FREQUENCY RESPONSE & TARGET COMPLIANCE (20Hz - 20kHz)", font=get_font(FONT_BOLD, 13), fill=(160, 165, 175, 255))
    
    # Grid lines inside graph
    for gy in range(250, 400, 35):
        draw.line([(580, gy), (1100, gy)], fill=(255, 255, 255, 15), width=1)
    for gx in range(580, 1100, 100):
        draw.line([(gx, 230), (gx, 390)], fill=(255, 255, 255, 15), width=1)
        
    freq_labels = ["20Hz", "100Hz", "500Hz", "1kHz", "5kHz", "20kHz"]
    for i, fl in enumerate(freq_labels):
        draw.text((580 + i * 100, 395), fl, font=get_font(FONT_REGULAR, 10), fill=(110, 115, 125, 255))

    # Curve 1: Raw Uncompensated (Soft White)
    pts_raw = []
    for x in range(580, 1100, 4):
        norm = (x - 580) / 520.0
        y = 310 - math.sin(norm * math.pi * 4) * 35 - math.cos(norm * 12) * 15 + (norm - 0.5) * 20
        pts_raw.append((x, y))
    for i in range(len(pts_raw) - 1):
        draw.line([pts_raw[i], pts_raw[i+1]], fill=(120, 125, 135, 160), width=2)

    # Curve 2: Harman Target (Warm Amber)
    pts_target = []
    for x in range(580, 1100, 4):
        norm = (x - 580) / 520.0
        y = 310 - math.sin(norm * math.pi * 3.2) * 40 - (norm * 25)
        pts_target.append((x, y))
    for i in range(len(pts_target) - 1):
        draw.line([pts_target[i], pts_target[i+1]], fill=(244, 162, 97, 240), width=2)

    # Curve 3: Calibrated Output (Vibrant Emerald)
    pts_eq = []
    for x in range(580, 1100, 4):
        norm = (x - 580) / 520.0
        y = 310 - math.sin(norm * math.pi * 3.2) * 38 - (norm * 24) + math.sin(norm * 20) * 3
        pts_eq.append((x, y))
    for i in range(len(pts_eq) - 1):
        draw.line([pts_eq[i], pts_eq[i+1]], fill=(52, 211, 153, 255), width=3)

    # Graph Legend at bottom of card
    draw.line([(580, 422), (605, 422)], fill=(120, 125, 135, 200), width=2)
    draw.text((615, 415), "Raw Measurement", font=get_font(FONT_REGULAR, 11), fill=(160, 165, 175, 255))
    
    draw.line([(740, 422), (765, 422)], fill=(244, 162, 97, 255), width=2)
    draw.text((775, 415), "Harman Target", font=get_font(FONT_REGULAR, 11), fill=(244, 162, 97, 255))
    
    draw.line([(880, 422), (905, 422)], fill=(52, 211, 153, 255), width=3)
    draw.text((915, 415), "Compensated Profile", font=get_font(FONT_REGULAR, 11), fill=(52, 211, 153, 255))

    out_path = os.path.join(OUTPUT_DIR, "banner_autoeq_dsp.png")
    img.save(out_path, "PNG")
    print("Saved:", out_path)

def render_lyrics_ui_banner():
    width, height = 1200, 500
    img = create_base_canvas(width, height)
    draw = ImageDraw.Draw(img)
    
    # Title
    draw_pill(draw, 70, 45, "LIQUID GLASS & IMMERSIVE UX", get_font(FONT_BOLD, 12), (30, 20, 35, 240), (244, 162, 97, 255), (244, 162, 97, 100), padding_x=12, padding_y=5, radius=8)
    draw.text((70, 80), "Frosted Liquid Glass & Synchronized Lyrics", font=get_font(FONT_BOLD, 36), fill=(255, 255, 255, 255))
    draw.text((70, 130), "Tactile ergonomics with high-precision timestamped LRC lyrics and genuine pitch black AMOLED", font=get_font(FONT_REGULAR, 16), fill=(160, 165, 175, 255))
    
    # Left Card: Now Playing Lyrics Card
    draw_glass_card(draw, [70, 175, 620, 445], radius=20, bg_color=(18, 20, 26, 240), border_color=(255, 255, 255, 30))
    draw_pill(draw, 95, 195, "SYNCHRONIZED LRC", get_font(FONT_BOLD, 11), (16, 185, 129, 30), (52, 211, 153, 255), (16, 185, 129, 80), padding_x=10, padding_y=4, radius=6)
    
    lyrics = [
        ("[01:24.10]", "Hold on to the feeling that we found", False),
        ("[01:28.45]", "Every single frequency spinning all around", True), # Active
        ("[01:33.20]", "Listen close as the warmth begins to speak", False),
        ("[01:37.80]", "Clear and pure, the rhythm reaches its peak", False)
    ]
    
    ly = 240
    for ts, line, is_active in lyrics:
        if is_active:
            # Highlight active lyric bar
            draw_glass_card(draw, [90, ly - 8, 600, ly + 36], radius=10, bg_color=(40, 30, 20, 220), border_color=(244, 162, 97, 120))
            draw.text((105, ly + 2), ts, font=get_font(FONT_BOLD, 12), fill=(244, 162, 97, 220))
            draw.text((190, ly), line, font=get_font(FONT_BOLD, 16), fill=(255, 255, 255, 255))
        else:
            draw.text((105, ly + 2), ts, font=get_font(FONT_REGULAR, 12), fill=(110, 115, 125, 200))
            draw.text((190, ly), line, font=get_font(FONT_REGULAR, 15), fill=(150, 155, 165, 200))
        ly += 45

    # Right Card: UX Principles
    draw_glass_card(draw, [650, 175, 1130, 445], radius=20, bg_color=(19, 22, 28, 230), border_color=(255, 255, 255, 25))
    
    ux_items = [
        ("📱 One-Handed Ergonomics", "All primary interactive zones engineered in the bottom 40%"),
        ("⚡ Smart Transport Button", "Single tap restarts song from 0:00; double-tap skips track"),
        ("🎨 Zero Blue Gradients", "Warm Amber, Sunset Coral, Deep Emerald against Obsidian"),
        ("✏️ In-App LRC Timing Editor", "Offset millisecond delays and fine-tune unsynced lyrics instantly"),
    ]
    uy = 195
    for title, desc in ux_items:
        draw.text((680, uy), title, font=get_font(FONT_BOLD, 15), fill=(52, 211, 153, 255))
        draw.text((680, uy + 24), desc, font=get_font(FONT_REGULAR, 12), fill=(170, 175, 185, 255))
        uy += 58

    out_path = os.path.join(OUTPUT_DIR, "banner_lyrics_ui.png")
    img.save(out_path, "PNG")
    print("Saved:", out_path)

def render_connected_auto_banner():
    width, height = 1200, 500
    img = create_base_canvas(width, height)
    draw = ImageDraw.Draw(img)
    
    # Title
    draw_pill(draw, 70, 45, "ECOSYSTEM & INTEGRATIONS", get_font(FONT_BOLD, 12), (25, 30, 40, 240), (52, 211, 153, 255), (52, 211, 153, 100), padding_x=12, padding_y=5, radius=8)
    draw.text((70, 80), "Android Auto & Connected Audiophile Ecosystem", font=get_font(FONT_BOLD, 36), fill=(255, 255, 255, 255))
    draw.text((70, 130), "Safe in-car entertainment, authenticated Last.fm scrobbling, and AES-256-GCM encrypted backups", font=get_font(FONT_REGULAR, 16), fill=(160, 165, 175, 255))
    
    cards = [
        ("🚗 Android Auto Head Units", 
         "MediaLibraryService with reactive browse tree. Seamless in-car transport, steering wheel integration, and lock-screen survival.", 
         "Automotive Ready", (244, 162, 97, 255), 70),
        ("📻 Last.fm Scrobbler", 
         "Authenticated MD5 mobile session handshake. Offline SQLite cache queues tracks and flushes automatically on reconnect.", 
         "Offline Queued", (52, 211, 153, 255), 430),
        ("🔒 AES-256-GCM Backup", 
         "Military-grade encryption for personal playlists, favorites, and API keys. Multi-pass fuzzy metadata reconciliation.", 
         "SAF Native", (224, 122, 95, 255), 790),
    ]
    
    cw = 340
    ch = 250
    for title, desc, pill, color, cx in cards:
        draw_glass_card(draw, [cx, 180, cx + cw, 180 + ch], radius=18, bg_color=(20, 23, 30, 230), border_color=(255, 255, 255, 24))
        draw_pill(draw, cx + 20, 205, pill, get_font(FONT_BOLD, 11), (color[0], color[1], color[2], 30), color, (color[0], color[1], color[2], 80), padding_x=10, padding_y=4, radius=6)
        draw.text((cx + 20, 245), title, font=get_font(FONT_BOLD, 18), fill=(255, 255, 255, 255))
        draw.text((cx + 20, 280), desc, font=get_font(FONT_REGULAR, 13), fill=(170, 175, 185, 255))
        
        # Spec box
        draw_glass_card(draw, [cx + 16, 375, cx + cw - 16, 415], radius=8, bg_color=(12, 14, 18, 200), border_color=(255, 255, 255, 12))
        draw.text((cx + 26, 386), "Full Scoped Storage (API 30+)", font=get_font(FONT_SEMIBOLD, 11), fill=(200, 205, 215, 255))

    out_path = os.path.join(OUTPUT_DIR, "banner_connected_auto.png")
    img.save(out_path, "PNG")
    print("Saved:", out_path)

if __name__ == "__main__":
    render_hero_banner()
    render_formats_banner()
    render_autoeq_banner()
    render_lyrics_ui_banner()
    render_connected_auto_banner()
    print("All 5 graphics successfully generated!")
