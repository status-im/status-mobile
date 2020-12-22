(ns status-im.ui.screens.routing.main
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.add-new.new-public-chat.view :as new-public-chat]
            [status-im.ui.screens.wallet.recipient.views :as recipient]
            [status-im.ui.screens.qr-scanner.views :as qr-scanner]
            [status-im.ui.screens.stickers.views :as stickers]
            [status-im.ui.screens.home.views :as home]
            [status-im.ui.screens.add-new.new-chat.views :as new-chat]
            [status-im.ui.screens.add-new.new-chat.events :as new-chat.events]
            [status-im.ui.screens.routing.intro-login-stack :as intro-login-stack]
            [status-im.ui.screens.routing.chat-stack :as chat-stack]
            [status-im.ui.screens.routing.wallet-stack :as wallet-stack]
            [status-im.ui.screens.routing.key-storage-stack :as key-storage-stack]
            [status-im.ui.screens.group.views :as group-chat]
            [status-im.ui.screens.group.events :as group.events]
            [status-im.ui.screens.routing.profile-stack :as profile-stack]
            [status-im.ui.screens.routing.browser-stack :as browser-stack]
            [status-im.ui.components.tabbar.core :as tabbar]
            [status-im.ui.components.invite.views :as invite]
            [status-im.ui.screens.routing.core :as navigation]
            [status-im.utils.platform :as platform]
            [quo.previews.main :as quo.preview]
            [status-im.utils.config :as config]
            [status-im.ui.screens.profile.contact.views :as contact]
            [status-im.ui.screens.notifications-settings.views :as notifications-settings]
            [status-im.ui.screens.wallet.send.views :as wallet]
            [status-im.ui.screens.link-previews-settings.views :as link-previews]
            [status-im.ui.screens.status.new.views :as status.new]
            [status-im.ui.screens.browser.bookmarks.views :as bookmarks]
            [status-im.ui.screens.routing.status-stack :as status-stack]))

(defonce main-stack (navigation/create-stack))
(defonce bottom-tabs (navigation/create-bottom-tabs))

;; TODO(Ferossgp):  Add two-pane navigator on chat-stack
(defn tabs []
  [bottom-tabs {:initial-route-name :chat-stack
                :lazy               true
                :header-mode        :none
                :tab-bar            tabbar/tabbar}
   [{:name      :chat-stack
     :insets    {:top false}
     :component chat-stack/chat-stack}
    {:name      :browser-stack
     :insets    {:top false}
     :component browser-stack/browser-stack}
    {:name      :wallet-stack
     :insets    {:top false}
     :component wallet-stack/wallet-stack}
    {:name      :status-stack
     :insets    {:top false}
     :component status-stack/status-stack}
    {:name      :profile-stack
     :insets    {:top false}
     :component profile-stack/profile-stack}]])

(views/defview get-main-component [_]
  (views/letsubs [logged-in? [:multiaccount/logged-in?]]
    [main-stack (merge {:header-mode :none}
                       ;; https://github.com/react-navigation/react-navigation/issues/6520
                       (when platform/ios?
                         {:mode :modal}))
     (concat
      [(if logged-in?
         {:name      :tabs
          :insets    {:top false}
          :component tabs}
         {:name      :intro-stack
          :insets    {:top    false
                      :bottom true}
          :component intro-login-stack/intro-stack})
       {:name      :stickers-pack-modal
        :component stickers/pack-modal}
       {:name         :welcome
        :back-handler :noop
        :component    home/welcome}
       {:name       :new-chat
        :on-focus   [::new-chat.events/new-chat-focus]
        :transition :presentation-ios
        :component  new-chat/new-chat}
       {:name       :new-contact
        :on-focus   [::new-chat.events/new-chat-focus]
        :transition :presentation-ios
        :component  new-chat/new-contact}
       {:name       :link-preview-settings
        :transition :presentation-ios
        :component  link-previews/link-previews-settings}
       {:name       :new-public-chat
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  new-public-chat/new-public-chat}
       {:name       :nickname
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  contact/nickname}
       {:name       :edit-group-chat-name
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  group-chat/edit-group-chat-name}
       {:name       :create-group-chat
        :transition :presentation-ios
        :component  chat-stack/new-group-chat}
       {:name       :communities
        :transition :presentation-ios
        :component  chat-stack/communities}
       {:name       :referral-invite
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  invite/referral-invite}
       {:name       :add-participants-toggle-list
        :on-focus   [::group.events/add-participants-toggle-list]
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  group-chat/add-participants-toggle-list}
       {:name       :recipient
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  recipient/recipient}
       {:name       :new-favourite
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  recipient/new-favourite}
       {:name      :qr-scanner
        :insets    {:top false :bottom false}
        :component qr-scanner/qr-scanner}
       {:name         :notifications-settings
        :back-handler :noop
        :insets       {:bottom true}
        :component    notifications-settings/notifications-settings}
       {:name         :notifications-advanced-settings
        :back-handler :noop
        :insets       {:bottom true}
        :component    notifications-settings/notifications-advanced-settings}
       {:name         :notifications-onboarding
        :back-handler :noop
        :insets       {:bottom true}
        :component    notifications-settings/notifications-onboarding}
       {:name       :prepare-send-transaction
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  wallet/prepare-send-transaction}
       {:name       :request-transaction
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  wallet/request-transaction}
       {:name       :my-status
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  status.new/my-status}
       {:name       :new-bookmark
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  bookmarks/new-bookmark}
       {:name       :profile
        :transition :presentation-ios
        :insets     {:bottom true}
        :component  contact/profile}
       {:name      :key-storage-stack
        :component key-storage-stack/key-storage-stack}]

      (when config/quo-preview-enabled?
        [{:name      :quo-preview
          :insets    {:top false :bottom false}
          :component quo.preview/preview-stack}]))]))
