(ns status-im.data-store.installations
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-cofx
 :data-store/get-all-installations
 (fn [coeffects _]))

(defn save
  "Returns tx function for saving a installation"
  [installation]
  (fn [realm]))

(defn enable
  [installation-id]
  (save {:installation-id installation-id
         :enabled? true}))

(defn disable
  [installation-id]
  (save {:installation-id installation-id
         :enabled? false}))

(defn delete
  "Returns tx function for deleting an installation"
  [id]
  (fn [realm]))
