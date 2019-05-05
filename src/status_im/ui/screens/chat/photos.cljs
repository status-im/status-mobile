(ns status-im.ui.screens.chat.photos
  (:require [clojure.string :as string]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as style]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.image :as utils.image])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defn photo [photo-path {:keys [size
                                accessibility-label]}]
  [react/view {:style (style/photo-container size)}
   [react/image {:source              (utils.image/source photo-path)
                 :style               (style/photo size)
                 :accessibility-label (or accessibility-label :chat-icon)}]
   [react/view {:style (style/photo-border size)}]])

(defview member-photo [from & [size]]
  (letsubs [photo-path [:chats/photo-path from]]
    (photo (if (string/blank? photo-path)
             (identicon/identicon from)
             photo-path)
           {:accessibility-label :member-photo
            :size                (or size style/default-size)})))
