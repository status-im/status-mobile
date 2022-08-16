(ns status-im.keycard.fx
  (:require [re-frame.core :as re-frame]
            [status-im.utils.types :as types]
            [status-im.keycard.card :as card]
            [status-im.native-module.core :as status]
            ["react-native" :refer (BackHandler)]
            [taoensso.timbre :as log]
            ["@react-native-community/async-storage" :default AsyncStorage]))

(re-frame/reg-fx
 :keycard/start-nfc
 (fn []
   (log/debug "fx start-nfc")
   (card/start-nfc
    {:on-success #(re-frame/dispatch [:keycard.callback/start-nfc-success])
     :on-failure #(re-frame/dispatch [:keycard.callback/start-nfc-failure])})))

(re-frame/reg-fx
 :keycard/stop-nfc
 (fn []
   (log/debug "fx stop-nfc")
   (card/stop-nfc
    {:on-success #(re-frame/dispatch [:keycard.callback/stop-nfc-success])
     :on-failure #(re-frame/dispatch [:keycard.callback/stop-nfc-failure])})))

(re-frame/reg-fx
 :keycard/set-nfc-message
 card/set-nfc-message)

(re-frame/reg-fx
 :keycard/start-nfc-and-show-connection-sheet
 (fn [args]
   (log/debug "fx start-nfc-and-show-connection-sheet")
   (card/start-nfc
    {:on-success
     (fn []
       (log/debug "nfc started successfully. next: show-connection-sheet")
       (re-frame/dispatch [:keycard.callback/start-nfc-success])
       (re-frame/dispatch [:keycard.callback/show-connection-sheet args]))
     :on-failure
     (fn []
       (log/debug "nfc failed star starting. not calling show-connection-sheet")
       (re-frame/dispatch [:keycard.callback/start-nfc-failure]))})))

(re-frame/reg-fx
 :keycard/stop-nfc-and-hide-connection-sheet
 (fn []
   (log/debug "fx stop-nfc-and-hide-connection-sheet")
   (card/stop-nfc
    {:on-success
     (fn []
       (re-frame/dispatch [:keycard.callback/stop-nfc-success])
       (re-frame/dispatch [:keycard.callback/hide-connection-sheet]))
     :on-failure
     (fn []
       (re-frame/dispatch [:keycard.callback/stop-nfc-failure]))})))

(re-frame/reg-fx
 :keycard/get-application-info
 card/get-application-info)

(re-frame/reg-fx
 :keycard/factory-reset
 card/factory-reset)

(re-frame/reg-fx
 :keycard/check-nfc-support
 card/check-nfc-support)

(re-frame/reg-fx
 :keycard/check-nfc-enabled
 card/check-nfc-enabled)

(re-frame/reg-fx
 :keycard/open-nfc-settings
 card/open-nfc-settings)

(re-frame/reg-fx
 :keycard/install-applet-and-init-card
 card/install-applet-and-init-card)

(re-frame/reg-fx
 :keycard/init-card
 card/init-card)

(re-frame/reg-fx
 :keycard/register-card-events
 card/register-card-events)

(re-frame/reg-fx
 :keycard/remove-event-listeners
 card/remove-event-listeners)

(re-frame/reg-fx
 :keycard/pair
 card/pair)

(re-frame/reg-fx
 :keycard/generate-and-load-key
 card/generate-and-load-key)

(re-frame/reg-fx
 :keycard/unblock-pin
 card/unblock-pin)

(re-frame/reg-fx
 :keycard/verify-pin
 card/verify-pin)

(re-frame/reg-fx
 :keycard/change-pin
 card/change-pin)

(re-frame/reg-fx
 :keycard/change-puk
 card/change-puk)

(re-frame/reg-fx
 :keycard/change-pairing
 card/change-pairing)

(re-frame/reg-fx
 :keycard/unpair
 card/unpair)

(re-frame/reg-fx
 :keycard/delete
 card/delete)

(re-frame/reg-fx
 :keycard/remove-key
 card/remove-key)

(re-frame/reg-fx
 :keycard/remove-key-with-unpair
 card/remove-key-with-unpair)

(re-frame/reg-fx
 :keycard/export-key
 card/export-key)

(re-frame/reg-fx
 :keycard/unpair-and-delete
 card/unpair-and-delete)

(re-frame/reg-fx
 :keycard/import-keys
 card/import-keys)

(re-frame/reg-fx
 :keycard/get-keys
 card/get-keys)

(re-frame/reg-fx
 :keycard/sign
 card/sign)

(re-frame/reg-fx
 :keycard/sign-typed-data
 card/sign-typed-data)

(re-frame/reg-fx
 :keycard/login-with-keycard
 card/login)

(re-frame/reg-fx
 :send-transaction-with-signature
 card/send-transaction-with-signature)

(re-frame/reg-fx
 :keycard/persist-pairings
 (fn [pairings]
   (.. AsyncStorage
       (setItem "status-keycard-pairings" (types/serialize pairings)))))

(re-frame/reg-fx
 :keycard/retrieve-pairings
 (fn []
   (.. AsyncStorage
       (getItem "status-keycard-pairings")
       (then #(re-frame/dispatch [:keycard.callback/on-retrieve-pairings-success
                                  (types/deserialize %)])))))

;; TODO: Should act differently on different views
(re-frame/reg-fx
 :keycard/listen-to-hardware-back-button
 ;;NOTE: not done in view because effect should happen under different conditions and is not dependent on
 ;;particular screen to be loaded. An fx is easier to re-use and test.
 (fn []
   (re-frame/dispatch [:keycard/add-listener-to-hardware-back-button
                       (.addEventListener BackHandler "hardwareBackPress"
                                          (fn []
                                            (re-frame/dispatch [:keycard/back-button-pressed])
                                            true))])))

(re-frame/reg-fx
 :keycard/remove-listener-to-hardware-back-button
 (fn [^js listener]
   (.remove listener)))

(re-frame/reg-fx
 :keycard/generate-name-and-photo
 (fn [{:keys [public-key on-success]}]
   (status/gfycat-identicon-async
    public-key
    (fn [whisper-name photo-path]
      (re-frame/dispatch
       [on-success whisper-name photo-path])))))

(re-frame/reg-fx
 :keycard/save-multiaccount-and-login
 card/save-multiaccount-and-login)
