(ns status-im2.contexts.chat.images-horizontal.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.chat.messages.content.album.style :as style]
            [status-im2.common.constants :as constants]
            [reagent.core :as reagent]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]))

(defn image-set-size
  [dimensions]
  (fn [evt]
    (let [width      (.-width (.-nativeEvent evt))
          height     (.-height (.-nativeEvent evt))
          max-width  (:width (rn/get-window))
          max-height (* 1.25 (:width (rn/get-window)))]
      (if (> height width)
        (let [calculated-height (* height (/ max-width width))]
          (reset! dimensions {:width max-width :height calculated-height :loaded true}))
        (let [calculated-height (* height (/ max-width width))]
          (reset! dimensions {:width max-width :height calculated-height :loaded true}))))))

(defn image [message index _ {:keys [dimensions-arr shared-element-id]}]
  (let [dimensions (nth dimensions-arr index)]
     [fast-image/fast-image
      {:source   {:uri (:image (:content message))}
       :on-error #(swap! dimensions assoc :error true)
       :style    {:width         (:width (rn/get-window))
                  :height        (* (:height dimensions) (/ (:width (rn/get-window)) (:width dimensions)))
                  :border-radius 12}
       :nativeID (when (= shared-element-id (:message-id message)) :shared-element)}]
    ))

(defn images-horizontal
  []
  (let [{:keys [messages dimensions]} (rf/sub [:get-screen-params])
        shared-element-id (rf/sub [:shared-element-id])]
    (rn/set-status-bar-style "light-content")
    [safe-area/consumer
     (fn [insets]
       [rn/view
        {:style {:background-color "#000"
                 :flex             1
                 :padding-top      (:top insets)}}
        [rn/view {:style {:position :absolute
                          :left     20
                          :top      (+ 12 (:top insets))
                          :z-index  1
                          }}
         [rn/touchable-opacity {:active-opacity 1
                                :on-press       #(rf/dispatch [:navigate-back])
                                :style          {:width            32
                                                 :height           32
                                                 :border-radius    12
                                                 :justify-content  :center
                                                 :align-items      :center
                                                 :background-color colors/neutral-80-opa-40}}
          [quo/icon :close {:size 20 :color colors/white}]]]
        [rn/flat-list
         {:key-fn      :message-id
          :data        messages
          :render-fn   image
          :render-data {:dimensions-arr dimensions
                        :shared-element-id shared-element-id}
          :horizontal true
          :content-container-style {:width "100%"
                                    :height "100%"
                                    :justify-content :center
                                    :align-items :center}}]])]))


