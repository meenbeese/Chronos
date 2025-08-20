import os
from PIL import Image
import shutil

source_dir = r"C:\Users\kuzey\GitHub\Chronos\assets\images"
dest_dir = r"C:\Users\kuzey\GitHub\Chronos\fastlane\metadata\android\en-US\images\phoneScreenshots"
temp_dir = r"C:\Users\kuzey\GitHub\Chronos\temp_resized"

if not os.path.exists(temp_dir):
    os.makedirs(temp_dir)

images = sorted([f for f in os.listdir(source_dir) if f.lower().endswith(".png")])

for idx, img_name in enumerate(images, start=1):
    img_path = os.path.join(source_dir, img_name)
    with Image.open(img_path) as img:
        new_size = (img.width // 3, img.height // 3)
        resized = img.resize(new_size, Image.Resampling.LANCZOS)
        
        new_name = f"{idx:02}.png"
        resized.save(
            os.path.join(temp_dir, new_name),
            optimize=True,
            compress_level=9
        )

if os.path.exists(dest_dir):
    shutil.rmtree(dest_dir)
os.makedirs(dest_dir)

for f in sorted(os.listdir(temp_dir)):
    shutil.move(os.path.join(temp_dir, f), os.path.join(dest_dir, f))

shutil.rmtree(temp_dir)

print("All images processed, resized, and copied successfully!")
