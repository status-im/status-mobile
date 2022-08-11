(ns quo2.foundations.typography)

;; Formulat for letter spacing from figma %: font-size*{figma-percentage}/100
(def heading-1 {:font-size      27
                :line-height    32
                :letter-spacing -0.567})

(def heading-2 {:font-size       19
                :line-height     25.65
                :letter-spacing  -0.304})

(def paragraph-1 {:font-size      15
                  :line-height    21.75
                  :letter-spacing -0.135})

(def paragraph-2 {:font-size      13
                  :line-height    18.2
                  :letter-spacing -0.039})

(def label {:font-size      11
            :line-height    15.62
            :letter-spacing -0.055})

(def font-regular {:font-family "Inter-Regular"}) ; 400

(def font-medium {:font-family "Inter-Medium"}) ; 500 ff

(def font-semi-bold {:font-family "Inter-SemiBold"}) ; 600

(def font-bold {:font-family "Inter-Bold"}) ; 700

(def monospace {:font-family "InterStatus-Regular"})
