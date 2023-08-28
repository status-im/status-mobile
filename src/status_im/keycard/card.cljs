(ns status-im.keycard.card
  (:require [re-frame.core :as re-frame]
            [status-im.keycard.keycard :as keycard]
            [status-im.keycard.real-keycard :as real-keycard]
            [status-im.keycard.simulated-keycard :as simulated-keycard]
            [status-im2.config :as config]
            [taoensso.timbre :as log]))

(defonce card
  (if config/keycard-test-menu-enabled?
    (simulated-keycard/SimulatedKeycard.)
    (real-keycard/RealKeycard.)))

(defn check-nfc-support
  []
  (log/debug "[keycard] check-nfc-support")
  (keycard/check-nfc-support
   card
   {:on-success
    (fn [response]
      (log/debug "[keycard response] check-nfc-support")
      (re-frame/dispatch
       [:keycard.callback/check-nfc-support-success response]))}))

(defn check-nfc-enabled
  []
  (log/debug "[keycard] check-nfc-enabled")
  (keycard/check-nfc-enabled
   card
   {:on-success
    (fn [response]
      (log/debug "[keycard response] check-nfc-enabled")
      (re-frame/dispatch
       [:keycard.callback/check-nfc-enabled-success response]))}))

(defn open-nfc-settings
  []
  (log/debug "[keycard] open-nfc-settings")
  (keycard/open-nfc-settings card))

(defn remove-event-listener
  [event]
  (log/debug "[keycard] remove-event-listener")
  (keycard/remove-event-listener card event))

(defn on-card-disconnected
  [callback]
  (log/debug "[keycard] on-card-disconnected")
  (keycard/on-card-disconnected card callback))

(defn on-card-connected
  [callback]
  (log/debug "[keycard] on-card-connected")
  (keycard/on-card-connected card callback))

(defn remove-event-listeners
  []
  (log/debug "[keycard] remove-event-listeners")
  (keycard/remove-event-listeners card))

(defn register-card-events
  []
  (log/debug "[keycard] register-card-events")
  (keycard/register-card-events
   card
   {:on-card-connected
    #(re-frame/dispatch [:keycard.callback/on-card-connected])

    :on-card-disconnected
    #(re-frame/dispatch [:keycard.callback/on-card-disconnected])

    :on-nfc-user-cancelled
    #(re-frame/dispatch [:keycard.callback/on-nfc-user-cancelled])

    :on-nfc-timeout
    #(re-frame/dispatch [:keycard.callback/on-nfc-timeout])

    :on-nfc-enabled
    #(re-frame/dispatch [:keycard.callback/check-nfc-enabled-success true])

    :on-nfc-disabled
    #(re-frame/dispatch [:keycard.callback/check-nfc-enabled-success false])}))

(defn- error-object->map
  [^js object]
  {:code  (.-code object)
   :error (.-message object)})

(defn set-pairings
  [pairings]
  (log/debug "[keycard] open-nfc-settings")
  (keycard/set-pairings card {:pairings pairings}))

(defn get-application-info
  [{:keys [on-success] :as args}]
  (log/debug "[keycard] get-application-info")
  (keycard/get-application-info
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] get-application-info")
       (re-frame/dispatch
        [:keycard.callback/on-get-application-info-success
         response on-success]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] get-application-info")
       (re-frame/dispatch
        [:keycard.callback/on-get-application-info-error
         (error-object->map response)]))})))

(defn factory-reset
  [{:keys [on-success] :as args}]
  (log/debug "[keycard] factory-reset")
  (keycard/factory-reset
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] get-application-info")
       (re-frame/dispatch
        [:keycard.callback/on-get-application-info-success
         response on-success]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] get-application-info")
       (re-frame/dispatch
        [:keycard.callback/on-get-application-info-error
         (error-object->map response)]))})))

(defn install-applet
  []
  (log/debug "[keycard] install-applet")
  (keycard/install-applet
   card
   {:on-success
    (fn [response]
      (log/debug "[keycard response succ] install-applet")
      (re-frame/dispatch
       [:keycard.callback/on-install-applet-success response]))
    :on-failure
    (fn [response]
      (log/debug "[keycard response fail] install-applet")
      (re-frame/dispatch
       [:keycard.callback/on-install-applet-error
        (error-object->map response)]))}))

