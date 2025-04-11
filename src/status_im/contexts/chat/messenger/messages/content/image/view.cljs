(ns status-im.contexts.chat.messenger.messages.content.image.view
  (:require
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [react-native.safe-area :as safe-area]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.messages.content.lightbox.utils :as lightbox-utils]
    [status-im.contexts.chat.messenger.messages.content.lightbox.view :as lightbox]
    [status-im.contexts.chat.messenger.messages.content.text.view :as text]
    [utils.re-frame :as rf]
    [utils.url :as url]))

;; these constants are used when there is no width or height defined for a specific image
(def ^:const fallback-image-width 1000)
(def ^:const fallback-image-height 1000)

(defn calculate-dimensions
  [width height max-container-width max-container-height]
  (let [max-width  (if (> width height) max-container-width (* 1.5 constants/image-size))
        max-height (if (> width height) max-container-height (* 2 constants/image-size))]
    {:width (min width max-width) :height (min height max-height)}))

(defn image-message
  [index
   {:keys [content image-width image-height message-id]
    :as   message
    :or   {image-width  fallback-image-width
           image-height fallback-image-height}}
   {:keys [on-long-press]}
   message-container-data]
  (let [{:keys [window-width padding-left padding-right avatar-container-width
                message-margin-left]} message-container-data
        max-container-width           (- window-width
                                         padding-left
                                         padding-right
                                         avatar-container-width
                                         message-margin-left)
        max-container-height          (* (/ image-height image-width) max-container-width)
        dimensions                    (calculate-dimensions image-width
                                                            image-height
                                                            max-container-width
                                                            max-container-height)
        animation-shared-element-id   (rf/sub [:animation-shared-element-id])
        image-local-url               (url/replace-port (:image content) (rf/sub [:mediaserver/port]))]
    [:<>
     (when (= index 0)
       [text/text-content message])
     [rn/touchable-opacity
      {:active-opacity 1
       :style          {:margin-top 4}
       :on-long-press  on-long-press
       :on-press       #(rf/dispatch [:lightbox/navigate-to-lightbox
                                      message-id
                                      {:images [(lightbox-utils/convert-message-to-lightbox-image
                                                 message)]
                                       :index 0
                                       :insets safe-area/insets
                                       :bottom-text-component
                                       [lightbox/bottom-text-for-lightbox message]
                                       :on-options-press (fn [images index]
                                                           (rf/dispatch [:show-bottom-sheet
                                                                         {:content (fn []
                                                                                     [lightbox/drawer
                                                                                      images
                                                                                      index])}]))}])}
      [fast-image/fast-image
       {:source              {:uri image-local-url}
        :style               (merge dimensions {:border-radius 12})
        :native-ID           (when (= animation-shared-element-id message-id) :shared-element)
        :accessibility-label :image-message}]]]))
