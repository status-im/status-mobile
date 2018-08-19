(ns status-im.ui.screens.desktop.main.tabs.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.i18n :as i18n]

            [status-im.ui.screens.desktop.main.tabs.home.styles :as styles]
            [clojure.string :as string]
            [status-im.ui.screens.home.views.inner-item :as chat-item]
            [taoensso.timbre :as log]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]))

(views/defview unviewed-indicator [chat-id]
  (let [unviewed-messages-count (re-frame/subscribe [:unviewed-messages-count chat-id])]
    (when (pos? @unviewed-messages-count)
      [react/view
       [react/text {:font  :medium}
        @unviewed-messages-count]])))

(views/defview chat-list-item-inner-view [{:keys [chat-id name group-chat color public? public-key] :as chat-item}]
  (letsubs [photo-path              [:get-chat-photo chat-id]
            unviewed-messages-count [:unviewed-messages-count chat-id]
            chat-name               [:get-chat-name chat-id]
            current-chat-id         [:get-current-chat-id]
            last-message            [:get-last-message chat-id]]
    (let [name (or chat-name
                   (gfycat/generate-gfy public-key))
          [unviewed-messages-label large?] (if (< 9 unviewed-messages-count)
                                             ["9+" true]
                                             [unviewed-messages-count false])]
      [react/view {:style (styles/chat-list-item (= current-chat-id chat-id))}
       [react/view {:style styles/img-container}
        (if public?
          [react/view {:style (styles/topic-image color)}
           [react/text {:style styles/topic-text}
            (string/capitalize (second name))]]
          [react/image {:style styles/chat-icon
                        :source {:uri photo-path}}])
        (when (pos? unviewed-messages-count)
          [react/view {:style styles/unread-messages-icon}
           [react/text {:style (styles/unread-messages-text large?)} unviewed-messages-label]])]
       [react/view {:style styles/chat-name-last-msg-box}
        [react/view {:style styles/chat-name-box}
         (when (and group-chat (not public?))
           [icons/icon :icons/group-chat])
         (when public?
           [icons/icon :icons/public-chat])
         [react/text {:ellipsize-mode  :tail
                      :number-of-lines 1
                      :style           styles/chat-name
                      :font            (if (= current-chat-id chat-id) :medium :default)}
          name]]
        [react/text {:ellipsize-mode  :tail
                     :number-of-lines 1
                     :style           styles/chat-last-message}
         (or (:content last-message) (i18n/label :no-messages-yet))]]
       [react/view {:style styles/timestamp}
        [chat-item/message-timestamp (:timestamp last-message)]]])))

(defn chat-list-item [[chat-id chat]]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to-chat chat-id])}
   [chat-list-item-inner-view (assoc chat :chat-id chat-id)]])

(views/defview chat-list-view []
  (views/letsubs [home-items [:home-items]]
    [react/view {:style styles/chat-list-view}
     [react/view {:style styles/chat-list-header}
      [react/view {:style {:flex 1}}]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :new-contact])}
       [react/view {:style styles/add-new}
        [icons/icon :icons/add {:style {:tint-color :white}}]]]]
     [react/view {:style styles/chat-list-separator}]
     [react/scroll-view
      [react/view
       (for [[index chat] (map-indexed vector home-items)]
         ^{:key (first chat)}
         [chat-list-item chat])]]]))
