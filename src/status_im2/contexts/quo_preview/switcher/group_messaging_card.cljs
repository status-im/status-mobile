(ns status-im2.contexts.quo-preview.switcher.group-messaging-card
  (:require [quo.react-native :as rn]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.constants :as constants]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label "Banner?"
    :key   :banner?
    :type  :boolean}
   {:label "Title"
    :key   :title
    :type  :text}
   {:label "New Notifications?"
    :key   :new-notifications?
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

   {:label   "Customization"
    :key     :customization-color
    :type    :select
    :options (map
              (fn [c]
                {:key   c
                 :value c})
              (keys colors/customization))}])

;; Mock data
(def banner {:source (resources/get-mock-image :community-banner)})
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
    {:text (:last-message data)}

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
  [data]
  (merge
   data
   {:banner  (when (:banner? data) banner)
    :content {:new-notifications?     (:new-notifications? data)
              :notification-indicator (:notification-indicator data)
              :counter-label          (:counter-label data)
              :content-type           (:content-type data)
              :community-channel      {:emoji "🍑" :channel-name "# random"}
              :community-info         {:type :kicked}
              :data                   (get-mock-content data)}}))

(defn cool-preview
  []
  (let [state (reagent/atom {:title                  "Hester, John, Steven, and 2 others"
                             :new-notifications?     true
                             :notification-indicator :counter
                             :counter-label          2
                             :content-type           constants/content-type-text
                             :last-message           "Hello there, there is a new message"
                             :customization-color    :camel
                             :banner?                false
                             :avatar                 true})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo/group-messaging-card (get-mock-data @state)]]]])))

(defn preview-group-messaging-card
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
