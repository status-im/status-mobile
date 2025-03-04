(ns status-im.contexts.wallet.trading.swap.components.transaction.title
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.contexts.wallet.trading.swap.components.transaction.style :as style]))

(defn container
  [& children]
  (into [rn/view {:style style/title-container}] children))

(defn text
  [{:keys [label accessibility-label]}]
  [quo/text
   {:size                :heading-1
    :weight              :semi-bold
    :style               style/title-text
    :accessibility-label accessibility-label}
   label])

(defn row
  [& args]
  (let [[props children] (if (map? (first args))
                           [(first args) (rest args)]
                           [nil args])]
    (into [rn/view
           {:style [style/title-row
                    (when-not (:first? props)
                      {:margin-top 4})]}]
          children)))