(defn init-card
  [pin]
  (log/debug "[keycard] init-card")
  (keycard/init-card
   card
   {:pin pin
    :on-success
    (fn [response]
      (log/debug "[keycard response succ] init-card")
      (re-frame/dispatch
       [:keycard.callback/on-init-card-success response]))
    :on-failure
    (fn [response]
      (log/debug "[keycard response fail] init-card")
      (re-frame/dispatch
       [:keycard.callback/on-init-card-error
        (error-object->map response)]))}))

(defn install-applet-and-init-card
  [pin]
  (log/debug "[keycard] install-applet-and-init-card")
  (keycard/install-applet-and-init-card
   card
   {:pin pin
    :on-success
    (fn [response]
      (log/debug "[keycard response succ] install-applet-and-init-card")
      #(re-frame/dispatch
        [:keycard.callback/on-install-applet-and-init-card-success
         response]))
    :on-failure
    (fn [response]
      (log/debug "[keycard response fail] install-applet-and-init-card")
      (re-frame/dispatch
       [:keycard.callback/on-install-applet-and-init-card-error
        (error-object->map response)]))}))

(defn pair
  [args]
  (log/debug "[keycard] pair")
  (keycard/pair
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] pair")
       (re-frame/dispatch
        [:keycard.callback/on-pair-success response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] pair")
       (re-frame/dispatch
        [:keycard.callback/on-pair-error (error-object->map response)]))})))

(defn generate-and-load-key
  [args]
  (log/debug "[keycard] generate-and-load-key")
  (keycard/generate-and-load-key
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] generate-and-load-key")
       (re-frame/dispatch
        [:keycard.callback/on-generate-and-load-key-success response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] generate-and-load-key")
       (re-frame/dispatch
        [:keycard.callback/on-generate-and-load-key-error
         (error-object->map response)]))})))

(defn unblock-pin
  [args]
  (log/debug "[keycard] unblock-pin")
  (keycard/unblock-pin
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] unblock-pin")
       (re-frame/dispatch
        [:keycard.callback/on-unblock-pin-success response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] unblock-pin")
       (re-frame/dispatch [:keycard.callback/on-unblock-pin-error
                           (error-object->map response)]))})))

(defn verify-pin
  [args]
  (log/debug "[keycard] verify-pin")
  (keycard/verify-pin
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] verify-pin")
       (re-frame/dispatch [:keycard.callback/on-verify-pin-success
                           response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] verify-pin")
       (re-frame/dispatch
        [:keycard.callback/on-verify-pin-error
         (error-object->map response)]))})))

(defn change-pin
  [args]
  (log/debug "[keycard] change-pin")
  (keycard/change-pin
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] change-pin")
       (re-frame/dispatch
        [:keycard.callback/on-change-pin-success response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] change-pin")
       (re-frame/dispatch
        [:keycard.callback/on-change-pin-error
         (error-object->map response)]))})))

(defn change-puk
  [args]
  (log/debug "[keycard] change-puk")
  (keycard/change-puk
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] change-puk")
       (re-frame/dispatch
        [:keycard.callback/on-change-puk-success response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] change-puk")
       (re-frame/dispatch
        [:keycard.callback/on-change-pin-error
         (error-object->map response)]))})))

(defn change-pairing
  [args]
  (log/debug "[keycard] change-pairing")
  (keycard/change-pairing
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] change-pairing")
       (re-frame/dispatch
        [:keycard.callback/on-change-pairing-success response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] change-pairing")
       (re-frame/dispatch
        [:keycard.callback/on-change-pin-error
         (error-object->map response)]))})))

(defn unpair
  [args]
  (log/debug "[keycard] unpair")
  (keycard/unpair
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] unpair")
       (re-frame/dispatch
        [:keycard.callback/on-unpair-success response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] unpair")
       (re-frame/dispatch
        [:keycard.callback/on-unpair-error
         (error-object->map response)]))})))

(defn delete
  []
  (log/debug "[keycard] delete")
  (keycard/delete
   card
   {:on-success
    (fn [response]
      (log/debug "[keycard response succ] delete")
      (re-frame/dispatch
       [:keycard.callback/on-delete-success response]))
    :on-failure
    (fn [response]
      (log/debug "[keycard response fail] delete")
      (re-frame/dispatch
       [:keycard.callback/on-delete-error
        (error-object->map response)]))}))

(defn remove-key
  [args]
  (log/debug "[keycard] remove-key")
  (keycard/remove-key
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] remove-key")
       (re-frame/dispatch [:keycard.callback/on-remove-key-success
                           response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] remove-key")
       (re-frame/dispatch [:keycard.callback/on-remove-key-error
                           (error-object->map response)]))})))

