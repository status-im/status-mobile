(ns status-im2.contexts.chat.messages.content.image.view
  (:require [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [reagent.core :as reagent]
            [status-im2.navigation.events :as navigation]
            [status-im2.navigation.state :as state]
            [utils.re-frame :as rf]))

(defn image-set-size
  [dimensions]
  (fn [evt]
    (let [width      (.-width (.-nativeEvent evt))
          height     (.-height (.-nativeEvent evt))
          max-width  (if (> width height) 320 190)
          max-height (if (> width height) 190 320)]
      (if (> height width)
        (let [calculated-height (* (min height max-height) (/ (max width max-width) width))
              calculated-width  (* (max width max-width) (/ (min height max-height) height))]
          (reset! dimensions {:width calculated-width :height calculated-height :loaded true}))

        (let [calculated-height (* (max height max-height) (/ (min width max-width) width))
              calculated-width  (* (min width max-width) (/ (max height max-height) height))]
          (reset! dimensions {:width calculated-width :height calculated-height :loaded true}))))))


(defn image-message
  [{:keys [content] :as message}]
  (let [dimensions (reagent/atom {:width 320 :height 320 :loaded false})]
    (fn []
      (let [style-opts {:outgoing false
                        :opacity  (if (:loaded @dimensions) 1 0)
                        :width    (:width @dimensions)
                        :height   (:height @dimensions)
                        :border-radius 12}
            shared-element-id (rf/sub [:shared-element-id])]
        [rn/touchable-opacity
         {:active-opacity 1
          ;:on-press #(rf/dispatch [:chat.ui/navigate-to-horizontal-images [message] [@dimensions] (:message-id message)])
          :on-press       (fn []
                            ;(swap! state/shared-element assoc :id (:message-id message))
                            (rf/dispatch [:chat.ui/update-shared-element-id (:message-id message)])
                            (rf/dispatch [:chat.ui/navigate-to-horizontal-images [message] [@dimensions] (:message-id message)]))
          }
         [fast-image/fast-image
          {:source   {:uri (:image content)}
           :on-load  (image-set-size dimensions)
           :on-error #(swap! dimensions assoc :error true)
           :style    (dissoc style-opts :outgoing)
           :nativeID (when (= shared-element-id (:message-id message)) "xyz")}]]))))
