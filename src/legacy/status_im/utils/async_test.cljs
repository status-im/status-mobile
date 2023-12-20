(ns legacy.status-im.utils.async-test
  (:require
    [cljs.core.async :as async]
    [cljs.test :refer-macros [deftest is testing async]]
    [legacy.status-im.utils.async :as async-util]))

(deftest chunking-test
  (testing "Accumulating result works as expected for `chunked-pipe!`"
    (let [input  (async/chan)
          output (async/chan)]
      (async-util/chunked-pipe! input output 100)
      (async done
             (async/go
              (async/put! input 1)
              (async/put! input 2)
              (async/put! input 3)
              (async/<! (async/timeout 110))
              (async/put! input 1)
              (async/put! input 2)
              (async/<! (async/timeout 300))
              (async/put! input 1)
              (is (= [1 2 3] (async/<! output)))
              (is (= [1 2] (async/<! output)))
              (is (= [1] (async/<! output)))
              (done))))))

(deftest chunking-closing-test
  (testing "Closing input channel closes output channel connected through `chunked-pipe!`"
    (let [input  (async/chan)
          output (async/chan)]
      (async-util/chunked-pipe! input output 100)
      (async done
             (async/go
              (async/close! input)
              (is (= nil (async/<! output)))
              (done))))))
