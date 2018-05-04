(ns status-im.data-store.browser
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.browser :as data-store])
  (:refer-clojure :exclude [exists?]))

(re-frame/reg-cofx
 :data-store/all-browsers
 (fn [cofx _]
   (assoc cofx :all-stored-browsers (data-store/get-all))))

(re-frame/reg-fx
  :data-store/save-browser
  (fn [{:keys [browser-id] :as browser}]
    (async/go (async/>! core/realm-queue #(data-store/save browser (data-store/exists? browser-id))))))

(re-frame/reg-fx
  :data-store/remove-browser
  (fn [browser-id]
    (async/go (async/>! core/realm-queue #(data-store/delete browser-id)))))
