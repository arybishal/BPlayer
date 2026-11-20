import subprocess
import time
import os
import shutil
import re
import xml.etree.ElementTree as ET

ADB = r"C:\LDPlayer\LDPlayer9\adb.exe"

BASE_DIR = r"D:\Webapp\BPlayer"
QA_DIR = os.path.join(BASE_DIR, "qa", "branding")
LIGHT_DIR = os.path.join(QA_DIR, "light")
DARK_DIR = os.path.join(QA_DIR, "dark")
BEFORE_DIR = os.path.join(QA_DIR, "before")
AFTER_DIR = os.path.join(QA_DIR, "after")

for d in [LIGHT_DIR, DARK_DIR, BEFORE_DIR, AFTER_DIR]:
    os.makedirs(d, exist_ok=True)

# Copy existing before-fixes screenshots to before directory
src_before = os.path.join(BASE_DIR, "qa", "screenshots", "bplayer-before-fixes")
if os.path.exists(src_before):
    for f in os.listdir(src_before):
        shutil.copy2(os.path.join(src_before, f), os.path.join(BEFORE_DIR, f))
    print(f"Copied before screenshots from {src_before} to {BEFORE_DIR}")

def adb(cmd):
    full = [ADB] + cmd
    res = subprocess.run(full, capture_output=True, text=True)
    return res.stdout.strip()

def tap(x, y, delay=1.0):
    adb(["shell", "input", "tap", str(x), str(y)])
    time.sleep(delay)

def keyevent(code, delay=1.0):
    adb(["shell", "input", "keyevent", str(code)])
    time.sleep(delay)

def swipe(x1, y1, x2, y2, duration=300, delay=1.0):
    adb(["shell", "input", "swipe", str(x1), str(y1), str(x2), str(y2), str(duration)])
    time.sleep(delay)

def screenshot(dest_path):
    adb(["shell", "screencap", "-p", "/sdcard/brand_sc.png"])
    adb(["pull", "/sdcard/brand_sc.png", dest_path])
    print(f"Saved: {dest_path}")

