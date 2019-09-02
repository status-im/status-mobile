(ns status-im.ui.screens.home.sheet.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.list-item.views :as list-item]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn add-new-view []
  [react/view {:flex 1 :flex-direction :row}
   [react/view {:flex 1}
    [list-item/list-item
     {:theme               :action
      :title               :t/start-new-chat
      :accessibility-label :start-1-1-chat-button
      :icon                :main-icons/one-on-one-chat
      :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-chat])}]
    [list-item/list-item
     {:theme               :action
      :title               :t/start-group-chat
      :accessibility-label :start-group-chat-button
      :icon                :main-icons/group-chat
      :on-press            #(hide-sheet-and-dispatch [:contact.ui/start-group-chat-pressed])}]
    [list-item/list-item
     {:theme               :action
      :title               :t/new-public-group-chat
      :accessibility-label :join-public-chat-button
      :icon                :main-icons/public-chat
      :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-public-chat])}]
    [list-item/list-item
     {:theme               :action
      :title               :t/scan-qr
      :accessibility-label :scan-qr-code-button
      :icon                :main-icons/qr
      :on-press            #(hide-sheet-and-dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                      {:toolbar-title (i18n/label :t/scan-qr)}
                                                      :handle-qr-code])}]
    [list-item/list-item
     {:theme               :action
      :title               :t/invite-friends
      :accessibility-label :invite-friends-button
      :icon                :main-icons/share
      :on-press            #(do
                              (re-frame/dispatch [:bottom-sheet/hide-sheet])
                              (list-selection/open-share {:message (i18n/label :t/get-status-at)}))}]]])

(defview public-chat-actions-view []
  (letsubs [{:keys [chat-id]} [:bottom-sheet/options]]
    [react/view {:flex 1 :flex-direction :row}
     [react/view {:flex 1}
      (when-not platform/desktop?
        (let [link    (universal-links/generate-link :public-chat :external chat-id)
              message (i18n/label :t/share-public-chat-text {:link link})]
          [list-item/list-item
           {:theme               :action
            :title               :t/share-chat
            :accessibility-label :share-chat-button
            :icon                :main-icons/share
            :icon-opts           {:color colors/blue}
            :on-press            #(do
                                    (re-frame/dispatch [:bottom-sheet/hide-sheet])
                                    (list-selection/open-share {:message message}))}]))
      [list-item/list-item
       {:theme               :action-destructive
        :title               :t/delete-chat
        :accessibility-label :delete-chat-button
        :icon                :main-icons/delete
        :on-press            #(hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id])}]]]))

(defview private-chat-actions-view []
  (letsubs [{:keys [chat-id]} [:bottom-sheet/options]]
    [react/view {:flex 1 :flex-direction :row}
     [react/view {:flex 1}
      [list-item/list-item
       {:theme               :action
        :title               :t/view-profile
        :accessibility-label :view-profile-button
        :icon                :main-icons/profile
        :on-press            #(hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])}]
      [list-item/list-item
       {:theme               :action-destructive
        :title               :t/delete-chat
        :accessibility-label :delete-chat-button
        :icon                :main-icons/delete
        :on-press            #(hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id])}]]]))

(defview group-chat-actions-view []
  (letsubs [{:keys [chat-id]} [:bottom-sheet/options]]
    [react/view {:flex 1 :flex-direction :row}
     [react/view {:flex 1}
      [list-item/list-item
       {:theme               :action
        :title               :t/group-info
        :accessibility-label :group-info-button
        :icon                :main-icons/group-chat
        :on-press            #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])}]
      [list-item/list-item
       {:theme               :action-destructive
        :title               :t/delete-and-leave-group
        :accessibility-label :delete-and-leave-group-button
        :icon                :main-icons/delete
        :on-press            #(hide-sheet-and-dispatch [:group-chats.ui/remove-chat-pressed chat-id])}]]]))

(def add-new
  {:content        add-new-view
   :content-height 320})

(def public-chat-actions
  {:content        public-chat-actions-view
   :content-height 128})

(def private-chat-actions
  {:content        private-chat-actions-view
   :content-height 128})

(def group-chat-actions
  {:content        group-chat-actions-view
   :content-height 128})
