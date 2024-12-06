(ns status-im.common.resources)

(def ui
  {:add-new-contact               (js/require "../resources/images/ui2/add-contact.png")
   :biometrics                    (js/require "../resources/images/ui2/biometrics.png")
   :chat-privately-with-friends   (js/require "../resources/images/ui2/chat-privately-with-friends.png")
   :desktop-how-to-pair-sign-in   (js/require "../resources/images/ui2/desktop-how-to-pair-sign-in.png")
   :desktop-how-to-pair-logged-in (js/require
                                   "../resources/images/ui2/desktop-how-to-pair-logged-in.png")
   :mobile-how-to-pair-sign-in    (js/require "../resources/images/ui2/mobile-how-to-pair-sign-in.png")
   :mobile-how-to-pair-logged-in  (js/require "../resources/images/ui2/mobile-how-to-pair-logged-in.png")
   :find-sync-code-desktop        (js/require "../resources/images/ui2/find-sync-code-desktop.png")
   :find-sync-code-mobile         (js/require "../resources/images/ui2/find-sync-code-mobile.png")
   :lifestyle                     (js/require "../resources/images/ui2/lifestyle.png")
   :music                         (js/require "../resources/images/ui2/music.png")
   :podcasts                      (js/require "../resources/images/ui2/podcasts.png")
   :generate-keys                 (js/require "../resources/images/ui2/generate-keys.png")
   :generate-keys1                (js/require "../resources/images/ui2/generating-keys-1.png")
   :ethereum-address              (js/require "../resources/images/ui2/ethereum-address.png")
   :use-keycard                   (js/require "../resources/images/ui2/keycard.png")
   :use-recovery-phrase           (js/require "../resources/images/ui2/recovery-phrase.png")
   :check-your-keycard            (js/require "../resources/images/ui2/check-your-keycard.png")
   :qr-code                       (js/require "../resources/images/ui2/qr-code.png")
   :keycard-logo                  (js/require "../resources/images/ui2/keycard-logo.png")
   :keycard-watermark             (js/require "../resources/images/ui2/keycard-watermark.png")
   :keycard-buy                   (js/require "../resources/images/ui2/keycard-buy.png")
   :keycard-migration             (js/require "../resources/images/ui2/keycard-migration.png")
   :keycard-migration-failed      (js/require "../resources/images/ui2/keycard-migration-failed.png")
   :keycard-migration-succeeded   (js/require "../resources/images/ui2/keycard-migration-succeeded.png")
   :not-keycard                   (js/require "../resources/images/ui2/not-keycard.png")
   :discover                      (js/require "../resources/images/ui2/discover.png")
   :invite-friends                (js/require "../resources/images/ui2/invite-friends.png")
   :transaction-progress          (js/require "../resources/images/ui2/transaction-progress.png")
   :welcome-illustration          (js/require "../resources/images/ui2/welcome_illustration.png")
   :notifications                 (js/require "../resources/images/ui2/notifications.png")
   :nfc-prompt                    (js/require "../resources/images/ui2/nfc-prompt.png")
   :nfc-success                   (js/require "../resources/images/ui2/nfc-success.png")
   :own-buy-and-swap-your-crypto  (js/require "../resources/images/ui2/own-buy-and-swap-your-crypto.png")
   :preparing-status              (js/require "../resources/images/ui2/preparing-status.png")
   :store-your-assets-on-keycard  (js/require "../resources/images/ui2/store-your-assets-on-keycard.png")
   :syncing-devices               (js/require "../resources/images/ui2/syncing_devices.png")
   :syncing-wrong                 (js/require "../resources/images/ui2/syncing_wrong.png")
   :buy                           (js/require "../resources/images/ui2/buy.png")
   :receive                       (js/require "../resources/images/ui2/receive.png")})

