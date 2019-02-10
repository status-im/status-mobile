(ns status-im.ui.screens.chat.photos
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as style]
            [status-im.utils.identicon :as identicon]
            [clojure.string :as string]
            [status-im.utils.image :as utils.image]))

(defn photo [photo-path {:keys [size
                                accessibility-label]}]
  [react/view {:style (style/photo-container size)}
   [react/image {:source              (utils.image/source photo-path)
                 :style               (style/photo size)
                 :accessibility-label (or accessibility-label :chat-icon)}]
   [react/view {:style (style/photo-border size)}]])

(defview member-photo [from]
  (letsubs [photo-path [:chats/photo-path from]]
    (photo (if (string/blank? photo-path)
             (identicon/identicon from)
             photo-path)
           {:accessibility-label :member-photo
            :size                style/default-size})))
