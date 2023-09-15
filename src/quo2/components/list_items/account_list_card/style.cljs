(ns quo2.components.list-items.account-list-card.style
  (:require [quo2.foundations.colors :as colors]))

(defn container
  [{:keys [state blur? theme]}]
  (let [light-bg     (if (= state :default) colors/neutral-2_5 colors/neutral-5)
        dark-bg      (if (= state :default) colors/neutral-80-opa-40 colors/neutral-80-opa-60)
        blur-bg      (if (= state :default) colors/white-opa-5 colors/white-opa-10)
        light-border (if (= state :default) colors/neutral-10 colors/neutral-20)
        dark-border  (if (= state :default) colors/neutral-80 colors/neutral-70)]
    {:height             48
     :border-radius      12
     :background-color   (if blur? blur-bg (colors/theme-colors light-bg dark-bg theme))
     :border-width       (if blur? 0 1)
     :border-color       (colors/theme-colors light-border dark-border theme)
     :flex-direction     :row
     :align-items        :center
     :padding-horizontal 8
     :padding-vertical   6
     :justify-content    :space-between}))

(def left-container
  {:flex-direction :row
   :align-items    :center})
