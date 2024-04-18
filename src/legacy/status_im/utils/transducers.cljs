(ns legacy.status-im.utils.transducers "Utility namespace containing various usefull transducers")

(defn last-distinct-by
  "Just like regular `distinct`, but you provide function
  computing the distinctness of input elements and when
  duplicate elements are removed, the last, not the first
  one is removed."
  [compare-fn]
  (fn [rf]
    (let [accumulated-input (volatile! {:seen  {}
                                        :input []})]
      (fn
        ([] (rf))
        ([result]
         (reduce rf result (:input @accumulated-input)))
        ([result input]
         (let [compare-value (compare-fn input)]
           (if-let [previous-duplicate-index (get-in @accumulated-input [:seen compare-value])]
             (do (vswap! accumulated-input assoc-in [:input previous-duplicate-index] input)
                 result)
             (do (vswap! accumulated-input
                         (fn [{previous-input :input :as accumulated-input}]
                           (-> accumulated-input
                               (update :seen assoc compare-value (count previous-input))
                               (update :input conj input))))
                 result))))))))
