(ns status-im2.contexts.chat.images-horizontal.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [utils.re-frame :as rf]
            [react-native.safe-area :as safe-area]
            [reagent.core :as reagent]
            [oops.core :as oops]
            [status-im2.contexts.chat.images-horizontal.style :as style]))

(defn image
  [message]
  (let [shared-element-id (rf/sub [:shared-element-id])
        window-width      (:width (rn/get-window))
        height            (* (:image-height message) (/ window-width (:image-width message)))]
    [fast-image/fast-image
     {:source    {:uri (:image (:content message))}
      :style     {:width         window-width
                  :height        height
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
        ;; The initial value of data is the image that was pressed (and not the whole album) in order for
        ;; the transition
        ;; animation to execute properly, otherwise it would animate towards outside the screen (even if
        ;; we have `initialScrollIndex` set).
        data                     (reagent/atom [(nth messages index)])
        flat-list-ref            (atom nil)]
    (reset! data messages)
    ;; We use setTimeout to enqueue `scrollToIndex` until the `data` has been updated.
    (js/setTimeout #(.scrollToIndex ^js @flat-list-ref #js {:animated false :index index}) 0)
    [safe-area/consumer
     (fn [insets]
       [rn/view
        {:style (style/container-view (:top insets))}
        [rn/view
         {:style (style/top-view-container (:top insets))}
         [rn/touchable-opacity
          {:active-opacity 1
           :on-press       #(rf/dispatch [:navigate-back])
           :style          style/close-container}
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
