(ns status-im.transactions.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]
            [status-im.utils.hex :as i]))

(reg-sub :transactions
  (fn [db]
    (vals (:transactions db))))

(reg-sub :contacts-by-address
  (fn [db]
    (into {} (map (fn [[_ {:keys [address] :as contact}]]
                    (when address
                      [address contact]))
                  (:contacts/contacts db)))))

(reg-sub :contact-by-address
  :<- [:contacts-by-address]
  (fn [contacts [_ address]]
    (let [address' (when address
                     (i/normalize-hex address))]
      (contacts address'))))

(reg-sub :wrong-password?
  (fn [db] (:wrong-password? db)))
