(ns status-im.ui.screens.chat.photos
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as style]
            [status-im.profile.db :as profile.db]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.utils.image :as utils.image]
            [quo.design-system.colors :as colors]))

(def memo-photo-rend
  (memoize
   (fn [photo-path size accessibility-label _]
     (let [identicon? (when photo-path (profile.db/base64-png? photo-path))]
       [react/view {:style (style/photo-container size)}
        [react/fast-image {:source              (utils.image/source photo-path)
                           :style               (style/photo size)
                           :accessibility-label (or accessibility-label :chat-icon)}]
        (when identicon?
          [react/view {:style (style/photo-border size)}])]))))

;; "(colors/dark?)" is passed to memoized function to avoid previous themes cache
(defn photo [photo-path {:keys [size accessibility-label]}]
  [memo-photo-rend photo-path size accessibility-label (colors/dark?)])

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
     [react/fast-image {:source              {:uri identicon}
                        :style               (style/photo size)
                        :accessibility-label :member-photo}]
     [react/view {:style (style/photo-border size)}]]))
