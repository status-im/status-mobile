(ns status-im.ui.screens.pairing.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.ethereum.core :as ethereum]))

(re-frame/reg-sub :pairing/installations
                  :<- [:get :pairing/installations]
                  (fn [installations]
                    (->> installations
                         vals
                         (sort-by (comp unchecked-negate :last-paired)))))

(re-frame/reg-sub :pairing/installation-id
                  :<- [:get :account/account]
                  :installation-id)
