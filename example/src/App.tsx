import { BlurTargetView, BlurView } from '@abeman/react-native-nitro-blur';
import { useRef } from 'react';
import {
  Dimensions,
  Image,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';
import Animated, {
  useAnimatedStyle,
  useSharedValue,
  withTiming,
} from 'react-native-reanimated';

const { width, height } = Dimensions.get('window');

// createAnimatedComponent works for style animations (transform, opacity, etc.)
const AnimatedBlurView = Animated.createAnimatedComponent(BlurView);

export default function App() {
  const targetRef = useRef<View>(null);
  const text = 'Hello, my container is blurring contents underneath!';

  const progress = useSharedValue(1);

  const toggleIntensity = () => {
    progress.set(() => withTiming(progress.value === 0 ? 1 : 0));
  };

  // Style animations (transform, opacity) work normally via useAnimatedStyle
  const animatedStyle = useAnimatedStyle(() => ({
    opacity: progress.value * 1,
  }));

  return (
    <View style={styles.outerContainer}>
      <Pressable onPress={toggleIntensity} style={styles.button}>
        <Text>Toggle blur</Text>
      </Pressable>
      <View style={styles.container}>
        <BlurTargetView ref={targetRef} style={styles.background}>
          <Image
            resizeMode={'stretch'}
            source={{
              uri: 'https://fastly.picsum.photos/id/198/200/300.webp?hmac=LcV-OWkhrfFoUHE4As1fN7zC9-NhjDsbmjK0SyL5oV8',
            }}
            style={styles.backgroundImage}
          />
        </BlurTargetView>
        <AnimatedBlurView
          blurTarget={targetRef}
          intensity={30}
          style={[styles.blurContainer, animatedStyle]}
          blurMethod="dimezisBlurViewSdk31Plus"
        >
          <Text style={styles.text}>{text}</Text>
        </AnimatedBlurView>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  outerContainer: {
    flex: 1,
    paddingTop: 64,
  },
  button: {
    padding: 16,
    backgroundColor: 'orangered',
    alignSelf: 'center',
  },
  container: {
    flex: 1,
  },
  blurContainer: {
    flex: 1,
    padding: 20,
    margin: 16,
    justifyContent: 'center',
    overflow: 'hidden',
    borderRadius: 20,
  },
  background: {
    flex: 1,
    flexWrap: 'wrap',
    ...StyleSheet.absoluteFill,
  },
  backgroundImage: {
    width,
    height,
  },
  text: {
    fontSize: 24,
    fontWeight: '600',
  },
});
