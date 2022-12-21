(ns status-im.ui.screens.home.sheet.views
  (:require [quo.core :as quo]
            [quo2.foundations.colors :as colors]
            [re-frame.core :as rf]
            [status-im.i18n.i18n :as i18n]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.components.react :as rn]
            [status-im.ui2.screens.chat.components.new-chat :as new-chat-aio]
            [status-im.utils.config :as config]
            [quo2.components.list-items.list-item :as quo2.list-item]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(defn add-new-view []
  [rn/view
   [rn/view {:style {:flex-direction  :row
                     :padding-left    16
                     :padding-right   8
                     :justify-content :space-between
                     :align-items     :center}}
    [quo/text {:size   :large
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
   (when @(rf/subscribe [:communities/enabled?])
     [quo/list-item
      {:theme               :accent
       :title               (i18n/label :t/communities-alpha)
       :accessibility-label :communities-button
       :icon                :main-icons/communities
       :on-press            #(hide-sheet-and-dispatch [:navigate-to :communities])}])
   [invite/list-item
    {:accessibility-label :chats-menu-invite-friends-button}]])

(defn add-new-sheet-view []
  [rn/view
   [quo2.list-item/list-item
    {:theme                        :main
     :title                        (i18n/label :t/new-chat)
     :icon-bg-color                :transparent
     :icon-container-style         {:padding-horizontal 0}
     :container-padding-horizontal {:padding-horizontal 4}
     :container-padding-vertical   12
     :title-column-style           {:margin-left 2}
     :icon-color                   (colors/theme-colors colors/neutral-50 colors/neutral-40)
     :accessibility-label          :start-a-new-chat
     :icon                         :i/new-message
     :on-press                     #(hide-sheet-and-dispatch [:bottom-sheet/show-sheet :start-a-new-chat])}]
   [quo2.list-item/list-item
    {:theme                        :main
     :title                        (i18n/label :t/add-a-contact)
     :icon-bg-color                :transparent
     :icon-container-style         {:padding-horizontal 0}
     :container-padding-horizontal {:padding-horizontal 4}
     :container-padding-vertical   12
     :title-column-style           {:margin-left 2}
     :icon-color                   (colors/theme-colors colors/neutral-50 colors/neutral-40)
     :accessibility-label          :add-a-contact
     :subtitle                     (i18n/label :t/enter-a-chat-key)
     :icon                         :i/add-user
     :on-press                     #(hide-sheet-and-dispatch [:open-modal :new-contact])}]])


(def add-new-sheet
  {:content add-new-sheet-view})
;; Deprecated
(def add-new
  {:content add-new-view})

(def start-a-new-chat
  {:content new-chat-aio/contact-toggle-list})
