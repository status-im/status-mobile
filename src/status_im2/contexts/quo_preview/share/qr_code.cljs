(ns status-im2.contexts.quo-preview.share.qr-code
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [status-im2.common.resources :as resources]))

(defn cool-preview
  []
  (fn []
    [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
     [rn/view {:padding-bottom 150}
      [rn/view {:flex 1}]
      [rn/view
       {:padding-vertical 60
        :flex-direction   :row
        :justify-content  :center}
       [quo/qr-code
        {:source (resources/get-mock-image :qr-code)
         :height 250
         :width  250}]]]]))

(defn preview-qr-code
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
