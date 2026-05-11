(ns legacy.status-im.browser.core-test
  (:require [cljs.test :refer [deftest is testing]]
            [clojure.string :as string]
            [legacy.status-im.browser.core :as sut]
            [legacy.status-im.utils.deprecated-types :as types]
            [status-im.constants :as constants]))

(defn- browser-cofx
  []
  {:db {:browser/options  {:browser-id "browser-1"
                           :url        "https://good.example"}
        :browser/browsers {"browser-1" {:browser-id    "browser-1"
                                        :bridge-token  "bridge-token"
                                        :history       ["https://good.example"]
                                        :history-index 0}}}})

(deftest process-bridge-message-test
  (testing "ignores bridge messages that do not carry the current browser token"
    (let [message (types/clj->json {:type     constants/history-state-changed
                                    :navState {:url "https://evil.example"}})]
      (is (nil? (sut/process-bridge-message (browser-cofx) message)))))

  (testing "accepts provider messages with the current browser token"
    (let [message (types/clj->json {:type        constants/history-state-changed
                                    :bridgeToken "bridge-token"
                                    :navState    {:url "https://next.example"}})
          result  (sut/process-bridge-message (browser-cofx) message)]
      (is (some? result)))))

(deftest bridge-callback-script-test
  (testing "passes native replies as a JavaScript string literal"
    (let [script (sut/bridge-callback-script
                  {:type constants/api-response
                   :data "');globalThis.__injected = true;//"})]
      (is (string/includes? script "ReactNativeWebView.onMessage("))
      (is (not (string/includes? script "ReactNativeWebView.onMessage('"))))))
