(ns status-im.react-native.resources
  (:require [quo.design-system.colors :as colors]))

(def ui
  {:empty-chats-header (js/require "../resources/images/ui/empty-chats-header.png")
   :welcome            (js/require "../resources/images/ui/welcome.jpg")
   :welcome-dark       (js/require "../resources/images/ui/welcome-dark.jpg")
   :add-new-contact    (js/require "../resources/images/ui/add-contact.png")
   :chat               (js/require "../resources/images/ui/chat.jpg")
   :chat-dark          (js/require "../resources/images/ui/chat-dark.jpg")
   :wallet             (js/require "../resources/images/ui/wallet.jpg")
   :browser            (js/require "../resources/images/ui/browser.jpg")
   :wallet-dark        (js/require "../resources/images/ui/wallet-dark.jpg")
   :browser-dark       (js/require "../resources/images/ui/browser-dark.jpg")
   :keys               (js/require "../resources/images/ui/keys.jpg")
   :keys-dark          (js/require "../resources/images/ui/keys-dark.jpg")
   :lock               (js/require "../resources/images/ui/lock.png")
   :empty-wallet       (js/require "../resources/images/ui/empty-wallet.png")
   :tribute-to-talk    (js/require "../resources/images/ui/tribute-to-talk.png")
   :keycard-card       (js/require "../resources/images/ui/hardwallet-card.png")
   :keycard-lock       (js/require "../resources/images/ui/keycard-lock.png")
   :keycard            (js/require "../resources/images/ui/keycard.png")
   :keycard-logo       (js/require "../resources/images/ui/keycard-logo.png")
   :keycard-logo-blue  (js/require "../resources/images/ui/keycard-logo-blue.png")
   :keycard-logo-gray  (js/require "../resources/images/ui/keycard-logo-gray.png")
   :keycard-key        (js/require "../resources/images/ui/keycard-key.png")
   :keycard-empty      (js/require "../resources/images/ui/keycard-empty.png")
   :keycard-phone      (js/require "../resources/images/ui/keycard-phone.png")
   :keycard-connection (js/require "../resources/images/ui/keycard-connection.png")
   :keycard-wrong      (js/require "../resources/images/ui/keycard-wrong.png")
   :not-keycard        (js/require "../resources/images/ui/not-keycard.png")
   :status-logo        (js/require "../resources/images/ui/status-logo.png")
   :warning-sign       (js/require "../resources/images/ui/warning-sign.png")
   :phone-nfc-on       (js/require "../resources/images/ui/phone-nfc-on.png")
   :phone-nfc-off      (js/require "../resources/images/ui/phone-nfc-off.png")
   :dapp-store         (js/require "../resources/images/ui/dapp-store.png")
   :ens-header         (js/require "../resources/images/ui/ens-header.png")
   :ens-header-dark    (js/require "../resources/images/ui/ens-header-dark.png")
   :unfurl             (js/require "../resources/images/ui/unfurl.png")
   :unfurl-dark        (js/require "../resources/images/ui/unfurl-dark.png")
   :new-chat-header    (js/require "../resources/images/ui/new-chat-header.png")
   :onboarding-phone   (js/require "../resources/images/ui/onboarding-phone.png")
   :theme-dark         (js/require "../resources/images/ui/theme-dark.png")
   :theme-light        (js/require "../resources/images/ui/theme-light.png")
   :theme-system       (js/require "../resources/images/ui/theme-system.png")
   :notifications      (js/require "../resources/images/ui/notifications.png")
   :collectible        (js/require "../resources/images/ui/collectible.png")
   :collectible-dark   (js/require "../resources/images/ui/collectible-dark.png")
   :hand-wave          (js/require "../resources/images/ui/hand-wave.png")
   :graph              (js/require "../resources/images/ui/graph.png")
   :discover           (js/require "../resources/images/ui/discover.png")
   :community-cover    (js/require "../resources/images/ui/community-cover.png")
   :lifestyle          (js/require "../resources/images/ui/lifestyle.png")
   :music              (js/require "../resources/images/ui/music.png")
   :podcasts           (js/require "../resources/images/ui/podcasts.png")
   :no-contacts        (js/require "../resources/images/ui/no-contacts.png")
   :no-contacts-dark   (js/require "../resources/images/ui/no-contacts-dark.png")})

(def mock-images
  {:photo1               (js/require "../resources/images/mock/photo1.png")
   :photo2               (js/require "../resources/images/mock/photo2.png")
   :photo3               (js/require "../resources/images/mock/photo3.png")
   :community-banner     (js/require "../resources/images/mock/community-banner.png")
   :community-logo       (js/require "../resources/images/mock/community-logo.png")
   :gif                  (js/require "../resources/images/mock/gif.png")
   :sticker              (js/require "../resources/images/mock/sticker.png")
   :user-picture-female2 (js/require "../resources/images/mock/user_picture_female2.png")
   :user-picture-male4   (js/require "../resources/images/mock/user_picture_male4.png")
   :user-picture-male5   (js/require "../resources/images/mock/user_picture_male5.png")
   :coinbase             (js/require "../resources/images/mock/coinbase.png")})

(defn get-theme-image
  [k]
  (get ui (when (colors/dark?) (keyword (str (name k) "-dark"))) (get ui k)))

(def loaded-images (atom {}))

(defn get-image
  [k]
  (if (contains? @loaded-images k)
    (get @loaded-images k)
    (get (swap! loaded-images assoc
           k
           (get ui k))
         k)))

(defn get-mock-image
  [k]
  (if (contains? @loaded-images k)
    (get @loaded-images k)
    (get (swap! loaded-images assoc
           k
           (get mock-images k))
         k)))

(def reactions-old
  {:love        (js/require "../resources/images/reactions/love.png")
   :angry       (js/require "../resources/images/reactions/angry.png")
   :sad         (js/require "../resources/images/reactions/sad.png")
   :laugh       (js/require "../resources/images/reactions/laugh.png")
   :thumbs-up   (js/require "../resources/images/reactions/thumbs-up.png")
   :thumbs-down (js/require "../resources/images/reactions/thumbs-down.png")})
