(ns status-im2.contexts.quo-preview.switcher.group-messaging-card
  (:require [quo.react-native :as rn]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label "Title"
    :key   :title
    :type  :text}
   {:label   "Status"
    :key     :status
    :type    :select
    :options [{:key   :read
               :value :read}
              {:key   :unread
               :value :unread}
              {:key   :mention
               :value :mention}]}
   {:label "Counter Label"
    :key   :counter-label
    :type  :text}
   {:label   "Type"
    :key     :type
    :type    :select
    :options [{:key   :message
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
               :value :code-snippet}]}
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
  (case (:type data)
    :message
    {:text (:last-message data)}

    :photo
    {:photos photos-list}

    :sticker
    sticker

    :gif
    gif

    :audio
    {:duration "00:32"}

    :community
    {:community-avatar coinbase-community
     :community-name   "Coinbase"}

    :link
    {:source (resources/get-mock-image :status-logo)
     :text   "Rolling St..."}

    :code
    nil))

(defn get-mock-data
  [data]
  (merge
   data
   {:content (merge (get-mock-content data)
                    {:mention-count (when (= (:status data) :mention) (:counter-label data))})
    ;; {:mention-count          (when (= (:status data) :mention) 5)
    ;;  :type                   (:type data)
    ;;  :community-channel      {:emoji        "🍑"
    ;;                           :channel-name "# random"}
    ;;  :community-info         {:type :kicked}
    ;;  :data                   (get-mock-content data)}
   }))

(defn cool-preview
  []
  (let [state (reagent/atom {:title               "Hester, John, Steven, and 2 others"
                             :type                :message
                             :status              :read
                             :last-message        "Hello there, there is a new message"
                             :customization-color :camel
                             :avatar              true
                             :counter-label       5})]
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
