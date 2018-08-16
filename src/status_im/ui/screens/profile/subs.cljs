(ns status-im.ui.screens.profile.subs
  (:require [re-frame.core :refer [reg-sub]]
            [clojure.string :as string]
            [status-im.utils.build :as build]
            [status-im.utils.platform :as platform]))

(reg-sub
 :get-profile-unread-messages-number
 :<- [:get-current-account]
 (fn [{:keys [seed-backed-up? mnemonic]}]
   (if (or seed-backed-up? (string/blank? mnemonic)) 0 1)))

(reg-sub
 :get-app-version
 (fn [{:keys [web3-node-version]}]
   (let [version (if platform/desktop? build/version build/build-no)]
     (str build/version " (" version "); node " (or web3-node-version "N/A") ""))))

(reg-sub :get-device-UUID
         (fn [db]
           (:device-UUID db)))