(def ui-themed
  {:angry-man
   {:light (js/require "../resources/images/ui2/angry-man-light.png")
    :dark  (js/require "../resources/images/ui2/angry-man-dark.png")}
   :cat-in-box
   {:light (js/require "../resources/images/ui2/cat-in-box-light.png")
    :dark  (js/require "../resources/images/ui2/cat-in-box-dark.png")}
   :no-contacts
   {:light (js/require "../resources/images/ui2/no-contacts-light.png")
    :dark  (js/require "../resources/images/ui2/no-contacts-dark.png")}
   :no-contacts-to-chat
   {:light (js/require "../resources/images/ui2/no-contacts-to-chat-light.png")
    :dark  (js/require "../resources/images/ui2/no-contacts-to-chat-dark.png")}
   :keycard-chip
   {:light (js/require "../resources/images/ui2/keycard-chip-light.png")
    :dark  (js/require "../resources/images/ui2/keycard-chip-dark.png")}
   :no-group-chats
   {:light (js/require "../resources/images/ui2/no-group-chats-light.png")
    :dark  (js/require "../resources/images/ui2/no-group-chats-dark.png")}
   :no-sent-requests
   {:light (js/require "../resources/images/ui2/no-sent-requests-light.png")
    :dark  (js/require "../resources/images/ui2/no-sent-requests-dark.png")}
   :no-received-requests
   {:light (js/require "../resources/images/ui2/no-received-requests-light.png")
    :dark  (js/require "../resources/images/ui2/no-received-requests-dark.png")}
   :no-communities
   {:light (js/require "../resources/images/ui2/no-communities-light.png")
    :dark  (js/require "../resources/images/ui2/no-communities-dark.png")}
   :no-pending-communities
   {:light (js/require "../resources/images/ui2/no-pending-communities-light.png")
    :dark  (js/require "../resources/images/ui2/no-pending-communities-dark.png")}
   :no-opened-communities
   {:light (js/require "../resources/images/ui2/no-opened-communities-light.png")
    :dark  (js/require "../resources/images/ui2/no-opened-communities-dark.png")}
   :no-contacts-to-invite
   {:light (js/require "../resources/images/ui2/no-contacts-to-invite-light.png")
    :dark  (js/require "../resources/images/ui2/no-contacts-to-invite-dark.png")}
   :no-notifications
   {:light (js/require "../resources/images/ui2/no-notifications-light.png")
    :dark  (js/require "../resources/images/ui2/no-notifications-dark.png")}
   :no-activity
   {:light (js/require "../resources/images/ui2/no-activity-light.png")
    :dark  (js/require "../resources/images/ui2/no-activity-dark.png")}
   :no-collectibles
   {:light (js/require "../resources/images/ui2/no-collectibles-light.png")
    :dark  (js/require "../resources/images/ui2/no-collectibles-dark.png")}
   :no-dapps
   {:light (js/require "../resources/images/ui2/no-dapps-light.png")
    :dark  (js/require "../resources/images/ui2/no-dapps-dark.png")}
   :no-permissions
   {:light (js/require "../resources/images/ui2/no-permissions-light.png")
    :dark  (js/require "../resources/images/ui2/no-permissions-dark.png")}
   :sweating-man
   {:light (js/require "../resources/images/ui2/sweating-man-light.png")
    :dark  (js/require "../resources/images/ui2/sweating-man-dark.png")}
   :no-pinned-messages
   {:light (js/require "../resources/images/ui2/no-pinned-messages-light.png")
    :dark  (js/require "../resources/images/ui2/no-pinned-messages-dark.png")}
   :no-assets
   {:light (js/require "../resources/images/ui2/no-assets-light.png")
    :dark  (js/require "../resources/images/ui2/no-assets-dark.png")}})

(def mock-images
  {:bored-ape             (js/require "../resources/images/mock2/bored-ape.png")
   :coinbase              (js/require "../resources/images/mock2/coinbase.png")
   :collectible           (js/require "../resources/images/mock2/collectible.png")
   :collectible-monkey    (js/require "../resources/images/mock2/collectible-monkey.png")
   :collectible1          (js/require "../resources/images/mock2/collectible1.png")
   :collectible2          (js/require "../resources/images/mock2/collectible2.png")
   :collectible3          (js/require "../resources/images/mock2/collectible3.png")
   :collectible4          (js/require "../resources/images/mock2/collectible4.png")
   :collectible5          (js/require "../resources/images/mock2/collectible5.png")
   :collectible6          (js/require "../resources/images/mock2/collectible6.png")
   :contact               (js/require "../resources/images/mock2/contact.png")
   :community-banner      (js/require "../resources/images/mock2/community-banner.png")
   :community-logo        (js/require "../resources/images/mock2/community-logo.png")
   :community-cover       (js/require "../resources/images/mock2/community-cover.png")
   :dark-blur-bg          (js/require "../resources/images/mock2/dark_blur_bg.png")
   :dark-blur-background  (js/require "../resources/images/mock2/dark-blur-background.png")
   :decentraland          (js/require "../resources/images/mock2/decentraland.png")
   :diamond               (js/require "../resources/images/mock2/diamond.png")
   :gif                   (js/require "../resources/images/mock2/gif.png")
   :monkey                (js/require "../resources/images/mock2/monkey.png")
   :light-blur-background (js/require "../resources/images/mock2/light-blur-background.png")
   :photo1                (js/require "../resources/images/mock2/photo1.png")
   :photo2                (js/require "../resources/images/mock2/photo2.png")
   :photo3                (js/require "../resources/images/mock2/photo3.png")
   :pinterest             (js/require "../resources/images/mock2/pinterest.png")
   :qr-code               (js/require "../resources/images/mock2/qr-code.png")
   :rarible               (js/require "../resources/images/mock2/rarible.png")
   :small-opt-card-icon   (js/require "../resources/images/mock2/small_opt_card_icon.png")
   :small-opt-card-main   (js/require "../resources/images/mock2/small_opt_card_main.png")
   :status-logo           (js/require "../resources/images/mock2/status-logo.png")
   :sticker               (js/require "../resources/images/mock2/sticker.png")
   :ring                  (js/require "../resources/images/mock2/ring.png")
   :verified              (js/require "../resources/images/mock2/verified.png")
   :user-picture-female2  (js/require "../resources/images/mock2/user_picture_female2.png")
   :user-picture-male4    (js/require "../resources/images/mock2/user_picture_male4.png")
   :user-picture-male5    (js/require "../resources/images/mock2/user_picture_male5.png")})

(defn get-mock-image
  [k]
  (get mock-images k))

(defn get-image
  [k]
  (get ui k))

(defn get-themed-image
  [k theme]
  (get-in ui-themed [k theme]))
