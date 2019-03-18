(ns status-im.ui.screens.home.sheet.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn add-new-view []
  [react/view {:flex 1 :flex-direction :row}
   [react/view action-button.styles/actions-list
    [action-button/action-button
     {:label               (i18n/label :t/start-new-chat)
      :accessibility-label :start-1-1-chat-button
      :icon                :main-icons/private-chat
      :icon-opts           {:color colors/blue}
      :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-chat])}]
    [action-button/action-button
     {:label               (i18n/label :t/start-group-chat)
      :accessibility-label :start-group-chat-button
      :icon                :main-icons/group-chat
      :icon-opts           {:color colors/blue}
      :on-press            #(hide-sheet-and-dispatch [:contact.ui/start-group-chat-pressed])}]
    [action-button/action-button
     {:label               (i18n/label :t/new-public-group-chat)
      :accessibility-label :join-public-chat-button
      :icon                :main-icons/public-chat
      :icon-opts           {:color colors/blue}
      :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-public-chat])}]
    [action-button/action-button
     {:label               (i18n/label :t/scan-qr)
      :accessibility-label :scan-qr-code-button
      :icon                :main-icons/qr
      :icon-opts           {:color colors/blue}
      :on-press            #(hide-sheet-and-dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                      {:toolbar-title (i18n/label :t/scan-qr)}
                                                      :handle-qr-code])}]
    [action-button/action-button
     {:label               (i18n/label :t/invite-friends)
      :accessibility-label :invite-friends-button
      :icon                :main-icons/share
      :icon-opts           {:color colors/blue}
      :on-press            #(do
                              (re-frame/dispatch [:bottom-sheet/hide-sheet])
                              (list-selection/open-share {:message (i18n/label :t/get-status-at)}))}]]])

(def add-new
  {:content        add-new-view
   :content-height 320})