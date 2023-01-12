(ns status-im2.contexts.chat.messages.content.album.view
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [status-im2.contexts.chat.messages.content.album.style :as style]
<<<<<<< HEAD
            [status-im2.constants :as constants]))
=======
            [status-im2.common.constants :as constants]
<<<<<<< HEAD
            [utils.re-frame :as rf]))
>>>>>>> cc112eeb1... updates
=======
            [utils.re-frame :as rf]
            [status-im2.contexts.chat.messages.content.image.view :as image]))
>>>>>>> 741c9b449... updates

(def max-display-count 6)

(defn border-tlr
  [index]
  (when (= index 0) 12))

(defn border-trr
  [index count album-style]
  (when (or (and (= index 1) (not= count 3))
            (and (= index 0) (= count 3) (= album-style :landscape))
            (and (= index 1) (= count 3) (= album-style :portrait)))
    12))

(defn border-blr
  [index count album-style]
  (when (or (and (= index 0) (< count 3))
            (and (= index 2) (> count 3))
            (and (= index 1) (= count 3) (= album-style :landscape))
            (and (= index 0) (= count 3) (= album-style :portrait)))
    12))

(defn border-brr
  [index count]
  (when (or (and (= index 1) (< count 3))
            (and (= index (- (min count max-display-count) 1)) (> count 2)))
    12))

(defn find-size
  [size-arr album-style]
  (if (= album-style :landscape)
    {:width (first size-arr) :height (second size-arr) :album-style album-style}
    {:width (second size-arr) :height (first size-arr) :album-style album-style}))

(defn album-message
  [{:keys [albumize?] :as message}]
  (let [shared-element-id (rf/sub [:shared-element-id])
        first-image       (first (:album message))
        album-style       (if (> (:image-width first-image) (:image-height first-image))
                            :landscape
                            :portrait)
        images-count      (count (:album message))
        ;; album images are always square, except when we have 3 images, then they must be rectangular
        ;; (portrait or landscape)
        portrait?         (and (= images-count 3) (= album-style :portrait))]
    (if (= images-count 1)
      [image/image-message 0 (first (:album message))]
      (if albumize?
        [rn/view
         {:style (style/album-container portrait?)}
         (map-indexed
          (fn [index item]
            (let [images-size-key (if (< images-count max-display-count) images-count :default)
                  size            (get-in constants/album-image-sizes [images-size-key index])
                  dimensions      (if (not= images-count 3)
                                    {:width size :height size}
                                    (find-size size album-style))]
              [rn/touchable-opacity
               {:key            (:message-id item)
                :active-opacity 1
                :on-press       (fn []
                                  (rf/dispatch [:chat.ui/update-shared-element-id (:message-id item)])
                                  (js/setTimeout #(rf/dispatch [:chat.ui/navigate-to-horizontal-images
                                                                (:album message) index])
                                                 100))}
               [fast-image/fast-image
                {:style     (merge
                             (style/image dimensions index)
                             {:border-top-left-radius     (border-tlr index)
                              :border-top-right-radius    (border-trr index images-count album-style)
                              :border-bottom-left-radius  (border-blr index images-count album-style)
                              :border-bottom-right-radius (border-brr index images-count)})
                 :source    {:uri (:image (:content item))}
                 :native-ID (when (and (= shared-element-id (:message-id item))
                                       (< index max-display-count))
                              :shared-element)}]
               (when (and (> images-count max-display-count) (= index (- max-display-count 1)))
                 [rn/view
                  {:style (merge style/overlay
                                 {:border-bottom-right-radius (border-brr index images-count)})}
                  [quo/text
                   {:weight :bold
                    :size   :heading-2
                    :style  {:color colors/white}}
                   (str "+" (- images-count (dec max-display-count)))]])]))
          (:album message))]
        [rn/view
         (map-indexed
          (fn [index item]
            [image/image-message index item])
          (:album message))]))))
