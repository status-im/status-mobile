(ns syng-im.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub]]))

(register-sub
  :get-greeting
  (fn [db _]
    (reaction
      (get @db :greeting))))

;; -- User data --------------------------------------------------------------
(register-sub
  :get-user-phone-number
  (fn [db _]
    (reaction
      (get @db :user-phone-number))))

(register-sub
  :get-user-identity
  (fn [db _]
    (reaction
      (get @db :user-identity))))

(register-sub
  :get-loading
  (fn [db _]
    (reaction
      (get @db :loading))))

(register-sub
  :get-confirmation-code
  (fn [db _]
    (reaction
      (get @db :confirmation-code))))

(register-sub
  :get-contacts
  (fn [db _]
    (reaction
      (get @db :contacts))))
