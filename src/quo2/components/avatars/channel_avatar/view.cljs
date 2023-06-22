(ns quo2.components.avatars.channel-avatar.view
  (:require [clojure.string :as string]
            [quo2.components.avatars.channel-avatar.style :as style]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            utils.string))

(defn- initials
  [full-name amount-initials color]
  [text/text
   {:accessibility-label :initials
    :size                :paragraph-2
    :number-of-lines     1
    :ellipsize-mode      :clip
    :weight              :semi-bold
    :style               {:color color}}
   (utils.string/get-initials full-name amount-initials)])

(defn- lock
  [locked? big?]
  (when (boolean? locked?)
    [rn/view
     {:accessibility-label :lock
      :style               (style/lock-container {:big? big?})}
     [icons/icon
      (cond (true? locked?)  :i/locked
            (false? locked?) :i/unlocked)
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

  :amount-initials - int (default 1) - Number of initials to be extracted
  from :full-name when :emoji is blank.
  "
  [{:keys [big? emoji color locked? full-name amount-initials]}]
  (let [amount-initials (or amount-initials 1)
        color           (or color (colors/custom-color-by-theme :blue 50 50))]
    [rn/view
     {:accessibility-label :channel-avatar
      :style               (style/outer-container {:big? big? :color color})}
     (if (string/blank? emoji)
       [initials full-name amount-initials color]
       [text/text
        {:accessibility-label :emoji
         :size                (if big? :paragraph-1 :label)}
        emoji])
     [lock locked? big?]]))
