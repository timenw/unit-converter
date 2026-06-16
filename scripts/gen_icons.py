#!/usr/bin/env python3
"""
Generate beautiful launcher icons for Unit Converter app.
Features:
- Modern gradient background (deep blue to cyan)
- Conversion arrows symbol (arrows pointing left and right)
- Rounded rectangle shape
- Professional look
"""
import struct, zlib, os, math

def create_icon_pillow(size, path):
    """Create icon using Pillow with a beautiful design."""
    from PIL import Image, ImageDraw, ImageFont
    
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    padding = max(2, size // 24)
    corner_radius = max(4, size // 6)
    bg_box = [padding, padding, size - padding, size - padding]
    
    # Draw gradient background (diagonal: dark blue -> teal -> cyan)
    for y in range(size):
        for x in range(size):
            dx = max(bg_box[0] - x, x - bg_box[2], 0)
            dy = max(bg_box[1] - y, y - bg_box[3], 0)
            if dx * dx + dy * dy > corner_radius * corner_radius:
                continue
            
            ratio = (x + y) / (size * 2)
            if ratio < 0.5:
                sub = ratio * 2
                r = int(10 + (0 - 10) * sub)
                g = int(30 + (140 - 30) * sub)
                b = int(80 + (180 - 80) * sub)
            else:
                sub = (ratio - 0.5) * 2
                r = int(0 + (60 - 0) * sub)
                g = int(140 + (200 - 140) * sub)
                b = int(180 + (240 - 180) * sub)
            
            img.putpixel((x, y), (r, g, b, 255))
    
    # Draw conversion symbol: two arrows pointing opposite directions
    center_x = size // 2
    center_y = size // 2
    
    # White circle background for the symbol
    circle_r = int(size * 0.32)
    for y in range(size):
        for x in range(size):
            dx = x - center_x
            dy = y - center_y
            dist = math.sqrt(dx * dx + dy * dy)
            if dist <= circle_r:
                alpha = 255
                if dist > circle_r - 2:
                    alpha = int(255 * (circle_r - dist) / 2)
                existing = img.getpixel((x, y))
                blend = 0.15
                r = int(existing[0] * (1 - blend) + 255 * blend)
                g = int(existing[1] * (1 - blend) + 255 * blend)
                b = int(existing[2] * (1 - blend) + 255 * blend)
                img.putpixel((x, y), (r, g, b, 255))
    
    # Draw arrows
    arrow_color = (255, 255, 255, 230)
    arrow_width = max(2, size // 16)
    arrow_y = center_y
    arrow_span = int(size * 0.18)
    arrow_x_start = center_x - arrow_span
    arrow_x_end = center_x + arrow_span
    
    # Right-pointing arrow shaft
    for i in range(arrow_width):
        y_off = i - arrow_width // 2
        draw.line(
            [(arrow_x_start, arrow_y + y_off), (arrow_x_end - arrow_width, arrow_y + y_off)],
            fill=arrow_color, width=1
        )
    
    # Right arrowhead
    head_size = arrow_width * 2
    draw.polygon([
        (arrow_x_end, arrow_y),
        (arrow_x_end - head_size, arrow_y - head_size),
        (arrow_x_end - head_size, arrow_y + head_size)
    ], fill=arrow_color)
    
    # Left-pointing arrow shaft
    for i in range(arrow_width):
        y_off = i - arrow_width // 2
        draw.line(
            [(arrow_x_start + arrow_width, arrow_y + y_off), (arrow_x_end, arrow_y + y_off)],
            fill=arrow_color, width=1
        )
    
    # Left arrowhead
    draw.polygon([
        (arrow_x_start, arrow_y),
        (arrow_x_start + head_size, arrow_y - head_size),
        (arrow_x_start + head_size, arrow_y + head_size)
    ], fill=arrow_color)
    
    # Small "UC" text below
    try:
        font_size = max(8, int(size * 0.12))
        font = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', font_size)
    except:
        font = ImageFont.load_default()
    
    text = "UC"
    try:
        bbox = draw.textbbox((0, 0), text, font=font)
        tw = bbox[2] - bbox[0]
        th = bbox[3] - bbox[1]
    except:
        tw, th = font_size, font_size // 2
    
    text_y = center_y + circle_r + max(2, size // 20)
    if text_y + th < size - padding:
        tx = center_x - tw // 2
        draw.text((tx + 1, text_y + 1), text, fill=(0, 0, 0, 80), font=font)
        draw.text((tx, text_y), text, fill=(255, 255, 255, 200), font=font)
    
    # Convert to RGB for launcher icon
    rgb_img = Image.new('RGB', (size, size), (0, 0, 0))
    rgb_img.paste(img, mask=img.split()[3])
    
    os.makedirs(os.path.dirname(path), exist_ok=True)
    rgb_img.save(path)
    print(f'{path} created ({size}x{size})')

def create_icon(size, path):
    try:
        create_icon_pillow(size, path)
    except ImportError:
        print(f'Pillow not available for {path}')

sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

for density, size in sizes.items():
    base = f'app/src/main/res/mipmap-{density}'
    create_icon(size, f'{base}/ic_launcher.png')
    create_icon(size, f'{base}/ic_launcher_round.png')

print('All icons done!')
