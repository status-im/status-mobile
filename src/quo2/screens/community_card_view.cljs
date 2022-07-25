(ns quo2.screens.community-card-view
  (:require [quo.react-native :as rn]
            [quo.previews.preview :as preview]
            [reagent.core :as reagent]
            [status-im.constants :as constants]
            [quo.design-system.colors :as quo.colors]
            [quo2.foundations.colors :as colors]
            [quo2.components.community-card-view :as community-view]
            [status-im.react-native.resources :as resources]))

(def community-data
  {:id             constants/status-community-id
   :name           "Status"
   :description    "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
   :status         "gated"
   :section        "popular"
   :permissions    true
   :cover          (resources/get-image :community-cover)
   :community-icon (resources/get-image :status-logo)
   :color          (rand-nth quo.colors/chat-colors)
   :tokens   [{:id  1 :group [{:id 1 :token-icon (resources/get-image :status-logo)}]}]})

(def descriptor [{:label   "Community views"
                  :key     :view-style
                  :type    :select
                  :options [{:key   :card-view
                             :value "Card view"}
                            {:key   :list-view
                             :value "List view"}]}])

(defn cool-preview []
  (let [state (reagent/atom {:view-style :card-view})]
    (fn []
      [rn/view {:margin-bottom 50
                :padding       16}
       [rn/view {:flex 1}
        [preview/customizer state descriptor]]
       [rn/view {:padding-vertical 60
                 :flex-direction   :row
                 :justify-content  :center}
        (if (= :card-view (:view-style @state))
          [community-view/community-card-view community-data]
          [community-view/communities-list-view community-data])]])))

(defn preview-community-card []
  [rn/view {:background-color (colors/theme-colors colors/neutral-5
                                                   colors/neutral-95)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])

