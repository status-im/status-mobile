(ns status-im.acquisition.persistance
  (:require [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            ["@react-native-community/async-storage" :default async-storage]))

(def chat-initialized-key "acquisition-chat-initialized")
(def referrer-flow-state-key "acquisition-referrer-flow-state")
(def tx-store-key "acquisition-watch-tx")
(def referrer-key "acquisition-referrer")

(def referrer-state {:accepted "accepted"
                     :declined "declined"
                     :claimed  "claimed"
                     :outdated "outdated"})

(re-frame/reg-fx
 ::set-referrer-state
 (fn [decision]
   (-> ^js async-storage
       (.setItem referrer-flow-state-key (get referrer-state decision))
       (.then (fn []
                (re-frame/dispatch [:set-in [:acquisition :flow-state]
                                    (get referrer-state decision)])))
       (.catch (fn [error]
                 (log/error "[async-storage]" error))))))

(defn get-referrer-flow-state [on-success]
  (-> ^js async-storage
      (.getItem referrer-flow-state-key)
      (.then (fn [^js data]
               (on-success data)))
      (.catch (fn [error]
                (log/error "[async-storage]" error)))))

(re-frame/reg-fx
 ::check-tx-state
 (fn [on-success]
   (-> ^js async-storage
       (.getItem tx-store-key)
       (.then (fn [^js tx]
                (on-success tx)))
       (.catch (fn [error]
                 (log/error "[async-storage]" error))))))

(re-frame/reg-fx
 ::set-watch-tx
 (fn [tx]
   (when tx
     (-> ^js async-storage
         (.setItem tx-store-key tx)
         (.catch (fn [error]
                   (log/error "[async-storage]" error)))))))

(re-frame/reg-fx
 ::chat-initalized!
 (fn []
   (-> ^js async-storage
       (.setItem chat-initialized-key "initialized")
       (.catch (fn [error]
                 (log/error "[async-storage]" error))))))

(re-frame/reg-fx
 ::chat-initialized?
 (fn [on-success]
   (-> ^js async-storage
       (.getItem chat-initialized-key)
       (.then (fn [^js data]
                (on-success data)))
       (.catch (fn [error]
                 (log/error "[async-storage]" error))))))

(defn set-referrer [referrer]
  (-> ^js async-storage
      (.setItem referrer-key referrer)
      (.catch (fn [error]
                (log/error "[async-storage]" error)))))

(defn get-referrer [on-success]
  (-> ^js async-storage
      (.getItem referrer-key)
      (.then (fn [^js data]
               (on-success data)))
      (.catch (fn [error]
                (log/error "[async-storage]" error)))))
