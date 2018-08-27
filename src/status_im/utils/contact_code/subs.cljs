(ns status-im.utils.contact-code.subs
  (:require
   [re-frame.core :as re-frame]
   [status-im.utils.contact-code.model :as contact-code]))

(re-frame/reg-sub
 :get-contact-code
 (fn [db _]
   (contact-code/fetch {:db db})))
