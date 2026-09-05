from pathlib import Path
import re
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
errors = []

root_gradle = (root / "build.gradle.kts").read_text(encoding="utf-8")
app_gradle = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
props = (root / "gradle.properties").read_text(encoding="utf-8")
workflow = (root / ".github/workflows/build-apk.yml").read_text(encoding="utf-8")

required = {
    "AGP 9.4.0": 'version "9.4.0"' in root_gradle,
    "KGP 2.4.10": "kotlin-gradle-plugin:2.4.10" in root_gradle,
    "Kotlin integrado AGP 9": "android.builtInKotlin=true" in props,
    "Sin kotlin-android plugin": "org.jetbrains.kotlin.android" not in root_gradle and
                                  "org.jetbrains.kotlin.android" not in app_gradle,
    "compileSdk 37.0": (
        "version = release(37)" in app_gradle and
        "minorApiLevel = 0" in app_gradle
    ),
    "targetSdk 37": "targetSdk = 37" in app_gradle,
    "Java 17": "JavaVersion.VERSION_17" in app_gradle,
    "buildToolsVersion 37.0.0": 'buildToolsVersion = "37.0.0"' in app_gradle,
    "Core KTX 1.19.0": "core-ktx:1.19.0" in app_gradle,
    "AppCompat 1.8.0": "appcompat:1.8.0" in app_gradle,
    "Activity 1.13.0": "activity-ktx:1.13.0" in app_gradle,
    "Lifecycle 2.11.0": "lifecycle-runtime-ktx:2.11.0" in app_gradle,
    "Material 1.14.0": "material:1.14.0" in app_gradle,
    "CameraX 1.6.2": 'val camerax = "1.6.2"' in app_gradle,
    "GPS 21.4.0": "play-services-location:21.4.0" in app_gradle,
    "Exif 1.4.2": "exifinterface:1.4.2" in app_gradle,
    "Gradle 9.6.0": 'gradle-version: "9.6.0"' in workflow,
    "Checkout v6": "actions/checkout@v6" in workflow,
    "Setup Java v6": "actions/setup-java@v6" in workflow,
    "Setup Gradle v6": "gradle/actions/setup-gradle@v6" in workflow,
    "Upload Artifact v7": "actions/upload-artifact@v7" in workflow,
    "Build Tools 37.0.0": '"build-tools;37.0.0"' in workflow,
    "Android Platform 37.0": '"platforms;android-37.0"' in workflow,
}

for label, ok in required.items():
    if not ok:
        errors.append(f"Configuración faltante: {label}")

# Old Kotlin DSL must not return.
if "kotlinOptions {" in app_gradle:
    errors.append("Se encontró kotlinOptions legacy")
if 'jvmTarget = "17"' in app_gradle:
    errors.append("Se encontró jvmTarget String legacy")

# XML well-formed.
xml_files = list((root / "app/src/main/res").rglob("*.xml"))
xml_files.append(root / "app/src/main/AndroidManifest.xml")
for path in xml_files:
    try:
        ET.parse(path)
    except Exception as exc:
        errors.append(f"XML inválido {path.relative_to(root)}: {exc}")

# Preference namespace.
prefs = (root / "app/src/main/res/xml/preferences.xml").read_text(encoding="utf-8")
if "android:useSimpleSummaryProvider" in prefs:
    errors.append("preferences.xml usa namespace Android incorrecto")
if 'app:useSimpleSummaryProvider="true"' not in prefs:
    errors.append("preferences.xml no usa app:useSimpleSummaryProvider")

# Bindings vs layouts.
binding_targets = {
    "MainActivity.kt": "activity_main.xml",
    "GalleryActivity.kt": "activity_gallery.xml",
    "PhotoAdapter.kt": "item_photo.xml",
}

java_root = root / "app/src/main/java/cl/fotobragps/app"
layout_root = root / "app/src/main/res/layout"

for kt_name, layout_name in binding_targets.items():
    matches = list(java_root.rglob(kt_name))
    if not matches:
        errors.append(f"No se encontró {kt_name}")
        continue

    kt_text = matches[0].read_text(encoding="utf-8")
    layout_text = (layout_root / layout_name).read_text(encoding="utf-8")

    fields = set(re.findall(r"\bbinding\.([a-z][A-Za-z0-9_]*)", kt_text))
    ids = set(re.findall(r'android:id="@\+id/([A-Za-z0-9_]+)"', layout_text))
    missing = sorted(fields - ids - {"root"})
    if missing:
        errors.append(f"{kt_name} usa bindings inexistentes: {missing}")

# Activities in manifest exist.
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
activities = re.findall(r'android:name="(\.[^"]+)"', manifest)
for activity in activities:
    class_name = activity.split(".")[-1] + ".kt"
    if not list(java_root.rglob(class_name)):
        errors.append(f"Activity sin clase Kotlin: {activity}")

# Basic Kotlin structural checks.
for path in java_root.rglob("*.kt"):
    text = path.read_text(encoding="utf-8")
    for a, b, label in [("{", "}", "{}"), ("(", ")", "()"), ("[", "]", "[]")]:
        if text.count(a) != text.count(b):
            errors.append(f"Delimitadores {label} desbalanceados en {path.relative_to(root)}")
    if "TODO(" in text or "FIXME" in text:
        errors.append(f"Marcador pendiente en {path.relative_to(root)}")

if errors:
    print("PRE-FLIGHT 2026 FALLÓ")
    for error in errors:
        print("-", error)
    raise SystemExit(1)

print("PRE-FLIGHT 2026 OK")
for label in required:
    print("-", label)
print(f"- XML revisados: {len(xml_files)}")
