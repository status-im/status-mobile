(ns status-im.common.qr-codes.view
  (:require
    [quo.core :as quo]
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
                              :qr-size     (or (and size (int size)) 400)
                              :error-level :highest})]
    [quo/qr-code
     (assoc props
            :qr-image-uri
            qr-media-server-uri)]))

(defn share-qr-code
  "Receives the following properties:
     - type:                  :profile | :wallet | :saved-address | :watched-address
     - qr-image-uri:          Image source value.
     - qr-data:               Text to show below the QR code.
     - on-text-press:         Callback for the `qr-data` text.
     - on-text-long-press:    Callback for the `qr-data` text.
     - on-share-press:        Callback for the share button.
     - customization-color:   Custom color for the QR code component.
     - unblur-on-android?:    [Android only] disables blur for this component.
     - full-name:             User full name.

     Depending on the `type`, different properties are accepted:
     `:profile`
       - profile-picture:     map ({:source image-source}) or any image source.
     `:wallet`
       - emoji:               Emoji in a string to show in the QR code.
     `:watched-address`
       - emoji:               Emoji in a string to show in the QR code. "
  [{:keys [qr-data qr-data-label-shown]
    :as   props}]
  (let [label               (or qr-data-label-shown qr-data)
        qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                             {:url         qr-data
                              :port        (rf/sub [:mediaserver/port])
                              :qr-size     600
                              :error-level :highest})]
    [quo/share-qr-code
     (assoc props
            :qr-data      label
            :qr-image-uri qr-media-server-uri)]))
