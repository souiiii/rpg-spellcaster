# RPG Spellcaster — Development Setup

## Prerequisites

| Tool                | Version | Required For         |
| ------------------- | ------- | -------------------- |
| **Java JDK**        | **17+** | Compiling the plugin |
| **Apache Maven**    | 3.8+    | Building the jar     |
| **Paper MC Server** | 1.20.x  | Testing in-game      |

> ⚠️ **Java 8 will NOT work.** Paper MC 1.20 requires Java 17+.

---

## Install Java 17

Download **Eclipse Temurin (recommended):**
👉 https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64&package=jdk

Or install via Scoop:

```powershell
scoop bucket add java
scoop install temurin17-jdk
```

---

## Install Maven

```powershell
# Option 1 — Scoop
scoop install maven

# Option 2 — Chocolatey (run as Admin)
choco install maven

# Option 3 — Manual
# Download zip from https://maven.apache.org/download.cgi
# Extract and add bin/ to your PATH
```

---

## Build the Plugin

```powershell
cd "c:\Users\Shahid\Documents\projects and experiments\experiments\plugins\rpg spellcaster"
mvn clean package
```

Or just double-click **`build.bat`**.

Output jar: `target/rpg-spellcaster-1.0.0.jar`

---

## Install to Server

1. Copy `target/rpg-spellcaster-1.0.0.jar` → your Paper server's `plugins/` folder.
2. Start / restart the server.
3. Check console for: `RPG Spellcaster v1.0.0 ready`

---

## In-Game Usage

| Command                  | Description                      |
| ------------------------ | -------------------------------- |
| `/wand gravity_pull`     | Get a Gravity Pull wand          |
| `/wand homing_missile 2` | Get a Tier-2 Homing Missile wand |
| `/wand wall_of_fire 3`   | Get a Tier-3 Wall of Fire wand   |
| `/wand nova_blast`       | Get a Nova Blast wand            |
| `/spellcaster list`      | List all spells                  |
| `/spellcaster reload`    | Reload config.yml                |

**Right-click** with any wand to cast its spell!

---

## Spells Reference

### 🌀 Gravity Pull (`gravity_pull`)

- Pulls nearby entities toward you using vector math
- Spiral PORTAL particle vortex
- Damage scales by distance (closer = more damage)
- Configurable: `radius`, `force`, `damage`, `cooldown`

### 🚀 Homing Missile (`homing_missile`)

- Launches a tracking Snowball projectile
- Per-tick velocity steering: blends current velocity → target direction
- FLAME trail + sphere detonation on impact
- Configurable: `tracking-range`, `base-speed`, `turn-speed`, `damage`

### 🔥 Wall of Fire (`wall_of_fire`)

- Projects a particle wall perpendicular to your look direction
- Uses **cross-product** of look × up vectors to orient the wall plane
- Burns entities in the wall every 0.5s
- Sets entities on fire for 1.5s
- Configurable: `width`, `height`, `damage-per-tick`, `duration-seconds`

### 💥 Nova Blast (`nova_blast`)

- Radial shockwave knocking all nearby entities outward
- Fibonacci-sphere particle burst + expanding ground ring animation
- Damage falloff: `max_damage * (1 - dist/radius)`
- Configurable: `radius`, `max-damage`, `knockback-force`

---

## Project Structure

```
src/main/java/com/shahid/spellcaster/
├── SpellcasterPlugin.java          Main plugin class
├── commands/
│   ├── WandCommand.java            /wand command
│   └── SpellcasterCommand.java     /spellcaster command
├── listeners/
│   ├── WandInteractListener.java   Right-click detection
│   └── ProjectileHitListener.java  Homing missile detonation
├── managers/
│   ├── WandManager.java            Item creation via PDC
│   ├── SpellRegistry.java          Spell lookup map
│   └── CooldownManager.java        Per-player cooldowns
├── particles/
│   └── ParticleEngine.java         Geometry engine (async)
└── spells/
    ├── Spell.java                  Interface
    ├── GravityPullSpell.java
    ├── HomingMissileSpell.java
    ├── WallOfFireSpell.java
    └── NovaBlastSpell.java
```
