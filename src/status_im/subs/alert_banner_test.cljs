(ns status-im.subs.alert-banner-test
  (:require
    [cljs.test :refer [is testing]]
    [re-frame.db :as rf-db]
    status-im.subs.activity-center
    [test-helpers.unit :as h]
    [utils.re-frame :as rf]))

(h/deftest-sub :alert-banners/top-margin
  [sub-name]
  (testing "returns 50 when only alert banner"
    (swap! rf-db/app-db assoc
      :alert-banners
      {:alert {:text "Alert"
               :type :alert}})
    (is (= (rf/sub [sub-name]) 50)))

  (testing "returns 50 when only error banner"
    (swap! rf-db/app-db assoc
      :alert-banners
      {:error {:text "Error"
               :type :error}})
    (is (= (rf/sub [sub-name]) 50)))

  (testing "returns 90 when both alert and error banner"
    (swap! rf-db/app-db assoc
      :alert-banners
      {:alert {:text "Alert"
               :type :alert}
       :error {:text "Error"
               :type :error}})
    (is (= (rf/sub [sub-name]) 90))))
