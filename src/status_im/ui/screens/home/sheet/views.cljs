(ns status-im.ui.screens.home.sheet.views
  (:require [quo.core :as quo]
            [utils.re-frame :as rf]
            [utils.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.react :as rn]
            [status-im2.config :as config]
            [status-im.ui.screens.home.sheet.styles :as style]))

(defn- hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide-old])
  (rf/dispatch event))

(defn add-new-view
  []
  [rn/view
   [rn/view style/add-new-view-wrapper
    [quo/text
     {:size   :large
      :weight :bold}
     (i18n/label :t/open-home)]
    [quo/button
     {:type                :icon
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
     :on-press            #(hide-sheet-and-dispatch [:open-modal :new-chat])}]
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
     :on-press            #(hide-sheet-and-dispatch [:open-modal :new-public-chat])}]
   [quo/list-item
    {:theme               :accent
     :title               (i18n/label :t/communities-alpha)
     :accessibility-label :communities-button
     :icon                :main-icons/communities
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :communities])}]
   [invite/list-item
    {:accessibility-label :chats-menu-invite-friends-button}]])

;; Deprecated
(def add-new
  {:content add-new-view})
