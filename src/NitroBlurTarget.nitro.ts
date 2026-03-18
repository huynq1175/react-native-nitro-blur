import type {
  HybridView,
  HybridViewMethods,
  HybridViewProps,
} from 'react-native-nitro-modules';

export interface NitroBlurTargetProps extends HybridViewProps {}
export interface NitroBlurTargetMethods extends HybridViewMethods {}

export type NitroBlurTarget = HybridView<
  NitroBlurTargetProps,
  NitroBlurTargetMethods
>;
