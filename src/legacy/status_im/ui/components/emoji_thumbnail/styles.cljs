(ns legacy.status-im.ui.components.emoji-thumbnail.styles
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.emoji-thumbnail.utils :as emoji-utils]
    [react-native.platform :as platform]))

(defn emoji-thumbnail-icon
  [color size]
  {:width               size
   :height              size
   :align-items         :center
   :justify-content     :center
   :border-radius       (/ size 2)
   :background-color    color
   :border-width        0.5
   :border-color        "rgba(0,0,0,0.1)"
   :accessibility-label :thumbnail-container-circle})

(defn emoji-thumbnail-icon-text
  [size]
  {:font-size   (emoji-utils/emoji-font-size size)
   :line-height size
   :margin-top  (emoji-utils/emoji-top-margin-for-vertical-alignment size)})  ;; Required for vertical alignment bug - Check function defination for more info


;; Styles Related to Emoji Thumbnail Picker

(def emoji-thumbnail-preview-size 60)

(def emoji-picker-upper-components-size
  (if platform/android? 350 405))

(defn emoji-picker-gray-color
  []
  (if (colors/dark?) "#c3c3bc99" "#3C3C4399"))

(defn emoji-picker-category-container
  []
  (if (colors/dark?) "#110d0a" "#EEF2F5"))

(defn emoji-picker-active-category-container
  []
  (if (colors/dark?) "#87877f33" "#78788033"))

(defn emoji-picker-active-category-color
  []
  (if (colors/dark?) "#bbbbbb" "#000000"))

(def emoji-thumbnail-preview
  {:margin-top          16
   :margin-bottom       16
   :align-items         :center
   :justify-content     :center
   :accessibility-label :emoji-preview})

(defn emoji-picker-keyboard-container
  []
  {:border-radius    0
   :background-color (colors/get-color :ui-background)})

(defn emoji-picker-search-bar
  []
  {:border-radius    10
   :height           36
   :background-color (:ui-01 @colors/theme)})

(defn emoji-picker-search-bar-text
  []
  {:color (emoji-picker-gray-color)})

(defn emoji-picker-header
  []
  {:font-size     13
   :color         (emoji-picker-gray-color)
   :font-weight   "600"
   :margin-top    7
   :margin-bottom 0})

(defn emoji-keyboard
  [func]
  {:onEmojiSelected              func
   :emojiSize                    23
   :containerStyles              (emoji-picker-keyboard-container)
   :categoryPosition             "floating"
   :categoryContainerColor       (emoji-picker-category-container)
   :activeCategoryColor          (emoji-picker-active-category-color)
   :activeCategoryContainerColor (emoji-picker-active-category-container)
   :categoryColor                (emoji-picker-gray-color)
   :enableSearchBar              true
   :searchBarTextStyles          (emoji-picker-search-bar-text)
   :searchBarStyles              (emoji-picker-search-bar)
   :searchBarPlaceholderColor    (emoji-picker-gray-color)
   :closeSearchColor             (emoji-picker-gray-color)
   :headerStyles                 (emoji-picker-header)
   :disabledCategory             ["symbols"]})

(def emoji-picker-color-row-container
  {:flex-direction  "row"
   :justify-content "space-around"
   :flex-grow       1})

(def emoji-picker-row1-style
  {:margin-top 10})

(def emoji-picker-row2-style
  {:margin-top    10
   :margin-bottom 10
   :margin-left   25
   :margin-right  25})

(def emoji-picker-row3-style
  {:margin-bottom 10})

(defn emoji-picker-color-border
  [item_color color-selected?]
  {:height          44
   :width           44
   :border-radius   22
   :border-width    2
   :border-color    (if color-selected? item_color "#00000000")
   :align-items     :center
   :justify-content :center})

(defn emoji-picker-color
  [item_color]
  {:height           36
   :width            36
   :border-radius    18
   :border-width     0.5
   :background-color item_color
   :border-color     "rgba(0,0,0,0.1)"})

(def emoji-picker-default-thumbnails
  [{:emoji "🐺" :color "#CCCCCC"}
   {:emoji "🏆" :color "#F5E4A3"}
   {:emoji "🦀" :color "#F5A3A3"}
   {:emoji "🍁" :color "#F5D7A3"}
   {:emoji "🐳" :color "#A3DCF5"}
   {:emoji "🦕" :color "#CFF5A3"}
   {:emoji "🐥" :color "#F5F5A3"}
   {:emoji "🐇" :color "#DAE2E7"}
   {:emoji "🐒" :color "#E0C2B8"}
   {:emoji "🦍" :color "#CCCCCC"}
   {:emoji "🤠" :color "#E0C2B8"}
   {:emoji "👾" :color "#F5A3BF"}
   {:emoji "🕴️" :color "#CCCCCC"}
   {:emoji "💃" :color "#F5A3A3"}
   {:emoji "🦹" :color "#E9A3F5"}
   {:emoji "🐕" :color "#F5D7A3"}
   {:emoji "🦇" :color "#C0A3F5"}
   {:emoji "🦜" :color "#F5A3A3"}
   {:emoji "🐢" :color "#CFF5A3"}
   {:emoji "🦎" :color "#CFF5A3"}
   {:emoji "🐊" :color "#CFF5A3"}
   {:emoji "🦋" :color "#F5B6A3"}
   {:emoji "🕸️" :color "#CCCCCC"}
   {:emoji "🦈" :color "#A3DCF5"}
   {:emoji "☘️" :color "#CFF5A3"}
   {:emoji "🍇" :color "#E9A3F5"}
   {:emoji "🍎" :color "#F5B6A3"}
   {:emoji "🥥" :color "#E0C2B8"}
   {:emoji "🧀" :color "#F5E4A3"}
   {:emoji "🥪" :color "#F5D7A3"}
   {:emoji "🥣" :color "#A3DCF5"}
   {:emoji "🍜" :color "#F5B6A3"}
   {:emoji "🧁" :color "#A3DCF5"}
   {:emoji "☕" :color "#DAE2E7"}
   {:emoji "🍷" :color "#F5A3A3"}
   {:emoji "🚆" :color "#A3DCF5"}
   {:emoji "👓" :color "#CCCCCC"}
   {:emoji "🎶" :color "#A3DCF5"}])
