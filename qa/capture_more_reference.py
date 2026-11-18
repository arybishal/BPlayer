import subprocess
import time
import os
import xml.etree.ElementTree as ET
import re

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
                        nodes[label] = ((x1 + x2) // 2, (y1 + y2) // 2)
        except Exception as e:
            print(f"Error: {e}")
    return nodes

def find_and_tap(label):
    nodes = get_nodes()
    for k, v in nodes.items():
        if label.lower() in k.lower():
            tap(v[0], v[1])
            return True
    swipe_up()
    nodes = get_nodes()
    for k, v in nodes.items():
        if label.lower() in k.lower():
            tap(v[0], v[1])
            return True
    return False

if __name__ == "__main__":
    # Make sure we start from Home
    run_adb(["shell", "am", "start", "-n", "com.lastwave.app/.MainActivity"])
    time.sleep(2)

    # Tap Back a couple times to exit any settings screen to Home
    key_back()
    key_back()
    time.sleep(1)

    # 1. Capture Search from Home
    # Search icon is at x=676, y=100
    tap(676, 100)
    time.sleep(2)
    screenshot("04_search_screen.png")
    key_back()

    # 2. Capture Recognition from Home
    # Recognition icon is at x=756, y=100
    tap(756, 100)
    time.sleep(2)
    screenshot("05_recognition_screen.png")
    key_back()

    # 3. Open Settings -> Audio -> Equalizer & AutoEQ
    tap(812, 100) # Open settings
    time.sleep(1.5)
    find_and_tap("Audio & Playback")
    time.sleep(1.5)
    # Scroll down in Audio to find Equalizer and AutoEQ
    swipe_up()
    screenshot("08_settings_audio_scrolled.png")
    if find_and_tap("Equalizer"):
        time.sleep(2)
        screenshot("08b_equalizer_sheet.png")
        key_back()
    if find_and_tap("AutoEQ"):
        time.sleep(2)
        screenshot("08c_autoeq_screen.png")
        key_back()
    key_back() # Exit Audio

    # 4. Open Appearance -> Switch to Dark Theme & AMOLED
    find_and_tap("Appearance & Visuals")
    time.sleep(1.5)
    find_and_tap("Dark")
    time.sleep(1.5)
    screenshot("07b_appearance_dark.png")
    find_and_tap("AMOLED Mode")
    time.sleep(1.5)
    screenshot("07c_appearance_amoled.png")
    key_back() # Exit Appearance

    # Exit Settings to Home in Dark Theme
    key_back()
    time.sleep(1.5)
    screenshot("01b_home_feed_dark.png")

    print("Reference screenshots finished!")
