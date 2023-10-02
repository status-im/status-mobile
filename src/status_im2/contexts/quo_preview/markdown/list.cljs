(ns status-im2.contexts.quo-preview.markdown.list
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:key :title :type :text}
   {:key :description :type :text}
   {:key :tag-name :type :text}
   {:key :description-after-tag :type :text}
   {:key :step-number :type :text}
   {:key :blur? :type :boolean}
   {:key     :type
    :type    :select
    :options [{:key :bullet}
              {:key :step}]}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:title       "Be respectful"
                             :description "Lorem ipsum dolor sit amet."})]
    (fn []
      (let [{:keys [title tag-name]} @state
            tag-picture              (when tag-name (resources/get-mock-image :monkey))]
        [preview/preview-container
         {:state                 state
          :descriptor            descriptor
          :blur?                 (:blur? @state)
          :show-blur-background? (:blur? @state)}
         [quo/markdown-list
          (assoc @state
                 :title       (when (pos? (count title)) title)
                 :tag-picture tag-picture)]]))))
