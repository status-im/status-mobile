(ns status-im.contact-code.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :contact-codes/contact-codes
 (fn [db]
   (:contact-codes/contact-codes db)))

(re-frame/reg-sub
 :contact-codes/contact-code
 :<- [:contact-codes/contact-codes]
 (fn [contact-codes [_ public-key]]
   (get contact-codes public-key)))

(re-frame/reg-sub
 :contact-codes/current-contact-code
 :<- [:chats/current-chat]
 :<- [:contact-codes/contact-codes]
 (fn [[{:keys [chat-id]} contact-codes]]
   (get contact-codes chat-id)))
