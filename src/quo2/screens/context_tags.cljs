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
(def example-pk2 "0x04c178513eb741e8c4e50326b22baefa7d60a2f4eb81e328c4bbe0b441f87b2a014a5907a419f5897fc3c0493a0ff9db689a1999d6ca7fdc63119dd1981d0c7ccf")

(def main-descriptor [{:label   "Type"
                       :key     :type
                       :type    :select
                       :options [{:key   :public-key
                                  :value "Public key"}
                                 {:key   :avatar
                                  :value "Avatar"}
                                 {:key   :group-avatar
                                  :value "Group avatar"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:label "Name"
                             :type :group-avatar})]
    (fn []
      (let [contacts {example-pk {:public-key example-pk
                                  :names {:three-words-name "Automatic incompatible Coati"}}
                      example-pk2 {:public-key example-pk2
                                   :names {:three-words-name "Clearcut Flickering Rattlesnake"}}}
            contacts-public-keys (map (fn [{:keys [public-key]}]
                                        {:key   public-key
                                         :value (multiaccounts/displayed-name
                                                 (get contacts public-key))})
                                      (vals contacts))
            descriptor
            (cond
              (= (:type @state) :group-avatar) (conj main-descriptor {:label "Label"
                                                                      :key   :label
                                                                      :type  :text})
              (= (:type @state) :avatar) (let [photo @(re-frame.core/subscribe [:chats/photo-path (:contact @state)])]
                                           (when-not (contains? @state :contacts)
                                             (swap! state assoc :contacts contacts-public-keys))
                                           (when-not (= (:photo @state)
                                                    photo)
                                             (swap! state assoc :photo photo))
                                             (conj main-descriptor {:label   "Contacts"
                                                                    :key     :contact
                                                                    :type    :select
                                                                    :options contacts-public-keys})))]
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
            [quo2/user-avatar-tag {} (:contact @state) (:photo @state) contacts])]]))))

(defn preview-context-tags []
  [rn/view {:background-color (colors/theme-colors colors/white
                                                   colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
