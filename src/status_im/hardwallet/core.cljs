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
   (navigation/navigate-to-cofx :hardwallet/connect nil)))

(defn hardwallet-supported? [db]
  (and config/hardwallet-enabled?
       platform/android?
       (get-in db [:hardwallet :nfc-supported?])))

(defn return-back-from-nfc-settings [app-coming-from-background? {:keys [db]}]
  (when (and app-coming-from-background?
             (= :hardwallet/connect (:view-id db)))
    {:hardwallet/check-nfc-enabled nil}))

(re-frame/reg-fx
 :hardwallet/check-nfc-support
 check-nfc-support)

(re-frame/reg-fx
 :hardwallet/check-nfc-enabled
 check-nfc-enabled)

(re-frame/reg-fx
 :hardwallet/open-nfc-settings
 open-nfc-settings)
