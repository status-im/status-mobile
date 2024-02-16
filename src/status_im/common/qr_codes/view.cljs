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
                              :qr-size     (or (int size) 400)
                              :error-level :highest})]
    [quo/qr-code
     (assoc props
            :qr-image-uri
            qr-media-server-uri)]))

(defn get-network-short-name-url
  [network]
  (case network
    :ethereum "eth:"
    :optimism "opt:"
    :arbitrum "arb1:"
    (str (name network) ":")))

(defn- get-qr-data-for-wallet-multichain
  [qr-data networks]
  (as-> networks $
    (map get-network-short-name-url $)
    (apply str $)
    (str $ qr-data)))

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
       - networks:            A vector of network names as keywords (`[:ethereum, :my-net, ...]`).
       - emoji:               Emoji in a string to show in the QR code.
       - on-legacy-press:     Callback for the legacy tab.
       - on-multichain-press: Callback for the multichain tab.
       - address:             :legacy | :multichain
     `:saved-address`
       - networks:            A vector of network names as keywords (`[:ethereum, :my-net, ...]`).
       - on-settings-press:   Callback for the settings button.
       - on-legacy-press:     Callback for the legacy tab.
       - address:             :legacy | :multichain
       - on-multichain-press: Callback for the multichain tab.
     `:watched-address`
       - networks:            A vector of network names as keywords (`[:ethereum, :my-net, ...]`).
       - on-settings-press:   Callback for the settings button.
       - emoji:               Emoji in a string to show in the QR code.
       - on-legacy-press:     Callback for the legacy tab.
       - address:             :legacy | :multichain
       - on-multichain-press: Callback for the multichain tab."
  [{:keys         [qr-data qr-data-label-shown networks address]
    share-qr-type :type
    :as           props}]
  (let [label               (or qr-data-label-shown qr-data)
        string-to-encode    (if (and (= share-qr-type :wallet) (= address :multichain))
                              (get-qr-data-for-wallet-multichain qr-data networks)
                              qr-data)
        qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                             {:url         string-to-encode
                              :port        (rf/sub [:mediaserver/port])
                              :qr-size     600
                              :error-level :highest})]
    [quo/share-qr-code
     (assoc props
            :qr-data      label
            :qr-image-uri qr-media-server-uri)]))
