(ns react-native.linear-gradient
  (:require
    ["react-native-linear-gradient" :default LinearGradient]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [taoensso.timbre :as log]))

(def ^:private linear-gradient* (reagent/adapt-react-class LinearGradient))

(defn- split-valid-colors
  [acc idx color]
  (let [color? (colors/valid-color? color)]
    (cond-> acc
      :always      (update :safe-colors conj (if color? color "transparent"))
      (not color?) (update :wrong-colors conj [idx color]))))

(defn- wrong-colors-str
  [colors]
  (reduce-kv (fn [s idx color]
               (str s "Index: " idx ", color: " (prn-str color)))
             "Invalid color values in vector passed to Linear Gradient:\n"
             colors))

(defn linear-gradient
  [props & children]
  (when ^boolean js/goog.DEBUG
    (assert (vector? (:colors props))))
  (let [{:keys [wrong-colors safe-colors]} (rn/use-memo
                                            (fn []
                                              (reduce-kv split-valid-colors
                                                         {:safe-colors  []
                                                          :wrong-colors {}}
                                                         (:colors props)))
                                            [(:colors props)])]
    (when (seq wrong-colors)
      (log/error (wrong-colors-str wrong-colors)))
    (into [linear-gradient* (assoc props :colors safe-colors)]
          children)))
