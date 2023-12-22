(ns status-im.contexts.preview-screens.quo-preview.tags.tags
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
   {:key  :scrollable?
    :type :boolean}
   {:key     :fade-end-percentage
    :type    :select
    :options [{:key   1
               :value "1%"}
              {:key   0.4
               :value "0.4%"}]}
   {:key  :labelled?
    :type :boolean}
   {:key  :disabled?
    :type :boolean}
   {:key  :blurred?
    :type :boolean}])

(defn view
  []
  (let [state (reagent/atom {:size                32
                             :labelled?           true
                             :type                :emoji
                             :fade-end-percentage 0.4
                             :scrollable?         false})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blurred? @state)
        :show-blur-background? true}
       [quo/tags
        (merge
         @state
         {:default-active 1
          :component      :tags
          :labelled?      (if (= :label (:type @state)) true (:labelled? @state))
          :data           [{:id 1 :label "Music" :resource (resources/get-image :music)}
                           {:id 2 :label "Lifestyle" :resource (resources/get-image :lifestyle)}
                           {:id 2 :label "Podcasts" :resource (resources/get-image :podcasts)}
                           {:id 2 :label "Music" :resource (resources/get-image :music)}
                           {:id 3 :label "Lifestyle" :resource (resources/get-image :lifestyle)}]}
         (when (:scrollable? @state)
           {:scroll-on-press? true
            :fade-end?        true}))]])))
