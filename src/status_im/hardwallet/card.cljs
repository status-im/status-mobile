(ns status-im.hardwallet.card
  (:require [re-frame.core :as re-frame]
            [status-im.hardwallet.ios-keycard :as ios-keycard]
            [status-im.hardwallet.keycard :as keycard]
            [status-im.hardwallet.real-keycard :as real-keycard]
            [status-im.hardwallet.simulated-keycard :as simulated-keycard]
            [status-im.utils.config :as config]
            [status-im.utils.platform :as platform]
            [taoensso.timbre :as log]))

(defonce card (if config/keycard-test-menu-enabled?
                (simulated-keycard/SimulatedKeycard.)
                (if platform/android?
                  (real-keycard/RealKeycard.)
                  (ios-keycard/IOSKeycard.))))

(defn check-nfc-support []
  (log/info "[keycard] check-nfc-support")
  (keycard/check-nfc-support
   card
   {:on-success
    (fn [response]
      (log/info "[keycard response] check-nfc-support")
      (re-frame/dispatch
       [:hardwallet.callback/check-nfc-support-success response]))}))

(defn check-nfc-enabled []
  (log/info "[keycard] check-nfc-enabled")
  (keycard/check-nfc-enabled
   card
   {:on-success
    (fn [response]
      (log/info "[keycard response] check-nfc-enabled")
      (re-frame/dispatch
       [:hardwallet.callback/check-nfc-enabled-success response]))}))

(defn open-nfc-settings []
  (log/info "[keycard] open-nfc-settings")
  (keycard/open-nfc-settings card))

(defn remove-event-listener [event]
  (log/info "[keycard] remove-event-listener")
  (keycard/remove-event-listener card event))

(defn on-card-disconnected [callback]
  (log/info "[keycard] on-card-disconnected")
  (keycard/on-card-disconnected card callback))

(defn on-card-connected [callback]
  (log/info "[keycard] on-card-connected")
  (keycard/on-card-connected card callback))

(defn remove-event-listeners []
  (log/info "[keycard] remove-event-listeners")
  (keycard/remove-event-listeners card))

(defn register-card-events []
  (log/info "[keycard] register-card-events")
  (keycard/register-card-events
   card
   {:on-card-connected
    #(re-frame/dispatch [:hardwallet.callback/on-card-connected])

    :on-card-disconnected
    #(re-frame/dispatch [:hardwallet.callback/on-card-disconnected])

    :on-nfc-enabled
    #(re-frame/dispatch [:hardwallet.callback/check-nfc-enabled-success true])

    :on-nfc-disabled
    #(re-frame/dispatch [:hardwallet.callback/check-nfc-enabled-success false])}))

(defn- error-object->map [^js object]
  {:code  (.-code object)
   :error (.-message object)})

(defn get-application-info [{:keys [on-success] :as args}]
  (log/info "[keycard] get-application-info")
  (keycard/get-application-info
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] get-application-info")
       (re-frame/dispatch
        [:hardwallet.callback/on-get-application-info-success
         response on-success]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] get-application-info")
       (re-frame/dispatch
        [:hardwallet.callback/on-get-application-info-error
         (error-object->map response)]))})))

(defn install-applet []
  (log/info "[keycard] install-applet")
  (keycard/install-applet
   card
   {:on-success
    (fn [response]
      (log/info "[keycard response succ] install-applet")
      (re-frame/dispatch
       [:hardwallet.callback/on-install-applet-success response]))
    :on-failure
    (fn [response]
      (log/info "[keycard response fail] install-applet")
      (re-frame/dispatch
       [:hardwallet.callback/on-install-applet-error
        (error-object->map response)]))}))

(defn init-card [pin]
  (log/info "[keycard] init-card")
  (keycard/init-card
   card
   {:pin pin
    :on-success
    (fn [response]
      (log/info "[keycard response succ] init-card")
      (re-frame/dispatch
       [:hardwallet.callback/on-init-card-success response]))
    :on-failure
    (fn [response]
      (log/info "[keycard response fail] init-card")
      (re-frame/dispatch
       [:hardwallet.callback/on-init-card-error
        (error-object->map response)]))}))

(defn install-applet-and-init-card [pin]
  (log/info "[keycard] install-applet-and-init-card")
  (keycard/install-applet-and-init-card
   card
   {:pin pin
    :on-success
    (fn [response]
      (log/info "[keycard response succ] install-applet-and-init-card")
      #(re-frame/dispatch
        [:hardwallet.callback/on-install-applet-and-init-card-success
         response]))
    :on-failure
    (fn [response]
      (log/info "[keycard response fail] install-applet-and-init-card")
      (re-frame/dispatch
       [:hardwallet.callback/on-install-applet-and-init-card-error
        (error-object->map response)]))}))

(defn pair [args]
  (log/info "[keycard] pair")
  (keycard/pair
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] pair")
       (re-frame/dispatch
        [:hardwallet.callback/on-pair-success response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] pair")
       (re-frame/dispatch
        [:hardwallet.callback/on-pair-error (error-object->map response)]))})))

(defn generate-and-load-key [args]
  (log/info "[keycard] generate-and-load-key")
  (keycard/generate-and-load-key
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] generate-and-load-key")
       (re-frame/dispatch
        [:hardwallet.callback/on-generate-and-load-key-success response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] generate-and-load-key")
       (re-frame/dispatch
        [:hardwallet.callback/on-generate-and-load-key-error
         (error-object->map response)]))})))

