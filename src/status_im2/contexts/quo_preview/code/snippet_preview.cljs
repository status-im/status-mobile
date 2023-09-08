(ns status-im2.contexts.quo-preview.code.snippet-preview
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def go-example
  "for(let ind")

(def clojure-example
  "(for [x [0 1 2 3 4 5]
      :let [y (* x 3)]
      :when (even? y)]
  y)")

(def examples
  {:clojure {:language :clojure
             :text     clojure-example}
   :go      {:language :go
             :text     go-example}})

(def descriptor
  [{:key     :language
    :type    :select
    :options [{:key :clojure}
              {:key :go}]}
   {:label "Syntax highlight?"
    :key   :syntax
    :type  :boolean}])

(defn view
  []
  (let [state (reagent/atom {:language  :clojure
                             :max-lines 1
                             :syntax    true})]
    (fn []
      (let [language (if (:syntax @state) (:language @state) :text)
            text     (-> (:language @state) examples :text)]
        [preview/preview-container {:state state :descriptor descriptor}
         [quo/snippet-preview
          {:language language} text]]))))
