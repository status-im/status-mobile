(ns status-im.contexts.preview-screens.quo-preview.text-combinations.standard-title
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :title :type :text}
   {:key     :right
    :type    :select
    :options [{:key   nil
               :value "Nothing (nil)"}
              {:key :counter}
              {:key :action}
              {:key :tag}]}
   {:key :blur? :type :boolean}])

(def counter-descriptor
  [{:key     :counter-left
    :type    :select
    :options (mapv (fn [n] {:key n})
                   (range 0 101 25))}
   {:key     :counter-right
    :type    :select
    :options (mapv (fn [n] {:key n})
                   (range 0 101 25))}])

(def action-descriptor
  [(preview/customization-color-option)
   {:key     :icon
    :type    :select
    :options [{:key :i/placeholder}
              {:key :i/info}
              {:key :i/key}]}])

(def tag-descriptor
  [{:key     :icon
    :type    :select
    :options [{:key :i/placeholder}
              {:key :i/info}
              {:key :i/key}]}
   {:key  :label
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:title               "Title"
                             :blur?               true
                             :right               :counter
                             :counter-left        50
                             :counter-right       100
                             :on-press            #(js/alert "Button clicked!")
                             :customization-color :army
                             :icon                :i/placeholder
                             :label               ""})]
    (fn []
      (let [typed-descriptor (case (:right @state)
                               :counter counter-descriptor
                               :action  action-descriptor
                               :tag     tag-descriptor
                               nil)]
        [preview/preview-container
         {:state                 state
          :descriptor            (concat descriptor typed-descriptor)
          :show-blur-background? true
          :blur?                 (:blur? @state)}
         [quo/standard-title @state]]))))
