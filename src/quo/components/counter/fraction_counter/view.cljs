(ns quo.components.counter.fraction-counter.view
  (:require [quo.components.counter.fraction-counter.style :as style]
            [quo.components.markdown.text :as text]
            [quo.context :as quo.context]
            [react-native.core :as rn]
            [schema.core :as schema]
            [utils.number :as number]))

(defn- parse-value
  [value]
  (if (int? value)
    value
    (number/parse-int value 0)))

(defn- error-state-counter?
  [numerator denominator]
  (let [numerator   (parse-value numerator)
        denominator (parse-value denominator)]
    (> numerator denominator)))

(defn- view-internal
  [{:keys [blur? left-value right-value suffix show-counter-warning?]}]
  (let [theme            (quo.context/use-theme)
        counter-warning? (when show-counter-warning?
                           (error-state-counter? left-value right-value))]
    [rn/view {:style style/counter}
     [text/text
      {:size   :paragraph-2
       :weight :regular
       :style  (style/counter-text counter-warning? blur? theme)}
      (str left-value
           "/"
           right-value
           (when suffix
             (str " " suffix)))]]))

(def ?schema
  [:=>
   [:cat
    [:map {:closed true}
     [:left-value [:or int? string?]]
     [:right-value [:or int? string?]]
     [:suffix {:optional true} [:maybe string?]]
     [:blur? {:optional true} [:maybe boolean?]]
     [:show-counter-warning? {:optional true} [:maybe boolean?]]]]
   :any])

(def view (schema/instrument #'view-internal ?schema))
