(ns quo2.components.avatars.channel-avatar.view
  (:require [clojure.string :as string]
            [quo2.components.avatars.channel-avatar.style :as style]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            utils.string))

(defn- initials
  [full-name big? color]
  (let [amount-initials (if big? 2 1)]
    [text/text
     {:accessibility-label :initials
      :size                :paragraph-2
      :weight              :semi-bold
      :style               {:color color}}
     (utils.string/get-initials full-name amount-initials)]))

(defn- lock
  [locked? big?]
  ;; When `locked?` is nil, we must not display the unlocked icon.
  (when (boolean? locked?)
    [rn/view
     {:accessibility-label :lock
      :style               (style/lock-container big?)}
     [icons/icon (if locked? :i/locked :i/unlocked)
      {:color           (colors/theme-colors colors/neutral-50 colors/neutral-40)
       :container-style style/lock-icon
       :size            12}]]))

(defn view
  "Options:

  :big? - bool (default nil) - Container size

  :emoji - string (default nil)

  :color - color (default nil) If the component is used for a community channel,
  then the default color should be the community custom color.

  :locked? - nil/bool (default nil) - When true/false display a locked/unlocked
  icon respectively. When nil does not show icon.

  :full-name - string (default nil) - When :emoji is blank, this value will be
  used to extract the initials.
  "
  [{:keys [big? emoji color locked? full-name]}]
  [rn/view
   {:accessibility-label :channel-avatar
    :style               (style/outer-container {:big? big? :color color})}
   (if (string/blank? emoji)
     [initials full-name big? color]
     [text/text
      {:accessibility-label :emoji
       :size                (if big? :paragraph-1 :label)}
      emoji])
   [lock locked? big?]])
