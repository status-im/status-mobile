(ns status-im2.contexts.quo-preview.community.community-card-view
  (:require [quo.design-system.colors :as quo.colors]
            [quo2.components.community.community-card-view :as community-card-view]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im.react-native.resources :as resources]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def community-data
  {:id             constants/status-community-id
   :name           "Status"
   :description
   "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
   :cover          (resources/get-image :community-cover)
   :community-icon (resources/get-image :status-logo)
   :color          (rand-nth quo.colors/chat-colors)
   :tokens         [{:id 1 :group [{:id 1 :token-icon (resources/get-image :status-logo)}]}]
   :tags           [{:id 1 :tag-label (i18n/label :t/music) :resource (resources/get-image :music)}
                    {:id        2
                     :tag-label (i18n/label :t/lifestyle)
                     :resource  (resources/get-image :lifestyle)}
                    {:id        3
                     :tag-label (i18n/label :t/podcasts)
                     :resource  (resources/get-image :podcasts)}]})

(def descriptor
  [{:label   "Status:"
    :key     :status
    :type    :select
    :options [{:key   :gated
               :value "Gated"}
              {:key   :open
               :value "Open"}]}
   {:label "Locked:"
    :key   :locked?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:status  :gated
                             :locked? true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:flex    1
          :padding 16}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical 60
          :justify-content  :center}
         [community-card-view/community-card-view-item (merge @state community-data)]]]])))

(defn preview-community-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/neutral-5
                                           colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])

