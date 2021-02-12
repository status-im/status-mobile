(ns status-im.ui.screens.chat.photos
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as style]
            [status-im.profile.db :as profile.db]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.utils.image :as utils.image]))

(defn photo [photo-path {:keys [size accessibility-label]}]
  (let [identicon? (when photo-path (profile.db/base64-png? photo-path))]
    [react/view {:style (style/photo-container size)}
     [react/image {:source              (utils.image/source photo-path)
                   :style               (style/photo size)
                   :resize-mode         :cover
                   :accessibility-label (or accessibility-label :chat-icon)}]
     (when identicon?
       [react/view {:style (style/photo-border size)}])]))

;; We optionally pass identicon for perfomance reason, so it does not have to be calculated for each message
(defn member-photo [pub-key identicon]
  (let [path @(re-frame/subscribe [:chats/photo-path pub-key identicon])]
    [photo path {:size                style/default-size
                 :accessibility-label :member-photo}]))

(defn account-photo [account]
  (let [path (multiaccounts/displayed-photo account)]
    [photo path {:size                style/default-size
                 :accessibility-label :own-account-photo}]))

(defn member-identicon [identicon]
  (let [size style/default-size]
    [react/view {:style (style/photo-container size)}
     [react/image {:source              {:uri identicon}
                   :style               (style/photo size)
                   :resize-mode         :cover
                   :accessibility-label :member-photo}]
     [react/view {:style (style/photo-border size)}]]))
