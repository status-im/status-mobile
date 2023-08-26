(ns status-im2.contexts.quo-preview.list-items.account-list-card
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :default}
              {:key :pressed}]}
   {:key     :action
    :type    :select
    :options [{:key :none}
              {:key :icon}]}])

(defn view
  []
  (let [state (reagent/atom {:account-props {:customization-color :purple
                                             :size                32
                                             :emoji               "🍑"
                                             :type                :default
                                             :name                "Trip to Vegas"
                                             :address             "0x0ah...78b"}
                             :networks      [:ethereum :optimism]
                             :state         :default
                             :action        :none
                             :on-press      (fn [] (js/alert "Button pressed"))})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/account-list-card @state]])))
