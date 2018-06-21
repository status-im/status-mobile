(ns status-im.test.models.browser-history
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.models.browser-history :as model]
            [re-frame.core :as re-frame]))

(def test-history ["http://oldest-site-visited.com", "http://most-recent-site-visited.com"])
(def test-browser-id "1234567890")

(deftest dont-store-history-on-nav-change?-test
  (testing "dont-store-history-on-nav-change?"
    (let [db {:browser/options {:dont-store-history-on-nav-change? true}}]
      (is (model/dont-store-history-on-nav-change? db)))))

(defn fake-dispatch-dont-store-history-on-nav-change! [event]
  (is (= :update-browser-options (get event 0)))
  (let [eventMap (get event 1)]
    (is (= (:dont-store-history-on-nav-change? eventMap) true))))

(deftest dont-store-history-on-nav-change!-test
  (testing "dont-store-history-on-nav-change!"
    (with-redefs [re-frame/dispatch fake-dispatch-dont-store-history-on-nav-change!]
      (model/dont-store-history-on-nav-change!))))

(defn fake-dispatch-clear-dont-store-history-on-nav-change! [event]
  (is (= :update-browser-options (get event 0)))
  (let [eventMap (get event 1)]
    (is (= (:dont-store-history-on-nav-change? eventMap) false))))

(deftest clear-dont-store-history-on-nav-change-test
  (testing "clear-dont-store-history-on-nav-change!"
    (with-redefs [re-frame/dispatch fake-dispatch-clear-dont-store-history-on-nav-change!]
      (model/clear-dont-store-history-on-nav-change!))))

(deftest dont-store-history-on-nav-change-if-history-exists-test
  (testing "dont-store-history-on-nav-change-if-history-exists"
    (let [browser {:browser-id test-browser-id :history test-history}
          db {:browser/browsers {test-browser-id browser}}
          browser-no-history {:browser-id test-browser-id}
          db-no-history {:browser/browsers {test-browser-id browser-no-history}}
          result (model/dont-store-history-on-nav-change-if-history-exists db test-browser-id)
          result-no-history (model/dont-store-history-on-nav-change-if-history-exists db-no-history test-browser-id)]
      (is (get result :dont-store-history-on-nav-change?))
      (is (not (get result-no-history :dont-store-history-on-nav-change?))))))

(defn dispatch-on-back-forwards [event expected-index]
  (let [eventType (get event 0)
        eventMap (get event 1)]
    (if (= :update-browser eventType)
      (do (is (= (:history-index eventMap) expected-index))
          (is (= (:url eventMap) (get test-history expected-index))))
      (do (is (= eventType :update-browser-options))
          (is (= (:dont-store-history-on-nav-change? eventMap) true))))))

(defn dispatch-on-back [event]
  (dispatch-on-back-forwards event 0))

(deftest back-test
  (testing "back"
    (let [browser {:browser-id test-browser-id :history-index 1 :history test-history}]
      (with-redefs [re-frame/dispatch dispatch-on-back]
        (model/back browser)))))

(defn dispatch-on-forward [event]
  (dispatch-on-back-forwards event 1))

(deftest forward-test
  (testing "forward"
    (let [browser {:browser-id test-browser-id :history-index 0 :history test-history}]
      (with-redefs [re-frame/dispatch dispatch-on-forward]
        (model/forward browser)))))

(deftest can-go-back?-test
  (testing "can-go-back?"
    (let [browser {:history-index 0 :history test-history}]
      (is (= (model/can-go-back? browser) false)))
    (let [browser {:history-index 1 :history test-history}]
      (is (= (model/can-go-back? browser) true)))))

(deftest can-go-forward?-test
  (testing "can-go-forward?"
    (let [browser {:history-index 0 :history test-history}]
      (is (= (model/can-go-forward? browser) true)))
    (let [browser {:history-index 1 :history test-history}]
      (is (= (model/can-go-forward? browser) false)))))

(deftest record-history-in-browser-if-needed-test-1
  (testing "record-history-in-browser-if-needed: dont record when still loading"
    (let [raw-browser {:history-index 1 :history test-history}
          url "http://third-site.com"
          db {:browser/browsers {test-browser-id raw-browser}}]
      (let [browser (model/record-history-in-browser-if-needed db raw-browser url true)]
        (is (= (:history-index browser) 1))
        (is (= (count (:history browser)) 2))))))

(defn record-history-in-browser-if-needed-test-2-dispatch [event]
  (is (= :update-browser-options (get event 0)))
  (let [eventMap (get event 1)]
    (is (= (:dont-store-history-on-nav-change? eventMap) false))))

(deftest record-history-in-browser-if-needed-test-2
  (testing "record-history-in-browser-if-needed: dont record if :dont-store-history-on-nav-change? true"
    (let [raw-browser {:history-index 1 :history test-history}
          url "http://third-site.com"
          db {:browser/browsers {test-browser-id raw-browser} :browser/options {:dont-store-history-on-nav-change? true}}]
      (with-redefs [re-frame/dispatch record-history-in-browser-if-needed-test-2-dispatch]
        (let [browser (model/record-history-in-browser-if-needed db raw-browser url false)]
          (is (= (:history-index browser) 1))
          (is (= (count (:history browser)) 2)))))))

(deftest record-history-in-browser-if-needed-test-3
  (testing "record-history-in-browser-if-needed: record if not loading and allowed"
    (let [raw-browser {:history-index 1 :history test-history}
          url "http://third-site.com"
          db {:browser/browsers {test-browser-id raw-browser} :browser/options {:dont-store-history-on-nav-change? false}}]
      (let [browser (model/record-history-in-browser-if-needed db raw-browser url false)]
        (is (= (:history-index browser) 2))
        (is (= (count (:history browser)) 3))))))
