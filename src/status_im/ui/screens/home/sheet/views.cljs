(ns status-im.ui.screens.home.sheet.views
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.qr-scanner.core :as qr-scanner]
            [quo.core :as quo]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.ui.components.invite.views :as invite]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn add-new-view []
  [react/view
   [react/view {:style {:flex-direction  :row
                        :padding-left    16
                        :padding-right   8
                        :justify-content :space-between
                        :align-items     :center}}
    [quo/text {:size   :large
               :weight :bold}
     (i18n/label :t/open-home)]
    [quo/button {:type                :icon
                 :theme               :icon
                 :accessibility-label :universal-qr-scanner
                 :on-press            #(hide-sheet-and-dispatch
                                        [::qr-scanner/scan-code
                                         {:handler ::qr-scanner/on-scan-success}])}
     :main-icons/qr]]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/start-new-chat)
     :accessibility-label :start-1-1-chat-button
     :icon                :main-icons/one-on-one-chat
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-chat])}]
   (when config/group-chat-enabled?
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/start-group-chat)
       :accessibility-label :start-group-chat-button
       :icon                :main-icons/group-chat
       :on-press            #(hide-sheet-and-dispatch [:contact.ui/start-group-chat-pressed])}])
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/new-public-group-chat)
     :accessibility-label :join-public-chat-button
     :icon                :main-icons/public-chat
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :new-public-chat])}]
   (when config/communities-enabled?
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/communities-alpha)
       :accessibility-label :communities-button
       :icon                :main-icons/communities
       :on-press            #(hide-sheet-and-dispatch [:navigate-to :communities {:screen :communities}])}])
   [invite/list-item
    {:accessibility-label :chats-menu-invite-friends-button}]])

(def add-new
  {:content add-new-view})
