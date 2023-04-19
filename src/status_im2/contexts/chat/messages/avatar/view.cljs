(ns status-im2.contexts.chat.messages.avatar.view
  (:require [utils.re-frame :as rf]
            [react-native.core :as rn]
            [quo2.core :as quo]))

(defn avatar
  [public-key size]
  (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))
        contact      (rf/sub [:contacts/contact-by-address public-key])
        photo-path   (when (seq (:images contact)) (rf/sub [:chats/photo-path public-key]))
        online?      (rf/sub [:visibility-status-updates/online? public-key])]
    [rn/touchable-without-feedback {:on-press #(rf/dispatch [:chat.ui/show-profile public-key])}
     [rn/view {:padding-top 2}
      [quo/user-avatar
       {:full-name         display-name
        :profile-picture   photo-path
        :status-indicator? true
        :online?           online?
        :size              size}]]]))
