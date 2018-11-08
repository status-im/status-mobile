(ns status-im.ui.screens.pairing.subs
  (:require [re-frame.core :as re-frame]
            [status-im.utils.ethereum.core :as ethereum]))

(re-frame/reg-sub :pairing/installations
                  :<- [:get :pairing/installations]
                  (fn [k]
                    (->> k
                         vals
                         (filter :device-type))))
