(ns status-im.hardwallet.fx
  (:require [re-frame.core :as re-frame]
            [status-im.utils.types :as types]
            [status-im.hardwallet.card :as card]
            [status-im.native-module.core :as status]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.platform :as platform]))

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
 :hardwallet/init-card
 card/init-card)

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
 :hardwallet/remove-key
 card/remove-key)

(re-frame/reg-fx
 :hardwallet/remove-key-with-unpair
 card/remove-key-with-unpair)

(re-frame/reg-fx
 :hardwallet/export-key
 card/export-key)

(re-frame/reg-fx
 :hardwallet/unpair-and-delete
 card/unpair-and-delete)

(re-frame/reg-fx
 :hardwallet/get-keys
 card/get-keys)

(re-frame/reg-fx
 :hardwallet/sign
 card/sign)

(re-frame/reg-fx
 :hardwallet/sign-typed-data
 card/sign-typed-data)

(re-frame/reg-fx
 :hardwallet/login-with-keycard
 status/login-with-keycard)

(re-frame/reg-fx
 :send-transaction-with-signature
 (fn [{:keys [transaction signature on-completed]}]
   (status/send-transaction-with-signature transaction signature on-completed)))

(re-frame/reg-fx
 :hardwallet/persist-pairings
 (fn [pairings]
   (.. js-dependencies/async-storage
       (setItem "status-keycard-pairings" (types/serialize pairings)))))

(re-frame/reg-fx
 :hardwallet/retrieve-pairings
 (fn []
   (when platform/android?
     (.. js-dependencies/async-storage
         (getItem "status-keycard-pairings")
         (then #(re-frame/dispatch [:hardwallet.callback/on-retrieve-pairings-success
                                    (types/deserialize %)]))))))

;; TODO: Should act differently on different views
(re-frame/reg-fx
 :hardwallet/listen-to-hardware-back-button
 ;;NOTE: not done in view because effect should happen under different conditions and is not dependent on 
 ;;particular screen to be loaded. An fx is easier to re-use and test.
 (fn []
   (re-frame/dispatch [:hardwallet/add-listener-to-hardware-back-button
                       (.addEventListener js-dependencies/back-handler "hardwareBackPress"
                                          (fn []
                                            (re-frame/dispatch [:hardwallet/back-button-pressed])
                                            true))])))

(re-frame/reg-fx
 :hardwallet/remove-listener-to-hardware-back-button
 (fn [listener]
   (.remove listener)))

(re-frame/reg-fx
 :hardwallet/generate-name-and-photo
 (fn [{:keys [public-key on-success]}]
   (status/gfycat-identicon-async
    public-key
    (fn [whisper-name photo-path]
      (re-frame/dispatch
       [on-success whisper-name photo-path])))))
