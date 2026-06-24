#!/usr/bin/env bash
# Align Rose business domains to convention B (see rose-bom/README.md).
# One-shot migration helper; prefer manual moves for incremental PRs.
# Supersedes scripts/migrate-package-layout.sh (removed — layer-first without .core.).
#
#   {domain}.core.*                 (core module)
#   {domain}.boot.autoconfigure.*   (spring-boot autoconfiguration)
#   {domain}.boot.registration.*    (devservice spring-boot registration)
#   {domain}.spring.*               (spring integration module)
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "=== multitenancy: remove duplicate boot classes ==="
rm -f rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/core/autoconfigure/*.java
rm -f rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/observability/*.java
rmdir rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/core/autoconfigure 2>/dev/null || true
rmdir rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/core 2>/dev/null || true
rmdir rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/observability 2>/dev/null || true

echo "=== multitenancy: move MultitenancyCoreAutoConfiguration to boot.autoconfigure ==="
git mv rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/web/MultitenancyCoreAutoConfiguration.java \
  rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/MultitenancyCoreAutoConfiguration.java 2>/dev/null || \
mv rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/web/MultitenancyCoreAutoConfiguration.java \
  rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/MultitenancyCoreAutoConfiguration.java

git mv rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/web/FixedTenantResolutionProperties.java \
  rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/FixedTenantResolutionProperties.java 2>/dev/null || \
mv rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/web/FixedTenantResolutionProperties.java \
  rose-multitenancy/rose-multitenancy-spring-boot/src/main/java/io/zhijun/multitenancy/boot/autoconfigure/FixedTenantResolutionProperties.java

echo "=== devservice: move core packages under core.* ==="
DS_CORE="rose-devservice/rose-devservice-core/src"
for sub in api bootstrap container docker util; do
  if [[ -d "$DS_CORE/main/java/io/zhijun/devservice/$sub" ]]; then
    mkdir -p "$DS_CORE/main/java/io/zhijun/devservice/core"
    git mv "$DS_CORE/main/java/io/zhijun/devservice/$sub" "$DS_CORE/main/java/io/zhijun/devservice/core/$sub" 2>/dev/null || \
      mv "$DS_CORE/main/java/io/zhijun/devservice/$sub" "$DS_CORE/main/java/io/zhijun/devservice/core/$sub"
  fi
  if [[ -d "$DS_CORE/test/java/io/zhijun/devservice/$sub" ]]; then
    mkdir -p "$DS_CORE/test/java/io/zhijun/devservice/core"
    git mv "$DS_CORE/test/java/io/zhijun/devservice/$sub" "$DS_CORE/test/java/io/zhijun/devservice/core/$sub" 2>/dev/null || \
      mv "$DS_CORE/test/java/io/zhijun/devservice/$sub" "$DS_CORE/test/java/io/zhijun/devservice/core/$sub"
  fi
done

echo "=== devservice: boot.autoconfigure + boot.registration ==="
DS_BOOT="rose-devservice/rose-devservice-spring-boot/src"
if [[ -d "$DS_BOOT/main/java/io/zhijun/devservice/autoconfigure" ]]; then
  mkdir -p "$DS_BOOT/main/java/io/zhijun/devservice/boot"
  git mv "$DS_BOOT/main/java/io/zhijun/devservice/autoconfigure" "$DS_BOOT/main/java/io/zhijun/devservice/boot/autoconfigure" 2>/dev/null || \
    mv "$DS_BOOT/main/java/io/zhijun/devservice/autoconfigure" "$DS_BOOT/main/java/io/zhijun/devservice/boot/autoconfigure"
fi
if [[ -d "$DS_BOOT/main/java/io/zhijun/devservice/registration" ]]; then
  mkdir -p "$DS_BOOT/main/java/io/zhijun/devservice/boot"
  git mv "$DS_BOOT/main/java/io/zhijun/devservice/registration" "$DS_BOOT/main/java/io/zhijun/devservice/boot/registration" 2>/dev/null || \
    mv "$DS_BOOT/main/java/io/zhijun/devservice/registration" "$DS_BOOT/main/java/io/zhijun/devservice/boot/registration"
fi
if [[ -d "$DS_BOOT/test/java/io/zhijun/devservice/autoconfigure" ]]; then
  mkdir -p "$DS_BOOT/test/java/io/zhijun/devservice/boot"
  git mv "$DS_BOOT/test/java/io/zhijun/devservice/autoconfigure" "$DS_BOOT/test/java/io/zhijun/devservice/boot/autoconfigure" 2>/dev/null || \
    mv "$DS_BOOT/test/java/io/zhijun/devservice/autoconfigure" "$DS_BOOT/test/java/io/zhijun/devservice/boot/autoconfigure"
fi
if [[ -d "$DS_BOOT/test/java/io/zhijun/devservice/registration" ]]; then
  mkdir -p "$DS_BOOT/test/java/io/zhijun/devservice/boot"
  git mv "$DS_BOOT/test/java/io/zhijun/devservice/registration" "$DS_BOOT/test/java/io/zhijun/devservice/boot/registration" 2>/dev/null || \
    mv "$DS_BOOT/test/java/io/zhijun/devservice/registration" "$DS_BOOT/test/java/io/zhijun/devservice/boot/registration"
fi

echo "=== devservice: connector autoconfigure -> boot.autoconfigure ==="
for module in rose-devservice/rose-devservice-spring-boot-*; do
  [[ -d "$module" ]] || continue
  for kind in main test; do
    src="$module/src/$kind/java/io/zhijun/devservice/autoconfigure"
    if [[ -d "$src" ]]; then
      mkdir -p "$module/src/$kind/java/io/zhijun/devservice/boot"
      git mv "$src" "$module/src/$kind/java/io/zhijun/devservice/boot/autoconfigure" 2>/dev/null || \
        mv "$src" "$module/src/$kind/java/io/zhijun/devservice/boot/autoconfigure"
    fi
  done
done

echo "=== devservice actuator autoconfigure ==="
DS_ACT="rose-devservice/rose-devservice-spring-boot-actuator/src"
if [[ -d "$DS_ACT/main/java/io/zhijun/devservice/autoconfigure/actuator" ]]; then
  mkdir -p "$DS_ACT/main/java/io/zhijun/devservice/boot"
  git mv "$DS_ACT/main/java/io/zhijun/devservice/autoconfigure/actuator" "$DS_ACT/main/java/io/zhijun/devservice/boot/autoconfigure/actuator" 2>/dev/null || \
    mv "$DS_ACT/main/java/io/zhijun/devservice/autoconfigure/actuator" "$DS_ACT/main/java/io/zhijun/devservice/boot/autoconfigure/actuator"
fi
if [[ -d "$DS_ACT/test/java/io/zhijun/devservice/autoconfigure/actuator" ]]; then
  mkdir -p "$DS_ACT/test/java/io/zhijun/devservice/boot"
  git mv "$DS_ACT/test/java/io/zhijun/devservice/autoconfigure/actuator" "$DS_ACT/test/java/io/zhijun/devservice/boot/autoconfigure/actuator" 2>/dev/null || \
    mv "$DS_ACT/test/java/io/zhijun/devservice/autoconfigure/actuator" "$DS_ACT/test/java/io/zhijun/devservice/boot/autoconfigure/actuator"
fi

echo "=== text replacements ==="
python3 <<'PY'
import pathlib

replacements = [
    ("io.zhijun.devservice.autoconfigure.bootstrap", "io.zhijun.devservice.boot.autoconfigure.bootstrap"),
    ("io.zhijun.devservice.autoconfigure.", "io.zhijun.devservice.boot.autoconfigure."),
    ("io.zhijun.devservice.registration", "io.zhijun.devservice.boot.registration"),
    ("io.zhijun.devservice.api", "io.zhijun.devservice.core.api"),
    ("io.zhijun.devservice.bootstrap", "io.zhijun.devservice.core.bootstrap"),
    ("io.zhijun.devservice.container", "io.zhijun.devservice.core.container"),
    ("io.zhijun.devservice.util", "io.zhijun.devservice.core.util"),
    ("io.zhijun.devservice.docker", "io.zhijun.devservice.core.docker"),
    ("io.zhijun.observation.autoconfigure.", "io.zhijun.observation.boot.autoconfigure."),
    ("io.zhijun.multitenancy.autoconfigure.spring.web.", "io.zhijun.multitenancy.boot.autoconfigure.web."),
    ("io.zhijun.multitenancy.autoconfigure.", "io.zhijun.multitenancy.boot.autoconfigure."),
]

ext = {".java", ".factories", ".imports", ".md", ".xml", ".properties"}
changed = 0
for path in pathlib.Path(".").rglob("*"):
    if not path.is_file() or path.suffix not in ext or "target" in path.parts:
        continue
    text = path.read_text(encoding="utf-8")
    new = text
    for old, rep in replacements:
        new = new.replace(old, rep)
    if new != text:
        path.write_text(new, encoding="utf-8")
        changed += 1
print(f"updated {changed} files")
PY

echo "=== clean empty src dirs ==="
find . \( -path './.git' -o -path '*/target' -o -path './.cursor' \) -prune -o -depth -type d -empty -print -delete 2>/dev/null | grep '/src/' || true

echo "=== done ==="
