# AgriTwin — Climate-Twin Farming Intelligence (v2 Sunlit Agriculture)

> **Developed by Team DevTech** for the **NASA Space Apps Challenge 2026**  
> **Challenge:** *Field Shift: Adapting Farms with NASA Data*  
> **Language:** British English (`en-GB`) · **Platform:** Native Android (Kotlin & Jetpack Compose)

---

## 1. What's New in v2 (Sunlit Agriculture & Live Data Upgrade)

- **Sunlit Agriculture Theme:** Replaced dark mode with a bright, clean, professional light theme (pure white `#FFFFFF`, soft slate `#F8FAFC`, fresh agricultural green `#16A34A`, and crisp sky-blue `#0284C7`).
- **Live NASA POWER API Execution:**
  - **Twin Matcher:** Live execution of NASA POWER Climatology point API (`T2M`, `PRECTOTCORR`). Climate-twin matching is computed directly from real returned observations.
  - **Water Dashboard:** Live execution of NASA POWER Daily point API (`T2M`, `PRECTOTCORR`, `WS2M`) for real 14-day surface rainfall telemetry.
  - **Alerts Feed:** Live execution of NASA POWER 30-day daily point API evaluated against extreme thresholds ($\ge 50$ mm rain, $\ge 42^\circ\text{C}$ heat, $\ge 10$ m/s wind).
- **Badge Bug Fixed:** "Live · NASA POWER" badge pills now render with unclipped single-line geometry, adequate vertical breathing room, and zero text truncation.
- **Pure Compose Hero Illustration:** Custom vector illustration on Landing Page showing a low-Earth orbit satellite traversing an orbital ring above lush terraced crop fields under the morning sun.
- **Data Honesty:** Any number fetched from NASA POWER gets the "Live · NASA POWER" badge. If a query fails, an honest error message is shown — never faking numbers. Reference datasets, unlinked field cards, example storms, and disease diagnosis remain clearly badged "Demo data".

---

## 2. Architecture & File Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Edge-to-edge entry point, light navigation bar & backstack
├── data/
│   ├── model/
│   │   ├── NasaPowerModels.kt           # Domain models, presets, climatology & alerts
│   │   ├── ClimateTwinMatcher.kt        # Normalized Euclidean proximity engine & ~12 benchmark regions
│   │   └── SoilHealthEngine.kt          # 4-pillar Soil Health Index calculation & formula
│   ├── network/
│   │   └── NasaPowerRepository.kt       # NASA POWER API client (parameters T2M, PRECTOTCORR, WS2M)
│   ├── aquora/
│   │   └── AquoraAdapter.kt             # AQUORA satellite water-monitoring pipeline plug-in contract
│   └── disease/
│       └── DiseaseModel.kt              # CropDiseaseModel interface & HeuristicHsvDiseaseModel
└── ui/
    ├── theme/
    │   ├── Color.kt                     # Sunlit agriculture palette (Fresh green, sky blue, dark slate)
    │   ├── Theme.kt                     # Material 3 light scheme configuration
    │   └── Type.kt                      # Accessible typography hierarchy
    ├── components/
    │   ├── CommonBadges.kt              # Unclipped "Live · NASA POWER", "Demo data", SkeletonBox
    │   └── SparklineChart.kt            # High-precision Canvas bezier trend visualization
    └── screens/
        ├── HomeScreen.kt                # Landing hero with vector illustration, story, feature navigators
        ├── ClimateTwinScreen.kt         # Live NASA climatology, projection controls, twin match cards
        ├── WaterDashboardScreen.kt      # Real 14-day daily rainfall bars, field cards, irrigation advice
        ├── DiseaseAnalyzerScreen.kt     # Leaf camera/photo picker, HSV breakdown, treatment steps
        └── AlertsSoilScreen.kt          # Real 30-day hazard alerts feed + Soil Health Index with sparklines
```

---

## 3. NASA Earth Observation Data Sources

| Feature | NASA Dataset / Endpoint | Ingestion Status |
|---|---|---|
| **Climatology Baseline** | NASA POWER Climatology Point API (`T2M`, `PRECTOTCORR`, `AG` community) | **Live · NASA POWER** |
| **14-Day Rainfall** | NASA POWER Daily Point API (`T2M`, `PRECTOTCORR`, `WS2M`) | **Live · NASA POWER** |
| **30-Day Weather Hazard Triggers** | NASA POWER Daily Point API (`PRECTOTCORR ≥ 50 mm`, `T2M ≥ 42 °C`, `WS2M ≥ 10 m/s`) | **Live · NASA POWER** |
| **Water Stress & Moisture** | AQUORA Satellite Pipeline (NASA SMAP L3/L4 & ECOSTRESS ESI contract) | *Plug-in Interface / Demo data* |
| **Soil Health Metrics** | AgriTwin 4-Pillar Soil Health Index (`0.35·SOC + 0.25·AS + 0.25·WHC + 0.15·MN`) | *Demo data* |
| **Crop Disease Diagnosis** | On-device HSV pixel colour segmentation heuristic | *Demo mode — not a real diagnosis* |

---

## 4. How to Run & Verify

```bash
# Compile and build debug APK
gradle :app:assembleDebug

# Run Robolectric unit tests
gradle :app:testDebugUnitTest
```
