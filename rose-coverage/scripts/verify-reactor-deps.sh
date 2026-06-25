#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "$0")/../.." && pwd)"
coverage_pom="${repo_root}/rose-coverage/pom.xml"

if [[ ! -f "$coverage_pom" ]]; then
  echo "Missing rose-coverage/pom.xml" >&2
  exit 1
fi

project_artifact_id() {
  awk '
    /<packaging>pom<\/packaging>/ { exit }
    /<\/parent>/ { after_parent = 1; next }
    after_parent && /<artifactId>/ {
      gsub(/^[[:space:]]*<artifactId>|<\/artifactId>.*/, "")
      print
      exit
    }
    !seen_parent && /<artifactId>/ && !/<parent>/ {
      gsub(/^[[:space:]]*<artifactId>|<\/artifactId>.*/, "")
      print
      exit
    }
    /<parent>/ { seen_parent = 1 }
  ' "$1"
}

mapfile -t expected < <(
  find "$repo_root" -name pom.xml \
    ! -path '*/rose-build/*' \
    ! -path '*/target/*' \
    -print \
  | while read -r pom; do
      if grep -q '<packaging>pom</packaging>' "$pom"; then
        continue
      fi
      project_artifact_id "$pom"
    done \
  | grep '^rose-' \
  | grep -v '^rose-coverage$' \
  | sort -u
)

mapfile -t declared < <(
  sed -n '/<dependencies>/,/<\/dependencies>/p' "$coverage_pom" \
    | awk -F'[<>]' '/<artifactId>/{print $3}' \
    | grep '^rose-' \
    | sort -u
)

missing=()
extra=()

for module in "${expected[@]}"; do
  if ! printf '%s\n' "${declared[@]}" | grep -qx "$module"; then
    missing+=("$module")
  fi
done

for module in "${declared[@]}"; do
  if ! printf '%s\n' "${expected[@]}" | grep -qx "$module"; then
    extra+=("$module")
  fi
done

if ((${#missing[@]} > 0 || ${#extra[@]} > 0)); then
  echo "rose-coverage dependencies are out of sync with reactor leaf modules." >&2
  if ((${#missing[@]} > 0)); then
    echo "Missing in rose-coverage/pom.xml:" >&2
    printf '  - %s\n' "${missing[@]}" >&2
  fi
  if ((${#extra[@]} > 0)); then
    echo "Extra in rose-coverage/pom.xml:" >&2
    printf '  - %s\n' "${extra[@]}" >&2
  fi
  exit 1
fi

echo "rose-coverage dependencies match ${#expected[@]} reactor leaf modules."
