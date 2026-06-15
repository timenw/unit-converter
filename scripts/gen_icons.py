import struct, zlib, os

def create_gradient_png(size, path, r1, g1, b1, r2, g2, b2):
    """创建渐变背景 PNG"""
    sig = b'\x89PNG\r\n\x1a\n'
    
    ihdr_data = struct.pack('>IIBBBBB', size, size, 8, 2, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data) & 0xffffffff
    ihdr = struct.pack('>I', 13) + b'IHDR' + ihdr_data + struct.pack('>I', ihdr_crc)
    
    raw = b''
    for y in range(size):
        raw += b'\x00'  # filter byte
        ratio = y / size
        r = int(r1 + (r2 - r1) * ratio)
        g = int(g1 + (g2 - g1) * ratio)
        b = int(b1 + (b2 - b1) * ratio)
        for x in range(size):
            raw += bytes([r, g, b])
    
    compressed = zlib.compress(raw)
    idat_crc = zlib.crc32(b'IDAT' + compressed) & 0xffffffff
    idat = struct.pack('>I', len(compressed)) + b'IDAT' + compressed + struct.pack('>I', idat_crc)
    
    iend_crc = zlib.crc32(b'IEND') & 0xffffffff
    iend = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', iend_crc)
    
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'wb') as f:
        f.write(sig + ihdr + idat + iend)

def create_icon_with_text(size, path):
    """创建渐变背景 + 居中文字图标"""
    # 蓝色渐变：深蓝 #0D47A1 到 亮蓝 #42A5F5
    create_gradient_png(size, path, 13, 71, 161, 66, 165, 245)
    
    # 尝试用 Pillow 添加文字
    try:
        from PIL import Image, ImageDraw, ImageFont
        
        img = Image.new('RGB', (size, size), '#0D47A1')
        draw = ImageDraw.Draw(img)
        
        # 渐变背景
        for y in range(size):
            ratio = y / size
            r = int(13 + (66 - 13) * ratio)
            g = int(71 + (165 - 71) * ratio)
            b = int(161 + (245 - 161) * ratio)
            for x in range(size):
                img.putpixel((x, y), (r, g, b))
        
        # 画文字 "UC" (Unit Converter)
        try:
            font = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf', int(size * 0.4))
        except:
            font = ImageFont.load_default()
        
        text = "UC"
        bbox = draw.textbbox((0, 0), text, font=font)
        w = bbox[2] - bbox[0]
        h = bbox[3] - bbox[1]
        x = (size - w) // 2
        y = (size - h) // 2 - int(size * 0.05)
        
        # 白色文字带阴影
        draw.text((x+2, y+2), text, fill='#00000055', font=font)
        draw.text((x, y), text, fill='white', font=font)
        
        img.save(path)
        print(f'{path} created ({size}x{size})')
    except ImportError:
        print(f'{path} created without text (Pillow not available)')

sizes = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192
}

for density, size in sizes.items():
    base = f'/root/unit-converter/app/src/main/res/mipmap-{density}'
    create_icon_with_text(size, f'{base}/ic_launcher.png')
    create_icon_with_text(size, f'{base}/ic_launcher_round.png')

print('All icons done')
