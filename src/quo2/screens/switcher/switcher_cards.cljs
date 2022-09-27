(ns quo2.screens.switcher.switcher-cards
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo.previews.preview :as preview]
            [quo2.foundations.colors :as colors]
            [status-im.react-native.resources :as resources]
            [quo2.components.switcher.switcher-cards :as switcher-cards]))

(def descriptor [{:label   "Type"
                  :key     :type
                  :type    :select
                  :options [{:key   :communities-discover
                             :value "Communities Discover"}
                            {:key   :messaging
                             :value "Messaging"}
                            {:key   :group-messaging
                             :value "Group Messaging"}
                            {:key   :community-card
                             :value "Community Card"}
                            {:key   :browser-card
                             :value "Browser Card"}
                            {:key   :wallet-card
                             :value "Wallet Card"}
                            {:key   :wallet-collectible
                             :value "Wallet Collectible"}
                            {:key   :wallet-graph
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
                 {:label "Customization"
                  :key   :customization-color
                  :type  :select
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

(defn get-mock-content [data]
  (case (:content-type data)
    :text           (:last-message data)
    :photo          photos-list
    :sticker        sticker
    :gif            gif
    :channel        {:emoji "🍑" :channel-name "# random"}
    :community-info {:type :kicked}
    (:audio :community :link :code) nil))

(defn get-mock-data [data]
  (merge
   data
   {:banner  (when (:banner? data) banner)
    :content {:new-notifications?     (:new-notifications? data)
              :notification-indicator (:notification-indicator data)
              :counter-label          (:counter-label data)
              :content-type           (:content-type data)
              :data                   (get-mock-content data)}}
   (case (:type data)
     :messaging       {:avatar-params {:full-name (:title data)}}
     :group-messaging {}
     :community-card  {:avatar-params community-avatar}
     {})))

(defn cool-preview []
  (let [state (reagent/atom {:type                   :group-messaging
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
        [rn/view {:padding-vertical 60
                  :align-items      :center}
         [switcher-cards/card (:type @state) (get-mock-data @state)]]]])))

(defn preview-switcher-cards []
  [rn/view {:background-color colors/neutral-100
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
