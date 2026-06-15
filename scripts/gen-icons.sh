#!/bin/bash
# 生成简单蓝色 PNG 图标
# 使用纯 bash + xxd

create_icon() {
    local size=$1
    local path=$2
    
    # 用 python3 生成（内联脚本）
    python3 -c "
import struct, zlib, sys

size = $size
path = '$path'

sig = b'\x89PNG\r\n\x1a\n'

ihdr_data = struct.pack('>IIBBBBB', size, size, 8, 2, 0, 0, 0)
ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data) & 0xffffffff
ihdr = struct.pack('>I', 13) + b'IHDR' + ihdr_data + struct.pack('>I', ihdr_crc)

raw = b''
for y in range(size):
    raw += b'\x00'
    for x in range(size):
        raw += b'\x19\x76\xd2'

compressed = zlib.compress(raw)
idat_crc = zlib.crc32(b'IDAT' + compressed) & 0xffffffff
idat = struct.pack('>I', len(compressed)) + b'IDAT' + compressed + struct.pack('>I', idat_crc)

iend_crc = zlib.crc32(b'IEND') & 0xffffffff
iend = struct.pack('>I', 0) + b'IEND' + struct.pack('>I', iend_crc)

with open(path, 'wb') as f:
    f.write(sig + ihdr + idat + iend)
print(f'Created {path}')
"
}

for density_info in "mdpi:48" "hdpi:72" "xhdpi:96" "xxhdpi:144" "xxxhdpi:192"; do
    density=$(echo $density_info | cut -d: -f1)
    size=$(echo $density_info | cut -d: -f2)
    base="/root/unit-converter/app/src/main/res/mipmap-$density"
    mkdir -p "$base"
    create_icon $size "$base/ic_launcher.png"
    create_icon $size "$base/ic_launcher_round.png"
done

echo "All icons created"
