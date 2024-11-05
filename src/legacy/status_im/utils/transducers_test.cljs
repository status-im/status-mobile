(ns legacy.status-im.utils.transducers-test
  (:require
    [cljs.test :refer-macros [deftest is testing]]
    [legacy.status-im.utils.transducers :as transducers]))

(def preview-call-1
  {:jail-id  1
   :path     [:preview]
   :params   {:chat-id 1}
   :callback (fn []
               [[:msg-id 1]])})

(def preview-call-2
  {:jail-id  1
   :path     [:preview]
   :params   {:chat-id 1}
   :callback (fn []
               [[:msg-id 2]])})

(def jail-calls
  '({:jail-id 1
     :path    [:suggestions]
     :params  {:arg 0}}
    {:jail-id 1
     :path    [:function]
     :params  {:sub :a}}
    {:jail-id 1
     :path    [:function]
     :params  {:sub :b}}
    {:jail-id 1
     :path    [:suggestions]
     :params  {:arg 1}}
    {:jail-id 1
     :path    [:suggestions]
     :params  {:arg 2}}
    preview-call-1
    preview-call-2))

(deftest last-distinct-by-test
  (testing
    "Elements are removed from input according to provided `compare-fn`,
           when duplicate elements are removed, the last one stays"
    (is (= (sequence (transducers/last-distinct-by (fn [{:keys [jail-id path] :as call}]
                                                     (if (= :suggestions (last path))
                                                       [jail-id path]
                                                       call)))
                     jail-calls)
           '({:jail-id 1
              :path    [:suggestions]
              :params  {:arg 2}}
             {:jail-id 1
              :path    [:function]
              :params  {:sub :a}}
             {:jail-id 1
              :path    [:function]
              :params  {:sub :b}}
             preview-call-1
             preview-call-2))))
  (testing "Edge cases with input size `N=0` and `N=1` work as well"
    (is (= (sequence (transducers/last-distinct-by identity) '())
           '()))
    (is (= (sequence (transducers/last-distinct-by identity) '(1))
           '(1)))))
