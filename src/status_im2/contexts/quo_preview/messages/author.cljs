(ns status-im2.contexts.quo-preview.messages.author
  (:require [react-native.core :as rn]
            [quo2.foundations.colors :as colors]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo2.components.messages.author.view :as quo2]
            [reagent.core :as reagent]))

(def descriptor [{:label "Profile name"
                  :key   :profile-name
                  :type  :text
                  :limit 24}
                 {:label "Nickname"
                  :key   :nickname
                  :type  :text}
                 {:label "Chat key"
                  :key   :chat-key
                  :type  :text}
                 {:label  "ENS name"
                  :key    :ens-name
                  :type   :text
                  :suffix ".eth"}
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

(defn cool-preview []
  (let [state (reagent/atom {:profile-name   "Alisher Yakupov"
                             :nickname       ""
                             :chat-key       "zQ3ssgRy5TtB47MMiMKMKaGyaawkCgMqqbrnAUYrZJ1sgt5N"
                             :time-str       "09:30"
                             :ens-name       ""
                             :contact?       false
                             :verified?      false
                             :untrustworthy? false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view {:padding-vertical    60
                  :padding--horizontal 15
                  :flex-direction      :row
                  :justify-content     :center}
         [quo2/author @state]]]])))

(defn preview-author []
  [rn/view {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}
   [rn/flat-list {:flex                      1
                  :keyboardShouldPersistTaps :always
                  :header                    [cool-preview]
                  :key-fn                    str}]])
