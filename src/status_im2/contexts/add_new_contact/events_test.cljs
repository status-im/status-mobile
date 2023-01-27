(ns status-im2.contexts.add-new-contact.events-test
  (:require [cljs.test :refer-macros [deftest is]]
            [status-im2.contexts.add-new-contact.events :as core]))

(def ukey
  "0x045596a7ff87da36860a84b0908191ce60a504afc94aac93c1abd774f182967ce694f1bf2d8773cd59f4dd0863e951f9b7f7351c5516291a0fceb73f8c392a0e88")
(def ckey "zQ3shWj4WaBdf2zYKCkXe6PHxDxNTzZyid1i75879Ue9cX9gA")
(def ens "esep")

(deftest identify-empty-string
  (is (= (core/identify-type "")
         {:input    ""
          :id       nil
          :type     :empty
          :ens-name nil})))

(deftest search-empty-string
  (is (= (core/set-new-identity {:db {:contacts/new-identity :foo}} "")
         {:db {}})))

(deftest identify-uncompressed-key
  (is (= (core/identify-type ukey)
         {:input    ukey
          :id       ukey
          :type     :public-key
          :ens-name nil})))

(deftest search-uncompressed-key
  (is (= (core/set-new-identity {:db {}} ukey)
         {:db {:contacts/new-identity
               {:input      ukey
                :public-key ukey
                :state      :error
                :error      :uncompressed-key}}})))

(deftest identify-ens
  (let [ens-name (str ens ".stateofus.eth")]
    (is (= (core/identify-type ens)
           {:input    ens
            :id       ens
            :type     :ens-name
            :ens-name ens-name}))))

(deftest identify-compressed-key
  (is (= (core/identify-type ckey)
         {:input    ckey
          :id       ckey
          :type     :compressed-key
          :ens-name nil})))

(deftest identify-link-key
  (let [input (str "https://join.status.im/u/" ckey)]
    (is (= (core/identify-type input)
           {:input    input
            :id       ckey
            :type     :compressed-key
            :ens-name nil}))))

