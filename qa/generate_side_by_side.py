import os
from PIL import Image, ImageDraw, ImageFont, ImageChops, ImageStat

def create_comparison(lw_path, bp_path, out_side_by_side, out_diff):
    if not os.path.exists(lw_path) or not os.path.exists(bp_path):
        print(f"Missing file: {lw_path} or {bp_path}")
        return None

    lw = Image.open(lw_path).convert('RGB')
    bp = Image.open(bp_path).convert('RGB')

    # Ensure same size
    if lw.size != bp.size:
        bp = bp.resize(lw.size, Image.Resampling.LANCZOS)

    w, h = lw.size
    banner_h = 70
    canvas_w = w * 2 + 20
    canvas_h = h + banner_h

    # Create side-by-side canvas
    canvas = Image.new('RGB', (canvas_w, canvas_h), (18, 18, 22))
    draw = ImageDraw.Draw(canvas)

    # Paste images
    canvas.paste(lw, (0, banner_h))
    canvas.paste(bp, (w + 20, banner_h))

    # Draw header banners
    draw.rectangle([(0, 0), (w, banner_h)], fill=(28, 30, 36))
    draw.rectangle([(w + 20, 0), (canvas_w, banner_h)], fill=(28, 30, 36))

    # Try default font or basic font
    try:
        font = ImageFont.truetype("arial.ttf", 26)
    except Exception:
        font = ImageFont.load_default()

    draw.text((20, 20), "LastWave-Native (Reference)", fill=(200, 255, 100), font=font)
    draw.text((w + 40, 20), "BPlayer (Current)", fill=(255, 200, 100), font=font)

    # Save side-by-side
    canvas.save(out_side_by_side, "PNG")

    # Compute difference image
    diff = ImageChops.difference(lw, bp)
    # Amplify differences so they are clearly visible
    diff_amplified = diff.point(lambda p: min(255, p * 3))
    diff_canvas = Image.new('RGB', (w, h + banner_h), (18, 18, 22))
    diff_draw = ImageDraw.Draw(diff_canvas)
    diff_draw.rectangle([(0, 0), (w, banner_h)], fill=(36, 20, 24))
    diff_draw.text((20, 20), "Visual Difference Map (Amplified x3)", fill=(255, 120, 120), font=font)
    diff_canvas.paste(diff_amplified, (0, banner_h))
    diff_canvas.save(out_diff, "PNG")

    stat = ImageStat.Stat(diff)
    print(f"Generated {out_side_by_side} and {out_diff}. Mean diff: {stat.mean}")
    return stat.mean

if __name__ == '__main__':
    create_comparison(
        'qa/side-by-side/home/lastwave.png',
        'qa/glass-design/dark/01_home_feed.png',
        'qa/side-by-side/home/comparison.png',
        'qa/side-by-side/home/difference.png'
    )