(defn remove-key-with-unpair
  [args]
  (log/debug "[keycard] remove-key-with-unpair")
  (keycard/remove-key-with-unpair
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] remove-key-with-unpair")
       (re-frame/dispatch [:keycard.callback/on-remove-key-success
                           response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] remove-key-with-unpair")
       (re-frame/dispatch [:keycard.callback/on-remove-key-error
                           (error-object->map response)]))})))

(defn export-key
  [args]
  (log/debug "[keycard] export-key")
  (keycard/export-key
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] export-key")
       (re-frame/dispatch [:keycard.callback/on-export-key-success
                           response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] export-key")
       (re-frame/dispatch [:keycard.callback/on-export-key-error
                           (error-object->map response)]))})))

(defn unpair-and-delete
  [args]
  (log/debug "[keycard] unpair-and-delete")
  (keycard/unpair
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] unpair-and-delete")
       (re-frame/dispatch [:keycard.callback/on-unpair-and-delete-success
                           response]))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] unpair-and-delete")
       (re-frame/dispatch [:keycard.callback/on-unpair-and-delete-error
                           (error-object->map response)]))})))

(defn import-keys
  [{:keys [on-success] :as args}]
  (log/debug "[keycard] import-keys")
  (keycard/import-keys
   card
   (assoc
    args
    :on-success
    (fn [response]
      (log/debug "[keycard response succ] import-keys")
      (re-frame/dispatch
       [(or on-success :keycard.callback/on-generate-and-load-key-success)
        response]))
    :on-failure
    (fn [response]
      (log/warn "[keycard response fail] import-keys"
                (error-object->map response))
      (re-frame/dispatch [:keycard.callback/on-get-keys-error
                          (error-object->map response)])))))

(defn get-keys
  [{:keys [on-success] :as args}]
  (log/debug "[keycard] get-keys")
  (keycard/get-keys
   card
   (assoc
    args
    :on-success
    (fn [response]
      (log/debug "[keycard response succ] get-keys")
      (re-frame/dispatch
       [(or on-success :keycard.callback/on-get-keys-success)
        response]))
    :on-failure
    (fn [response]
      (log/warn "[keycard response fail] get-keys"
                (error-object->map response))
      (re-frame/dispatch [:keycard.callback/on-get-keys-error
                          (error-object->map response)])))))

(defn sign
  [{:keys [on-success on-failure] :as args}]
  (log/debug "[keycard] sign")
  (keycard/sign
   card
   (merge
    args
    {:on-success
     (fn [response]
       (log/debug "[keycard response succ] sign")
       (if on-success
         (on-success response)
         (re-frame/dispatch [:keycard.callback/on-sign-success response])))
     :on-failure
     (fn [response]
       (log/debug "[keycard response fail] sign")
       (if on-failure
         (on-failure response)
         (re-frame/dispatch
          [:keycard.callback/on-sign-error
           (error-object->map response)])))})))

(defn install-cash-applet
  []
  (log/debug "[keycard] install-cash-applet")
  (keycard/install-cash-applet
   card
   {:on-success
    (fn [response]
      (log/debug "[keycard response succ] install-cash-applet")
      (re-frame/dispatch
       [:keycard.callback/on-install-applet-success response]))
    :on-failure
    (fn [response]
      (log/debug "[keycard response fail] install-cash-applet")
      (re-frame/dispatch
       [:keycard.callback/on-install-applet-error
        (error-object->map response)]))}))

(defn sign-typed-data
  [{card-hash :hash}]
  (log/debug "[keycard] sign-typed-data")
  (keycard/sign-typed-data
   card
   {:hash card-hash
    :on-success
    (fn [response]
      (log/debug "[keycard response succ] sign-typed-data")
      (re-frame/dispatch [:keycard.callback/on-sign-success
                          response]))
    :on-failure
    (fn [response]
      (log/debug "[keycard response fail] sign-typed-data")
      (re-frame/dispatch
       [:keycard.callback/on-sign-error
        (error-object->map response)]))}))

(defn save-multiaccount-and-login
  [args]
  (keycard/save-multiaccount-and-login card args))

(defn login
  [args]
  (keycard/login card args))

(defn send-transaction-with-signature
  [args]
  (keycard/send-transaction-with-signature card args))

(defn start-nfc
  [args]
  (keycard/start-nfc card args))

(defn stop-nfc
  [args]
  (keycard/stop-nfc card args))

(defn set-nfc-message
  [args]
  (keycard/set-nfc-message card args))

(defn delete-multiaccount-before-migration
  [args]
  (keycard/delete-multiaccount-before-migration card args))
