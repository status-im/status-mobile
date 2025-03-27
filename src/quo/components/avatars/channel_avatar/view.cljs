(ns quo.components.avatars.channel-avatar.view
  (:require
    [clojure.string :as string]
    [quo.components.avatars.channel-avatar.style :as style]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.context :as quo.context]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [utils.string]))

(defn- initials
  [{:keys [full-name size customization-color theme]}]
  (let [amount-initials (if (#{:size-32 :size-64 :size-80} size) 2 1)
        channel-name    (utils.string/safe-replace full-name "#" "")]
    [text/text
     (cond-> {:accessibility-label :initials
              :style               {:color (colors/resolve-color customization-color theme)}
              :size                :paragraph-2
              :weight              :semi-bold}
       (#{:size-64 :size-80} size) (assoc :size   :heading-1
                                          :weight :medium))
     (utils.string/get-initials channel-name amount-initials)]))

(defn- lock
  [locked? size theme]
  ;; When `locked?` is nil, we must not display the unlocked icon.
  (when (boolean? locked?)
    [rn/view
     {:accessibility-label :lock
      :style               (style/lock-container size theme)}
     [icons/icon (if locked? :i/locked :i/unlocked)
      {:color           (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
       :container-style style/lock-icon
       :size            12}]]))

(defn view
  "Options:

  :size - keyword (default nil) - Container size, for the moment,
  only :size/l (meaning large) is supported.

  :emoji - string (default nil)

  :customization-color - color (default nil) If the component is used for a
  community channel, then the default color should be the community custom
  color.

  :locked? - nil/bool (default nil) - When true/false display a locked/unlocked
  icon respectively. When nil does not show icon.

  :full-name - string (default nil) - When :emoji is blank, this value will be
  used to extract the initials.
  "
  [{:keys [size emoji customization-color locked? full-name]}]
  (let [theme (quo.context/use-theme)]
    [rn/view
     {:accessibility-label :channel-avatar
      :style               (style/outer-container {:theme               theme
                                                   :size                size
                                                   :customization-color customization-color})}
     (if (string/blank? emoji)
       [initials
        {:full-name           full-name
         :size                size
         :customization-color customization-color
         :theme               theme}]
       [rn/text
        {:style               (style/emoji-size size)
         :accessibility-label :emoji}
        (when emoji (string/trim emoji))])
     [lock locked? size theme]]))
