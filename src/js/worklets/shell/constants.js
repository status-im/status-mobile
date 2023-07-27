import { Easing } from 'react-native-reanimated';

// Home Stack States
export const CLOSE_WITHOUT_ANIMATION = 0;
export const OPEN_WITHOUT_ANIMATION = 1;
export const CLOSE_WITH_ANIMATION = 2;
export const OPEN_WITH_ANIMATION = 3;

// Floating Screen States
export const CLOSE_SCREEN_WITHOUT_ANIMATION = 0;
export const OPEN_SCREEN_WITHOUT_ANIMATION = 1;
export const CLOSE_SCREEN_WITH_SLIDE_ANIMATION = 2;
export const OPEN_SCREEN_WITH_SLIDE_ANIMATION = 3;
export const CLOSE_SCREEN_WITH_SHELL_ANIMATION = 4;
export const OPEN_SCREEN_WITH_SHELL_ANIMATION = 5;

export const SHELL_ANIMATION_TIME = 200;

export const LINEAR_EASING = {
  duration: SHELL_ANIMATION_TIME,
  easing: Easing.bezier(0, 0, 1, 1),
};

export const EASE_OUT_EASING = {
  duration: SHELL_ANIMATION_TIME,
  easing: Easing.bezier(0, 0, 0.58, 1),
};
