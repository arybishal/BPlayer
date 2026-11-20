import os
import re
import glob

def clean_strings():
    res_dirs = glob.glob("app/src/main/res/values-*/strings.xml")
    
    total_replaced = 0
    modified_files = []
    
    for filepath in res_dirs:
        with open(filepath, "r", encoding="utf-8") as f:
            content = f.read()
            
        original = content
        
        # Replace LastWave with BPlayer inside string values
        # Match <string name="...">...LastWave...</string>
        def repl(match):
            key = match.group(1)
            inner = match.group(2)
            # Replace LastWave with BPlayer in inner text
            new_inner = inner.replace("LastWave", "BPlayer")
            new_inner = new_inner.replace("lastwave", "bplayer")
            return f'<string name="{key}">{new_inner}</string>'

        new_content = re.sub(r'<string name="([^"]+)">([^<]*LastWave[^<]*)</string>', repl, content, flags=re.IGNORECASE)
        
        # Also clean any specific desktop app strings
        new_content = re.sub(r'\s*<string name="settings_desktop_app">[^<]*</string>', '', new_content)
        new_content = re.sub(r'\s*<string name="app_icon_desktop">[^<]*</string>', '', new_content)
        new_content = re.sub(r'\s*<string name="app_icon_desktop_sub">[^<]*</string>', '', new_content)
        
        if new_content != original:
            with open(filepath, "w", encoding="utf-8") as f:
                f.write(new_content)
            modified_files.append(filepath)
            print(f"Cleaned {filepath}")

    print(f"Total localized files modified: {len(modified_files)}")

if __name__ == "__main__":
    clean_strings()
