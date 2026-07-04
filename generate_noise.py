import os
from PIL import Image
import random

def generate_noise(filename, width=256, height=256, intensity=30):
    img = Image.new('RGBA', (width, height), (0,0,0,0))
    pixels = img.load()
    
    for y in range(height):
        for x in range(width):
            # Create a greyscale noise pixel with low opacity
            val = random.randint(0, 255)
            # Alpha is random up to intensity
            alpha = random.randint(0, intensity)
            pixels[x, y] = (val, val, val, alpha)
            
    img.save(filename)
    print(f"Saved {filename}")

if __name__ == '__main__':
    # Ensure drawable dir exists
    drawable_dir = "app/src/main/res/drawable"
    os.makedirs(drawable_dir, exist_ok=True)
    
    generate_noise(os.path.join(drawable_dir, "noise_texture.png"))
