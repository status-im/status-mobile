(ns status-im.hardwallet.fx
  (:require [re-frame.core :as re-frame]
            [status-im.hardwallet.card :as card]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.datetime :as utils.datetime]))

(re-frame/reg-fx
 :hardwallet/get-application-info
 card/get-application-info)

(re-frame/reg-fx
 :hardwallet/check-nfc-support
 card/check-nfc-support)

(re-frame/reg-fx
 :hardwallet/check-nfc-enabled
 card/check-nfc-enabled)

(re-frame/reg-fx
 :hardwallet/open-nfc-settings
 card/open-nfc-settings)

(re-frame/reg-fx
 :hardwallet/install-applet-and-init-card
 card/install-applet-and-init-card)

(re-frame/reg-fx
 :hardwallet/register-card-events
 card/register-card-events)

(re-frame/reg-fx
 :hardwallet/remove-event-listeners
 card/remove-event-listeners)

(re-frame/reg-fx
 :hardwallet/pair
 card/pair)

(re-frame/reg-fx
 :hardwallet/generate-mnemonic
 card/generate-mnemonic)

(re-frame/reg-fx
 :hardwallet/generate-and-load-key
 card/generate-and-load-key)

(re-frame/reg-fx
 :hardwallet/unblock-pin
 card/unblock-pin)

(re-frame/reg-fx
 :hardwallet/verify-pin
 card/verify-pin)

(re-frame/reg-fx
 :hardwallet/change-pin
 card/change-pin)

(re-frame/reg-fx
 :hardwallet/unpair
 card/unpair)

(re-frame/reg-fx
 :hardwallet/delete
 card/delete)

(re-frame/reg-fx
 :hardwallet/unpair-and-delete
 card/unpair-and-delete)

;TODO remove when keycard login will be ready
(re-frame/reg-fx
 :hardwallet/persist-pairing
 (fn [pairing]
   (.. js-dependencies/react-native
       -AsyncStorage
       (setItem "status-keycard-pairing"
                (js/JSON.stringify
                 #js {"pairing"   pairing
                      "paired-on" (utils.datetime/timestamp)})))))

;TODO remove when keycard login will be ready
(re-frame/reg-fx
 :hardwallet/retrieve-pairing
 (fn []
   (.. js-dependencies/react-native
       -AsyncStorage
       (getItem "status-keycard-pairing")
       (then #(re-frame/dispatch [:hardwallet.callback/on-retrieve-pairing-success
                                  (-> %
                                      (js/JSON.parse)
                                      (js->clj :keywordize-keys true))])))))

;TODO remove when keycard login will be ready
(re-frame/reg-fx
 :hardwallet/remove-pairing
 (fn []
   (.. js-dependencies/react-native
       -AsyncStorage
       (removeItem "status-keycard-pairing"))))
