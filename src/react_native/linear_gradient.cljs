(ns react-native.linear-gradient
  (:require
    ["react-native-linear-gradient" :default LinearGradient]
    [clojure.string :as string]
    [reagent.core :as reagent]
    [taoensso.timbre :as log]))

(def ^:private linear-gradient* (reagent/adapt-react-class LinearGradient))

(defn- valid-color?
  [color]
  (or (keyword? color)
      (and (string? color)
           (or (string/starts-with? color "#")
               (string/starts-with? color "rgb")))))

(defn- split-valid-colors
  [acc idx color]
  (let [color? (valid-color? color)]
    (cond-> acc
      :always      (update :safe-colors conj (if color? color "transparent"))
      (not color?) (update :wrong-colors conj [idx color]))))

(defn- wrong-colors-str
  [colors]
  (reduce-kv (fn [s idx color]
               (str s "Index: " idx ", color: " (prn-str color)))
             "Invalid color values in vector passed to Linear Gradient:\n"
             colors))

(def linear-gradient
  (if ^boolean js/goog.DEBUG
    (fn [props & children]
      (assert (vector? (:colors props)))
      (let [{:keys [wrong-colors safe-colors]} (reduce-kv split-valid-colors
                                                          {:safe-colors  []
                                                           :wrong-colors {}}
                                                          (:colors props))]
        (when (seq wrong-colors)
          (log/error (wrong-colors-str wrong-colors)))
        (into [linear-gradient* (assoc props :colors safe-colors)]
              children)))
    linear-gradient*))
