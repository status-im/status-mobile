(ns quo2.components.avatars.channel-avatar.view
  (:require
    [clojure.string :as string]
    [quo2.components.avatars.channel-avatar.style :as style]
    [quo2.components.icon :as icons]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [utils.string]))

(defn- initials
  [{:keys [full-name size customization-color theme]}]
  (let [amount-initials (if (#{:size-32 :size-64} size) 2 1)
        channel-name    (string/replace full-name "#" "")]
    [text/text
     (cond-> {:accessibility-label :initials
              :style               {:color (colors/resolve-color customization-color theme)}
              :size                :paragraph-2
              :weight              :semi-bold}
       (= size :size-64) (assoc :size   :heading-1
                                :weight :medium))
     (utils.string/get-initials channel-name amount-initials)]))

(defn- badge-icon
  [{:keys [badge size theme]}]
  [rn/view
   {:accessibility-label :channel-avatar-badge
    :style               (style/badge-container size theme)}
   [icons/icon
    (case badge
      :locked   :i/locked
      :unlocked :i/unlocked)
    {:color               (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
     :container-style     style/badge-icon
     :size                12
     :accessibility-label (keyword (str "channel-avatar-badge-" (name badge)))}]])

(defn- view-internal
  "Options:

  :size - keyword (defaults to 24) - Container size (:size-32 :size-64)

  :emoji - string (default nil)

  :customization-color - color (default nil) If the component is used for a
  community channel, then the default color should be the community custom
  color.

  :badge - keyword (default nil) shows a badge next to the avatar (:locked :unlocked)

  :full-name - string (default nil) - When :emoji is blank, this value will be
  used to extract the initials.
  "
  [{:keys [size emoji customization-color badge full-name theme]}]
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
      (string/trim emoji)])
   (when (keyword? badge)
     [badge-icon
      {:badge badge
       :size  size
       :theme theme}])])

(def view (quo.theme/with-theme view-internal))
