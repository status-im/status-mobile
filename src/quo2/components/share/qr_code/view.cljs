(ns quo2.components.share.qr-code.view
  (:require [quo2.components.avatars.account-avatar.view :as account-avatar]
            [quo2.components.avatars.channel-avatar.view :as channel-avatar]
            [quo2.components.avatars.user-avatar.view :as user-avatar]
            [quo2.components.avatars.wallet-user-avatar :as wallet-avatar]
            [quo2.components.share.qr-code.style :as style]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]
            [utils.image-server :as image-server]))

(defn- qr-code-image
  [{:keys [size url media-server-port]}]
  (let [media-server-uri (image-server/get-qr-image-uri-for-any-url
                          {:url         url
                           :port        media-server-port
                           :qr-size     (or 400 (int size))
                           :error-level :highest})]
    [fast-image/fast-image
     {:style  (style/qr-image size)
      :source {:uri media-server-uri}}]))

(defn- avatar-image
  [{avatar-type :avatar
    :as         props}]
  [rn/view {:style style/avatar-overlay}
   [rn/view
    {:style (if (= avatar-type :wallet-account)
              style/avatar-container-rounded
              style/avatar-container-circular)}
    (case avatar-type
      :profile
      [user-avatar/user-avatar
       (assoc props
              :size              :medium
              :status-indicator? false
              :online?           false
              :ring?             false)]

      :wallet-account
      [account-avatar/view (assoc props :size 48 :type :default)]

      :community
      [rn/image
       {:style  style/community-logo-image
        :source (:picture props)}]

      :channel
      [channel-avatar/view (assoc props :locked? nil :size :size/l)]

      :saved-address
      [wallet-avatar/wallet-user-avatar (assoc props :size :large)]

      nil)]])

(defn view
  "Receives a URL to show a QR code with an avatar (optional) over it.
   Parameters:
     - url: String codified in the QR code
     - media-server-port: receives the value of the subscription `[:mediaserver/port]`
     - size: if not provided, the QR code image will grow according to its parent
     - avatar: Type of the avatar, defaults to `:none` and it can be:
         `:profile`, `:wallet-account`, `:community`, `:channel` or `:saved-address`

   Depending on the type selected, different properties are accepted:
   `:profile`:
     - profile-picture
     - full-name
     - customization-color
   `:wallet-account`
     - emoji
     - customization-color
   `:community`
     - picture
   `:channel`
      - emoji
      - customization-color
   `:saved-address`
      - f-name
      - l-name
      - customization-color"
  [{:keys [avatar size]
    :or   {avatar :none}
    :as   props}]
  [rn/view {:style (style/container size)}
   [qr-code-image props]
   (when-not (= avatar :none)
     [avatar-image props])])
