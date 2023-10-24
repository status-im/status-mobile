(ns status-im2.common.qr-codes.view
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
                              :qr-size     (or 400 (int size))
                              :error-level :highest})]
    [quo/qr-code (assoc props :qr-image-uri qr-media-server-uri)]))

(defn- get-network-short-name-url
  [network-kw]
  (case network-kw
    :ethereum "eth:"
    :optimism "opt:"
    :arbitrum "arb1:"
    (str (name network-kw) ":")))

(defn- get-qr-data-for-wallet-multichain
  [qr-data networks]
  (as-> networks $
    (map get-network-short-name-url $)
    (apply str $)
    (str $ qr-data)))

(defn share-qr-code
  [{:keys         [qr-data qr-data-label-shown networks]
    share-qr-type :type
    :as           props}]
  (let [label               (or qr-data-label-shown qr-data)
        share-qr-data       (if (= share-qr-type :wallet-multichain)
                              (get-qr-data-for-wallet-multichain qr-data networks)
                              qr-data)
        qr-media-server-uri (image-server/get-qr-image-uri-for-any-url
                             {:url         share-qr-data
                              :port        (rf/sub [:mediaserver/port])
                              :qr-size     500
                              :error-level :highest})]
    [quo/share-qr-code
     (assoc props
            :qr-data      label
            :qr-image-uri qr-media-server-uri)]))
