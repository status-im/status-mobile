(ns status-im2.contexts.quo-preview.community.community-card-view
  (:require [quo.design-system.colors :as quo.colors]
            [quo2.components.community.community-card-view :as community-card-view]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [utils.i18n :as i18n]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def community-data
  {:id "id"
   :name "Status"
   :description
   "Status is a secure messaging app, crypto wallet and web3 browser built with the state of the art technology"
   :cover (resources/get-mock-image :community-cover)
   :community-icon (resources/get-mock-image :status-logo)
   :color (rand-nth quo.colors/chat-colors)
   :tokens [{:id 1 :group [{:id 1 :token-icon (resources/get-mock-image :status-logo)}]}]
   :tags [{:id        1
           :tag-label (i18n/label :t/music)
           :emoji     (resources/get-image :music)}
          {:id        2
           :tag-label (i18n/label :t/lifestyle)
           :emoji     (resources/get-image :lifestyle)}
          {:id        3
           :tag-label (i18n/label :t/podcasts)
           :emoji     (resources/get-image :podcasts)}]})

(def descriptor
  [{:label   "Status:"
    :key     :status
    :type    :select
    :options [{:key   :gated
               :value "Gated"}
              {:key   :open
               :value "Open"}]}
   {:label "Locked?"
    :key   :locked?
    :type  :boolean}
   {:label "Loading?"
    :key   :loading?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:status   :gated
                             :locked?  true
                             :loading? false})]
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
         [community-card-view/view
          {:community (merge @state community-data)
           :loading?  (:loading? @state)}]]]])))

(defn preview-community-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/neutral-5
                                           colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])

