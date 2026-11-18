import subprocess
import time
import re
import os
import xml.etree.ElementTree as ET

ADB = r"C:\LDPlayer\LDPlayer9\adb.exe"
OUT_DIR = "qa/screenshots/bplayer-after-fixes"

def run_adb(cmd):
    full_cmd = [ADB] + cmd
    res = subprocess.run(full_cmd, capture_output=True, text=True)
    return res.stdout

def tap(x, y):
    run_adb(["shell", "input", "tap", str(x), str(y)])
    time.sleep(1.2)

def keyevent(key):
    run_adb(["shell", "input", "keyevent", key])
    time.sleep(1.2)

def swipe_up():
    run_adb(["shell", "input", "swipe", "450", "1200", "450", "400", "300"])
    time.sleep(1.2)

def swipe_down():
    run_adb(["shell", "input", "swipe", "450", "400", "450", "1200", "300"])
    time.sleep(1.2)

def screenshot(filename):
    os.makedirs(OUT_DIR, exist_ok=True)
    dest_path = os.path.join(OUT_DIR, filename)
    run_adb(["shell", "screencap", "-p", "/sdcard/sc.png"])
    run_adb(["pull", "/sdcard/sc.png", dest_path])
    print(f"Captured: {dest_path}")

def get_nodes():
    run_adb(["shell", "uiautomator", "dump", "/sdcard/window_dump.xml"])
    run_adb(["pull", "/sdcard/window_dump.xml", "dump.xml"])
    nodes = {}
    if os.path.exists("dump.xml"):
        try:
            tree = ET.parse("dump.xml")
            for node in tree.iter("node"):
                text = node.attrib.get("text", "").strip()
                desc = node.attrib.get("content-desc", "").strip()
                bounds = node.attrib.get("bounds", "")
                label = text or desc
                if label and bounds:
                    m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
                    if m:
                        x1, y1, x2, y2 = map(int, m.groups())
                        cx, cy = (x1 + x2) // 2, (y1 + y2) // 2
                        nodes[label] = (cx, cy)
        except Exception as e:
            print(f"Error parsing dump: {e}")
    return nodes

def tap_node(pattern):
    nodes = get_nodes()
    for k, v in nodes.items():
        if re.search(pattern, k, re.I):
            print(f"Tapping '{k}' at {v}")
            tap(v[0], v[1])
            return True
    return False

def open_subpage(pattern, filename, do_scroll=False, extra_action=None):
    print(f"\n--- Opening subpage: {pattern} -> {filename} ---")
    nodes = get_nodes()
    target = None
    for k, v in nodes.items():
        if re.search(pattern, k, re.I):
            target = v
            break
    if not target:
        swipe_up()
        nodes = get_nodes()
        for k, v in nodes.items():
            if re.search(pattern, k, re.I):
                target = v
                break
    if not target:
        swipe_down()
        swipe_down()
        nodes = get_nodes()
        for k, v in nodes.items():
            if re.search(pattern, k, re.I):
                target = v
                break

    if target:
        tap(target[0], target[1])
        time.sleep(1.5)
        screenshot(filename)
        if extra_action:
            extra_action()
        if do_scroll:
            swipe_up()
            time.sleep(1)
            scroll_filename = filename.replace(".png", "_scrolled.png")
            screenshot(scroll_filename)
            swipe_down()
        keyevent("KEYCODE_BACK")
        time.sleep(1.2)
        return True
    else:
        print(f"Failed to find node for: {pattern}")
        return False

def audio_extra():
    # In audio settings, capture Equalizer and AutoEQ if present
    print("Checking Audio sub-screens (EQ, AutoEQ)...")
    nodes = get_nodes()
    for k, v in nodes.items():
        if "equalizer" in k.lower():
            print(f"Opening Equalizer at {v}...")
            tap(v[0], v[1])
            time.sleep(1.5)
            screenshot("08b_equalizer.png")
            keyevent("KEYCODE_BACK")
            time.sleep(1)
            break
    nodes = get_nodes()
    for k, v in nodes.items():
        if "autoeq" in k.lower():
            print(f"Opening AutoEQ at {v}...")
            tap(v[0], v[1])
            time.sleep(1.5)
            screenshot("08c_autoeq.png")
            keyevent("KEYCODE_BACK")
            time.sleep(1)
            break

