(ns status-im.contexts.preview-screens.quo-preview.numbered-keyboard.numbered-keyboard
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :blur? :type :boolean}
   {:key :disabled? :type :boolean}
   {:key :delete-key? :type :boolean}
   {:type    :select
    :key     :left-action
    :options [{:key :dot}
              {:key :face-id}
              {:key :none}]}])

(defn view
  []
  (let [state (reagent/atom {:disabled?   false
                             :on-press    (fn [item] (js/alert (str item " pressed")))
                             :blur?       false
                             :delete-key? true
                             :left-action :dot})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? (:blur? @state)
        :blur-height           300}
       [quo/numbered-keyboard @state]])))
