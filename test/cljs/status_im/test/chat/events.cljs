(ns status-im.test.chat.events
  (:require [cljs.test :refer [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer [run-test-sync]]
            status-im.ui.screens.db
            [status-im.ui.screens.events :as events]
            [status-im.chat.events :as chat-events]
            [status-im.chat.events.input :as chat-input-events]))

(def contact
  {:address "c296367a939e0957500a25ca89b70bd64b03004e"
   :whisper-identity "0x04f5722fba79eb36d73263417531007f43d13af76c6233573a8e3e60f667710611feba0785d751b50609bfc0b7cef35448875c5392c0a91948c95798a0ce600847"
   :name "testuser"
   :photo-path "contacts://testuser"
   :dapp? false})

(defn test-fixtures []
  (rf/reg-fx ::events/init-store #())
  (rf/reg-fx :call-jail #()))

(deftest chat-input-events
  (run-test-sync
   (test-fixtures)
   (let [chat (rf/subscribe [:chat])]
     (rf/dispatch [:initialize-db]))))
