import React from 'react';
import {
  findNodeHandle,
  Platform,
  StyleSheet,
  View,
  type ViewProps,
} from 'react-native';
import { getHostComponent } from 'react-native-nitro-modules';
import type {
  BlurMethod,
  BlurTint,
  NitroBlurMethods,
  NitroBlurProps,
} from './NitroBlur.nitro';
import type {
  NitroBlurTargetMethods,
  NitroBlurTargetProps,
} from './NitroBlurTarget.nitro';

import NitroBlurConfig from '../nitrogen/generated/shared/json/NitroBlurConfig.json';
import NitroBlurTargetConfig from '../nitrogen/generated/shared/json/NitroBlurTargetConfig.json';

const NativeBlurView = getHostComponent<NitroBlurProps, NitroBlurMethods>(
  'NitroBlur',
  () => NitroBlurConfig
);

const NativeBlurTargetView = getHostComponent<
  NitroBlurTargetProps,
  NitroBlurTargetMethods
>('NitroBlurTarget', () => NitroBlurTargetConfig);

// ----- BlurViewProps -----

export type BlurViewProps = {
  /**
   * A ref to a BlurTargetView, which this BlurView will blur as its background.
   *
   * @platform android
   */
  blurTarget?: React.RefObject<View | null>;

  /**
   * A tint mode which will be applied to the view.
   * @default 'default'
   */
  tint?: BlurTint;

  /**
   * A number from `1` to `100` to control the intensity of the blur effect.
   *
   * You can animate this property using `react-native-reanimated`.
   *
   * @default 50
   */
  intensity?: number;

  /**
   * A number by which the blur intensity will be divided on Android.
   *
   * When using blur methods on Android, the perceived blur intensity might differ from iOS
   * at different intensity levels. This property can be used to fine tune it on Android
   * to match it more closely with iOS.
   * @default 4
   * @platform android
   */
  blurReductionFactor?: number;

  /**
   * Blur method to use on Android.
   *
   * @default 'none'
   * @platform android
   */
  blurMethod?: BlurMethod;

  /**
   * @hidden
   * @deprecated Use `blurMethod` instead.
   * @default 'none'
   * @platform android
   */
  experimentalBlurMethod?: BlurMethod;
} & ViewProps;

export type BlurTargetViewProps = {
  ref?: React.RefObject<View | null>;
} & ViewProps;

// ----- BlurView -----

type BlurViewState = {
  blurTargetId?: number | null;
};

export class BlurView extends React.Component<BlurViewProps, BlurViewState> {
  constructor(props: BlurViewProps) {
    super(props);
    this.state = {
      blurTargetId: undefined,
    };
  }

  componentDidMount(): void {
    this._updateBlurTargetId();
    this._maybeWarnAboutBlurMethod();

    if (this.props.experimentalBlurMethod != null) {
      console.warn(
        'The `experimentalBlurMethod` prop has been deprecated. Please use the `blurMethod` prop instead.'
      );
    }
  }

  componentDidUpdate(prevProps: Readonly<BlurViewProps>): void {
    if (prevProps.blurTarget?.current !== this.props.blurTarget?.current) {
      this._updateBlurTargetId();
    }
  }

  _maybeWarnAboutBlurMethod(): void {
    const blurMethod = this._getBlurMethod();
    if (
      Platform.OS === 'android' &&
      (blurMethod === 'dimezisBlurView' ||
        blurMethod === 'dimezisBlurViewSdk31Plus') &&
      !this.props.blurTarget
    ) {
      // The fallback happens on the native side
      console.warn(
        `You have selected the "${blurMethod}" blur method, but the \`blurTarget\` prop has not been configured. The blur view will fallback to "none" blur method to avoid errors.`
      );
    }
  }

  _updateBlurTargetId = () => {
    const blurTarget = this.props.blurTarget?.current;
    const blurTargetId = blurTarget ? findNodeHandle(blurTarget) : undefined;
    this.setState(() => ({
      blurTargetId,
    }));
  };

  _getBlurMethod(): BlurMethod {
    const providedMethod =
      this.props.blurMethod ?? this.props.experimentalBlurMethod;
    return providedMethod ?? 'none';
  }

  render() {
    const {
      tint = 'default',
      intensity = 30,
      blurReductionFactor = 4,
      style,
      children,
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      blurTarget,
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      blurMethod,
      // eslint-disable-next-line @typescript-eslint/no-unused-vars
      experimentalBlurMethod,
      ...props
    } = this.props;

    return (
      <View {...props} style={[styles.container, style]}>
        <NativeBlurView
          blurTargetId={this.state.blurTargetId ?? 0}
          tint={tint}
          intensity={intensity}
          blurReductionFactor={blurReductionFactor}
          blurMethod={this._getBlurMethod()}
          style={StyleSheet.absoluteFill}
        />
        {children}
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: { backgroundColor: 'transparent' },
});

// ----- BlurTargetView -----

/**
 * A container view that can be used as a blur target on Android.
 * Wrap the content you want to blur with this view, then pass
 * a ref to it via the `blurTarget` prop of `BlurView`.
 *
 * On iOS, this is just a regular View since UIVisualEffectView
 * naturally blurs whatever is behind it.
 */
export const BlurTargetView = React.forwardRef<View, ViewProps>(
  (props, ref) => {
    if (Platform.OS === 'android') {
      return <NativeBlurTargetView {...props} ref={ref as any} />;
    }
    return <View {...props} ref={ref as any} collapsable={false} />;
  }
);

// ----- Exports -----

export type {
  BlurTint,
  BlurMethod,
  NitroBlurProps,
  NitroBlurMethods,
} from './NitroBlur.nitro';
