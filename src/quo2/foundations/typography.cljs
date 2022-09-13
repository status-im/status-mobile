(ns quo2.foundations.typography)

(defn- tracking-fn
  "Check inter dynamic metric https://rsms.me/inter/dynmetrics/"
  [font-size]
  (let [a -0.0223
        b 0.185
        c -0.1745
        e js/Math.E]
    (->> font-size
         (* c)
         (js/Math.pow e)
         (* b)
         (+ a)
         (* font-size))))

(def tracking (memoize tracking-fn))

(def heading-1 {:font-size      27
                :line-height    32
                :letter-spacing (tracking 27)})

(def heading-2 {:font-size      19
                :line-height    25.65
                :letter-spacing (tracking 19)})

(def paragraph-1 {:font-size      15
                  :line-height    21.75
                  :letter-spacing (tracking 15)})

(def paragraph-2 {:font-size      13
                  :line-height    18.2
                  :letter-spacing (tracking 13)})

(def label {:font-size      11
            :line-height    15.62
            :letter-spacing (tracking 11)})

(def font-regular {:font-family "Inter-Regular"}) ; 400

(def font-medium {:font-family "Inter-Medium"}) ; 500 ff

(def font-semi-bold {:font-family "Inter-SemiBold"}) ; 600

(def font-bold {:font-family "Inter-Bold"}) ; 700

(def monospace {:font-family "InterStatus-Regular"})
