(ns status-im2.contexts.quo-preview.foundations.typography
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:type :text
    :key  :label}
   {:type    :select
    :key     :size
    :options [{:key   :label
               :value "label"}
              {:key   :paragraph-1
               :value "paragraph-1"}
              {:key   :paragraph-2
               :value "paragraph-2"}
              {:key   :heading-1
               :value "heading-1"}
              {:key   :heading-2
               :value "heading-2"}]}
   {:type    :select
    :key     :weight
    :options [{:key   :regular
               :value "regular"}
              {:key   :medium
               :value "medium"}
              {:key   :semi-bold
               :value "semi-bold"}
              {:key   :bold
               :value "bold"}
              {:key   :monospace
               :value "monospace"}
              {:key   :code
               :value "code"}]}])

(defn view
  []
  (let [state (reagent/atom {:label  "Let’s meet for 🍔 and 🍺"
                             :size   :heading-1
                             :weight :bold})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/text @state (:label @state)]])))
