(ns status-im.ui.screens.communities.icon
  (:require [quo.design-system.colors :as colors]
            [status-im2.setup.constants :as constants]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.photos :as photos]))

(defn community-icon
  [{:keys [id name images color]}]
  (let [color           (or color (rand-nth colors/chat-colors))
        thumbnail-image (get-in images [:thumbnail :uri])]
    (cond
      (= id constants/status-community-id)
      [react/image
       {:source (resources/get-image :status-logo) ;; TODO replace with real data (or remove this code)
        :style  {:width  40
                 :height 40}}]
      (seq thumbnail-image)
      [photos/photo thumbnail-image {:size 40}]

      :else
      [chat-icon.screen/chat-icon-view-chat-list
       id true name color false false])))

;; TODO (flexsurfer) reimplement with new design, its still old design, photos and chat-icon components
;; from old design
(defn community-icon-redesign
  [{:keys [id name images color]} size]
  (let [color           (or color (rand-nth colors/chat-colors))
        thumbnail-image (get-in images [:thumbnail :uri])]
    (cond
      (= id constants/status-community-id)
      [react/image
       {:source (resources/get-image :status-logo) ;; TODO replace with real data
        :style  {:width  size
                 :height size}}]
      (seq thumbnail-image)
      [photos/photo thumbnail-image {:size size}]

      :else
      [chat-icon.screen/chat-icon-view-chat-list-redesign
       id true name color size])))
