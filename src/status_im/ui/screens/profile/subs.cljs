(ns status-im.ui.screens.profile.subs
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.utils.build :as build]
            [status-im.utils.platform :as platform]))

(defn- node-version [{:keys [web3-node-version]}]
  (str "status-go v" (or web3-node-version "N/A") ""))

(def app-short-version
  (let [version (if platform/desktop? build/version build/build-no)]
    (str build/version " (" version ")")))

(re-frame/reg-sub
 :get-profile-unread-messages-number
 :<- [:account/account]
 (fn [{:keys [seed-backed-up? mnemonic]}]
   (if (or seed-backed-up? (string/blank? mnemonic)) 0 1)))

(re-frame/reg-sub
 :get-app-version
 (fn [db]
   (str app-short-version "; " (node-version db))))

(re-frame/reg-sub
 :get-app-short-version
 (fn [db] app-short-version))

(re-frame/reg-sub
 :get-app-node-version
 node-version)

(re-frame/reg-sub
 :get-device-UUID
 (fn [db]
   (:device-UUID db)))

(re-frame/reg-sub
 :my-profile/recovery
 (fn [db]
   (or (:my-profile/seed db) {:step :intro})))