def main():
    os.makedirs(OUT_DIR, exist_ok=True)

    # We are currently in Settings!
    print("Capturing 06_settings.png / 06_settings_main.png / 06_settings_root.png...")
    screenshot("06_settings.png")
    screenshot("06_settings_main.png")
    screenshot("06_settings_root.png")

    swipe_up()
    screenshot("06_settings_bottom.png")
    swipe_down()

    # Capture Settings subsections
    open_subpage("BPlayer Jam|Jam", "15_settings_jam.png")
    open_subpage("Provider Modules", "16_settings_modules.png")
    open_subpage("Last.fm", "13_settings_lastfm.png")
    open_subpage("YouTube", "14_settings_youtube.png")
    open_subpage("Spotify", "12_settings_spotify.png")
    open_subpage("Appearance & Visuals", "07_settings_appearance.png")
    open_subpage("Audio & Playback", "08_settings_audio.png", do_scroll=True, extra_action=audio_extra)
    open_subpage("Library & Content", "09_settings_library.png")
    open_subpage("Data & Storage", "10_settings_storage.png")
    open_subpage("About & System", "11_settings_about.png")

    # Now go back from Settings to Home Shell
    print("\nReturning to Home shell...")
    keyevent("KEYCODE_BACK")
    time.sleep(1.5)

    # 1. Home / Feed
    print("\nCapturing Home / Feed...")
    tap(150, 1570) # Feed tab
    time.sleep(1.5)
    screenshot("01_home.png")
    screenshot("01_home_feed.png")
    screenshot("01b_home_feed_dark.png")

    # 2. Stats
    print("\nCapturing Stats...")
    tap(450, 1570) # Stats tab
    time.sleep(1.5)
    screenshot("02_stats.png")

    # 3. Library
    print("\nCapturing Library...")
    tap(750, 1570) # Library tab
    time.sleep(1.5)
    screenshot("03_library.png")

    # 4. Search
    print("\nCapturing Search...")
    tap(150, 1570) # Back to Feed
    time.sleep(1)
    tap_node("Search")
    time.sleep(1.5)
    screenshot("04_search.png")
    screenshot("04_search_screen.png")
    keyevent("KEYCODE_BACK")
    time.sleep(1)

    # 5. Recognition
    print("\nCapturing Recognition...")
    tap_node("Identify music")
    time.sleep(1.5)
    screenshot("05_recognition.png")
    screenshot("05_recognition_screen.png")
    keyevent("KEYCODE_BACK")
    time.sleep(1)

    # 6. Now Playing, Signal Path, Lyrics
    print("\nOpening Now Playing mini player...")
    # Tap the mini player bar at [400, 1370]
    tap(400, 1370)
    time.sleep(2)
    screenshot("17_nowplaying.png")

    # Signal Path
    print("Opening Signal Path...")
    if tap_node("Signal Path|Path|Audio track"):
        time.sleep(1.5)
        screenshot("18_signal_path.png")
        keyevent("KEYCODE_BACK")
        time.sleep(1)
    else:
        # Try tapping audio quality badge or options menu
        tap(100, 1400)
        time.sleep(1)
        screenshot("18_signal_path.png")
        keyevent("KEYCODE_BACK")

    # Lyrics
    print("Opening Lyrics...")
    if tap_node("Lyrics|Lyric"):
        time.sleep(1.5)
        screenshot("19_lyrics.png")
        keyevent("KEYCODE_BACK")
    else:
        # Swipe left or tap lyrics icon
        swipe_up()
        time.sleep(1)
        screenshot("19_lyrics.png")
        keyevent("KEYCODE_BACK")

    print("\nAll BPlayer after-fixes screenshots captured successfully!")

if __name__ == "__main__":
    main()
