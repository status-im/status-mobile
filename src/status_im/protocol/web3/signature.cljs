(ns status-im.protocol.web3.signature
  (:require [taoensso.timbre :refer-macros [debug]]
            [taoensso.timbre :as log]))

(defn sign [{:keys [web3 address message callback]}]
  (when address
    (let [to-sign (->> (.toString message)
                       (.sha3 web3))]
      (.sendAsync (.-currentProvider web3)
                  (clj->js {:method "personal_sign"
                            :params [to-sign (str "0x" address) "testpass"]
                            :from   address
                            :id     1})
                  (fn [err res]
                    (if-let [res (-> res (js->clj) (get "result"))]
                      (callback nil res)
                      (callback :sign-error nil)))))))

(defn check-signature [{:keys [web3 message from address signature callback]}]
  (let [message (->> (.toString message)
                     (.sha3 web3))]
    (.sendAsync (.-currentProvider web3)
                (clj->js {:method "personal_ecRecover"
                          :params [message signature]
                          :from   from
                          :id     1})
                (fn [err res]
                  (if-let [res (-> res (js->clj) (get "result"))]
                    (callback nil (= res (str "0x" address)))
                    (callback :validation-error false))))))