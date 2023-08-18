(ns status-im2.contexts.quo-preview.inputs.search-input
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :value
    :type :text}
   {:key  :blur?
    :type :boolean}
   {:key  :disabled?
    :type :boolean}
   {:key     :number-tags
    :type    :select
    :options (map (fn [n]
                    {:key n :value (str n)})
                  (range 0 5))}])

(defn example-tags
  [blur?]
  [[quo/context-tag
    {:blur?           blur?
     :size            24
     :profile-picture (resources/get-mock-image :user-picture-male5)
     :full-name       "alisher.eth"}]
   [quo/context-tag
    {:blur?           blur?
     :size            24
     :profile-picture (resources/get-mock-image :user-picture-male4)
     :full-name       "Pedro"}]
   [quo/context-tag
    {:blur?           blur?
     :size            24
     :profile-picture (resources/get-mock-image :user-picture-female2)
     :full-name       "Freya Odinson"}]])

(defn view
  []
  (let [state          (reagent/atom {:blur?       false
                                      :disabled?   false
                                      :number-tags 0
                                      :placeholder "Search..."
                                      :value       ""})
        on-change-text (fn [new-text]
                         (swap! state assoc :value new-text))]
    (fn []
      (let [tags (take (:number-tags @state) (example-tags (:blur? @state)))]
        [preview/preview-container
         {:state                 state
          :descriptor            descriptor
          :blur?                 (:blur? @state)
          :show-blur-background? true}
         [quo/search-input
          (assoc @state
                 :on-clear       #(swap! state assoc :value "")
                 :tags           tags
                 :on-change-text on-change-text)]]))))
