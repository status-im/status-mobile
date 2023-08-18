(ns status-im2.contexts.quo-preview.list-items.account-list-card
  (:require
    [quo2.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "State:"
    :key     :state
    :type    :select
    :options [{:key   :default
               :value "Default"}
              {:key   :pressed
               :value "Pressed"}]}
   {:label   "Action:"
    :key     :action
    :type    :select
    :options [{:key   :none
               :value "None"}
              {:key   :icon
               :value "Icon"}]}])

(defn preview
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
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view
        {:style {:flex               1
                 :padding-horizontal 20}}
        [rn/view {:style {:min-height 150}} [preview/customizer state descriptor]]
        [quo/account-list-card @state]]])))
