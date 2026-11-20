import os
from render_bplayer_png import render_bplayer_png

def export_mipmap_icons():
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192,
    }
    
    base_res = "app/src/main/res"
    
    for folder, size in densities.items():
        dir_path = os.path.join(base_res, folder)
        os.makedirs(dir_path, exist_ok=True)
        
        # 1. Default BPlayer Launcher Icon (Dark Obsidian background with White & Emerald glyph)
        # On light launchers, this provides crisp contrast; on dark launchers it seamlessly integrates.
        # Adaptive XML in mipmap-anydpi-v26 handles full day/night vector adaptivity on API 26+!
        default_icon = render_bplayer_png(is_dark=True, size=size, background_color=(14, 16, 19, 255), is_round=False)
        default_icon.save(os.path.join(dir_path, "ic_launcher.png"))
        
        default_round = render_bplayer_png(is_dark=True, size=size, background_color=(14, 16, 19, 255), is_round=True)
        default_round.save(os.path.join(dir_path, "ic_launcher_round.png"))
        
        # 2. White Edition (Pure White background with Deep Charcoal & Emerald glyph)
        white_icon = render_bplayer_png(is_dark=False, size=size, background_color=(255, 255, 255, 255), is_round=False)
        white_icon.save(os.path.join(dir_path, "ic_launcher_white.png"))
        
        white_round = render_bplayer_png(is_dark=False, size=size, background_color=(255, 255, 255, 255), is_round=True)
        white_round.save(os.path.join(dir_path, "ic_launcher_round_white.png"))
        
        # 3. Black Edition (Pitch AMOLED Black background with White & Vibrant Emerald glyph)
        black_icon = render_bplayer_png(is_dark=True, size=size, background_color=(0, 0, 0, 255), is_round=False)
        black_icon.save(os.path.join(dir_path, "ic_launcher_black.png"))
        
        black_round = render_bplayer_png(is_dark=True, size=size, background_color=(0, 0, 0, 255), is_round=True)
        black_round.save(os.path.join(dir_path, "ic_launcher_round_black.png"))
        
        # Clean up legacy desktop pngs if present
        for old in ["ic_launcher_desktop.png", "ic_launcher_round_desktop.png"]:
            p = os.path.join(dir_path, old)
            if os.path.exists(p):
                os.remove(p)
                print(f"Removed unused desktop asset: {p}")
                
        print(f"Exported all icon variants for {folder} ({size}x{size})")

if __name__ == "__main__":
    export_mipmap_icons()
