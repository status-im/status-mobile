(ns status-im.ui.screens.home.sheet.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.action-button.styles :as action-button.styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.ui.components.react :as react]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.platform :as platform]))

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

(defview public-chat-actions-view []
  (letsubs [{:keys [chat-id]} [:bottom-sheet/options]]
    [react/view {:flex 1 :flex-direction :row}
     [react/view action-button.styles/actions-list
      (when-not platform/desktop?
        (let [link    (universal-links/generate-link :public-chat :external chat-id)
              message (i18n/label :t/share-public-chat-text {:link link})]
          [action-button/action-button
           {:label               (i18n/label :t/share-chat)
            :accessibility-label :share-chat-button
            :icon                :main-icons/share
            :icon-opts           {:color colors/blue}
            :on-press            #(do
                                    (re-frame/dispatch [:bottom-sheet/hide-sheet])
                                    (list-selection/open-share {:message message}))}]))
      [action-button/action-button
       {:label               (i18n/label :t/delete-chat)
        :label-style         {:color colors/red}
        :accessibility-label :delete-chat-button
        :icon                :main-icons/delete
        :icon-opts           {:color colors/red}
        :cyrcle-color        colors/red-light
        :on-press            #(hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id])}]]]))

(defview private-chat-actions-view []
  (letsubs [{:keys [chat-id]} [:bottom-sheet/options]]
    [react/view {:flex 1 :flex-direction :row}
     [react/view action-button.styles/actions-list
      [action-button/action-button
       {:label               (i18n/label :t/view-profile)
        :accessibility-label :view-profile-button
        :icon                :main-icons/profile
        :icon-opts           {:color colors/blue}
        :on-press            #(hide-sheet-and-dispatch [:chat.ui/show-profile chat-id])}]
      [action-button/action-button
       {:label               (i18n/label :t/delete-chat)
        :label-style         {:color colors/red}
        :accessibility-label :delete-chat-button
        :icon                :main-icons/delete
        :icon-opts           {:color colors/red}
        :cyrcle-color        colors/red-light
        :on-press            #(hide-sheet-and-dispatch [:chat.ui/remove-chat-pressed chat-id])}]]]))

(defview group-chat-actions-view []
  (letsubs [{:keys [chat-id]} [:bottom-sheet/options]]
    [react/view {:flex 1 :flex-direction :row}
     [react/view action-button.styles/actions-list
      [action-button/action-button
       {:label               (i18n/label :t/group-info)
        :accessibility-label :group-info-button
        :icon                :main-icons/group-chat
        :icon-opts           {:color colors/blue}
        :on-press            #(hide-sheet-and-dispatch [:show-group-chat-profile chat-id])}]
      [action-button/action-button
       {:label               (i18n/label :t/delete-and-leave-group)
        :label-style         {:color colors/red}
        :accessibility-label :delete-and-leave-group-button
        :icon                :main-icons/delete
        :icon-opts           {:color colors/red}
        :cyrcle-color        colors/red-light
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
