(ns status-im.mailserver.core-perf-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.mailserver.core :as mailserver]
            [status-im.utils.fx :as fx]
            [status-im.transport.filters.core :as transport.filters]
            [status-im.chat.models :as chat]
            [status-im.mailserver.core-perf-data :as core-perf-data]
            ["perf_hooks" :refer (performance)]))

(def cofx {:db {:chats (reduce-kv #(assoc %1 %2 (assoc %3 :might-have-join-time-messages? true)) {} core-perf-data/chats)
                :mailserver/topics core-perf-data/mailserver-topics
                :multiaccount {:use-mailservers? true}
                :mailserver/current-request {}}})

(fx/defn start-public-chat-temp
   [cofx profile-public-key topic]
   (fx/merge cofx
             (chat/add-public-chat topic profile-public-key false)
             (transport.filters/load-chat topic)))

(defn rand-str []
  (apply str (take 7 (repeatedly #(char (+ (rand 26) 65))))))

(deftest handle-request-success-perf-test
  (testing "handle-request-success-perf-test"
    (let [cofx (apply fx/merge cofx (map (partial start-public-chat-temp "0x0")
                                         (take 500 (repeatedly rand-str))))
          n (.now performance)]
      (mailserver/handle-request-success cofx {:request-id "0x06cbaee0544ba007f50d20130d5cd2d9b696f2cc1cc65137d84709e6a22d24a6",
                                               :topics #{"0x869583d9"}})
      ;;TODO currently its really slow, so we can't add a test, we need to optimize it first, so there is only a print for now
      (println "TIME " (- (.now performance) n) "ms"))))