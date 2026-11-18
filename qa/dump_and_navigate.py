import subprocess
import time
import re
import os
import xml.etree.ElementTree as ET

ADB = r"C:\LDPlayer\LDPlayer9\adb.exe"

def run_adb(cmd):
    return subprocess.run([ADB] + cmd, capture_output=True, text=True).stdout

def tap(x, y):
    run_adb(["shell", "input", "tap", str(x), str(y)])
    time.sleep(1.5)

def key_back():
    run_adb(["shell", "input", "keyevent", "KEYCODE_BACK"])
    time.sleep(1.5)

def screenshot(name):
    path = os.path.join("qa/screenshots/lastwave-reference", name)
    run_adb(["shell", "screencap", "-p", "/sdcard/sc.png"])
    run_adb(["pull", "/sdcard/sc.png", path])
    print(f"Captured: {path}")

def swipe_up():
    run_adb(["shell", "input", "swipe", "450", "1200", "450", "400", "300"])
    time.sleep(1)

def swipe_down():
    run_adb(["shell", "input", "swipe", "450", "400", "450", "1200", "300"])
    time.sleep(1)

def get_nodes():
    run_adb(["shell", "uiautomator", "dump"])
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
            print(f"Error: {e}")
    return nodes

def capture_category(label, filename):
    nodes = get_nodes()
    # Try finding label
    target = None
    for k, v in nodes.items():
        if label.lower() in k.lower():
            target = v
            break
    if not target:
        # Swipe up and look again
        swipe_up()
        nodes = get_nodes()
        for k, v in nodes.items():
            if label.lower() in k.lower():
                target = v
                break
    if not target:
        # Swipe down and look again
        swipe_down()
        swipe_down()
        nodes = get_nodes()
        for k, v in nodes.items():
            if label.lower() in k.lower():
                target = v
                break
    if target:
        print(f"Opening {label} at {target}...")
        tap(target[0], target[1])
        screenshot(filename)
        # Check if this is Audio & Playback, if so also capture Equalizer & AutoEQ
        if "audio" in label.lower():
            subnodes = get_nodes()
            for sk, sv in subnodes.items():
                if "equalizer" in sk.lower():
                    print(f"Opening Equalizer at {sv}...")
                    tap(sv[0], sv[1])
                    screenshot("08b_equalizer_sheet.png")
                    tap(88, 100) # or back
                elif "autoeq" in sk.lower():
                    print(f"Opening AutoEQ at {sv}...")
                    tap(sv[0], sv[1])
                    screenshot("08c_autoeq_screen.png")
                    tap(88, 100)
        # Go back
        tap(88, 100)
    else:
        print(f"Could NOT find node for {label}")

if __name__ == "__main__":
    os.makedirs("qa/screenshots/lastwave-reference", exist_ok=True)
    # Tap back from Last.fm to Settings
    tap(88, 100)

    # Now we are in Settings
    screenshot("06_settings_root.png")

    targets = [
        ("LastWave Jam", "15_settings_jam.png"),
        ("Provider Modules", "16_settings_modules.png"),
        ("Last.fm", "13_settings_lastfm.png"),
        ("YouTube & Sync", "14_settings_youtube.png"),
        ("Spotify", "12_settings_spotify.png"),
        ("Appearance & Visuals", "07_settings_appearance.png"),
        ("Audio & Playback", "08_settings_audio.png"),
        ("Library & Content", "09_settings_library.png"),
        ("Data & Storage", "10_settings_storage.png"),
        ("About & System", "11_settings_about.png"),
    ]

    for label, filename in targets:
        capture_category(label, filename)

    print("All categories captured successfully!")
