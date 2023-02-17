(ns status-im2.contexts.chat.messages.content.image.view
  (:require
   [quo2.core :as quo]
   [react-native.core :as rn]
   [react-native.fast-image :as fast-image]
   [status-im2.constants :as constants]
   [utils.re-frame :as rf]))

(defn calculate-dimensions
  [width height]
  (let [max-width  (if (> width height) (* 2 constants/image-size) (* 1.5 constants/image-size))
        max-height (if (> width height) (* 1.5 constants/image-size) (* 2 constants/image-size))]
    (if (> height width)
      (let [calculated-height (* (min height max-height) (/ (max width max-width) width))
            calculated-width  (* (max width max-width) (/ (min height max-height) height))]
        {:width calculated-width :height calculated-height})
      (let [calculated-height (* (max height max-height) (/ (min width max-width) width))
            calculated-width  (* (min width max-width) (/ (max height max-height) height))]
        {:width calculated-width :height calculated-height}))))

(defn image-message
  [_ {:keys [content image-width image-height message-id] :as message} context on-long-press]
  (let [dimensions (calculate-dimensions (or image-width 1000) (or image-height 1000))
        text       (:text content)]
    (fn []
      (let [shared-element-id (rf/sub [:shared-element-id])]
        [rn/touchable-opacity
         {:active-opacity 1
          :key            message-id
          :on-long-press  #(on-long-press message context)
          :on-press       (fn []
                            (rf/dispatch [:chat.ui/update-shared-element-id message-id])
                            (js/setTimeout #(rf/dispatch [:navigate-to :lightbox
                                                          {:messages [message] :index 0}])
                                           100))}
         ;; This text comp is temporary. Should later use
         ;; `status-im2.contexts.chat.messages.content.text.view`
         (when (not= text "placeholder") [quo/text {:style {:margin-bottom 10}} text])
         [fast-image/fast-image
          {:source    {:uri (:image content)}
           :style     (merge dimensions {:border-radius 12})
           :native-ID (when (= shared-element-id message-id) :shared-element)}]]))))
