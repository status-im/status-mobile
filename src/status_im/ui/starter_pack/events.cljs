(ns status-im.ui.starter-pack.events
  (:require [re-frame.core :as re-frame]
            [status-im.utils.handlers :as handlers]))

(re-frame/reg-sub
 ::starter-pack-state
 (fn [db]
   (get-in db [:iap/payment :starter-pack])))

(handlers/register-handler-fx
 ::close-starter-pack
 (fn [{:keys [db]} _]
   (assoc-in db [:iap/payment :starter-pack] :hidden)))
