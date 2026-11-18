import subprocess
import time
import re
import os
import xml.etree.ElementTree as ET

ADB = r"C:\LDPlayer\LDPlayer9\adb.exe"

def run_adb(cmd):
    full_cmd = [ADB] + cmd
    res = subprocess.run(full_cmd, capture_output=True, text=True)
    return res.stdout

def tap(x, y):
    run_adb(["shell", "input", "tap", str(x), str(y)])
    time.sleep(1)

def keyevent(key):
    run_adb(["shell", "input", "keyevent", key])
    time.sleep(1)

def dump_and_find(text_pattern):
    run_adb(["shell", "uiautomator", "dump", "/sdcard/window_dump.xml"])
    xml_data = run_adb(["shell", "cat", "/sdcard/window_dump.xml"])
    try:
        root = ET.fromstring(xml_data)
        for node in root.iter("node"):
            text = node.attrib.get("text", "")
            desc = node.attrib.get("content-desc", "")
            if re.search(text_pattern, text, re.I) or re.search(text_pattern, desc, re.I):
                bounds = node.attrib.get("bounds", "")
                m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", bounds)
                if m:
                    x1, y1, x2, y2 = map(int, m.groups())
                    return (x1 + x2) // 2, (y1 + y2) // 2
    except Exception as e:
        print(f"Error parsing dump: {e}")
    return None

def tap_text(text_pattern):
    coords = dump_and_find(text_pattern)
    if coords:
        tap(coords[0], coords[1])
        return True
    return False

def screenshot(dest_path):
    run_adb(["shell", "screencap", "-p", "/sdcard/sc.png"])
    run_adb(["pull", "/sdcard/sc.png", dest_path])
    print(f"Saved: {dest_path}")

def swipe_up():
    run_adb(["shell", "input", "swipe", "450", "1200", "450", "400", "300"])
    time.sleep(1)

def swipe_down():
    run_adb(["shell", "input", "swipe", "450", "400", "450", "1200", "300"])
    time.sleep(1)

def open_settings_subpage(title_pattern, filename):
    print(f"Navigating to {title_pattern}...")
    # First bring settings to view
    found = tap_text(title_pattern)
    if not found:
        swipe_up()
        found = tap_text(title_pattern)
    if not found:
        swipe_down()
        found = tap_text(title_pattern)
    if found:
        time.sleep(1.5)
        screenshot(os.path.join("qa/screenshots/lastwave-reference", filename))
        keyevent("KEYCODE_BACK")
        time.sleep(1)
    else:
        print(f"Could not find: {title_pattern}")

def run():
    out_dir = "qa/screenshots/lastwave-reference"
    os.makedirs(out_dir, exist_ok=True)

    # 1. Start MainActivity
    run_adb(["shell", "am", "start", "-n", "com.lastwave.app/.MainActivity"])
    time.sleep(2)

    # Make sure we are at Settings
    # Look for Back button in Settings or open Settings from Home
    if not dump_and_find("^Settings$"):
        # Open settings by tapping top right profile icon
        tap(812, 100)
        time.sleep(1.5)

    # Save Settings root if not already
    screenshot(os.path.join(out_dir, "06_settings_main.png"))

    # Settings sub-screens
    open_settings_subpage("Appearance", "07_settings_appearance.png")
    open_settings_subpage("Audio & Playback", "08_settings_audio.png")
    open_settings_subpage("Library & Content", "09_settings_library.png")
    open_settings_subpage("Data & Storage", "10_settings_storage.png")
    open_settings_subpage("About & System", "11_settings_about.png")
    open_settings_subpage("Last\\.fm", "13_settings_lastfm.png")
    open_settings_subpage("YouTube & Sync", "14_settings_youtube.png")
    open_settings_subpage("LastWave Jam", "15_settings_jam.png")
    open_settings_subpage("Provider Modules", "16_settings_modules.png")

    # Let's go into Audio & Playback to capture Equalizer
    if tap_text("Audio & Playback"):
        time.sleep(1.5)
        # Find Equalizer row
        if tap_text("Equalizer"):
            time.sleep(1.5)
            screenshot(os.path.join(out_dir, "08b_settings_equalizer.png"))
            keyevent("KEYCODE_BACK")
        # Find AutoEQ row
        if tap_text("AutoEQ"):
            time.sleep(1.5)
            screenshot(os.path.join(out_dir, "08c_settings_autoeq.png"))
            keyevent("KEYCODE_BACK")
        keyevent("KEYCODE_BACK")
        time.sleep(1)

    # Go back to main shell
    keyevent("KEYCODE_BACK")
    time.sleep(1)

    # Tap Search icon (center ~676, 100)
    tap(676, 100)
    time.sleep(1.5)
    screenshot(os.path.join(out_dir, "04_search.png"))
    keyevent("KEYCODE_BACK")
    time.sleep(1)

    # Tap Recognition icon (center ~756, 100)
    tap(756, 100)
    time.sleep(1.5)
    screenshot(os.path.join(out_dir, "05_recognition.png"))
    keyevent("KEYCODE_BACK")
    time.sleep(1)

    print("Reference screen capture completed!")

if __name__ == "__main__":
    run()
