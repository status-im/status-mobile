(ns status-im2.contexts.chat.messages.avatar.view
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [utils.re-frame :as rf]))

(defn avatar
  [{:keys [public-key size hide-ring?]}]
  (let [display-name (first (rf/sub [:contacts/contact-two-names-by-identity public-key]))
        photo-path   (rf/sub [:chats/photo-path public-key])
        online?      (rf/sub [:visibility-status-updates/online? public-key])]
    [rn/view {:style {:padding-top 4}}
     [rn/touchable-opacity
      {:active-opacity 1
       :on-press       #(rf/dispatch [:chat.ui/show-profile public-key])}
      [quo/user-avatar
       {:full-name       display-name
        :ring?           (not hide-ring?)
        :profile-picture photo-path
        :online?         online?
        :size            size}]]]))
