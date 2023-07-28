(ns quo2.components.record-audio.soundtrack.view
  (:require [quo2.components.record-audio.soundtrack.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.audio-toolkit :as audio]
            [taoensso.timbre :as log]
            [react-native.platform :as platform]
            [react-native.slider :as slider]))

(def ^:private thumb-light (js/require "../resources/images/icons2/12x12/thumb-light.png"))
(def ^:private thumb-dark (js/require "../resources/images/icons2/12x12/thumb-dark.png"))

(defn f-soundtrack
  [{:keys [audio-current-time-ms player-ref style seeking-audio? max-audio-duration-ms]}]
  (let [audio-duration-ms (min max-audio-duration-ms (audio/get-player-duration player-ref))]
    [:<>
     [slider/slider
      {:test-ID                  "soundtrack"
       :style                    (merge
                                  (style/player-slider-container)
                                  (or style {}))
       :minimum-value            0
       :maximum-value            audio-duration-ms
       :value                    @audio-current-time-ms
       :on-sliding-start         #(reset! seeking-audio? true)
       :on-sliding-complete      (fn [seek-time]
                                   (reset! seeking-audio? false)
                                   (audio/seek-player
                                    player-ref
                                    seek-time
                                    #(log/debug "[record-audio] on seek - seek time: " seek-time)
                                    #(log/error "[record-audio] on seek - error: " %)))
       :on-value-change          #(when @seeking-audio?
                                    (reset! audio-current-time-ms %))
       :thumb-image              (if (colors/dark?) thumb-dark thumb-light)
       :minimum-track-tint-color (colors/theme-colors colors/primary-50 colors/primary-60)
       :maximum-track-tint-color (colors/theme-colors
                                  (if platform/ios? colors/neutral-20 colors/neutral-40)
                                  (if platform/ios? colors/neutral-80 colors/neutral-60))}]]))
