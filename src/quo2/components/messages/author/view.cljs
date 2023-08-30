(ns quo2.components.messages.author.view
  (:require [clojure.string :as string]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.messages.author.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def middle-dot "·")

(defn author
  [{:keys [primary-name secondary-name style short-chat-key time-str contact? verified? untrustworthy?
           muted? size]
    :or   {size 13}}]
  [rn/view {:style (merge style/container style)}
   [text/text
    {:weight              :semi-bold
     :size                (if (= size 15) :paragraph-1 :paragraph-2)
     :number-of-lines     1
     :accessibility-label :author-primary-name
     :style               {:color (if muted?
                                    colors/neutral-50
                                    (colors/theme-colors colors/neutral-100 colors/white))}}
    primary-name]
   (when (not (string/blank? secondary-name))
     [:<>
      [text/text
       {:size            :label
        :number-of-lines 1
        :style           (style/middle-dot-nickname)}
       middle-dot]
      [text/text
       {:weight              :medium
        :size                :label
        :number-of-lines     1
        :accessibility-label :author-secondary-name
        :style               {:padding-top 1
                              :color       (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
       secondary-name]])
   (when contact?
     [icons/icon :main-icons2/contact
      {:size            12
       :no-color        true
       :container-style style/icon-container}])
   (cond
     verified?
     [icons/icon :main-icons2/verified
      {:size            12
       :no-color        true
       :container-style style/icon-container}]
     untrustworthy?
     [icons/icon :main-icons2/untrustworthy
      {:size            12
       :no-color        true
       :container-style style/icon-container}])
   (when (and (not verified?) short-chat-key)
     [text/text
      {:monospace       true
       :size            :label
       :number-of-lines 1
       :style           (style/chat-key-text)}
      short-chat-key])
   (when (and (not verified?) time-str short-chat-key)
     [text/text
      {:monospace       true
       :size            :label
       :number-of-lines 1
       :style           (style/middle-dot-chat-key)}
      middle-dot])
   (when time-str
     [text/text
      {:monospace           true
       :size                :label
       :accessibility-label :message-timestamp
       :number-of-lines     1
       :style               (style/time-text verified?)}
      time-str])])
