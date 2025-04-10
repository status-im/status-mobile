(ns quo.components.share.qr-code.view
  (:require
    [quo.components.avatars.account-avatar.view :as account-avatar]
    [quo.components.avatars.channel-avatar.view :as channel-avatar]
    [quo.components.avatars.user-avatar.view :as user-avatar]
    [quo.components.avatars.wallet-user-avatar.view :as wallet-avatar]
    [quo.components.share.qr-code.style :as style]
    [react-native.core :as rn]
    [react-native.fast-image :as fast-image]))

(defn- avatar-image
  [{avatar-type :avatar
    :as         props}]
  [rn/view {:style [rn/stylesheet-absolute-fill style/avatar-overlay]}
   [rn/view
    {:style (case avatar-type
              :wallet-account style/avatar-container-rounded
              :saved-address  style/big-avatar-container-rounded
              style/avatar-container-circular)}
    (case avatar-type
      :profile
      [user-avatar/user-avatar
       (assoc props
              :size              :size-64
              :status-indicator? false
              :online?           false)]

      :wallet-account
      [account-avatar/view (assoc props :size :size-64 :type :default)]

      :community
      [rn/image
       {:style  style/community-logo-image
        :source (:picture props)}]

      :channel
      [channel-avatar/view (assoc props :locked? nil :size :size-64)]

      :saved-address
      [wallet-avatar/wallet-user-avatar (assoc props :size :size-80)]

      nil)]])

(defn view
  "Receives a URL to show a QR code with an avatar (optional) over it.
   Parameters:
     - qr-image-uri: A valid uri representing the QR code to display using `fast-image`
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
  [{:keys [avatar size qr-image-uri]
    :or   {avatar :none}
    :as   props}]
  [rn/view {:style (style/container size)}
   [fast-image/fast-image
    {:style  (style/qr-image size)
     :source {:uri qr-image-uri}}]
   (when-not (= avatar :none)
     [avatar-image props])])
