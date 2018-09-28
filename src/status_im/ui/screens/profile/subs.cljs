(ns status-im.ui.screens.profile.subs
  (:require [re-frame.core :as re-frame]
            [clojure.string :as string]
            [status-im.utils.build :as build]
            [status-im.utils.platform :as platform]))

(re-frame/reg-sub
 :get-profile-unread-messages-number
 :<- [:get-current-account]
 (fn [{:keys [seed-backed-up? mnemonic]}]
   (if (or seed-backed-up? (string/blank? mnemonic)) 0 1)))

(re-frame/reg-sub
 :get-app-version
 (fn [{:keys [web3-node-version]}]
   (let [version (if platform/desktop? build/version build/build-no)]
     (str build/version " (" version "); node " (or web3-node-version "N/A") ""))))

(re-frame/reg-sub
 :get-device-UUID
 (fn [db]
   (:device-UUID db)))

(re-frame/reg-sub
 :my-profile/recovery
 (fn [db]
   (or (:my-profile/seed db) {:step :intro})))
