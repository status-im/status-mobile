(ns status-im2.contexts.chat.images-horizontal.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]))

(defn image [message index _ {:keys [shared-element-id]}]
  [rn/view {:style  {
                     ;:width "100%"
                     ;:height "100%"
                     :justify-content :center
                     :align-items :center}}
   [fast-image/fast-image
    {:source   {:uri (:image (:content message))}
     :style    {:width         (:width (rn/get-window))
                :height        (* (:image-height message) (/ (:width (rn/get-window)) (:image-width message)))
                :border-radius 12}
     :nativeID (when (= shared-element-id (:message-id message)) :shared-element)}]]

  )


(defn get-item-layout
  [_ index]
  #js {:length (:width (rn/get-window)) :offset (* (:width (rn/get-window)) index) :index index})

(defn images-horizontal
  []
  (let [{:keys [messages index]} (rf/sub [:get-screen-params])
        shared-element-id (rf/sub [:shared-element-id])]
    (rn/set-status-bar-style "light-content")
    [safe-area/consumer
     (fn [insets]
       [rn/view
        {:style {:background-color "#000"
                 :padding-top      (:top insets)
                 :height "100%"}}
        [rn/view {:style {:position :absolute
                          :left     20
                          :top      (+ 12 (:top insets))
                          :z-index  1}}
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
          :render-data {:shared-element-id shared-element-id}
          :horizontal true
          :paging-enabled true
          :initial-scroll-index index
          :get-item-layout get-item-layout}]
        ])]))
