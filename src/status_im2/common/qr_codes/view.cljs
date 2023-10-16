(ns status-im2.common.qr-codes.view
  (:require
    [quo2.core :as quo]
    [utils.image-server :as image-server]
    [utils.re-frame :as rf]))

(defn qr-code
  "Receives a URL to show a QR code with an avatar (optional) over it.
   Parameters:
     - url: String to transform to QR
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
  [{:keys [url size] :as props}]
  (let [qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                             {:url         url
                              :port        (rf/sub [:mediaserver/port])
                              :qr-size     (or 400 (int size))
                              :error-level :highest})]
    [quo/qr-code (assoc props :qr-image-uri qr-media-server-uri)]))
