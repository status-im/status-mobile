(ns status-im2.contexts.add-new-contact.events-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im2.contexts.add-new-contact.events :as core]))

(def init-db
  {:networks/current-network "mainnet_rpc"
   :networks/networks        {"mainnet_rpc"
                              {:id     "mainnet_rpc"
                               :config {:NetworkId 1}}}})

(defn search-db
  [input]
  {:contacts/new-identity {:input      input
                           :public-key nil
                           :ens-name   nil
                           :state      :searching
                           :error      nil}})

(deftest search-empty-string
  (let [input    ""
        expected {:db init-db}
        actual   (core/set-new-identity {:db init-db} input)]
    (is (= actual expected))))

(deftest search-ens
  (let [input    "esep"
        clean    (fn [db]
                   (-> db
                       (assoc-in [:contacts/resolve-public-key-from-ens-name :on-success] nil)
                       (assoc-in [:contacts/resolve-public-key-from-ens-name :on-error] nil)))
        expected {:db                                        (merge init-db (search-db input))
                  :contacts/resolve-public-key-from-ens-name
                  {:chain-id   1
                   :ens-name   (str input ".stateofus.eth")
                   :on-success nil
                   :on-error   nil}}
        actual   (core/set-new-identity {:db init-db} input)]
    (is (= (clean actual) expected))))

(deftest search-compressed-key
  (let [input    "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA"
        clean    (fn [db]
                   (-> db
                       (assoc-in [:contacts/decompress-public-key :on-success] nil)
                       (assoc-in [:contacts/decompress-public-key :on-error] nil)))
        expected {:db                             (merge init-db (search-db input))
                  :contacts/decompress-public-key
                  {:public-key input
                   :on-success nil
                   :on-error   nil}}
        actual   (core/set-new-identity {:db init-db} input)]
    (is (= (clean actual) expected))))

