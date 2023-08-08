(ns status-im2.contexts.shell.jump-to.components.floating-screens.style
  (:require [quo2.foundations.colors :as colors]
            [react-native.reanimated :as reanimated]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]))

(defn screen
  [{:keys [screen-left screen-top screen-width screen-height screen-border-radius screen-z-index]}
   screen-id]
  (reanimated/apply-animations-to-style
   {:left          screen-left
    :top           screen-top
    :width         screen-width
    :height        screen-height
    :border-radius screen-border-radius
    :z-index       screen-z-index}
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :overflow         :hidden
    ;; KeyboardAvoidingView which is used for chat screen composer,
    ;; not working when we use :absolute layout. One fix is to add
    ;; KeyboardAvoidingView :behaviour height in android, which is also
    ;; recommended in the documentation. It fixes KeyboardAvoidingView but
    ;; the pushing of views by the keyboard is not smooth & while animating it creates a weird jump.
    :position         (if (= screen-id shell.constants/chat-screen) :relative :absolute)
    :flex             1}))

(defn screen-container
  [{:keys [width height]}]
  {:width  width
   :flex   1
   :height height})
