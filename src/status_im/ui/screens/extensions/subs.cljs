(ns status-im.ui.screens.extensions.subs
  (:require [re-frame.core :as re-frame]
            status-im.ui.screens.extensions.add.subs))

(re-frame/reg-sub
 :extensions/all-extensions
 :<- [:get :account/account]
 (fn [account]
   (get account :extensions)))

