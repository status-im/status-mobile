(ns status-im.protocol.encryption
  (:require [status-im.js-dependencies :as dependencies]))

(def default-curve 384)

(defn new-keypair!
  "Returns {:private \"private key\" :public \"public key\""
  []
  (let [{:keys [enc dec]}
        (-> dependencies/eccjs
            (.generate (.-ENC_DEC dependencies/eccjs) default-curve)
            (js->clj :keywordize-keys true))]
    {:private dec
     :public  enc}))

(defn encrypt [public-key content]
  (.encrypt dependencies/eccjs public-key content))

(defn decrypt [private-key content]
  (.decrypt dependencies/eccjs private-key content))

