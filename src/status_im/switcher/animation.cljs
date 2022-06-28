(ns status-im.switcher.animation
  (:require [quo2.reanimated :as reanimated]
            [status-im.switcher.constants :as constants]))

;;;; Switcher Animations

;; Component Animations
(defn switcher-touchable-on-press-in
  [touchable-scale]
  (reanimated/animate-shared-value-with-timing touchable-scale constants/switcher-pressed-scale 300 :easing1))

(defn switcher-touchable-on-press-out [switcher-opened? view-id shared-values]
  (let [{:keys [width height]}       (constants/dimensions)
        switcher-bottom-position     (constants/switcher-pressed-bottom-position view-id)
        switcher-target-radius       (Math/hypot
                                      (/ width 2)
                                      (- height constants/switcher-pressed-radius switcher-bottom-position))
        switcher-size                (* 2 switcher-target-radius)]
    (reanimated/animate-shared-value-with-timing (:button-touchable-scale shared-values) 1 300 :easing1)
    (if @switcher-opened?
      (do
        (reanimated/animate-shared-value-with-timing (:switcher-button-opacity shared-values) 1 300 :easing1)
        (reanimated/animate-shared-value-with-timing (:switcher-screen-size shared-values) constants/switcher-pressed-size 300 :linear)
        (reanimated/animate-shared-value-with-timing (:switcher-container-scale shared-values) 0.9 300 :linear))
      (do
        (reanimated/animate-shared-value-with-timing (:switcher-button-opacity shared-values) 0 300 :easing1)
        (reanimated/animate-shared-value-with-timing (:switcher-screen-size shared-values) switcher-size 300 :linear)
        (reanimated/animate-shared-value-with-timing (:switcher-container-scale shared-values) 1 300 :linear)))
    (swap! switcher-opened? not)))

;; Derived Values

(defn switcher-close-button-opacity [switcher-button-opacity]
  (.switcherCloseButtonOpacity ^js reanimated/worklet-factory switcher-button-opacity))

(defn switcher-screen-radius [switcher-screen-size]
  (.switcherScreenRadius ^js reanimated/worklet-factory switcher-screen-size))

(defn switcher-screen-bottom-position [switcher-screen-radius view-id]
  (.switcherScreenBottomPosition ^js reanimated/worklet-factory
                                 switcher-screen-radius
                                 constants/switcher-pressed-radius
                                 (constants/switcher-pressed-bottom-position view-id)))

(defn switcher-container-bottom-position [switcher-screen-bottom]
  (.switcherContainerBottomPosition ^js reanimated/worklet-factory
                                    switcher-screen-bottom
                                    (+ constants/switcher-container-height-padding
                                       constants/switcher-height-offset)))


;;;; Bottom Tabs & Home Stack Animations


(defn bottom-tab-on-press [shared-values selected-stack-id]
  (doseq [id constants/stacks-ids]
    (let [selected-tab?              (= id selected-stack-id)
          tab-opacity-shared-value   (get shared-values (get constants/tabs-opacity-keywords id))
          stack-opacity-shared-value (get shared-values (get constants/stacks-opacity-keywords id))
          stack-pointer-shared-value (get shared-values (get constants/stacks-pointer-keywords id))]
      (reanimated/animate-shared-value-with-timing tab-opacity-shared-value (if selected-tab? 1 0) 300 :easing3)
      (reanimated/set-shared-value stack-pointer-shared-value (if selected-tab? "auto" "none"))
      (if selected-tab?
        (reanimated/animate-shared-value-with-delay stack-opacity-shared-value 1 300 :easing3 150)
        (reanimated/animate-shared-value-with-timing stack-opacity-shared-value 0 300 :easing3)))))
