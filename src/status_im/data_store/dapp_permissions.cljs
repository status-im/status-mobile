(ns status-im.data-store.dapp-permissions
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-cofx
 :data-store/all-dapp-permissions
 (fn [cofx _]
   cofx))

(defn save-dapp-permissions
  "Returns tx function for saving dapp permissions"
  [permissions]
  (fn [realm]))

(defn remove-dapp-permissions
  "Returns tx function for removing dapp permissions"
  [dapp]
  (fn [realm]))
