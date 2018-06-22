(ns status-im.chat.views.photos
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.chat.styles.photos :as style]
            [status-im.utils.identicon :as identicon]
            [clojure.string :as string]
            [status-im.react-native.resources :as resources]))

(defn- source [photo-path]
  (if (and (not (string/blank? photo-path))
           (string/starts-with? photo-path "contacts://"))
    (->> (string/replace photo-path #"contacts://" "")
         (keyword)
         (get resources/contacts))
    {:uri photo-path}))

(defn photo [photo-path {:keys [size
                                accessibility-label]}]
  [react/view {:style (style/photo-container size)}
   [react/image {:source              (source photo-path)
                 :style               (style/photo size)
                 :accessibility-label (or accessibility-label :chat-icon)}]
   [react/view {:style (style/photo-border size)}]])

(defview member-photo [from]
  (letsubs [photo-path [:get-photo-path from]]
    (photo (if (string/blank? photo-path)
             (identicon/identicon from)
             photo-path)
           {:accessibility-label :member-photo
            :size                style/default-size})))
