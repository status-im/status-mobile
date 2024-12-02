(ns status-im.common.alert-banner.events-test
  (:require
    [cljs.test :refer [deftest is testing]]
    matcher-combinators.test
    [status-im.common.alert-banner.events :as events]))

(deftest add-alert-banner-test
  (testing "Alert banner is added"
    (is (match? {:db                {:alert-banners
                                     {:alert {:text "Alert"
                                              :type :alert}}}
                 :show-alert-banner [nil nil]}
                (events/add-alert-banner {:db {}}
                                         [{:text "Alert"
                                           :type :alert}])))))

(deftest remove-alert-banner-test
  (testing "Alert banner is removed"
    (is (match? {:db                {}
                 :hide-alert-banner [nil nil]}
                (events/remove-alert-banner {:db {:alert-banners
                                                  {:alert {:text "Alert"
                                                           :type :alert}}}}
                                            [:alert]))))
  (testing "Alert banner is not removed"
    (is (match? {:db {:alert-banners
                      {:alert {:text "Alert"
                               :type :alert}}}}
                (events/remove-alert-banner {:db {:alert-banners
                                                  {:alert {:text "Alert"
                                                           :type :alert}}}}
                                            [:error])))))

(deftest remove-all-alert-banners-test
  (testing "All Alert banners are removed"
    (is (match? {:db                {}
                 :hide-alert-banner [nil nil]}
                (events/remove-all-alert-banners {:db {:alert-banners
                                                       {:alert {:text "Alert"
                                                                :type :alert}
                                                        :error {:text "Error"
                                                                :type :error}}}})))))
