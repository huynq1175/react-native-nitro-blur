# @abeman/react-native-nitro-blur

A high-performance blur view component for React Native, built with [Nitro Modules](https://nitro.margelo.com/) (JSI). Drop-in alternative to `expo-blur` without any Expo dependency.

## Features

- Native `UIVisualEffectView` blur on iOS with adjustable intensity
- Real blur on Android via [Dimezis BlurView](https://github.com/Dimezis/BlurView) library
- 21 tint styles matching Apple's iOS blur effects
- `BlurTargetView` for Android real-blur support (same pattern as expo-blur)
- Reanimated-compatible style animations
- Compatible with React Native 0.76+ (New Architecture)

## Installation

```sh
npm install @abeman/react-native-nitro-blur react-native-nitro-modules
```

> `react-native-nitro-modules` is a required peer dependency — this library is built on [Nitro Modules](https://nitro.margelo.com/).

For iOS, run `pod install` in the `ios/` directory after installing.

## Usage

### Basic (iOS + Android tint overlay)

```tsx
import { BlurView } from '@abeman/react-native-nitro-blur';

function MyComponent() {
  return (
    <View style={{ flex: 1 }}>
      <Image source={{ uri: 'https://example.com/photo.jpg' }} style={StyleSheet.absoluteFill} />
      <BlurView
        tint="light"
        intensity={80}
        style={StyleSheet.absoluteFill}
      />
    </View>
  );
}
```

### With Real Blur on Android (BlurTargetView)

To enable real blur on Android, wrap the content you want to blur with `BlurTargetView` and pass a ref to `BlurView`:

```tsx
import { useRef } from 'react';
import { View, StyleSheet, Image } from 'react-native';
import { BlurView, BlurTargetView } from '@abeman/react-native-nitro-blur';

function MyComponent() {
  const blurTargetRef = useRef(null);

  return (
    <BlurTargetView ref={blurTargetRef} style={{ flex: 1 }}>
      <Image source={{ uri: 'https://example.com/photo.jpg' }} style={StyleSheet.absoluteFill} />
      <BlurView
        tint="light"
        intensity={80}
        blurMethod="dimezisBlurView"
        blurTarget={blurTargetRef}
        style={StyleSheet.absoluteFill}
      />
    </BlurTargetView>
  );
}
```

> On iOS, `BlurTargetView` is just a regular `View` and `blurTarget`/`blurMethod` are no-ops — iOS uses `UIVisualEffectView` which naturally blurs whatever is behind it.

## Props

### `BlurView`

| Prop | Type | Default | Platform | Description |
|------|------|---------|----------|-------------|
| `tint` | `BlurTint` | `'default'` | iOS + Android | The tint style applied to the blur effect. |
| `intensity` | `number` | `50` | iOS + Android | Blur intensity from `1` to `100`. |
| `blurReductionFactor` | `number` | `4` | Android | Divides blur intensity on Android to match iOS. |
| `blurMethod` | `BlurMethod` | `'none'` | Android | Blur implementation to use on Android. |
| `blurTarget` | `RefObject<View>` | — | Android | Ref to a `BlurTargetView` wrapping content to blur. |

Plus all standard `View` props (`style`, `children`, etc.).

### `BlurTargetView`

Accepts all standard `View` props. On Android, this is a native container view that enables the Dimezis BlurView to capture and blur its content. On iOS, it's just a regular `View`.

### `BlurTint` values

`'light'` | `'dark'` | `'default'` | `'extraLight'` | `'regular'` | `'prominent'` | `'systemUltraThinMaterial'` | `'systemThinMaterial'` | `'systemMaterial'` | `'systemThickMaterial'` | `'systemChromeMaterial'` | `'systemUltraThinMaterialLight'` | `'systemThinMaterialLight'` | `'systemMaterialLight'` | `'systemThickMaterialLight'` | `'systemChromeMaterialLight'` | `'systemUltraThinMaterialDark'` | `'systemThinMaterialDark'` | `'systemMaterialDark'` | `'systemThickMaterialDark'` | `'systemChromeMaterialDark'`

### `BlurMethod` values (Android only)

| Method | Description |
|--------|-------------|
| `'none'` | Semi-transparent tint overlay only (no real blur). Default. |
| `'dimezisBlurView'` | Real blur via [Dimezis BlurView](https://github.com/Dimezis/BlurView). Requires `blurTarget`. May decrease performance on SDK 30 and below. |
| `'dimezisBlurViewSdk31Plus'` | Real blur on SDK 31+ only, falls back to `'none'` on older versions. Requires `blurTarget`. |

## Animating with Reanimated

### Style animations (works)

`useAnimatedStyle` works normally for style props (transform, opacity, etc.):

```tsx
import Animated, { useAnimatedStyle, useSharedValue, withTiming } from 'react-native-reanimated';
import { BlurView } from '@abeman/react-native-nitro-blur';

const AnimatedBlurView = Animated.createAnimatedComponent(BlurView);

function MyComponent() {
  const opacity = useSharedValue(1);
  const animatedStyle = useAnimatedStyle(() => ({ opacity: opacity.value }));

  return (
    <AnimatedBlurView
      tint="light"
      intensity={80}
      style={[styles.blur, animatedStyle]}
    />
  );
}
```

### Animating `intensity` (workaround required)

> **`animatedProps` does NOT work with Nitro HybridViews.** Reanimated's `animatedProps` calls native `updateProps` directly on the ShadowNode with `folly::dynamic` values, but Nitro's Fabric component descriptor expects `jsi::Value`. This is a fundamental incompatibility at the C++ level.

Use `useAnimatedReaction` with `runOnJS` to bridge animated values to React state:

```tsx
import { useState } from 'react';
import Animated, {
  useSharedValue,
  useAnimatedReaction,
  runOnJS,
  withTiming,
} from 'react-native-reanimated';
import { BlurView } from '@abeman/react-native-nitro-blur';

function MyComponent() {
  const progress = useSharedValue(0);
  const [intensity, setIntensity] = useState(0);

  useAnimatedReaction(
    () => progress.value * 100,
    (value) => runOnJS(setIntensity)(value)
  );

  return (
    <BlurView
      tint="light"
      intensity={intensity}
      style={StyleSheet.absoluteFill}
    />
  );
}
```

## Platform behavior

### iOS
Uses `UIVisualEffectView` with `UIViewPropertyAnimator` for smooth, adjustable blur intensity. All tint styles map directly to native `UIBlurEffect.Style` values.

### Android
- **`blurMethod: 'none'`** (default): Renders a semi-transparent tinted overlay.
- **`blurMethod: 'dimezisBlurView'`**: Uses the [Dimezis BlurView](https://github.com/Dimezis/BlurView) library for real blur. Requires wrapping content in `BlurTargetView` and passing a ref via the `blurTarget` prop.
- **`blurMethod: 'dimezisBlurViewSdk31Plus'`**: Same as above but only on SDK 31+; older versions fall back to tint overlay.

The `blurReductionFactor` prop helps match the perceived blur intensity between iOS and Android.

## Migrating from expo-blur

| expo-blur | @abeman/react-native-nitro-blur |
|-----------|------------------------|
| `import { BlurView } from 'expo-blur'` | `import { BlurView } from '@abeman/react-native-nitro-blur'` |
| `import { BlurTargetView } from 'expo-blur'` | `import { BlurTargetView } from '@abeman/react-native-nitro-blur'` |
| `experimentalBlurMethod` | `blurMethod` (deprecated alias supported) |
| `Animated.createAnimatedComponent(BlurView)` + `animatedProps` | Use `useAnimatedReaction` + `runOnJS` for prop animations (see above) |

The API is designed to be compatible. Replace imports and it should work. The only difference is the Reanimated `animatedProps` limitation — use `useAnimatedReaction` instead.

## Contributing

- [Development workflow](CONTRIBUTING.md#development-workflow)
- [Sending a pull request](CONTRIBUTING.md#sending-a-pull-request)
- [Code of conduct](CODE_OF_CONDUCT.md)

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
