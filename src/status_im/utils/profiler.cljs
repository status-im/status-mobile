(ns status-im.utils.profiler
  "Performance profiling for react components."
  (:require-macros [status-im.utils.profiler])
  (:require [reagent.core :as reagent]
            [taoensso.timbre :as log]
            [oops.core :refer [oget ocall]]
            [goog.functions :as f]))

(defonce memo (atom {}))

(def td (js/require "tdigest"))
(def tdigest (oget td "TDigest"))

(def react (js/require "react"))
(def profiler (reagent/adapt-react-class (oget react "Profiler")))

(defn on-render-factory
  [label]
  (let [buf (new tdigest)
        log (f/debounce
             (fn [phase buf]
               (log/info "[profile:" label "(" phase ")]: \n"
                         (ocall buf "summary")))
             300)]
    (fn [_ phase adur _ _ _ _]
      (.push buf adur)
      (log phase buf))))

(defn perf [{:keys [label]}]
  (let [this      (reagent/current-component)
        children  (reagent/children this)
        on-render (if-let [render-fn (get @memo label)]
                    render-fn
                    (do
                      (swap! memo assoc label (on-render-factory label))
                      (get @memo label)))]
    (into [profiler {:id        label
                     :on-render on-render}]
          children)))
