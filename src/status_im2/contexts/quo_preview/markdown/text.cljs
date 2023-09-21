(ns status-im2.contexts.quo-preview.markdown.text
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key :heading-1}
              {:key :heading-2}
              {:key :paragraph-1}
              {:key :paragraph-2}
              {:key :label}]}
   {:key     :weight
    :type    :select
    :options [{:key :regular}
              {:key :medium}
              {:key :semi-bold}
              {:key :monospace}]}])

(defn view
  []
  (let [state (reagent/atom {})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60}}
       [quo/text @state
        "The quick brown fox jumped over the lazy dog."]])))
