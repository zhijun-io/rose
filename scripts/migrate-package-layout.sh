#!/usr/bin/env bash
# Migrate Rose business domains to layer-first package layout.
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

git_mv_dir() {
  local src="$1" dst="$2"
  if [[ -d "$src" ]]; then
    mkdir -p "$(dirname "$dst")"
    git mv "$src" "$dst"
  fi
}

git_mv_file() {
  local src="$1" dst="$2"
  if [[ -f "$src" ]]; then
    mkdir -p "$(dirname "$dst")"
    git mv "$src" "$dst"
  fi
}

echo "=== multitenancy core ==="
MT_CORE="rose-multitenancy/rose-multitenancy-core/src"
git_mv_dir "$MT_CORE/main/java/io/zhijun/multitenancy/core/context" "$MT_CORE/main/java/io/zhijun/multitenancy/context"
git_mv_dir "$MT_CORE/main/java/io/zhijun/multitenancy/core/detail" "$MT_CORE/main/java/io/zhijun/multitenancy/detail"
git_mv_dir "$MT_CORE/main/java/io/zhijun/multitenancy/core/exception" "$MT_CORE/main/java/io/zhijun/multitenancy/exception"
git_mv_dir "$MT_CORE/test/java/io/zhijun/multitenancy/core/context" "$MT_CORE/test/java/io/zhijun/multitenancy/context"
git_mv_dir "$MT_CORE/test/java/io/zhijun/multitenancy/core/detail" "$MT_CORE/test/java/io/zhijun/multitenancy/detail"
git_mv_dir "$MT_CORE/test/java/io/zhijun/multitenancy/core/exception" "$MT_CORE/test/java/io/zhijun/multitenancy/exception"
rmdir "$MT_CORE/main/java/io/zhijun/multitenancy/core" 2>/dev/null || true
rmdir "$MT_CORE/test/java/io/zhijun/multitenancy/core" 2>/dev/null || true

