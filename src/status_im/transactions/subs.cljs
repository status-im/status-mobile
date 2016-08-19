(ns status-im.transactions.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-sub subscribe]]
            [clojure.string :as s]))

(register-sub :transactions
  (fn [db]
    (reaction (vals (:transactions @db)))))

(register-sub :contacts-by-address
  (fn [db]
    (reaction (into {} (map (fn [[_ {:keys [address] :as contact}]]
                              [address contact])

                            (:contacts @db)
                            )))))

(register-sub :contact-by-address
  (fn [_ [_ address]]
    (let [contacts (subscribe [:contacts-by-address])
          address' (when address
                     (if (s/starts-with? address "0x")
                       (subs address 2)
                       address))]
      (reaction (@contacts address')))))

(register-sub :wrong-password?
  (fn [db] (reaction (:wrong-password? @db))))
