(ns utils.reagent
  (:refer-clojure :exclude [partial atom flush])
  (:require ["react" :refer (useState useEffect)]
            [reagent.core]))

(reagent.core/set-default-compiler! (reagent.core/create-compiler {:function-components true}))

(defn as-element [& children]
  ;(println "REAGENT" "as-element")
  (apply reagent.core/as-element children))

(defn adapt-react-class [& children]
  ;(println "REAGENT" "adapt-react-class")
  (apply reagent.core/adapt-react-class children))

(defn reactify-component [& children]
  ;(println "REAGENT" "reactify-component")
  (apply reagent.core/reactify-component children))

(defn create-class [& children]
  ;(println "REAGENT" "create-class")
  (apply reagent.core/create-class children))

(defn props [& children]
  ;(println "REAGENT" "props" (first children))
  (when (map? (first children)) (first children)))

(defn children [& children]
  ;(println "REAGENT" "children")
  (if (apply props children)
    (rest children)
    children))

(defn flush [& children]
  ;(println "REAGENT" "flush")
  (apply reagent.core/flush children))

;; Ratom

(defn track! [& children]
  ;(println "REAGENT" "track!")
  (apply reagent.core/track! children))

(defn dispose! [& children]
  ;(println "REAGENT" "dispose!")
  (apply reagent.core/dispose! children))

(defn use-atom
  [value]
  (println "ATOM" value)
  (let [ratom (reagent.core/atom value)
        [state set-state] (useState value)]
    (useEffect (fn []
                 (let [track (track! #(set-state @ratom))]
                   #(dispose! track)))
               #js [])
    ratom))

(def atom use-atom)
;; RCursor

(defn cursor [& children]
  ;(println "REAGENT" "cursor")
  (apply reagent.core/cursor children))

;; Utilities

(defn next-tick [& children]
  ;(println "REAGENT" "next-tick")
  (apply reagent.core/next-tick children))

(defn after-render [& children]
  ;(println "REAGENT" "after-render")
  (apply reagent.core/after-render children))
