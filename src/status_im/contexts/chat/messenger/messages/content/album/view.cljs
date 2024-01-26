(ns status-im.contexts.chat.messenger.messages.content.album.view
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.messages.content.album.style :as style]
    [status-im.contexts.chat.messenger.messages.content.image.view :as image]
    [status-im.contexts.chat.messenger.messages.content.lightbox.utils :as lightbox-utils]
    [status-im.contexts.chat.messenger.messages.content.lightbox.view :as lightbox]
    [status-im.contexts.chat.messenger.messages.content.text.view :as text]
    [utils.re-frame :as rf]
    [utils.url :as url]))

(def rectangular-style-count 3)

(defn find-size
  [size-arr album-style]
  (if (= album-style :landscape)
    {:width (first size-arr) :height (second size-arr) :album-style album-style}
    {:width (second size-arr) :height (first size-arr) :album-style album-style}))

(defn album-message
  [{:keys [albumize?] :as message} context on-long-press message-container-data]
  (let [animation-shared-element-id (rf/sub [:animation-shared-element-id])
        media-server-port           (rf/sub [:mediaserver/port])
        album-messages              (:album message)
        first-image                 (first album-messages)
        album-style                 (if (> (:image-width first-image) (:image-height first-image))
                                      :landscape
                                      :portrait)
        images-count                (count album-messages)
        ;; album images are always square, except when we have 3 images, then they must be
        ;; rectangular
        ;; (portrait or landscape)
        portrait?                   (and (= images-count rectangular-style-count)
                                         (= album-style :portrait))]
    (if (and albumize? (> images-count 1))
      [:<>
       [rn/view {:style {:margin-bottom 4}} [text/text-content first-image]]
       [rn/view
        {:style               (style/album-container portrait?)
         :accessible          true
         :accessibility-label :image-container}
        (map-indexed
         (fn [index item]
           (let [images-size-key (if (< images-count constants/max-album-photos)
                                   images-count
                                   :default)
                 size            (get-in constants/album-image-sizes [images-size-key index])
                 dimensions      (if (not= images-count rectangular-style-count)
                                   {:width size :height size}
                                   (find-size size album-style))]
             [rn/touchable-opacity
              {:key            (:message-id item)
               :active-opacity 1
               :on-long-press  #(on-long-press message context)
               :on-press       #(rf/dispatch
                                 [:lightbox/navigate-to-lightbox
                                  (:message-id item)
                                  {:index index
                                   :images (into []
                                                 (map
                                                  lightbox-utils/convert-message-to-lightbox-image
                                                  album-messages))
                                   :bottom-text-component
                                   [lightbox/bottom-text-for-lightbox
                                    first-image]
                                   :options-drawer-component lightbox/drawer}])}
              [fast-image/fast-image
               {:style     (style/image dimensions index portrait? images-count)
                :source    {:uri (url/replace-port (:image (:content item)) media-server-port)}
                :native-ID (when (and (= animation-shared-element-id (:message-id item))
                                      (< index constants/max-album-photos))
                             :shared-element)}]
              (when (and (> images-count constants/max-album-photos)
                         (= index (dec constants/max-album-photos)))
                [rn/view
                 {:style style/overlay}
                 [quo/text
                  {:weight :bold
                   :size   :heading-2
                   :style  {:color colors/white}}
                  (str "+" (- images-count (dec constants/max-album-photos)))]])]))
         album-messages)]]
      [:<>
       (map-indexed
        (fn [index item]
          [:<> {:key (:message-id item)}
           [image/image-message index item {:on-long-press #(on-long-press message context)}
            message-container-data]])
        album-messages)])))
