(ns env.test.utils
  (:require [cljs.test :refer-macros [deftest is]]
            [env.utils :as utils]))

(deftest get-host-test
  (is (= (utils/get-host "http://localhost:8000") "localhost"))
  (is (= (utils/get-host "http://0.0.0.0:8000") "0.0.0.0"))
  (is (= (utils/get-host "invalid") "")))

(deftest re-frisk-url-test
  (is (= (utils/re-frisk-url "ws://localhost:3449/figwheel-ws") "localhost:4567"))
  (is (= (utils/re-frisk-url "ws://10.0.2.2:3449/figwheel-ws") "10.0.2.2:4567"))
  (is (thrown? js/Error (utils/re-frisk-url "invalid"))))
