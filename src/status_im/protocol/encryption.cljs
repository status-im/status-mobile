(ns status-im.protocol.encryption)

(def default-curve 384)

(def ecc (js/require "eccjs"))

(defn new-keypair!
  "Returns {:private \"private key\" :public \"public key\""
  []
  (let [{:keys [enc dec]}
        (-> ecc
            (.generate (.-ENC_DEC ecc) default-curve)
            (js->clj :keywordize-keys true))]
    {:private dec
     :public  enc}))

(defn encrypt [public-key content]
  (.encrypt ecc public-key content))

(defn decrypt [private-key content]
  (.decrypt ecc private-key content))

