(ns status-im2.contexts.chat.images-horizontal.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [oops.core :as oops]))

(defn image
  [message]
  (let [shared-element-id (rf/sub [:shared-element-id])]
    [fast-image/fast-image
     {:source    {:uri (:image (:content message))}
      :style     {:width         (:width (rn/get-window))
                  :height        (* (:image-height message)
                                    (/ (:width (rn/get-window)) (:image-width message)))
                  :border-radius 12}
      :native-ID (when (= shared-element-id (:message-id message)) :shared-element)}]))

(defn get-item-layout
  [_ index]
  #js {:length (:width (rn/get-window)) :offset (* (:width (rn/get-window)) index) :index index})

(defn on-viewable-items-changed
  [e]
  (rf/dispatch [:chat.ui/update-shared-element-id
                (:message-id (oops/oget (first (oops/oget e "changed")) "item"))]))


(defn images-horizontal
  []
  (let [{:keys [messages index]} (rf/sub [:get-screen-params])
        data                     (reagent/atom [(nth messages index)])
        flat-list-ref            (atom nil)]
    (reset! data messages)
    (.then (new js/Promise (fn [resolve] (js/setTimeout resolve 0)))
           (fn [] (.scrollToIndex ^js @flat-list-ref #js {:animated false :index index})))
    (rn/set-status-bar-style "light-content")
    [safe-area/consumer
     (fn [insets]
       [rn/view
        {:style {:background-color "#000"
                 :height           "100%"
                 :padding-top      (:top insets)}}
        [rn/view
         {:style {:position :absolute
                  :left     20
                  :top      (+ 12 (:top insets))
                  :z-index  1
                 }}
         [rn/touchable-opacity
          {:active-opacity 1
           :on-press       #(rf/dispatch [:navigate-back])
           :style          {:width            32
                            :height           32
                            :border-radius    12
                            :justify-content  :center
                            :align-items      :center
                            :background-color colors/neutral-80-opa-40}}
          [quo/icon :close {:size 20 :color colors/white}]]]
        [rn/flat-list
         {:ref                       #(reset! flat-list-ref %)
          :key-fn                    :message-id
          :data                      @data
          :render-fn                 image
          :horizontal                true
          :paging-enabled            true
          :get-item-layout           get-item-layout
          :viewability-config        {:view-area-coverage-percent-threshold 95}
          :on-viewable-items-changed on-viewable-items-changed
          :content-container-style   {:justify-content :center
                                      :align-items     :center}}]])]))
