(ns status-im2.contexts.add-new-contact.events-test
  (:require [cljs.test :refer-macros [deftest is are]]
            [status-im2.contexts.add-new-contact.events :as core]))

(def ukey
  "0x045596a7ff87da36860a84b0908191ce60a504afc94aac93c1abd774f182967ce694f1bf2d8773cd59f4dd0863e951f9b7f7351c5516291a0fceb73f8c392a0e88")
(def ckey "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA")
(def ens "esep")
(def ens-stateofus-eth (str ens ".stateofus.eth"))
(def link-ckey (str "https://join.status.im/u/" ckey))
(def link-ens (str "https://join.status.im/u/" ens))

(deftest identify-type-test
  (are [input expected] (= (core/identify-type input) expected)
   ""        {:input    ""
              :id       nil
              :type     :empty
              :ens-name nil}

   ukey      {:input    ukey
              :id       ukey
              :type     :public-key
              :ens-name nil}

   ens       {:input    ens
              :id       ens
              :type     :ens-name
              :ens-name ens-stateofus-eth}

   ckey      {:input    ckey
              :id       ckey
              :type     :compressed-key
              :ens-name nil}

   link-ckey {:input    link-ckey
              :id       ckey
              :type     :compressed-key
              :ens-name nil}

   link-ens  {:input    link-ens
              :id       ens
              :type     :ens-name
              :ens-name ens-stateofus-eth}))

(deftest search-empty-string-test
  (is (= (core/set-new-identity {:db {:contacts/new-identity :foo}} "")
         {:db {}})))

(deftest search-uncompressed-key-test
  (is (= (core/set-new-identity {:db {}} ukey)
         {:db {:contacts/new-identity
               {:input      ukey
                :public-key ukey
                :state      :error
                :error      :uncompressed-key}}})))

