(ns status-im.ui.screens.chat.photos
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as style]
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

(defn member-photo [photo-path]
  (photo photo-path
         {:accessibility-label :member-photo
          :size                style/default-size}))
