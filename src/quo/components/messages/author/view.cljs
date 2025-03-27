(ns quo.components.messages.author.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.components.messages.author.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(def middle-dot "·")

(defn view
  [{:keys [primary-name secondary-name style short-chat-key time-str contact? verified? untrustworthy?
           muted? size]
    :or   {size 13}}]
  (let [theme (quo.context/use-theme)

        short-chat-key-component
        (when (and (not verified?) short-chat-key)
          [text/text
           {:weight          :monospace
            :size            :label
            :number-of-lines 1
            :style           (style/chat-key-text theme)}
           short-chat-key])

        time-str-component
        (when time-str
          [text/text
           {:monospace           true
            :size                :label
            :accessibility-label :message-timestamp
            :number-of-lines     1
            :style               (style/time-text theme)}
           time-str])

        middle-dot-seperator-component
        (when (and short-chat-key-component time-str-component)
          [text/text
           {:monospace       true
            :size            :label
            :number-of-lines 1
            :style           (style/middle-dot theme)}
           middle-dot])]
    [rn/view
     {:style (merge (style/container size) style)}
     [text/text
      {:weight              :semi-bold
       :size                (if (= size 15) :paragraph-1 :paragraph-2)
       :number-of-lines     1
       :accessibility-label :author-primary-name
       :style               (style/primary-name muted? theme size)}
      primary-name]
     (when-not (string/blank? secondary-name)
       [:<>
        [text/text
         {:size            :label
          :number-of-lines 1
          :style           (style/middle-dot theme)}
         middle-dot]
        [text/text
         {:weight              :medium
          :size                :label
          :number-of-lines     1
          :accessibility-label :author-secondary-name
          :style               (style/secondary-name theme)}
         secondary-name]])
     (when contact?
       [icons/icon :main-icons2/contact
        {:size            12
         :no-color        true
         :container-style (style/icon-container true size)}])
     (cond
       verified?
       [icons/icon :main-icons2/verified
        {:size            12
         :no-color        true
         :container-style (style/icon-container contact? size)}]
       untrustworthy?
       [icons/icon :main-icons2/untrustworthy
        {:size            12
         :no-color        true
         :container-style (style/icon-container contact? size)}])

     (when (or short-chat-key-component time-str-component)
       [rn/view {:style {:width 8}}])

     short-chat-key-component
     middle-dot-seperator-component
     time-str-component]))
