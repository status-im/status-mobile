(ns status-im.contexts.preview-screens.quo-preview.tags.tag
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:key     :type
    :type    :select
    :options [{:key   :emoji
               :value "Emoji"}
              {:key   :icon
               :value "Icons"}
              {:key   :label
               :value "Label"}]}
   {:key  :labelled?
    :type :boolean}
   {:key  :disabled?
    :type :boolean}
   {:key  :blurred?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:size      32
                             :labelled? true
                             :type      :emoji})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :show-blur-background? true
        :blur?                 (:blurred? @state)}
       [quo/tag
        (merge @state
               {:id        1
                :label     "Tag"
                :labelled? (if (= (:type @state) :label)
                             true
                             (:labelled? @state))
                :resource  (if (= :emoji (:type @state))
                             (resources/get-image :music)
                             :i/placeholder)})]])))
