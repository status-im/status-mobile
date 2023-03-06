(ns status-im.ui.screens.chat.photos
  (:require [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.ui.components.fast-image :as fast-image]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as style]))

(def memo-photo-rend
  (memoize
   (fn [photo-path size accessibility-label _]
     [react/view {:style (style/photo-container size)}
      [fast-image/fast-image
       {:source              {:uri photo-path}
        :style               (style/photo size)
        :accessibility-label (or accessibility-label :chat-icon)}]
      [react/view {:style (style/photo-border size)}]])))

;; "(colors/dark?)" is passed to memoized function to avoid previous themes cache
(defn photo
  [photo-path {:keys [size accessibility-label]}]
  [memo-photo-rend photo-path size accessibility-label (colors/dark?)])

;; We optionally pass identicon for perfomance reason, so it does not have to be calculated for each
;; message
(defn member-photo
  ([pub-key]
   (member-photo pub-key nil))
  ([pub-key identicon]
   (member-photo pub-key identicon style/default-size))
  ([pub-key identicon size]
   (let [path @(re-frame/subscribe [:chats/photo-path pub-key identicon])]
     [photo path
      {:size                size
       :accessibility-label :member-photo}])))

(defn account-photo
  [account]
  (let [path (multiaccounts/displayed-photo account)]
    [photo path
     {:size                style/default-size
      :accessibility-label :own-account-photo}]))
