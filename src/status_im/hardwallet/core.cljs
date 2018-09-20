(ns status-im.hardwallet.core
  (:require-macros [status-im.utils.handlers-macro :as handlers-macro])
  (:require [status-im.utils.platform :as platform]
            [status-im.utils.config :as config]
            [status-im.react-native.js-dependencies :as js-dependencies]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.navigation :as navigation]))

(defn check-nfc-support []
  (when config/hardwallet-enabled?
    (.. js-dependencies/nfc-manager
        -default
        isSupported
        (then #(re-frame/dispatch [:hardwallet.callback/check-nfc-support-success %])))))

(defn check-nfc-enabled []
  (when platform/android?
    (.. js-dependencies/nfc-manager
        -default
        isEnabled
        (then #(re-frame/dispatch [:hardwallet.callback/check-nfc-enabled-success %])))))

(defn set-nfc-support [supported? {:keys [db]}]
  {:db (assoc-in db [:hardwallet :nfc-supported?] supported?)})

(defn set-nfc-enabled [enabled? {:keys [db]}]
  {:db (assoc-in db [:hardwallet :nfc-enabled?] enabled?)})

(defn open-nfc-settings []
  (when platform/android?
    (.. js-dependencies/nfc-manager
        -default
        goToNfcSetting)))

(defn navigate-to-connect-screen [cofx]
  (handlers-macro/merge-fx
   cofx
   {:hardwallet/check-nfc-enabled nil}
   (navigation/navigate-to-cofx :hardwallet-connect nil)))

(defn hardwallet-supported? [db]
  (and config/hardwallet-enabled?
       platform/android?
       (get-in db [:hardwallet :nfc-supported?])))

(defn return-back-from-nfc-settings [app-coming-from-background? {:keys [db]}]
  (when (and app-coming-from-background?
             (= :hardwallet-connect (:view-id db)))
    {:hardwallet/check-nfc-enabled nil}))

(defn- proceed-to-pin-confirmation [fx]
  (assoc-in fx [:db :hardwallet :pin :enter-step] :confirmation))

(defn- pin-match [fx]
  (assoc-in fx [:db :hardwallet :pin :status] :validating))

(defn- pin-mismatch [fx]
  (assoc-in fx [:db :hardwallet :pin] {:status       :error
                                       :error        :t/pin-mismatch
                                       :original     []
                                       :confirmation []
                                       :enter-step   :original}))

(defn process-pin-input [number enter-step {:keys [db]}]
  (let [db' (update-in db [:hardwallet :pin enter-step] conj number)
        numbers-entered (count (get-in db' [:hardwallet :pin enter-step]))]
    (cond-> {:db (assoc-in db' [:hardwallet :pin :status] nil)}
      (and (= enter-step :original)
           (= 6 numbers-entered))
      (proceed-to-pin-confirmation)

      (and (= enter-step :confirmation)
           (= (get-in db' [:hardwallet :pin :original])
              (get-in db' [:hardwallet :pin :confirmation])))
      (pin-match)

      (and (= enter-step :confirmation)
           (= 6 numbers-entered)
           (not= (get-in db' [:hardwallet :pin :original])
                 (get-in db' [:hardwallet :pin :confirmation])))
      (pin-mismatch))))

(re-frame/reg-fx
 :hardwallet/check-nfc-support
 check-nfc-support)

(re-frame/reg-fx
 :hardwallet/check-nfc-enabled
 check-nfc-enabled)

(re-frame/reg-fx
 :hardwallet/open-nfc-settings
 open-nfc-settings)
