(ns status-im.ui.starter-pack.events
  (:require [re-frame.core :as re-frame]
            [status-im.popover.core :as popover]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]))

(re-frame/reg-sub
 ::starter-pack-state
 (fn [db]
   (get-in db [:iap/payment :starter-pack])))

(fx/defn close-starter-pack
  {:events [::close-starter-pack]}
  [{:keys [db]}]
  {:db (assoc-in db [:iap/payment :starter-pack] :hidden)})

(fx/defn success-buy
  {:events [::success-buy]}
  [cofx]
  (fx/merge
   ;; TODO: Wait for tx to be mined, on success refresh wallet
   (close-starter-pack)
   (popover/show-popover {:view :starter-pack-success})))
