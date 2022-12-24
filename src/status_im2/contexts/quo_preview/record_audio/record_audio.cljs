(ns status-im2.contexts.quo-preview.record-audio.record-audio
  (:require
   [quo2.components.record-audio.record-audio.view :as record-audio]
   [react-native.core :as rn]))

(defn cool-preview
  []
  [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
   [rn/view
    [rn/view
     {:padding-top      150
      :align-items      :center
      :background-color :transparent}
     [record-audio/input-view]]]])

(defn preview-record-audio
  []
  [rn/view {:flex 1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :never
     :scroll-enabled               false
     :header                       [cool-preview]
     :key-fn                       str}]])
