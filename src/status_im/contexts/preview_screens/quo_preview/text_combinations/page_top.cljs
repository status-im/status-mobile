(ns status-im.contexts.preview-screens.quo-preview.text-combinations.page-top
  (:require [quo.core :as quo]
            [quo.foundations.resources :as quo.resources]
            [reagent.core :as reagent]
            [status-im.common.resources :as resources]
            [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def avatar-1
  {:emoji               "🥨"
   :customization-color :army})

(def avatar-2
  {:emoji               "🍑"
   :customization-color :blue})

(def context-tag-1
  {:type                :community
   :state               :default
   :customization-color :army
   :community-logo      (resources/mock-images :coinbase)
   :community-name      "Coinbase"
   :emoji               "😝"})

(def context-tag-2
  {:type                :collectible
   :state               :default
   :customization-color :army
   :collectible         (resources/mock-images :collectible)
   :collectible-name    "Collectible"
   :collectible-number  "123"})

(def context-tag-3
  {:type   :token
   :state  :default
   :token  "SNT"
   :amount "250,000"})

(def context-tag-4
  {:type                :account
   :blur?               false
   :state               :default
   :customization-color :sky
   :account-name        "Trip to vegas"
   :emoji               "⚡"})
(def context-tag-5
  {:type                :default
   :customization-color :army
   :profile-picture     nil
   :full-name           "Random user"})
(def context-tag-6
  {:type                :network
   :customization-color :army
   :network-logo        (quo.resources/get-network :optimism)
   :network-name        "Optimism"})

(def emoji-dash-1
  ["❤️" "✏️" "💬" "😋" "📱" "🚓" "💹" "😝" "👊" "👤" "😚" "🚉" "👻" "\uD83D\uDC6F"])

(def emoji-dash-2
  ["🍩" "🤖" "🏀" "🔥" "🌂" "💎" "🚨" "😍" "🐷" "🌶" "🍑" "😈" "🦄" "🕵️‍♀️"])

(def main-descriptor
  [{:key  :blur?
    :type :boolean}
   {:key  :title
    :type :text}
   {:key     :avatar
    :type    :select
    :options [{:key   nil
               :value "(No avatar)"}
              {:key   avatar-1
               :value "Avatar variation 1"}
              {:key   avatar-2
               :value "Avatar variation 2"}]}
   {:key     :description
    :type    :select
    :options [{:key   nil
               :value "(No description)"}
              {:key :text}
              {:key :context-tag}
              {:key :summary}
              {:key :collection}
              {:key :community}]}
   {:key     :emoji-dash
    :type    :select
    :options [{:key   nil
               :value "(No emoji dash)"}
              {:key   emoji-dash-1
               :value "Emoji dash variation 1"}
              {:key   emoji-dash-2
               :value "Emoji dash variation 2"}]}
   {:key     :input
    :type    :select
    :options [{:key   nil
               :value "(No input)"}
              {:key :search}
              {:key :address}
              {:key :recovery-phrase}]}])

(def description-text-descriptor
  [{:key  :description-text
    :type :text}])

(def context-tag-descriptor
  [{:key     :context-tag
    :type    :select
    :options [{:key   context-tag-1
               :value "Context tag variation 1"}
              {:key   context-tag-2
               :value "Context tag variation 2"}
              {:key   context-tag-3
               :value "Context tag variation 3"}
              {:key   context-tag-4
               :value "Context tag variation 4"}
              {:key   context-tag-5
               :value "Context tag variation 5"}
              {:key   context-tag-6
               :value "Context tag variation 6"}]}])

(def recovery-phrase-descriptor
  [{:key  :counter-top
    :type :text}
   {:key  :counter-bottom
    :type :text}])

(defn view
  []
  (let [state (reagent/atom
               {:blur?            true
                :title            "Title"
                :description      nil
                :description-text (str "Share random funny stuff with the community "
                                       "and then something more here.")
                :context-tag      context-tag-1
                :summary          {:row-1 {:text-1        "Send"
                                           :text-2        "from"
                                           :context-tag-1 context-tag-3
                                           :context-tag-2 context-tag-4}
                                   :row-2 {:text-1        "to"
                                           :text-2        "via"
                                           :context-tag-1 context-tag-5
                                           :context-tag-2 context-tag-6}}
                :emoji-dash       nil
                :input            nil
                :collection-text  "Collectible Collection"
                :collection-image (resources/get-mock-image :collectible-monkey)
                :community-image  (resources/get-mock-image :community-logo)
                :community-text   "Doodles"
                :input-props      {:placeholder "Input placeholder"}
                :counter-top      "50"
                :counter-bottom   "100"})]
    (fn []
      (let [descriptor (concat main-descriptor
                               (case (:description @state)
                                 :text        description-text-descriptor
                                 :context-tag context-tag-descriptor
                                 nil)
                               (when (= (:input @state) :recovery-phrase)
                                 recovery-phrase-descriptor))]
        [preview/preview-container
         {:state                     state
          :descriptor                descriptor
          :show-blur-background?     true
          :blur?                     (:blur? @state)
          :component-container-style {:padding-horizontal 10}}
         [quo/page-top @state]]))))
