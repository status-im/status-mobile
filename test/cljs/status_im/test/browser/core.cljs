(ns status-im.test.browser.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.browser.core :as browser]
            [status-im.utils.types :as types]
            [status-im.utils.handlers-macro :as handlers-macro]))

(defn has-navigated-to-browser? [result]
  (and (= (get result :status-im.ui.screens.navigation/navigate-to)
          :browser)
       (= (get-in result [:db :view-id])
          :browser)))

(defn has-wrong-properties?
  [result dapp-url expected-browser]
  (let [browser (get-in result [:db :browser/browsers dapp-url])]
    (reduce (fn [acc k]
              (if (= (k browser)
                     (k expected-browser))
                acc
                (conj acc [k (str "was expecting " (k expected-browser) " got " (k browser))])))
            nil
            (keys expected-browser))))

(deftest browser-test
  (let [dapp1-url "cryptokitties.co"
        dapp2-url "http://test2.com"]

    (testing "user opens a dapp"
      (let [result-open (browser/open-url dapp1-url {:now 1})]
        (is (= dapp1-url (get-in result-open [:db :browser/options :browser-id]))
            "browser-id should be dapp1-url")
        (is (has-navigated-to-browser? result-open)
            "should navigate to :browser")
        (is (not (has-wrong-properties? result-open
                                        dapp1-url
                                        {:browser-id "cryptokitties.co"
                                         :history-index 0
                                         :history ["http://cryptokitties.co"]
                                         :dapp? true
                                         :name "CryptoKitties"
                                         :timestamp 1}))
            "some properties of the browser are not correct")

        (testing "then a second dapp"
          (let [result-open-2 (browser/open-url dapp2-url {:db (:db result-open)
                                                           :now 2})
                dapp2-host "test2.com"]
            (is (= dapp2-host (get-in result-open-2 [:db :browser/options :browser-id]))
                "browser-id should be dapp2 host")
            (is (has-navigated-to-browser? result-open-2)
                "should navigate to :browser")
            (is (not (has-wrong-properties? result-open-2
                                            dapp2-host
                                            {:browser-id "test2.com"
                                             :history-index 0
                                             :history ["http://test2.com"]
                                             :dapp? false
                                             :timestamp 2}))
                "some properties of the browser are not correct")

            (testing "then removes the second dapp"
              (let [result-remove-2 (browser/remove-browser dapp2-host {:db (:db result-open-2)})]
                (is (= #{dapp1-url}
                       (set (keys (get-in result-remove-2 [:db :browser/browsers]))))
                    "the second dapp shouldn't be in the browser list anymore")))))

        (testing "then opens the dapp again"
          (let [result-open-existing (browser/open-existing-browser dapp1-url {:db (:db result-open)
                                                                               :now 2})
                dapp1-url2 (str "http://" dapp1-url "/nav2")
                browser (get-in result-open-existing [:db :browser/browsers dapp1-url])]
            (is (not (has-wrong-properties? result-open-existing
                                            dapp1-url
                                            {:browser-id "cryptokitties.co"
                                             :history-index 0
                                             :history ["http://cryptokitties.co"]
                                             :dapp? true
                                             :name "CryptoKitties"
                                             :timestamp 2}))
                "some properties of the browser are not correct")
            (is (nil? (browser/navigate-to-next-page result-open-existing))
                "nothing should happen if user tries to navigate to next page")
            (is (nil? (browser/navigate-to-previous-page result-open-existing))
                "nothing should happen if user tries to navigate to previous page")

            (testing "then navigates to a new url in the dapp"
              (let [result-navigate (browser/navigation-state-changed
                                     (clj->js {"url" dapp1-url2
                                               "loading" false})
                                     false
                                     {:db (:db result-open-existing)
                                      :now 4})]
                (is (not (has-wrong-properties? result-navigate
                                                dapp1-url
                                                {:browser-id "cryptokitties.co"
                                                 :history-index 1
                                                 :history ["http://cryptokitties.co" dapp1-url2]
                                                 :dapp? true
                                                 :name "CryptoKitties"
                                                 :timestamp 4}))
                    "some properties of the browser are not correct")

                (testing "then navigates to previous page"
                  (let [result-previous (browser/navigate-to-previous-page {:db (:db result-navigate)
                                                                            :now 5})]
                    (is (not (has-wrong-properties? result-previous
                                                    dapp1-url
                                                    {:browser-id "cryptokitties.co"
                                                     :history-index 0
                                                     :history ["http://cryptokitties.co" dapp1-url2]
                                                     :dapp? true
                                                     :name "CryptoKitties"
                                                     :timestamp 5}))
                        "some properties of the browser are not correct")

                    (testing "then navigates to next page")
                    (let [result-next (browser/navigate-to-next-page {:db (:db result-previous)
                                                                      :now 6})]
                      (is (not (has-wrong-properties? result-next
                                                      dapp1-url
                                                      {:browser-id "cryptokitties.co"
                                                       :history-index 1
                                                       :history ["http://cryptokitties.co" dapp1-url2]
                                                       :dapp? true
                                                       :name "CryptoKitties"
                                                       :timestamp 6}))
                          "some properties of the browser are not correct"))))))))))))
