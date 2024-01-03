(ns status-im.contexts.chat.home.add-new-contact.events-test
  (:require
    [cljs.test :refer-macros [deftest are]]
    matcher-combinators.test
    [status-im.contexts.chat.home.add-new-contact.events :as events]))

(def user-ukey
  "0x04ca27ed9c7c4099d230c6d8853ad0cfaf084a019c543e9e433d3c04fac6de9147cf572b10e247cfe52f396b5aa10456b56dd1cf1d8a681e2b93993d44594b2e85")
(def user-ckey "zQ3shtFEo4PxpQiYGcNZZ8xhJmhD6WBXwnHPBueu5SRnvPXjk")
(def ukey
  "0x045596a7ff87da36860a84b0908191ce60a504afc94aac93c1abd774f182967ce694f1bf2d8773cd59f4dd0863e951f9b7f7351c5516291a0fceb73f8c392a0e88")
(def ckey "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA")
(def ens "esep")
(def ens-stateofus-eth (str ens ".stateofus.eth"))
(def link-ckey (str "https://status.app/u#" ckey))
(def link-ckey-with-encoded-data (str "https://status.app/u/CwSACgcKBVBhdmxvAw==#" ckey))
(def link-ens (str "https://status.app/u#" ens))

;;; unit tests (no app-db involved)

(deftest validate-contact-test
  (are [i e] (match? (events/validate-contact (events/init-contact
                                               {:user-public-key user-ukey
                                                :input           i}))
                     (events/init-contact e))

   ""                          {:user-public-key user-ukey
                                :input           ""
                                :type            :empty
                                :state           :empty}

   " "                         {:user-public-key user-ukey
                                :input           " "
                                :type            :empty
                                :state           :empty}

   ukey                        {:user-public-key user-ukey
                                :input           ukey
                                :id              ukey
                                :type            :public-key
                                :public-key      ukey
                                :state           :invalid
                                :msg             :t/not-a-chatkey}

   ens                         {:user-public-key user-ukey
                                :input           ens
                                :id              ens
                                :type            :ens
                                :ens             ens-stateofus-eth
                                :state           :resolve-ens}

   (str " " ens)               {:user-public-key user-ukey
                                :input           (str " " ens)
                                :id              ens
                                :type            :ens
                                :ens             ens-stateofus-eth
                                :state           :resolve-ens}

   ckey                        {:user-public-key user-ukey
                                :input           ckey
                                :id              ckey
                                :type            :compressed-key
                                :state           :decompress-key}

   link-ckey                   {:user-public-key user-ukey
                                :input           link-ckey
                                :id              ckey
                                :type            :compressed-key
                                :state           :decompress-key}
   link-ckey-with-encoded-data {:user-public-key user-ukey
                                :input           link-ckey-with-encoded-data
                                :id              ckey
                                :type            :compressed-key
                                :state           :decompress-key}

   link-ens                    {:user-public-key user-ukey
                                :input           link-ens
                                :id              ens
                                :type            :ens
                                :ens             ens-stateofus-eth
                                :state           :resolve-ens}))

;;; event handler tests (no callbacks)

(def db
  {:profile/profile          {:public-key user-ukey}
   :networks/current-network "mainnet_rpc"
   :networks/networks        {"mainnet_rpc"
                              {:id     "mainnet_rpc"
                               :config {:NetworkId 1}}}})

(deftest set-new-identity-test
  (with-redefs [events/dispatcher (fn [& args] args)]
    (are [i edb] (match? (events/set-new-identity {:db db} i nil) edb)

     ""        {:db db}

     ukey      {:db (assoc db
                           :contacts/new-identity
                           (events/init-contact
                            {:user-public-key user-ukey
                             :input           ukey
                             :id              ukey
                             :type            :public-key
                             :public-key      ukey
                             :state           :invalid
                             :msg             :t/not-a-chatkey}))}

     ens       {:db (assoc db
                           :contacts/new-identity
                           (events/init-contact
                            {:user-public-key user-ukey
                             :input           ens
                             :id              ens
                             :type            :ens
                             :ens             ens-stateofus-eth
                             :public-key      nil ; not yet...
                             :state           :resolve-ens}))
                :effects.contacts/resolve-public-key-from-ens
                {:chain-id   1
                 :ens        ens-stateofus-eth
                 :on-success [:contacts/set-new-identity-success ens]
                 :on-error   [:contacts/set-new-identity-error ens]}}

     ;; compressed-key & add-self-as-contact
     user-ckey {:db (assoc db
                           :contacts/new-identity
                           (events/init-contact
                            {:user-public-key user-ukey
                             :input           user-ckey
                             :id              user-ckey
                             :type            :compressed-key
                             :public-key      nil ; not yet...
                             :state           :decompress-key}))
                :effects.contacts/decompress-public-key
                {:compressed-key user-ckey
                 :on-success     [:contacts/set-new-identity-success user-ckey]
                 :on-error       [:contacts/set-new-identity-error user-ckey]}})))
