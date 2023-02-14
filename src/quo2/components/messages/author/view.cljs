(ns quo2.components.messages.author.view
  (:require [clojure.string :as string]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.messages.author.style :as style]
            [react-native.core :as rn]
            [quo2.foundations.colors :as colors]))

(def middle-dot "·")

(defn author
  [{:keys [primary-name secondary-name short-chat-key time-str contact? verified? untrustworthy?]}]
  [:f>
   (fn []
     [rn/view {:style style/container}
      [:<>
       [text/text
        {:weight :semi-bold
         :size   :paragraph-2
         :style  {:color (colors/theme-colors colors/neutral-100 colors/white)}}
        primary-name]
       (when (not (string/blank? secondary-name))
         [:<>
          [text/text
           {:size  :paragraph-2
            :style style/middle-dot-nickname}
           middle-dot]
          [text/text
           {:weight :medium
            :size   :paragraph-2
            :style  {:color (colors/theme-colors colors/neutral-60 colors/neutral-40)}}
           secondary-name]])]
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
      (when-not verified?
        [text/text
         {:monospace true
          :size      :paragraph-2
          :style     style/chat-key-text}
         short-chat-key])
      (when (and (not verified?) time-str)
        [text/text
         {:monospace true
          :size      :paragraph-2
          :style     style/middle-dot-chat-key}
         middle-dot])
      [text/text
       {:monospace           true
        :size                :paragraph-2
        :accessibility-label :message-timestamp
        :style               (style/time-text verified?)}
       time-str]])])
