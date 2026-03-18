# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Install dependencies
yarn

# Generate Nitro boilerplate (MUST run before first build and after changing .nitro.ts files)
yarn nitrogen

# Build the library
yarn prepare

# Type checking
yarn typecheck

# Lint
yarn lint
yarn lint --fix

# Tests
yarn test

# Example app
yarn example start          # Metro dev server
yarn example android        # Run on Android
yarn example ios            # Run on iOS

# Clean build artifacts
yarn clean
```

For Android example builds (from `example/android/`):
```bash
./gradlew :react-native-nitro-blur:compileDebugKotlin
./gradlew assembleDebug
```

## Architecture

This is a React Native **Nitro Modules** library providing `BlurView` and `BlurTargetView` components — a drop-in alternative to `expo-blur` without Expo dependency. The build flow requires running `yarn nitrogen` to auto-generate JSI bridging code before any native builds.

### Data flow

```
src/NitroBlur.nitro.ts         ← Nitro spec for BlurView (BlurTint, BlurMethod, props)
src/NitroBlurTarget.nitro.ts   ← Nitro spec for BlurTargetView
    ↓  yarn nitrogen
nitrogen/generated/            ← Auto-generated C++/Swift/Kotlin specs (do not edit)
    ↓
ios/NitroBlur.swift            ← iOS: UIVisualEffectView with UIViewPropertyAnimator
ios/NitroBlurTarget.swift      ← iOS: plain UIView (blur target not needed on iOS)
android/.../NitroBlur.kt       ← Android: Dimezis BlurView + tint overlay fallback
android/.../NitroBlurTarget.kt ← Android: BlurTarget container (child delegation)
android/.../TintStyle.kt       ← Android: BlurTint → ARGB color mapping
    ↓  yarn prepare (bob build)
lib/                           ← Built JS/TS output
```

### Key files

- **`src/NitroBlur.nitro.ts`** — Defines `NitroBlurProps` (extends `HybridViewProps`) with `tint`, `intensity`, `blurReductionFactor`, `blurTargetId`, and `blurMethod` props, plus `BlurTint` and `BlurMethod` union types. Any prop/method changes start here, followed by `yarn nitrogen`.
- **`src/NitroBlurTarget.nitro.ts`** — Defines `NitroBlurTargetProps` (extends `HybridViewProps`) for the blur target container view.
- **`src/index.tsx`** — `BlurView` class component (matches expo-blur API: `blurTarget` ref → `blurTargetId` conversion, deprecation warnings). Exports `BlurTargetView` (native on Android, plain `View` on iOS).
- **`src/getBackgroundColor.ts`** — Computes CSS `rgba()` background color for tint/intensity (web/fallback).
- **`nitro.json`** — Nitro configuration with autolinking for both `NitroBlur` and `NitroBlurTarget` HybridViews.
- **`ios/NitroBlur.swift`** — Swift implementation. `HybridNitroBlur` → `BlurEffectView` (UIVisualEffectView with adjustable intensity via UIViewPropertyAnimator). `BlurTint` extension maps 21 values to `UIBlurEffect.Style`.
- **`ios/NitroBlurTarget.swift`** — Simple `HybridNitroBlurTargetSpec` returning a plain UIView.
- **`android/.../NitroBlur.kt`** — Kotlin implementation using Dimezis [BlurView](https://github.com/Dimezis/BlurView) library. Supports 3 blur methods: `NONE` (tint overlay only), `DIMEZISBLURVIEW` (real blur), `DIMEZISBLURVIEWSDK31PLUS` (SDK 31+ only). Finds blur target by React tag via `findViewById`.
- **`android/.../NitroBlurTarget.kt`** — `BlurTargetContainer` delegates all child operations to inner `ReactCompatibleBlurTarget` (extends Dimezis `BlurTarget`), matching expo-blur's `ExpoBlurTargetView` pattern.
- **`android/.../NitroBlurTargetViewGroupManager.kt`** — Custom `ViewGroupManager` for `NitroBlurTarget`. Required because Nitrogen generates `SimpleViewManager` (no children support), but `BlurTargetView` must accept children.
- **`android/.../TintStyle.kt`** — `BlurTint.toOverlayColor()` extension computing ARGB colors based on Apple's iOS 14 Sketch Kit specs.
- **`android/CMakeLists.txt`** — Builds the `nitroblur` shared C++ library (C++20).

### Props

| Prop | Type | Default | Platform |
|------|------|---------|----------|
| `tint` | `BlurTint` (21 values) | `'default'` | iOS + Android |
| `intensity` | `number` (1-100) | `50` | iOS + Android |
| `blurReductionFactor` | `number` | `4` | Android only (no-op on iOS) |
| `blurMethod` | `BlurMethod` | `'none'` | Android only (no-op on iOS) |
| `blurTarget` | `RefObject<View>` | — | Android only (no-op on iOS) |

### BlurMethod (Android)

| Method | Description |
|--------|-------------|
| `'none'` | Semi-transparent tint overlay only (no real blur) |
| `'dimezisBlurView'` | Real blur via Dimezis BlurView (requires `blurTarget`) |
| `'dimezisBlurViewSdk31Plus'` | Real blur on SDK 31+, falls back to `'none'` on older |

### Adding a new prop

1. Add the prop to `NitroBlurProps` in `src/NitroBlur.nitro.ts`
2. Run `yarn nitrogen` to regenerate specs
3. Implement the prop in `ios/NitroBlur.swift` and `android/.../NitroBlur.kt`
4. Update the `BlurView` class component in `src/index.tsx`

### Reanimated compatibility (IMPORTANT)

**`animatedProps` does NOT work with Nitro HybridViews.** This is a fundamental incompatibility:
- Reanimated's `animatedProps` calls native `updateProps` directly on the ShadowNode, passing values as `folly::dynamic`
- Nitro's Fabric component descriptor handles props via `jsi::Value`
- The `folly::dynamic` → `jsi::Value` cast crashes: "Cannot cast dynamic to a jsi::Value type"
- `getAnimatableRef()` is intentionally NOT implemented because Nitro host component refs are raw Fabric nodes (`__nativeTag`, `__viewConfig`) that Reanimated's `findHostInstance` cannot resolve

**What works:**
- `Animated.createAnimatedComponent(BlurView)` + `useAnimatedStyle` for **style animations** (transform, opacity, etc.) — these target the wrapper `View`
- Regular React props for `intensity`, `tint`, etc. — passed via React re-renders

**How to animate `intensity`:**
```tsx
const [intensity, setIntensity] = useState(50);
useAnimatedReaction(
  () => progress.value * 100,
  (value) => runOnJS(setIntensity)(value)
);
<BlurView intensity={intensity} />
```

### Monorepo structure

- Root package — library source and build tooling
- `example/` — React Native app for development/testing (yarn workspace)
- `__refer__/` — Reference expo-blur source (not compiled, excluded from tsconfig)
- Metro is configured via `react-native-monorepo-config` to resolve the workspace root

### Commit convention

Conventional Commits are enforced via lefthook pre-commit hooks (`yarn typecheck` + `yarn lint`) and commitlint.
