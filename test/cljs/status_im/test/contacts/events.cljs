(ns status-im.test.contacts.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            reagent.core
            [re-frame.core :as rf]
            [day8.re-frame.test :refer-macros [run-test-sync]]
            status-im.ui.screens.db
            status-im.ui.screens.subs
            [status-im.ui.screens.events :as events]
            [status-im.utils.js-resources :as js-res]))

(def test-contact-group
  {:group-id  "1501682106404-685e041e-38e7-593e-b42c-fb4cabd7faa4"
   :name      "Test"
   :timestamp 0
   :order     0
   :pending?  false
   :contacts  (list
               {:identity "bchat"}
               {:identity "Commiteth"}
               {:identity "demo-bot"})})

(def dapps-contact-group
  {:group-id  "dapps"
   :name      "ÐApps"
   :order     1
   :timestamp 0
   :contacts  [{:identity "oaken-water-meter"}
               {:identity "melonport"}
               {:identity "bchat"}
               {:identity "Dentacoin"}
               {:identity "Augur"}
               {:identity "Ethlance"}
               {:identity "Commiteth"}]
   :pending?  false})

(def demo-bot-contact
  {:address          nil
   :name             "Demo bot"
   :description      nil
   :hide-contact?    false
   :dapp-hash        nil
   :photo-path       nil
   :dapp-url         nil
   :bot-url          "local://demo-bot"
   :whisper-identity "demo-bot"
   :dapp?            true
   :pending?         false
   :unremovable?     false
   :public-key       nil})

(def console-contact
  {:whisper-identity "console"
   :name             "status-console"
   :photo-path       "contacts://console"
   :dapp?            true
   :unremovable?     true
   :bot-url          "local://console-bot"
   :status           "intro-status"
   :pending?         false})
