(ns status-im2.contexts.quo-preview.messages.author
  (:require [quo2.components.markdown.text :as text]
            [quo2.components.messages.author.view :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Primary name"
    :key   :primary-name
    :type  :text
    :limit 24}
   {:label "Secondary name"
    :key   :secondary-name
    :type  :text}
   {:label "Chat key"
    :key   :chat-key
    :type  :text}
   {:label "Time"
    :key   :time-str
    :type  :text
    :limit 5}
   {:label "Is contact?"
    :key   :contact?
    :type  :boolean}
   {:label "Is verified?"
    :key   :verified?
    :type  :boolean}
   {:label "Is untrustworthy?"
    :key   :untrustworthy?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:primary-name    "Alisher Yakupov"
                             :seconadary-name ""
                             :time-str        "09:30"
                             :contact?        false
                             :verified?       false
                             :untrustworthy?  false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical    60
          :padding--horizontal 15
          :justify-content     :center}
         [rn/view
          [text/text "Author:"]
          [quo2/author @state]]]]])))

(defn preview-author
  []
  [rn/keyboard-avoiding-view {:style {:flex 1}}
   [rn/view
    {:background-color (colors/theme-colors colors/white colors/neutral-90)
     :flex             1}
    [rn/flat-list
     {:flex                         1
      :keyboard-should-persist-taps :always
      :header                       [cool-preview]
      :key-fn                       str}]]])
