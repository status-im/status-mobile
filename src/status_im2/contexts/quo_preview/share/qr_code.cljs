(ns status-im2.contexts.quo-preview.share.qr-code
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [quo2.core :as quo]
            [utils.image-server :as image-server]
            [utils.re-frame :as rf]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "URL For QR"
    :key   :text
    :type  :text}
   {:label   "Error Correction Level:"
    :key     :error-correction-level
    :type    :select
    :options [{:key   :low
               :value "Low"}
              {:key   :medium
               :value "Medium"}
              {:key   :quart
               :value "Quart"}
              {:key   :highest
               :value "Highest"}]}])

(defn cool-preview
  []
  (let [state                  (reagent/atom {:text                   "https://status.im"
                                              :error-correction-level :highest})
        text                   (reagent/cursor state [:text])
        error-correction-level (reagent/cursor state [:error-correction-level])
        media-server-uri       (reagent/atom "")]
    (fn []
      (reset! media-server-uri (image-server/get-qr-image-uri-for-any-url
                                {:url         @text
                                 :port        (rf/sub [:mediaserver/port])
                                 :error-level @error-correction-level
                                 :qr-size     250}))
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:style {:padding-bottom 150}}
        [preview/customizer state descriptor]
        [rn/view {:style {:flex 1}}]
        [rn/view
         {:style {:padding-vertical 60
                  :flex-direction   :row
                  :justify-content  :center}}
         [rn/view
          [quo/qr-code
           {:source {:uri @media-server-uri}
            :height 250
            :width  250}]

          [rn/view
           [rn/text {:style {:padding 20 :flex-shrink 1}} "Media server url -> "
            @media-server-uri]]]]]])))

(defn preview-qr-code
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
