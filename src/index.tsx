import { getHostComponent } from 'react-native-nitro-modules';
const NitroBlurConfig = require('../nitrogen/generated/shared/json/NitroBlurConfig.json');
import type {
  NitroBlurMethods,
  NitroBlurProps,
} from './NitroBlur.nitro';

export const NitroBlurView = getHostComponent<
  NitroBlurProps,
  NitroBlurMethods
>('NitroBlur', () => NitroBlurConfig);
