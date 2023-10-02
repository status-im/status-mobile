(ns status-im2.contexts.quo-preview.switcher.switcher-cards
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.contexts.shell.jump-to.components.switcher-cards.view :as switcher-cards]
            [status-im2.contexts.shell.jump-to.constants :as shell.constants]))

(def descriptor
  [{:label   "Type"
    :key     :type
    :type    :select
    :options [{:key   shell.constants/communities-discover
               :value "Communities Discover"}
              {:key   shell.constants/one-to-one-chat-card
               :value "Messaging"}
              {:key   shell.constants/private-group-chat-card
               :value "Group Messaging"}
              {:key   shell.constants/community-card
               :value "Community Card"}
              {:key   shell.constants/community-channel-card
               :value "Community Channel Card"}
              {:key   shell.constants/browser-card
               :value "Browser Card"}
              {:key   shell.constants/wallet-card
               :value "Wallet Card"}
              {:key   shell.constants/wallet-collectible
               :value "Wallet Collectible"}
              {:key   shell.constants/wallet-graph
               :value "Wallet Graph"}
              {:key   shell.constants/empty-card
               :value "Empty Card"}]}
   {:label "Title"
    :key   :title
    :type  :text}
   {:label "New Notifications?"
    :key   :new-notifications?
    :type  :boolean}
   {:label "Banner?"
    :key   :banner?
    :type  :boolean}
   {:label   "Notification Indicator"
    :key     :notification-indicator
    :type    :select
    :options [{:key   :counter
               :value :counter}
              {:key   :unread-dot
               :value :unread-dot}]}
   {:label "Counter Label"
    :key   :counter-label
    :type  :text}
   {:label   "Content Type"
    :key     :content-type
    :type    :select
    :options [{:key   constants/content-type-text
               :value :text}
              {:key   constants/content-type-image
               :value :photo}
              {:key   constants/content-type-sticker
               :value :sticker}
              {:key   constants/content-type-gif
               :value :gif}
              {:key   constants/content-type-audio
               :value :audio}
              {:key   constants/content-type-community
               :value :community}
              {:key   constants/content-type-link
               :value :link}]}
   {:label "Last Message"
    :key   :last-message
    :type  :text}
   {:label "Customization"
    :key :customization-color
    :type :select
    :options
    (map
     (fn [c]
       {:key   c
        :value c})
     (keys colors/customization))}])

;; Mocked Data

(def banner (resources/get-mock-image :community-banner))
(def sticker {:source (resources/get-mock-image :sticker)})
(def community-avatar {:source (resources/get-mock-image :community-logo)})
(def gif {:source (resources/get-mock-image :gif)})
(def coinbase-community (resources/get-mock-image :coinbase))

(def photos-list
  [{:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}
   {:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}])

(defn get-mock-content
  [data]
  (case (:content-type data)
    constants/content-type-text
    (:last-message data)

    constants/content-type-image
    photos-list

    constants/content-type-sticker
    sticker

    constants/content-type-gif
    gif

    constants/content-type-audio
    "00:32"

    constants/content-type-community
    {:avatar         coinbase-community
     :community-name "Coinbase"}

    constants/content-type-link
    nil))

(defn get-mock-data
  [{:keys [type] :as data}]
  (merge
   data
   {:type    type
    :banner  (when (:banner? data) banner)
    :content {:new-notifications?     (:new-notifications? data)
              :notification-indicator (:notification-indicator data)
              :counter-label          (:counter-label data)
              :content-type           (:content-type data)
              :community-channel      {:emoji "🍑" :channel-name "# random"}
              :community-info         {:type :kicked}
              :data                   (get-mock-content data)}}
   (case type
     shell.constants/one-to-one-chat-card
     {:avatar-params {:full-name (:title data)}}

     shell.constants/private-group-chat-card
     {}

     (shell.constants/community-card
      shell.constants/community-channel-card)
     {:avatar-params community-avatar}
     {})))

(defn preview-switcher-cards
  []
  (let [state (reagent/atom {:type                   shell.constants/private-group-chat-card
                             :title                  "Alisher Yakupov"
                             :customization-color    :turquoise
                             :new-notifications?     true
                             :banner?                false
                             :notification-indicator :counter
                             :counter-label          2
                             :content-type           constants/content-type-text
                             :last-message           "This is fantastic! Ethereum"
                             :preview-label-color    colors/white})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [switcher-cards/card (get-mock-data @state)]]]])))
