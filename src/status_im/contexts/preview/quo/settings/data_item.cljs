(ns status-im.contexts.preview.quo.settings.data-item
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def communities-list
  [{:source (resources/get-mock-image :coinbase)}
   {:source (resources/get-mock-image :decentraland)}
   {:source (resources/get-mock-image :rarible)}])

(def networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(def descriptor
  [{:key :blur? :type :boolean}
   {:key :card? :type :boolean}
   {:type    :select
    :key     :right-icon
    :options [{:key :i/chevron-right}
              {:key :i/copy}
              {:key   nil
               :value "None"}]}
   {:type    :select
    :key     :title-icon
    :options [{:key :i/chevron-right}
              {:key :i/copy}
              {:key   nil
               :value "None"}]}
   {:type    :select
    :key     :right-content
    :options [{:key   nil
               :value "None"}
              {:key   {:type :communities
                       :data communities-list}
               :value "Communities"}
              {:key   {:type :network
                       :data networks-list}
               :value "Networks"}
              {:key   {:type :accounts
                       :data [{:emoji "🔥" :customization-color :yellow}]}
               :value "Account (size-24)"}
              {:key   {:type :accounts
                       :data [{:emoji "🔥" :customization-color :yellow}]
                       :size :size-32}
               :value "Account (size-32)"}]}
   {:type    :select
    :key     :subtitle-type
    :options [{:key :default}
              {:key :icon}
              {:key :network}
              {:key :account}
              {:key :editable}]}
   {:type    :select
    :key     :status
    :options [{:key :default}
              {:key :loading}]}
   {:type    :select
    :key     :size
    :options [{:key :default}
              {:key :small}
              {:key :large}]}])

(defn view
  []
  (let [state (reagent/atom {:on-press            #(js/alert (str "pressed"))
                             :blur?               false
                             :subtitle-type       :account
                             :card?               true
                             :status              :default
                             :size                :default
                             :title               "Label"
                             :subtitle            "Subtitle"
                             :icon                :i/placeholder
                             :right-icon          :i/chevron-right
                             :right-content       nil
                             :emoji               "🎮"
                             :customization-color :yellow
                             :network-image       (quo.resources/get-network :ethereum)})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true
        :blur-dark-only?       true}
       [quo/data-item @state]])))
