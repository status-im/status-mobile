(ns status-im.protocol.encryption
  (:require [cljsjs.chance]
            [cljsjs.eccjs]))

(def default-curve 384)

(defn new-keypair!
  "Returns {:private \"private key\" :public \"public key\""
  []
  (let [{:keys [enc dec]}
        (-> (.generate js/ecc (.-ENC_DEC js/ecc) default-curve)
            (js->clj :keywordize-keys true))]
    {:private dec
     :public  enc}))

(defn encrypt [public-key content]
  (.encrypt js/ecc public-key content))

(defn decrypt [private-key content]
  (.decrypt js/ecc private-key content))

