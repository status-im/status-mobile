(ns status-im.ui.screens.communities.icon
  (:require [status-im.ui.components.colors :as colors]
            [status-im.constants :as constants]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.photos :as photos]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]))

(defn community-icon [{:keys [id name images color]}]
  (let [color (or color (rand-nth colors/chat-colors))
        thumbnail-image (get-in images [:thumbnail :uri])]
    (cond
      (= id constants/status-community-id)
      [react/image {:source (resources/get-image :status-logo)
                    :style  {:width  40
                             :height 40}}]
      (seq thumbnail-image)
      [photos/photo thumbnail-image {:size 40}]

      :else
      [chat-icon.screen/chat-icon-view-chat-list
       id true name color false false])))