(ns status-im.ui.screens.chat.components.hooks
  (:require [quo.platform :as platform]
            [quo.react :as react]
            [quo.react-native :refer [use-window-dimensions] :as rn]
            [react-native.safe-area :as safe-area]))

(def ^:private keyboard-change-event (if platform/android? "keyboardDidShow" "keyboardWillChangeFrame"))

(def default-kb-height (if platform/ios? 258 272))
(def min-duration 100)

(defn use-keyboard-dimension
  []
  (let [{:keys [height]}  (use-window-dimensions)
        bottom            (safe-area/get-bottom)
        keyboard-listener (atom nil)
        keyboard          (react/state
                           {:height       0
                            :duration     min-duration
                            :end-position height
                            :max-height   (+ (if platform/ios? bottom 0) default-kb-height)})]
    (react/effect!
     (fn []
       (letfn
         [(dimensions-change [evt]
            (swap! keyboard assoc :end-position (-> ^js evt .-window .-height)))
          (keyboard-dimensions [evt]
            (let [duration   (.-duration ^js evt)
                  easing     (.-easing ^js evt)
                  screen-y   (-> ^js evt .-endCoordinates .-screenY)
                  new-height (- height screen-y)]
              (when-not (= new-height (:height @keyboard))
                (when (and duration easing platform/ios?)
                  (rn/configure-next
                   #js
                    {:duration (max min-duration duration)
                     :update   #js
                                {:duration (max min-duration duration)
                                 :type     (-> ^js rn/layout-animation .-Types (aget easing))}})))
              (reset! keyboard {:height       new-height
                                :end-position screen-y
                                :duration     (max min-duration duration)
                                :max-height   (max new-height (:max-height @keyboard))})))]
         (.addEventListener rn/dimensions "change" dimensions-change)
         (reset! keyboard-listener (.addListener rn/keyboard keyboard-change-event keyboard-dimensions))
         (fn []
           (.removeEventListener rn/dimensions "change" dimensions-change)
           (some-> ^js @keyboard-listener
                   .remove)))))
    @keyboard))
