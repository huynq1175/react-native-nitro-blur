import type {
  HybridView,
  HybridViewMethods,
  HybridViewProps,
} from 'react-native-nitro-modules';

export type BlurTint =
  | 'light'
  | 'dark'
  | 'default'
  | 'extraLight'
  | 'regular'
  | 'prominent'
  | 'systemUltraThinMaterial'
  | 'systemThinMaterial'
  | 'systemMaterial'
  | 'systemThickMaterial'
  | 'systemChromeMaterial'
  | 'systemUltraThinMaterialLight'
  | 'systemThinMaterialLight'
  | 'systemMaterialLight'
  | 'systemThickMaterialLight'
  | 'systemChromeMaterialLight'
  | 'systemUltraThinMaterialDark'
  | 'systemThinMaterialDark'
  | 'systemMaterialDark'
  | 'systemThickMaterialDark'
  | 'systemChromeMaterialDark';

/**
 * Blur method to use on Android.
 *
 * - `'none'` - Renders a semi-transparent view instead of rendering a blur effect.
 * - `'dimezisBlurView'` - Uses a native blur view implementation based on BlurView library.
 *   This method may lead to decreased performance on Android SDK 30 and below.
 * - `'dimezisBlurViewSdk31Plus'` - Uses BlurView library on Android SDK 31 and above,
 *   for older versions falls back to 'none'.
 *
 * @platform android
 */
export type BlurMethod =
  | 'none'
  | 'dimezisBlurView'
  | 'dimezisBlurViewSdk31Plus';

export interface NitroBlurProps extends HybridViewProps {
  tint: BlurTint;
  intensity: number;
  blurReductionFactor: number;
  blurTargetId: number;
  blurMethod: BlurMethod;
}

export interface NitroBlurMethods extends HybridViewMethods {}

export type NitroBlur = HybridView<NitroBlurProps, NitroBlurMethods>;
