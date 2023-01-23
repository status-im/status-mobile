(ns quo2.components.messages.author.view
  (:require [clojure.string :as string]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.messages.author.style :as style]
            [react-native.core :as rn]))

(def middle-dot "·")

(defn display-name
  [{:keys [profile-name nickname ens-name text-style]}]
  (let [ens?      (-> ens-name string/blank? not)
        nickname? (-> nickname string/blank? not)]
    (if ens?
      [text/text
       (merge {:weight :semi-bold
               :size   :paragraph-2
               :style  (style/ens-text)}
              text-style)
       ens-name]
      [:<>
       (if nickname?
         [text/text
          (merge {:weight :semi-bold
                  :size   :paragraph-2
                  :style  (style/nickname-text)}
                 text-style)
          nickname]
         [text/text
          (merge {:weight :semi-bold
                  :size   :paragraph-2
                  :style  (style/profile-name-text nickname?)}
                 text-style)
          profile-name])])))

(defn author
  [{:keys [profile-name nickname short-chat-key ens-name time-str contact? verified? untrustworthy?]}]
  [:f>
   (fn []
     (let [ens?      (-> ens-name string/blank? not)
           nickname? (-> nickname string/blank? not)]
       [rn/view {:style style/container}
        (if ens?
          [text/text
           {:weight :semi-bold
            :size   :paragraph-2
            :style  (style/ens-text)}
           ens-name]
          [:<>
           (when nickname?
             [:<>
              [text/text
               {:weight :semi-bold
                :size   :paragraph-2
                :style  (style/nickname-text)}
               nickname]
              [text/text
               {:size  :paragraph-2
                :style style/middle-dot-nickname}
               middle-dot]])
           [text/text
            {:weight (if nickname? :medium :semi-bold)
             :size   :paragraph-2
             :style  (style/profile-name-text nickname?)}
            profile-name]])
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
        (when-not ens?
          [text/text
           {:monospace true
            :size      :paragraph-2
            :style     style/chat-key-text}
           short-chat-key])
        (when-not ens?
          [text/text
           {:monospace true
            :size      :paragraph-2
            :style     style/middle-dot-chat-key}
           middle-dot])
        [text/text
         {:monospace           true
          :size                :paragraph-2
          :accessibility-label :message-timestamp
          :style               (style/time-text ens?)}
         time-str]]))])
