(ns status-im.contexts.preview.quo.drawers.drawer-action
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :title
    :type :text}
   {:key     :state
    :type    :select
    :options [{:key :selected}]}
   {:key     :icon
    :type    :select
    :options [{:key :i/placeholder}
              {:key :i/info}
              {:key :i/browser}]}
   {:key     :action
    :type    :select
    :options [{:key :arrow}
              {:key :toggle}
              {:key :input}]}
   {:key  :description
    :type :text}
   {:key  :blur?
    :type :boolean}
   (preview/customization-color-option)])

(defn view
  []
  (let [[state set-state] (rn/use-state {:title               "Action"
                                         :description         "This is a description"
                                         :customization-color :blue
                                         :on-press            #(js/alert "Pressed!")
                                         :input-props         {:placeholder "Type something"
                                                               :right-icon  {:icon-name :i/placeholder
                                                                             :style-fn  identity}}})]
    [preview/preview-container
     {:state                 state
      :set-state             set-state
      :descriptor            descriptor
      :blur?                 (:blur? state)
      :show-blur-background? true
      :blur-dark-only?       true}
     [quo/drawer-action state]]))
