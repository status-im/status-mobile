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
    {:width (min width max-width) :height (min height max-height)}))

(defn image-message
  [index {:keys [content image-width image-height message-id] :as message} context on-long-press]
  (let [dimensions (calculate-dimensions (or image-width 1000) (or image-height 1000))
        text       (:text content)]
    (fn []
      (let [shared-element-id (rf/sub [:shared-element-id])]
        [rn/touchable-opacity
         {:active-opacity 1
          :key            message-id
          :style          {:margin-top (when (> index 0) 20)}
          :on-long-press  #(on-long-press message context)
          :on-press       (fn []
                            (rf/dispatch [:chat.ui/update-shared-element-id message-id])
                            (js/setTimeout #(rf/dispatch [:navigate-to :lightbox
                                                          {:messages [message] :index 0}])
                                           100))}
         ;; This text comp is temporary. Should later use
         ;; `status-im2.contexts.chat.messages.content.text.view`
         (when (and (not= text "placeholder") (= index 0)) [quo/text {:style {:margin-bottom 10}} text])
         [fast-image/fast-image
          {:source    {:uri (:image content)}
           :style     (merge dimensions {:border-radius 12})
           :native-ID (when (= shared-element-id message-id) :shared-element)}]]))))
