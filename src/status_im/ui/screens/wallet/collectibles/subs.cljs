(ns status-im.ui.screens.wallet.collectibles.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub :collectibles
                  (fn [db [_ s]]
                    (vals (get-in db [:collectibles s]))))
