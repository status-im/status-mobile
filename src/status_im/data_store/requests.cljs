(ns status-im.data-store.requests
  (:require [cljs.core.async :as async]
            [re-frame.core :as re-frame]
            [status-im.data-store.realm.core :as core]
            [status-im.data-store.realm.requests :as data-store]))

(re-frame/reg-cofx
  :data-store/get-unanswered-requests
  (fn [cofx _]
    (assoc cofx :stored-unanswered-requests (data-store/get-all-unanswered))))

(re-frame/reg-fx
  :data-store/save-request
  (fn [request]
    (async/go (async/>! core/realm-queue #(data-store/save request)))))

(re-frame/reg-fx
  :data-store/mark-request-as-answered
  (fn [{:keys [chat-id message-id]}]
    (async/go (async/>! core/realm-queue #(data-store/mark-as-answered chat-id message-id)))))
