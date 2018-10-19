(ns status-im.test.extensions.core
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.extensions.core :as extensions]))

(deftest valid-uri?
  (is (= false (extensions/valid-uri? nil)))
  (is (= false (extensions/valid-uri? "http://get.status.im/extension/ipfs")))
  (is (= false (extensions/valid-uri? " http://get.status.im/extension/ipfs@QmWKqSanV4M4zrd55QMkvDMZEQyuHzCMHpX1Fs3dZeSExv ")))
  (is (= true (extensions/valid-uri? " https://get.status.im/extension/ipfs@QmWKqSanV4M4zrd55QMkvDMZEQyuHzCMHpX1Fs3dZeSExv ")))
  (is (= true (extensions/valid-uri? "https://get.status.im/extension/ipfs@QmWKqSanV4M4zrd55QMkvDMZEQyuHzCMHpX1Fs3dZeSExv"))))
