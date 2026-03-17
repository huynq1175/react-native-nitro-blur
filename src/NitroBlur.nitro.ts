import type {
  HybridView,
  HybridViewMethods,
  HybridViewProps,
} from 'react-native-nitro-modules';

export interface NitroBlurProps extends HybridViewProps {
  color: string;
}
export interface NitroBlurMethods extends HybridViewMethods {}

export type NitroBlur = HybridView<
  NitroBlurProps,
  NitroBlurMethods
>;
