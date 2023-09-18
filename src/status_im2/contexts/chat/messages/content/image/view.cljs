(ns status-im2.contexts.chat.messages.content.image.view
  (:require
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.safe-area :as safe-area]
    [status-im2.constants :as constants]
    [utils.re-frame :as rf]
    [status-im2.contexts.chat.messages.content.text.view :as text]
    [utils.url :as url]))

(defn calculate-dimensions
  [width height max-container-width max-container-height]
  (let [max-width  (if (> width height) max-container-width (* 1.5 constants/image-size))
        max-height (if (> width height) max-container-height (* 2 constants/image-size))]
    {:width (min width max-width) :height (min height max-height)}))

(defn image-message
  [index {:keys [content image-width image-height message-id] :as message} {:keys [on-long-press]} message-container-data]
  (let [insets            (safe-area/get-insets)
        max-width         (- (:window-width            message-container-data)
                             (:padding-horizontal      message-container-data)
                             (:avatar-container-width  message-container-data)
                             (:message-margin-left     message-container-data))
        max-height        (* (/ image-height image-width) max-width)
        dimensions        (calculate-dimensions image-width image-height max-width max-height)
        shared-element-id (rf/sub [:shared-element-id])
        image-local-url   (url/replace-port (:image content) (rf/sub [:mediaserver/port]))]
    [:<>
     (when (= index 0)
       [text/text-content message])
     [rn/touchable-opacity
      {:active-opacity 1
       :style          {:margin-top 4}
       :on-long-press  on-long-press
       :on-press       #(rf/dispatch [:chat.ui/navigate-to-lightbox
                                      message-id
                                      {:messages [message]
                                       :index    0
                                       :insets   insets}])}
      [fast-image/fast-image
       {:source              {:uri image-local-url}
        :style               (merge dimensions {:border-radius 12})
        :native-ID           (when (= shared-element-id message-id) :shared-element)
        :accessibility-label :image-message}]]]))
