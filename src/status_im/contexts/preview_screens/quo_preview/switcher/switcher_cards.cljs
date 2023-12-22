(ns status-im.contexts.preview-screens.quo-preview.switcher.switcher-cards
  (:require
    [quo.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im.common.resources :as resources]
    [status-im.constants :as constants]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]
    [status-im.contexts.shell.jump-to.components.switcher-cards.view :as switcher-cards]
    [status-im.contexts.shell.jump-to.constants :as shell.constants]))

(def descriptor
  [{:key     :type
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
   {:key :title :type :text}
   {:key :new-notifications? :type :boolean}
   {:key :banner? :type :boolean}
   {:key     :notification-indicator
    :type    :select
    :options [{:key :counter}
              {:key :unread-dot}]}
   {:key :counter-label :type :text}
   {:key     :content-type
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
   {:key :last-message :type :text}
   (preview/customization-color-option)])

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
  (condp = (:content-type data)
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
   (cond
     (= type shell.constants/one-to-one-chat-card)
     {:avatar-params {:full-name (:title data)}}

     (= type shell.constants/private-group-chat-card)
     {}

     (#{shell.constants/community-card
        shell.constants/community-channel-card}
      type)
     {:avatar-params community-avatar}

     :else
     {})))

(defn view
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
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60}}
       [switcher-cards/card (get-mock-data @state)]])))
