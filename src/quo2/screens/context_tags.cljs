(ns quo2.screens.context-tags
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts]
            [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [quo2.components.context-tags :as quo2]))

(def group-avatar-default-params
  {:size :small
   :color :purple})

(def example-pk "0x04fcf40c526b09ff9fb22f4a5dbd08490ef9b64af700870f8a0ba2133f4251d5607ed83cd9047b8c2796576bc83fa0de23a13a4dced07654b8ff137fe744047917")

(def descriptor [{:label "Type"
                  :key   :type
                  :type  :select
                  :options [{:key :public-key
                             :value "Public key"}
                            {:key :avatar
                             :value "Avatar"}
                            {:key :group-avatar
                             :value "Group avatar"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:label "Name"
                             :type :group-avatar})]
    (fn []
      (let [contacts @(re-frame/subscribe [:contacts/contacts])
            descriptor
            (cond-> descriptor
              (= (:type @state) :group-avatar)
              (conj  {:label "Label"
                      :key :label
                      :type :text})
              (= (:type @state) :avatar)
              (conj {:label "Contacts"
                     :key :contact
                     :type :select
                     :options
                     (map (fn [{:keys [public-key]}]
                            {:key public-key
                             :value (multiaccounts/displayed-name
                                     (get contacts public-key))})
                          (vals contacts))}))]
        [rn/view {:margin-bottom 50
                  :padding       16}
         [rn/view {:flex 1}
          [preview/customizer state descriptor]]
         [rn/view {:padding-vertical 60
                   :flex-direction   :row
                   :justify-content  :center}
          (case (:type @state)
            :group-avatar
            [quo2/group-avatar-tag (:label @state) group-avatar-default-params]
            :public-key
            [quo2/public-key-tag {} example-pk]

            :avatar
            [quo2/user-avatar-tag {} (:contact @state)])]]))))

(defn preview-context-tags []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
