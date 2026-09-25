#!/usr/bin/env python3
"""Keep RuntimeInstaller.AGY_BUNDLE in sync with the Antigravity bundle built in CI.

zstd output can differ between versions, so the freshly built archive may not be
byte-identical to the one the source pins. Patch the pinned SHA-256 / size to the
archive that is actually embedded in this APK so the installer's integrity check passes.
"""
import hashlib
import pathlib
import re
import sys

archive = pathlib.Path("dist/runtime-bundles/pocketdev-agy-arm64-2026.09.1.tar.zst")
installer = pathlib.Path("app/src/main/java/com/jarves/mh/runtime/RuntimeInstaller.kt")

if not archive.is_file():
    sys.exit(f"missing {archive}")

data = archive.read_bytes()
sha = hashlib.sha256(data).hexdigest()
size = len(data)
size_literal = f"{size:,}".replace(",", "_") + "L"

source = installer.read_text()
block = re.search(r"private val AGY_BUNDLE = RuntimeBundle\((.*?)\n        \)", source, re.S)
if not block:
    sys.exit("AGY_BUNDLE block not found in RuntimeInstaller.kt")

old = block.group(0)
new = re.sub(r'sha256 = "[0-9a-f]{64}"', f'sha256 = "{sha}"', old)
new = re.sub(r"compressedBytes = [0-9_]+L", f"compressedBytes = {size_literal}", new)

print(f"Antigravity bundle sha256={sha} bytes={size}")
if new == old:
    print("Pinned checksum already matches.")
else:
    installer.write_text(source.replace(old, new))
    print("Patched AGY_BUNDLE checksum in RuntimeInstaller.kt")
