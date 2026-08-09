# ReEvent MVP Impact Estimate Factors

## Scope and safety boundary

This document describes the only numeric CO2e estimate enabled by the MVP. It is a **demonstration estimate**, not a product lifecycle assessment, certification, carbon credit, or Malaysia-specific environmental claim. The app only creates an estimate when a completed recycling transaction has a recorded mass in kilograms; resources recorded as `item`, `box`, or another non-mass unit remain explicitly labelled **estimate unavailable**.

The app still reports completed recovery channels and recovery rate when no numeric estimate is available.

## Enabled factor

| Resource material | Completed action | Resource unit | Material mass | Avoided CO2e factor | Stored calculation |
|---|---|---|---|---:|---|
| `plastic` or `acrylic` | `RECYCLE` | `kg`, `kilogram`, or `kilograms` | transaction quantity × 1.0 kg | 1.59710826 kgCO2e/kg | material mass × 1.59710826 |

`acrylic` is grouped under the source's broad **average plastics** category only for this disclosed MVP demonstration. It is not a material-specific acrylic factor.

The source values are 3,172.49932 kgCO2e per tonne for average-plastics primary material production and 1,575.39106 kgCO2e per tonne for the closed-loop source. The enabled factor is their difference divided by 1,000: `(3172.49932 - 1575.39106) / 1000 = 1.59710826 kgCO2e/kg`.

Source: UK Department for Energy Security and Net Zero, [2025 greenhouse-gas reporting conversion factors](https://www.gov.uk/government/publications/greenhouse-gas-reporting-conversion-factors-2025), published 10 June 2025; flat-file rows `19_505_5120_15_1` and `19_505_5123_15_1`, accessed 9 August 2026.

## Exclusions

- No estimate is made for reuse, repair, donation, return, buy-back, disposal, or an unrecognised material/action pair.
- No transport, collection, energy mix, contamination, local processing method, or item-specific mass is inferred.
- `valueRecoveredCents` is only the proportional share of the organiser-entered resource value; it is not a market-price valuation.
- Add or change a factor only after documenting the source, version/access date, scope, unit conversion, material mapping, and limitations here.
