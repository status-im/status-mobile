(ns status-im.test.events
  (:require [cljs.spec.alpha :as s]
            [status-im.chat.handlers]
            [clojure.test.check.properties :as prop]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.clojure-test :as tc-test :include-macros true]
            [re-frame.core :as rf]
            [day8.re-frame.test :refer [run-test-sync]]))

(def event-gen (gen/one-of
                 [(s/gen :status-im.chat.handlers/update-message-overhead!-event)]))

(def events-gen (gen/vector event-gen 1 10))

(def prop:all-events-run
  (prop/for-all*
    [events-gen]
    (fn [events]
      (let [_ (run-test-sync
                (rf/reg-fx :status-im.ui.screens.events/init-store #())
                (rf/reg-fx :get-prices #())
                (rf/reg-fx :get-balance #())
                (rf/dispatch [:initialize-db])
                (doseq [e events]
                  #_(rf/dispatch e)))
            prop-result true]
        prop-result))))

(tc-test/defspec prop:all-events-run-test
                 {:num-tests 50}
                 prop:all-events-run)


