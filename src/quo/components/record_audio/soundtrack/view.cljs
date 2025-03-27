(ns quo.components.record-audio.soundtrack.view
  (:require
    [quo.components.icons.icons :as icons]
    [quo.components.record-audio.soundtrack.style :as style]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.audio-toolkit :as audio]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.slider :as slider]
    [taoensso.timbre :as log]))

(def ^:private thumb-light (icons/icon-source :thumb-light12))
(def ^:private thumb-dark (icons/icon-source :thumb-dark12))

(defn soundtrack
  [{:keys [audio-current-time-ms set-audio-current-time-ms player-ref style
           seeking-audio? set-seeking-audio max-audio-duration-ms]}]
  (let [audio-duration-ms   (min max-audio-duration-ms (audio/get-player-duration player-ref))
        theme               (quo.context/use-theme)
        on-sliding-start    (rn/use-callback #(set-seeking-audio true))
        on-sliding-complete (rn/use-callback
                             (fn [seek-time]
                               (set-seeking-audio false)
                               (audio/seek-player
                                player-ref
                                seek-time
                                #(log/debug "[record-audio] on seek - seek time: " seek-time)
                                #(log/error "[record-audio] on seek - error: " %)))
                             [player-ref])
        on-value-change     (rn/use-callback #(when seeking-audio? (set-audio-current-time-ms %))
                                             [seeking-audio?])]
    [slider/slider
     {:test-ID                  "soundtrack"
      :style                    (merge (style/player-slider-container) style)
      :minimum-value            0
      :maximum-value            audio-duration-ms
      :value                    audio-current-time-ms
      :on-sliding-start         on-sliding-start
      :on-sliding-complete      on-sliding-complete
      :on-value-change          on-value-change
      :thumb-image              (if (= theme :light) thumb-light thumb-dark)
      :minimum-track-tint-color (colors/theme-colors colors/primary-50 colors/primary-60 theme)
      :maximum-track-tint-color (colors/theme-colors
                                 (if platform/ios? colors/neutral-20 colors/neutral-40)
                                 (if platform/ios? colors/neutral-80 colors/neutral-60)
                                 theme)}]))
