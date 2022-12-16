(ns status-im2.contexts.quo-preview.switcher.switcher-cards
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.react-native.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.contexts.shell.cards.view :as switcher-cards]
            [status-im2.contexts.shell.constants :as constants]))

(def descriptor
  [{:label   "Type"
    :key     :type
    :type    :select
    :options [{:key   constants/communities-discover
               :value "Communities Discover"}
              {:key   constants/one-to-one-chat-card
               :value "Messaging"}
              {:key   constants/private-group-chat-card
               :value "Group Messaging"}
              {:key   constants/community-card
               :value "Community Card"}
              {:key   constants/browser-card
               :value "Browser Card"}
              {:key   constants/wallet-card
               :value "Wallet Card"}
              {:key   constants/wallet-collectible
               :value "Wallet Collectible"}
              {:key   constants/wallet-graph
               :value "Wallet Graph"}]}
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
    :options [{:key   :text
               :value :text}
              {:key   :photo
               :value :photo}
              {:key   :sticker
               :value :sticker}
              {:key   :gif
               :value :gif}
              {:key   :audio
               :value :audio}
              {:key   :community
               :value :community}
              {:key   :link
               :value :link}
              {:key   :code
               :value :code}
              {:key   :channel
               :value :channel}
              {:key   :community-info
               :value :community-info}]}
   {:label "Last Message"
    :key   :last-message
    :type  :text}
   {:label   "Customization"
    :key     :customization-color
    :type    :select
    :options
    (map
     (fn [c]
       {:key   c
        :value c})
     (keys colors/customization))}])

;; Mocked Data

(def banner {:source (resources/get-mock-image :community-banner)})
(def sticker {:source (resources/get-mock-image :sticker)})
(def community-avatar {:source (resources/get-mock-image :community-logo)})
(def gif {:source (resources/get-mock-image :gif)})

(def photos-list
  [{:source (resources/get-mock-image :photo1)}
   {:source (resources/get-mock-image :photo2)}
   {:source (resources/get-mock-image :photo3)}
   {:source (resources/get-mock-image :photo4)}
   {:source (resources/get-mock-image :photo5)}
   {:source (resources/get-mock-image :photo6)}])

(defn get-mock-content
  [data]
  (case (:content-type data)
    :text                           (:last-message data)
    :photo                          photos-list
    :sticker                        sticker
    :gif                            gif
    :channel                        {:emoji "🍑" :channel-name "# random"}
    :community-info                 {:type :kicked}
    (:audio :community :link :code) nil))

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
              :data                   (get-mock-content data)}}
   (case type
     constants/one-to-one-chat-card    {:avatar-params {:full-name (:title data)}}
     constants/private-group-chat-card {}
     constants/community-card          {:avatar-params community-avatar}
     {})))

(defn cool-preview
  []
  (let [state (reagent/atom {:type                   constants/private-group-chat-card
                             :title                  "Alisher Yakupov"
                             :customization-color    :turquoise
                             :new-notifications?     true
                             :banner?                false
                             :notification-indicator :counter
                             :counter-label          2
                             :content-type           :text
                             :last-message           "This is fantastic! Ethereum"
                             :preview-label-color    colors/white})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [switcher-cards/card (get-mock-data @state)]]]])))

(defn preview-switcher-cards
  []
  [rn/view
   {:background-color colors/neutral-100
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