(defn unblock-pin [args]
  (log/info "[keycard] unblock-pin")
  (keycard/unblock-pin
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] unblock-pin")
       (re-frame/dispatch
        [:hardwallet.callback/on-unblock-pin-success response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] unblock-pin")
       (re-frame/dispatch [:hardwallet.callback/on-unblock-pin-error
                           (error-object->map response)]))})))

(defn verify-pin [args]
  (log/info "[keycard] verify-pin")
  (keycard/verify-pin
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] verify-pin")
       (re-frame/dispatch [:hardwallet.callback/on-verify-pin-success
                           response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] verify-pin")
       (re-frame/dispatch
        [:hardwallet.callback/on-verify-pin-error
         (error-object->map response)]))})))

(defn change-pin [args]
  (log/info "[keycard] change-pin")
  (keycard/change-pin
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] change-pin")
       (re-frame/dispatch
        [:hardwallet.callback/on-change-pin-success response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] change-pin")
       (re-frame/dispatch
        [:hardwallet.callback/on-change-pin-error
         (error-object->map response)]))})))

(defn unpair [args]
  (log/info "[keycard] unpair")
  (keycard/unpair
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] unpair")
       (re-frame/dispatch
        [:hardwallet.callback/on-unpair-success response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] unpair")
       (re-frame/dispatch
        [:hardwallet.callback/on-unpair-error
         (error-object->map response)]))})))

(defn delete []
  (log/info "[keycard] delete")
  (keycard/delete
   card
   {:on-success
    (fn [response]
      (log/info "[keycard response succ] delete")
      (re-frame/dispatch
       [:hardwallet.callback/on-delete-success response]))
    :on-failure
    (fn [response]
      (log/debug "[keycard response fail] delete")
      (re-frame/dispatch
       [:hardwallet.callback/on-delete-error
        (error-object->map response)]))}))

(defn remove-key [args]
  (log/info "[keycard] remove-key")
  (keycard/remove-key
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] remove-key")
       (re-frame/dispatch [:hardwallet.callback/on-remove-key-success
                           response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] remove-key")
       (re-frame/dispatch [:hardwallet.callback/on-remove-key-error
                           (error-object->map response)]))})))

(defn remove-key-with-unpair [args]
  (log/info "[keycard] remove-key-with-unpair")
  (keycard/remove-key-with-unpair
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] remove-key-with-unpair")
       (re-frame/dispatch [:hardwallet.callback/on-remove-key-success
                           response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] remove-key-with-unpair")
       (re-frame/dispatch [:hardwallet.callback/on-remove-key-error
                           (error-object->map response)]))})))

(defn export-key [args]
  (log/info "[keycard] export-key")
  (keycard/export-key
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] export-key")
       (re-frame/dispatch [:hardwallet.callback/on-export-key-success
                           response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] export-key")
       (re-frame/dispatch [:hardwallet.callback/on-export-key-error
                           (error-object->map response)]))})))

(defn unpair-and-delete [args]
  (log/info "[keycard] unpair-and-delete")
  (keycard/unpair-and-delete
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] unpair-and-delete")
       (re-frame/dispatch [:hardwallet.callback/on-delete-success
                           response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] unpair-and-delete")
       (re-frame/dispatch [:hardwallet.callback/on-delete-error
                           (error-object->map response)]))})))

(defn get-keys [{:keys [on-success] :as args}]
  (log/info "[keycard] get-keys")
  (keycard/get-keys
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] get-keys")
       (re-frame/dispatch
        [(or on-success :hardwallet.callback/on-get-keys-success)
         response]))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] get-keys"
                 (error-object->map response))
       (re-frame/dispatch [:hardwallet.callback/on-get-keys-error
                           (error-object->map response)]))})))

(defn sign [{:keys [on-success on-failure] :as args}]
  (log/info "[keycard] sign")
  (keycard/sign
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/info "[keycard response succ] sign")
       (if on-success
         (on-success response)
         (re-frame/dispatch [:hardwallet.callback/on-sign-success response])))
     :on-failure
     (fn [response]
       (log/info "[keycard response fail] sign")
       (if on-failure
         (on-failure response)
         (re-frame/dispatch
          [:hardwallet.callback/on-sign-error
           (error-object->map response)])))})))

(defn install-cash-applet []
  (log/info "[keycard] install-cash-applet")
  (keycard/install-cash-applet
   card
   {:on-success
    (fn [response]
      (log/info "[keycard response succ] install-cash-applet")
      (re-frame/dispatch
       [:hardwallet.callback/on-install-applet-success response]))
    :on-failure
    (fn [response]
      (log/info "[keycard response fail] install-cash-applet")
      (re-frame/dispatch
       [:hardwallet.callback/on-install-applet-error
        (error-object->map response)]))}))

(defn sign-typed-data
  [{:keys [hash]}]
  (log/info "[keycard] sign-typed-data")
  (keycard/sign-typed-data
   card
   {:hash hash
    :on-success
    (fn [response]
      (log/info "[keycard response succ] sign-typed-data")
      (re-frame/dispatch [:hardwallet.callback/on-sign-success
                          response]))
    :on-failure
    (fn [response]
      (log/info "[keycard response fail] sign-typed-data")
      (re-frame/dispatch
       [:hardwallet.callback/on-sign-error
        (error-object->map response)]))}))

(defn save-multiaccount-and-login [args]
  (keycard/save-multiaccount-and-login card args))

(defn login [args]
  (keycard/login card args))

(defn send-transaction-with-signature [args]
  (keycard/send-transaction-with-signature card args))
