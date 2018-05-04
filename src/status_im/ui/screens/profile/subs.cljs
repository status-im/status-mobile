(ns status-im.ui.screens.profile.subs
  (:require [re-frame.core :refer [reg-sub]]
            [clojure.string :as string]))

(reg-sub
 :get-profile-unread-messages-number
 :<- [:get-current-account]
 (fn [{:keys [seed-backed-up? mnemonic]}]
   (if (or seed-backed-up? (string/blank? mnemonic)) 0 1)))
