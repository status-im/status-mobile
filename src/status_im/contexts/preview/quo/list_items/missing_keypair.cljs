(ns status-im.contexts.preview.quo.list-items.missing-keypair
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key  :blur?
    :type :boolean}])

(def component-props
  {:blur?   false
   :keypair {:type     :seed
             :key-uid  "0x01"
             :name     "Trip to Vegas"
             :accounts [{:type                :default
                         :emoji               "🍑"
                         :customization-color :purple}]}})

(defn view
  []
  (let [state (reagent/atom component-props)]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [rn/view {:style {:align-items :flex-start}}
        [quo/missing-keypair @state]]])))
