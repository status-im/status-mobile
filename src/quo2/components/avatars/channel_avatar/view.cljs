(ns quo2.components.avatars.channel-avatar.view
  (:require [quo2.components.avatars.channel-avatar.style :as style]
            [quo2.components.icon :as icons]
            [quo2.foundations.colors :as colors]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))

(defn- lock
  [{:keys [locked? big?]}]
  (when (boolean? locked?)
    [rn/view {:style (style/lock-container {:big? big?})}
     [icons/icon
      (cond (true? locked?)  :i/locked
            (false? locked?) :i/unlocked)
      {:color           (colors/theme-colors colors/neutral-50 colors/neutral-40)
       :container-style style/lock-icon
       :size            12}]]))

(defn view
  [{:keys [big? locked? emoji-background-color emoji]}]
  [rn/view {:style (style/outer-container {:big? big? :background-color emoji-background-color})}
   [rn/view {:style style/inner-container}
    [text/text {:size (if big? :paragraph-1 :label)}
     emoji]
    [lock {:locked? locked? :big? big?}]]])
