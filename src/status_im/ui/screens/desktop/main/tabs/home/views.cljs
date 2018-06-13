(ns status-im.ui.screens.desktop.main.tabs.home.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.utils.gfycat.core :as gfycat]
            [taoensso.timbre :as log]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]))

(views/defview unviewed-indicator [chat-id]
  (let [unviewed-messages-count (re-frame/subscribe [:unviewed-messages-count chat-id])]
    (when (pos? @unviewed-messages-count)
      [react/view
       [react/text {:font  :medium}
        @unviewed-messages-count]])))

(views/defview chat-list-item-inner-view [{:keys [chat-id name group-chat public? public-key]}]
  (let [name (str
               (if public? "#" "")
               (or name
                   (gfycat/generate-gfy public-key)))]
    [react/view {:style {:padding 12 :background-color :white :flex-direction :row :align-items :center}}
     (when public?
       [icons/icon :icons/public-chat])
     (when (and group-chat (not public?))
       [icons/icon :icons/group-chat])
     [react/text
      name]
     [react/view {:style {:flex 1}}]
     [unviewed-indicator chat-id]]))

(defn chat-list-item [[chat-id chat]]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to-chat chat-id])}
   [react/view
    [chat-list-item-inner-view (assoc chat :chat-id chat-id)]]])

(views/defview chat-list-view []
  (views/letsubs [home-items [:home-items]]
    [react/view {:style {:flex 1 :background-color :white}}
     [react/view {:style {:align-items :center :flex-direction :row :padding 11}}
      [react/view {:style {:flex 1}}]
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:navigate-to :new-contact])}
       [icons/icon :icons/add]]]
     [react/view {:style {:height 1 :background-color "#e8ebec" :margin-horizontal 16}}]
     [react/scroll-view
      [react/view
       (for [[index chat] (map-indexed vector home-items)]
         ^{:key (first chat)}
         [chat-list-item chat])]]]))
