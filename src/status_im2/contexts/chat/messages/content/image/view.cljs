(ns status-im2.contexts.chat.messages.content.image.view
  (:require [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [utils.re-frame :as rf]))

(defn calculate-dimensions
  [width height]
  (let [max-width  (if (> width height) 320 190)
        max-height (if (> width height) 190 320)]
    (if (> height width)
      (let [calculated-height (* (min height max-height) (/ (max width max-width) width))
            calculated-width  (* (max width max-width) (/ (min height max-height) height))]
        {:width calculated-width :height calculated-height})
      (let [calculated-height (* (max height max-height) (/ (min width max-width) width))
            calculated-width  (* (min width max-width) (/ (max height max-height) height))]
        {:width calculated-width :height calculated-height}))))

(defn image-message
  [index {:keys [content image-width image-height message-id] :as message}]
  (let [dimensions (calculate-dimensions (or image-width 1000) (or image-height 1000))
        text       (:text content)]
    (fn []
      (let [shared-element-id (rf/sub [:shared-element-id])]
        [rn/touchable-opacity
         {:active-opacity 1
          :style          {:margin-top (when (> index 0) 20)}
          :on-press       (fn []
                            (rf/dispatch [:chat.ui/update-shared-element-id message-id])
                            (js/setTimeout #(rf/dispatch [:chat.ui/navigate-to-horizontal-images
                                                          [message] 0])
                                           100))}
         ;; This text comp is temporary. Should later use
         ;; `status-im2.contexts.chat.messages.content.text.view`
         (when (and (not= text "placeholder") (= index 0)) [rn/text text])
         [rn/image
          {:source    {:uri (:image content)}
           :style     (merge dimensions {:border-radius 12})
           :native-ID (when (= shared-element-id message-id) :shared-element)}]]))))
