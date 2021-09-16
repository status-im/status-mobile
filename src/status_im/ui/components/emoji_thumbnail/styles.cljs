(ns status-im.ui.components.emoji-thumbnail.styles
  (:require [quo.design-system.colors :as colors]
            [status-im.ui.components.emoji-thumbnail.utils :as emoji-utils]))

(defn emoji-thumbnail-icon [color size]
  {:width            size
   :height           size
   :align-items      :center
   :justify-content  :center
   :border-radius    (/ size 2)
   :background-color color
   :border-width     0.5
   :border-color     "rgba(0,0,0,0.1)"})

(defn emoji-thumbnail-icon-text [size]
  {:font-size   (emoji-utils/emoji-font-size size)
   :line-height size
   :margin-top  (emoji-utils/emoji-top-margin-for-vertical-alignment size)})  ;; Required for vertical alignment bug - Check function defination for more info


;; Styles Related to Emoji Thumbnail Picker

(def emoji-thumbnail-preview-size 60)

(def emoji-picker-upper-components-size 350)

(defn emoji-picker-gray-color []
  (if (colors/dark?) "#c3c3bc99" "#3C3C4399"))

(defn emoji-picker-category-or-search-container []
  (if (colors/dark?) "#110d0a" "#EEF2F5"))

(defn emoji-picker-active-category-container []
  (if (colors/dark?) "#87877f33" "#78788033"))

(defn emoji-picker-active-category-color []
  (if (colors/dark?) "#bbbbbb" "#000000"))

(def emoji-thumbnail-preview
  {:margin-top       16
   :margin-bottom    16
   :align-items      :center
   :justify-content  :center})

(defn emoji-picker-keyboard-container []
  {:border-radius    0
   :background-color (colors/get-color :ui-background)})

(defn emoji-picker-search-bar []
  {:border-radius    10
   :height           36
   :background-color (emoji-picker-category-or-search-container)})

(defn emoji-picker-search-bar-text []
  {:color (emoji-picker-gray-color)})

(defn emoji-picker-header []
  {:font-size     13
   :color         (emoji-picker-gray-color)
   :font-weight   "600"
   :margin-top    7
   :margin-bottom 0})

(defn emoji-keyboard [func]
  {:onEmojiSelected              func
   :emojiSize                    23
   :containerStyles              (emoji-picker-keyboard-container)
   :categoryPosition             "floating"
   :categoryContainerColor       (emoji-picker-category-or-search-container)
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

(def emoji-picker-colors-row1
  [{:name "red"     :color "#F5A3A3"}
   {:name "pink"    :color "#F5A3BF"}
   {:name "magenta" :color "#E9A3F5"}
   {:name "purple"  :color "#C0A3F5"}
   {:name "indigo"  :color "#A3B0F5"}
   {:name "blue"    :color "#A3C2F5"}
   {:name "cyan"    :color "#A3DCF5"}])

(def emoji-picker-colors-row2
  [{:name "teal"   :color "#A3ECF5"}
   {:name "mint"   :color "#A3F5E2"}
   {:name "green"  :color "#A3F5BA"}
   {:name "moss"   :color "#CFF5A3"}
   {:name "lemon"  :color "#EEF5A3"}
   {:name "yellow" :color "#F5F5A3"}])

(def emoji-picker-colors-row3
  [{:name "honey"  :color "#F5E4A3"}
   {:name "orange" :color "#F5D7A3"}
   {:name "peach"  :color "#F5B6A3"}
   {:name "brown"  :color "#E0C2B8"}
   {:name "grey"   :color "#CCCCCC"}
   {:name "dove"   :color "#DAE2E7"}
   {:name "white"  :color "#FFFFFF"}])

(def emoji-picker-color-row-container
  {:flex-direction  "row"
   :justify-content "space-around"
   :flex-grow       1})

(def emoji-picker-row1-style
  {:margin-top     10})

(def emoji-picker-row2-style
  {:margin-top     10
   :margin-bottom  10
   :margin-left    25
   :margin-right   25})

(def emoji-picker-row3-style
  {:margin-bottom  10})

(defn emoji-picker-color-border [item_color color-selected?]
  {:height          44
   :width           44
   :border-radius   22
   :border-width    2
   :border-color    (if color-selected? item_color "#00000000")
   :align-items     :center
   :justify-content :center})

(defn emoji-picker-color [item_color]
  {:height 36
   :width 36
   :border-radius 18
   :border-width 0.05
   :background-color item_color
   :border-color "#000000"})

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
