(ns status-im.contexts.profile.utils-test
  (:require
    [cljs.test :refer [are deftest]]
    [status-im.contexts.profile.utils :as sut]))

(deftest displayed-name-test
  (are [expected arg] (= expected (sut/displayed-name arg))
   "Display Name"   {:display-name "Display Name"}
   "Primary Name"   {:primary-name "Primary Name"}
   "Alias"          {:alias "Alias"}
   "Name"           {:name "Name"}
   nil              {:preferred-name "  "}
   "Preferred Name" {:preferred-name "Preferred Name"}
   "Preferred Name" {:preferred-name "Preferred Name" :display-name "Display Name"}
   "Preferred Name" {:preferred-name "Preferred Name" :display-name "Display Name" :name "Name"}
   "Display Name"   {:ens-verified true :name "Name" :display-name "Display Name"}
   "Name"           {:ens-verified true :name "Name" :display-name "  "}
   "Name"           {:ens-verified true :name "Name"}
   "Name"           {:name "Name"}))
