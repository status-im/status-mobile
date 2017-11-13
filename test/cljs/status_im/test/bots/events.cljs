(ns status-im.test.bots.events
  (:require [cljs.test :refer-macros [deftest is testing]]
            [status-im.bots.events :as bots-events]))

(def ^:private initial-db
  {:bot-db {}
   :bot-subscriptions {}
   :contacts/contacts
   {"bot1" {:subscriptions
            {:feeExplanation
             {:subscriptions {:fee ["sliderValue"]
                              :tx ["transaction"]}}}}
    "bot2" {:subscriptions
            {:roundedValue
             {:subscriptions {:amount ["input"]}}}}
    "bot3" {:subscriptions
            {:warning
             {:subscriptions {:amount ["input"]}}}}}})

(deftest add-active-bot-subscriptions-test
  (testing "That active bot subscriptions are correctly transformed and added to db"
    (let [db (bots-events/add-active-bot-subscriptions initial-db #{"bot1" "bot3"})]
      (is (= #{"bot1" "bot3"} (-> db :bot-subscriptions keys set)))
      (is (= {[:sliderValue] {:feeExplanation {:fee [:sliderValue]
                                               :tx [:transaction]}}
              [:transaction] {:feeExplanation {:fee [:sliderValue]
                                               :tx [:transaction]}}}
             (-> db :bot-subscriptions (get "bot1")))))))

(defn- fake-subscription-call
  [db {:keys [chat-id parameters]}]
  (let [{:keys [name subscriptions]} parameters
        simulated-jail-response {:result {:returned {:sub-call-arg-map subscriptions}}}]
    (bots-events/calculated-subscription db {:bot chat-id
                                             :path [name]
                                             :result simulated-jail-response})))

(deftest set-in-bot-db-test
  (let [{:keys [db call-jail-function-n]} (-> initial-db
                                              (bots-events/add-active-bot-subscriptions #{"bot1" "bot2"})
                                              (bots-events/set-in-bot-db {:bot "bot1"
                                                                          :path [:sliderValue]
                                                                          :value 2}))
        new-db (reduce fake-subscription-call db call-jail-function-n)] 
    (testing "That setting in value in bot-db correctly updates bot-db"
      (is (= 2 (get-in new-db [:bot-db "bot1" :sliderValue]))))
    (testing "That subscriptions are fired-off"
      (is (= {:sub-call-arg-map {:fee 2
                                 :tx nil}}
             (get-in new-db [:bot-db "bot1" :feeExplanation]))))))
