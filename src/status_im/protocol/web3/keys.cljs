(ns status-im.protocol.web3.keys
  (:require [taoensso.timbre :as log]
            [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]))

(defn get-new-key-pair [{:keys [web3 on-success on-error]}]
  (if web3
    (.. web3
        -shh
        (newKeyPair (fn [err resp]
                      (if-not err
                        (on-success resp)
                        (on-error err)))))
    (on-error "web3 not available.")))

(re-frame/reg-fx
  :shh/get-new-key-pair
  (fn [{:keys [web3 success-event error-event]}]
    (get-new-key-pair {:web3       web3
                       :on-success #(re-frame/dispatch [success-event %])
                       :on-error   #(re-frame/dispatch [error-event %])})))

(defn get-public-key [{:keys [web3 key-pair-id on-success on-error]}]
  (if (and web3 key-pair-id)
    (.. web3
        -shh
        (getPublicKey key-pair-id (fn [err resp]
                                    (if-not err
                                      (on-success resp)
                                      (on-error err)))))
    (on-error "web3 or key-pair id not available.")))

(re-frame/reg-fx
  :shh/get-public-key
  (fn [{:keys [web3 key-pair-id success-event error-event]}]
    (get-public-key {:web3       web3
                     :key-pair-id key-pair-id
                     :on-success #(re-frame/dispatch [success-event %])
                     :on-error   #(re-frame/dispatch [error-event %])})))

(def status-key-password "status-key-password")
(def status-group-key-password "status-public-group-key-password")

(defonce password->keys (atom {}))

(defn- add-sym-key-from-password
  [web3 password callback]
  (.. web3
      -shh
      (generateSymKeyFromPassword password callback)))

(defn get-sym-key
  "Memoizes expensive calls by password."
  ([web3 password success-fn]
   ;; TODO:(dmitryn) add proper error handling
   ;; to other usages of get-sym-key fn
   (get-sym-key web3 password success-fn #(log/error %)))
  ([web3 password success-fn error-fn]
   (if-let [key-id (get @password->keys password)]
     (success-fn key-id)
     (add-sym-key-from-password
      web3 password
      (fn [err res]
        (if err
          (error-fn err)
          (do (swap! password->keys assoc password res)
              (success-fn res))))))))

(defn reset-keys! []
  (reset! password->keys {}))