echo "=== multitenancy spring-boot ==="
MT_BOOT="rose-multitenancy/rose-multitenancy-spring-boot/src"
for f in "$MT_BOOT/main/java/io/zhijun/multitenancy/autoconfigure/core"/*.java; do
  [[ -f "$f" ]] && git_mv_file "$f" "$MT_BOOT/main/java/io/zhijun/multitenancy/autoconfigure/$(basename "$f")"
done
for f in "$MT_BOOT/main/java/io/zhijun/multitenancy/autoconfigure/async"/*.java; do
  [[ -f "$f" ]] && git_mv_file "$f" "$MT_BOOT/main/java/io/zhijun/multitenancy/autoconfigure/$(basename "$f")"
done
git_mv_file "$MT_BOOT/main/java/io/zhijun/multitenancy/detail/PropertiesTenantDetailsService.java" \
  "$MT_BOOT/main/java/io/zhijun/multitenancy/autoconfigure/PropertiesTenantDetailsService.java"
for f in "$MT_BOOT/test/java/io/zhijun/multitenancy/web/autoconfigure"/*.java; do
  [[ -f "$f" ]] && git_mv_file "$f" "$MT_BOOT/test/java/io/zhijun/multitenancy/autoconfigure/web/$(basename "$f")"
done
rmdir "$MT_BOOT/main/java/io/zhijun/multitenancy/autoconfigure/core" 2>/dev/null || true
rmdir "$MT_BOOT/main/java/io/zhijun/multitenancy/autoconfigure/async" 2>/dev/null || true
rmdir "$MT_BOOT/main/java/io/zhijun/multitenancy/detail" 2>/dev/null || true
rmdir "$MT_BOOT/test/java/io/zhijun/multitenancy/web/autoconfigure" 2>/dev/null || true

echo "=== observation ==="
for module in rose-observation/rose-observation-spring-boot \
  rose-observation/rose-observation-spring-boot-otel \
  rose-observation/rose-observation-spring-boot-logback \
  rose-observation/rose-observation-spring-boot-micrometer-otlp \
  rose-observation/rose-observation-spring-boot-micrometer-bridge \
  rose-observation/rose-observation-spring-boot-conventions-otel; do
  for kind in main test; do
    base="$module/src/$kind/java/io/zhijun/observation"
  git_mv_dir "$base/core/autoconfigure" "$base/autoconfigure" 2>/dev/null || true
  git_mv_dir "$base/otel/autoconfigure" "$base/autoconfigure/otel" 2>/dev/null || true
  git_mv_dir "$base/logback/autoconfigure" "$base/autoconfigure/logback" 2>/dev/null || true
  git_mv_dir "$base/micrometer/otlp/autoconfigure" "$base/autoconfigure/micrometer/otlp" 2>/dev/null || true
  git_mv_dir "$base/micrometer/bridge/autoconfigure" "$base/autoconfigure/micrometer/bridge" 2>/dev/null || true
  git_mv_dir "$base/conventions/otel/autoconfigure" "$base/autoconfigure/conventions/otel" 2>/dev/null || true
  done
done
OBS_CORE="rose-observation/rose-observation-core/src"
if [[ -f "$OBS_CORE/main/java/io/zhijun/observation/core/TelemetryConventionsBackend.java" ]]; then
  git_mv_file "$OBS_CORE/main/java/io/zhijun/observation/core/TelemetryConventionsBackend.java" \
    "$OBS_CORE/main/java/io/zhijun/observation/TelemetryConventionsBackend.java"
  rmdir "$OBS_CORE/main/java/io/zhijun/observation/core" 2>/dev/null || true
fi

echo "=== devservice core ==="
DS_CORE="rose-devservice/rose-devservice-core/src"
for sub in api bootstrap container docker util; do
  git_mv_dir "$DS_CORE/main/java/io/zhijun/devservice/core/$sub" "$DS_CORE/main/java/io/zhijun/devservice/$sub"
  git_mv_dir "$DS_CORE/test/java/io/zhijun/devservice/core/$sub" "$DS_CORE/test/java/io/zhijun/devservice/$sub"
done
rmdir "$DS_CORE/main/java/io/zhijun/devservice/core" 2>/dev/null || true
rmdir "$DS_CORE/test/java/io/zhijun/devservice/core" 2>/dev/null || true

echo "=== devservice spring-boot ==="
DS_BOOT="rose-devservice/rose-devservice-spring-boot/src"
git_mv_dir "$DS_BOOT/main/java/io/zhijun/devservice/core/autoconfigure" "$DS_BOOT/main/java/io/zhijun/devservice/autoconfigure"
git_mv_dir "$DS_BOOT/main/java/io/zhijun/devservice/core/registration" "$DS_BOOT/main/java/io/zhijun/devservice/registration"
git_mv_dir "$DS_BOOT/main/java/io/zhijun/devservice/core/docker" "$DS_BOOT/main/java/io/zhijun/devservice/autoconfigure/docker"
git_mv_dir "$DS_BOOT/test/java/io/zhijun/devservice/core/autoconfigure" "$DS_BOOT/test/java/io/zhijun/devservice/autoconfigure"
git_mv_dir "$DS_BOOT/test/java/io/zhijun/devservice/core/registration" "$DS_BOOT/test/java/io/zhijun/devservice/registration"
git_mv_dir "$DS_BOOT/test/java/io/zhijun/devservice/core/docker" "$DS_BOOT/test/java/io/zhijun/devservice/autoconfigure/docker"

echo "=== devservice connectors ==="
CONNECTORS=(postgresql mysql redis mongodb kafka rabbitmq artemis activemq ollama mqtt openlit otel)
for tech in "${CONNECTORS[@]}"; do
  module="rose-devservice/rose-devservice-spring-boot-$tech"
  [[ -d "$module" ]] || continue
  for kind in main test; do
    src="$module/src/$kind/java/io/zhijun/devservice/$tech"
    dst="$module/src/$kind/java/io/zhijun/devservice/autoconfigure/$tech"
    git_mv_dir "$src" "$dst"
  done
done

echo "=== devservice actuator ==="
DS_ACT="rose-devservice/rose-devservice-spring-boot-actuator/src"
git_mv_dir "$DS_ACT/main/java/io/zhijun/devservice/actuator/autoconfigure" \
  "$DS_ACT/main/java/io/zhijun/devservice/autoconfigure/actuator"
git_mv_file "$DS_ACT/test/java/io/zhijun/devservice/actuator/DevServicesEndpointAutoConfigurationTests.java" \
  "$DS_ACT/test/java/io/zhijun/devservice/autoconfigure/actuator/DevServicesEndpointAutoConfigurationTests.java"

echo "=== package/import text replacements ==="
python3 <<'PY'
import pathlib
import re

root = pathlib.Path(".")

replacements = [
    ("io.zhijun.observation.conventions.otel.autoconfigure", "io.zhijun.observation.autoconfigure.conventions.otel"),
    ("io.zhijun.observation.micrometer.otlp.autoconfigure", "io.zhijun.observation.autoconfigure.micrometer.otlp"),
    ("io.zhijun.observation.micrometer.bridge.autoconfigure", "io.zhijun.observation.autoconfigure.micrometer.bridge"),
    ("io.zhijun.observation.logback.autoconfigure", "io.zhijun.observation.autoconfigure.logback"),
    ("io.zhijun.observation.otel.autoconfigure", "io.zhijun.observation.autoconfigure.otel"),
    ("io.zhijun.observation.core.autoconfigure", "io.zhijun.observation.autoconfigure"),
    ("io.zhijun.observation.core", "io.zhijun.observation"),
    ("io.zhijun.devservice.core.autoconfigure.bootstrap", "io.zhijun.devservice.autoconfigure.bootstrap"),
    ("io.zhijun.devservice.core.autoconfigure", "io.zhijun.devservice.autoconfigure"),
    ("io.zhijun.devservice.core.api", "io.zhijun.devservice.api"),
    ("io.zhijun.devservice.core.bootstrap", "io.zhijun.devservice.bootstrap"),
    ("io.zhijun.devservice.core.container", "io.zhijun.devservice.container"),
    ("io.zhijun.devservice.core.docker", "io.zhijun.devservice.docker"),
    ("io.zhijun.devservice.core.registration", "io.zhijun.devservice.registration"),
    ("io.zhijun.devservice.core.util", "io.zhijun.devservice.util"),
    ("io.zhijun.devservice.actuator.autoconfigure", "io.zhijun.devservice.autoconfigure.actuator"),
    ("io.zhijun.multitenancy.autoconfigure.core", "io.zhijun.multitenancy.autoconfigure"),
    ("io.zhijun.multitenancy.autoconfigure.async", "io.zhijun.multitenancy.autoconfigure"),
    ("io.zhijun.multitenancy.web.autoconfigure", "io.zhijun.multitenancy.autoconfigure.web"),
    ("io.zhijun.multitenancy.core.context", "io.zhijun.multitenancy.context"),
    ("io.zhijun.multitenancy.core.detail", "io.zhijun.multitenancy.detail"),
    ("io.zhijun.multitenancy.core.exception", "io.zhijun.multitenancy.exception"),
    ("io.zhijun.multitenancy.core.observation", "io.zhijun.multitenancy.observation"),
    ("io.zhijun.mybatisplus.core.autoconfigure", "io.zhijun.mybatisplus.autoconfigure"),
]
connectors = [
    "postgresql", "mysql", "redis", "mongodb", "kafka", "rabbitmq",
    "artemis", "activemq", "ollama", "mqtt", "openlit", "otel",
]
for tech in connectors:
    replacements.append((f"io.zhijun.devservice.{tech}", f"io.zhijun.devservice.autoconfigure.{tech}"))

extensions = {".java", ".factories", ".imports", ".md", ".xml", ".properties"}
skip_dirs = {"target", ".git", "node_modules"}

def should_process(path: pathlib.Path) -> bool:
    if path.suffix not in extensions:
        return False
    return not any(part in skip_dirs for part in path.parts)

changed = 0
for path in root.rglob("*"):
    if not path.is_file() or not should_process(path):
        continue
    text = path.read_text(encoding="utf-8")
    original = text
    path_replacements = list(replacements)
    path_str = str(path)
    if "devservice/autoconfigure/docker" in path_str or "devservice/core/docker/DevServiceDocker" in path_str:
        path_replacements = [
            (old, new) for old, new in path_replacements
            if old != "io.zhijun.devservice.core.docker"
        ] + [("io.zhijun.devservice.core.docker", "io.zhijun.devservice.autoconfigure.docker")]
    for old, new in path_replacements:
        text = text.replace(old, new)
    if text != original:
        path.write_text(text, encoding="utf-8")
        changed += 1

print(f"Updated {changed} files")
PY

echo "=== done ==="