def get_ui_nodes():
    adb(["shell", "uiautomator", "dump", "/sdcard/brand_dump.xml"])
    dump_local = os.path.join(BASE_DIR, "qa", "brand_dump.xml")
    adb(["pull", "/sdcard/brand_dump.xml", dump_local])
    nodes = {}
    if os.path.exists(dump_local):
        try:
            tree = ET.parse(dump_local)
            for node in tree.iter("node"):
                text = node.attrib.get("text", "").strip()
                desc = node.attrib.get("content-desc", "").strip()
                bounds = node.attrib.get("bounds", "")
                label = text or desc
                if label and bounds:
                    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
                    if m:
                        x1, y1, x2, y2 = map(int, m.groups())
                        nodes[label] = ((x1 + x2) // 2, (y1 + y2) // 2)
        except Exception as e:
            print("Dump parse error:", e)
    return nodes

def tap_node_matching(pattern, delay=1.0):
    nodes = get_ui_nodes()
    for label, pt in nodes.items():
        if re.search(pattern, label, re.I):
            print(f"Tapping '{label}' at {pt}")
            tap(pt[0], pt[1], delay)
            return True
    return False

def dismiss_startup_dialog():
    nodes = get_ui_nodes()
    if any("recommendations" in k.lower() or "ok, thanks" in k.lower() for k in nodes.keys()):
        print("Dismissing startup recommendations dialog...")
        tap_node_matching("Don't show again")
        tap_node_matching("OK, thanks")
        time.sleep(1.0)

def run_suite(mode_name, is_night):
    print(f"\n==========================================")
    print(f"STARTING CAPTURE SUITE: {mode_name.upper()} (Night: {is_night})")
    print(f"==========================================")
    
    target_dir = DARK_DIR if is_night else LIGHT_DIR
    adb(["shell", "cmd", "uimode", "night", "yes" if is_night else "no"])
    time.sleep(1.5)

    # 1. Launcher icon
    print("1. Capturing Launcher Icon...")
    keyevent(3, 1.5) # Home
    screenshot(os.path.join(target_dir, "01_launcher_icon.png"))

    # 2. Splash screen
    print("2. Capturing Splash Screen...")
    adb(["shell", "am", "force-stop", "com.pulse.player"])
    time.sleep(0.5)
    adb(["shell", "am", "start", "-n", "com.pulse.player/com.lastwave.app.MainActivity"])
    time.sleep(0.2)
    screenshot(os.path.join(target_dir, "02_splash_screen.png"))
    time.sleep(2.5) # wait for launch
    dismiss_startup_dialog()

    # 3. Home / Feed screen
    print("3. Capturing Home / Feed Screen...")
    tap(150, 1534, 1.2) # Feed tab
    screenshot(os.path.join(target_dir, "03_home_screen.png"))

    # 4. Library Screen
    print("4. Capturing Library Screen...")
    tap(750, 1534, 1.5) # Library tab
    screenshot(os.path.join(target_dir, "05_library_screen.png"))

    # 5. Playlists Screen
    print("5. Capturing Playlists Screen...")
    if not tap_node_matching("Playlists", 1.2):
        tap(520, 150, 1.2) # fallback tab position
    screenshot(os.path.join(target_dir, "06_playlists_screen.png"))

    # Back to Tracks tab in Library
    tap_node_matching("Tracks", 1.0)
    time.sleep(1.0)

    # Start playback by tapping a track
    print("Starting playback...")
    tap(350, 380, 1.5) # tap first track in list

    # 6. Player Screen (Now Playing)
    print("6. Capturing Now Playing Full Player...")
    tap(450, 1400, 1.5) # tap mini player to open full player
    screenshot(os.path.join(target_dir, "04_player_screen.png"))

    # 7. Queue Screen
    print("7. Capturing Queue Screen...")
    if not tap_node_matching("Queue", 1.2):
        tap(780, 1500, 1.2) # tap queue icon
    screenshot(os.path.join(target_dir, "07_queue_screen.png"))
    keyevent(4, 1.0) # dismiss queue sheet

    # 8. Lyrics Screen
    print("8. Capturing Lyrics Screen...")
    swipe(750, 700, 150, 700, 300, 1.5) # swipe to lyrics page in pager
    screenshot(os.path.join(target_dir, "08_lyrics_screen.png"))
    swipe(150, 700, 750, 700, 300, 1.2) # swipe back to player
    keyevent(4, 1.2) # collapse full player

    # 9. Search Screen
    print("9. Capturing Search Screen...")
    keyevent(84, 1.0) # KEYCODE_SEARCH or tap search icon
    if not tap_node_matching("Search", 1.0):
        tap(710, 100, 1.2)
    adb(["shell", "input", "text", "Akash"])
    time.sleep(1.2)
    screenshot(os.path.join(target_dir, "10_search_screen.png"))
    keyevent(4, 0.8) # hide keyboard
    keyevent(4, 1.0) # exit search

    # 10. Settings Screen
    print("10. Capturing Settings Screen...")
    tap(812, 100, 1.5) # tap settings icon in top bar
    screenshot(os.path.join(target_dir, "11_settings_screen.png"))

    # 11. Equalizer Screen
    print("11. Capturing Equalizer Screen...")
    if tap_node_matching("Audio & Playback", 1.5):
        if tap_node_matching("Equalizer", 1.5):
            screenshot(os.path.join(target_dir, "09_equalizer_screen.png"))
            keyevent(4, 1.0)
        keyevent(4, 1.0)
    else:
        # If already on Audio or couldn't find, try scrolling
        swipe(450, 1200, 450, 600, 300, 1.2)
        if tap_node_matching("Audio & Playback", 1.5):
            tap_node_matching("Equalizer", 1.5)
            screenshot(os.path.join(target_dir, "09_equalizer_screen.png"))
            keyevent(4, 1.0)
            keyevent(4, 1.0)

    # 12. About Screen
    print("12. Capturing About & System Screen...")
    swipe(450, 1300, 450, 300, 300, 1.2) # scroll down
    swipe(450, 1300, 450, 300, 300, 1.2)
    tap_node_matching("About & System", 1.5)
    swipe(450, 1200, 450, 500, 300, 1.2) # scroll to AboutCard
    screenshot(os.path.join(target_dir, "12_about_screen.png"))

    # 13. Update Check
    print("13. Capturing Update Check...")
    swipe(450, 400, 450, 1100, 300, 1.0) # scroll back up slightly if needed
    if not tap_node_matching("Check for Updates", 1.5):
        tap_node_matching("Check", 1.5)
    time.sleep(2.5) # allow update checker to fetch
    screenshot(os.path.join(target_dir, "13_update_check.png"))

    # Clean exit back to home
    keyevent(4, 1.0)
    keyevent(4, 1.0)
    print(f"Finished {mode_name} capture suite.")

if __name__ == "__main__":
    # Light mode suite
    run_suite("light", is_night=False)
    # Dark mode suite
    run_suite("dark", is_night=True)

    # Populate after directory with key after comparison screenshots
    for f in ["03_home_screen.png", "04_player_screen.png", "05_library_screen.png", "10_search_screen.png", "11_settings_screen.png", "12_about_screen.png"]:
        src = os.path.join(DARK_DIR, f)
        if os.path.exists(src):
            shutil.copy2(src, os.path.join(AFTER_DIR, f))
    print("\nAll QA branding screenshots successfully captured and organized!")
