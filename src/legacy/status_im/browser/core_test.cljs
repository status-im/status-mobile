(ns legacy.status-im.browser.core-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.browser.core :as browser]
    [utils.url :as url]))

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

(defn get-dapp-id
  [result dapp-url]
  (some #(when (= (url/normalize-and-decode-url dapp-url) (first (:history %))) (:browser-id %))
        (vals (get-in result [:db :browser/browsers]))))

(deftest browser-test
  (let [dapp1-url "cryptokitties.co"
        dapp2-url "http://test2.com"]

    (testing "user opens a dapp"
      (let [result-open (browser/open-url {:db {} :now 1} dapp1-url)
            dapp1-id    (get-dapp-id result-open dapp1-url)]
        (is (= dapp1-id (get-in result-open [:db :browser/options :browser-id]))
            "browser-id should be dapp1-url")
        (is (not (has-wrong-properties? result-open
                                        dapp1-id
                                        {:browser-id    dapp1-id
                                         :history-index 0
                                         :history       ["https://cryptokitties.co"]
                                         :dapp?         false
                                         :name          "Browser"}))
            "some properties of the browser are not correct")

        (testing "then a second dapp"
          (let [result-open-2 (browser/open-url {:db  (:db result-open)
                                                 :now 2}
                                                dapp2-url)
                dapp2-id      (get-dapp-id result-open-2 dapp2-url)]
            (is (= dapp2-id (get-in result-open-2 [:db :browser/options :browser-id]))
                "browser-id should be dapp2 host")
            (is (not (has-wrong-properties? result-open-2
                                            dapp2-id
                                            {:browser-id    dapp2-id
                                             :history-index 0
                                             :history       ["http://test2.com"]
                                             :dapp?         false}))
                "some properties of the browser are not correct")

            (testing "then removes the second dapp"
              (let [result-remove-2 (browser/remove-browser {:db (:db result-open-2)} dapp2-id)]
                (is (= #{dapp1-id}
                       (set (keys (get-in result-remove-2 [:db :browser/browsers]))))
                    "the second dapp shouldn't be in the browser list anymore")))))

        (testing "then opens the dapp again"
          (let [result-open-existing (browser/open-existing-browser {:db  (:db result-open)
                                                                     :now 2}
                                                                    dapp1-id)
                dapp1-url2           (str "https://" dapp1-url "/nav2")]
            (is (not (has-wrong-properties? result-open-existing
                                            dapp1-id
                                            {:browser-id    dapp1-id
                                             :history-index 0
                                             :history       ["https://cryptokitties.co"]
                                             :dapp?         false
                                             :name          "Browser"}))
                "some properties of the browser are not correct")
            (is (nil? (browser/navigate-to-next-page result-open-existing))
                "nothing should happen if user tries to navigate to next page")
            (is (nil? (browser/navigate-to-previous-page result-open-existing))
                "nothing should happen if user tries to navigate to previous page")

            (testing "then navigates to a new url in the dapp"
              (let [result-navigate (browser/navigation-state-changed
                                     {:db  (:db result-open-existing)
                                      :now 4}
                                     (clj->js {"url"     dapp1-url2
                                               "loading" false})
                                     false)]
                (is (not (has-wrong-properties? result-navigate
                                                dapp1-id
                                                {:browser-id    dapp1-id
                                                 :history-index 1
                                                 :history       ["https://cryptokitties.co" dapp1-url2]
                                                 :dapp?         false
                                                 :name          "Browser"}))
                    "some properties of the browser are not correct")

                (testing "then navigates to previous page"
                  (let [result-previous (browser/navigate-to-previous-page {:db  (:db result-navigate)
                                                                            :now 5})]
                    (is
                     (not (has-wrong-properties? result-previous
                                                 dapp1-id
                                                 {:browser-id    dapp1-id
                                                  :history-index 0
                                                  :history       ["https://cryptokitties.co" dapp1-url2]
                                                  :dapp?         false
                                                  :name          "Browser"}))
                     "some properties of the browser are not correct")

                    (testing "then navigates to next page")
                    (let [result-next (browser/navigate-to-next-page {:db  (:db result-previous)
                                                                      :now 6})]
                      (is (not
                           (has-wrong-properties? result-next
                                                  dapp1-id
                                                  {:browser-id    dapp1-id
                                                   :history-index 1
                                                   :history       ["https://cryptokitties.co" dapp1-url2]
                                                   :dapp?         false
                                                   :name          "Browser"}))
                          "some properties of the browser are not correct"))))))))))))
