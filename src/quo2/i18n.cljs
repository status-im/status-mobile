(ns quo2.i18n)

(defn default-label-fn
  [label-key]
  (name label-key))

(def label-fn-atom (atom default-label-fn))

(defn init
  [label-fn]
  (reset! label-fn-atom label-fn))

(defn label
  [label-key]
  (@label-fn-atom label-key))
