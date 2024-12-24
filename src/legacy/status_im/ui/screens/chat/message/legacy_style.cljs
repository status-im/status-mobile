(ns legacy.status-im.ui.screens.chat.message.legacy-style
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]
    [quo.foundations.colors :as quo.colors]
    [quo.foundations.typography :as typography]
    [status-im.constants :as constants]))

(defn message-wrapper
  [{:keys [in-popover?]}]
  (if (not in-popover?)
    {:margin-left   10
     :padding-right 5}
    {:margin-right 10}))

(def status-container
  {:padding-horizontal 5})

(defn status-text
  []
  {:margin-top 9
   :font-size  14
   :color      colors/gray})

(defn message-default-style
  [theme]
  {:font-family    "Inter-Regular"
   :color          (quo.colors/theme-colors quo.colors/neutral-100 quo.colors/white theme)
   :font-size      15
   :line-height    21.75
   :letter-spacing -0.135})

;; Markdown styles
(defn default-text-style
  [theme]
  {:max-font-size-multiplier react/max-font-size-multiplier
   :style                    (message-default-style theme)})

(defn system-text-style
  [theme]
  (update (default-text-style theme)
          :style       assoc
          :color       colors/gray
          :line-height 20
          :font-size   14
          :text-align  :center
          :font-weight "400"))

(defn text-style
  [content-type in-popover? theme]
  (merge
   (when in-popover? {:number-of-lines 2})
   (cond
     (= content-type constants/content-type-system-text)           (system-text-style theme)
     (= content-type constants/content-type-system-pinned-message) (system-text-style theme)
     :else                                                         (default-text-style theme))))

(defn emph-text-style
  [theme]
  (update (default-text-style theme)
          :style
          assoc
          :font-style :italic))

(defn emph-style
  [theme]
  (emph-text-style theme))

(defn strong-text-style
  [theme]
  (update (default-text-style theme)
          :style
          assoc
          :font-weight "700"))

(defn outgoing-strong-text-style
  [theme]
  (update (strong-text-style theme)
          :style
          assoc
          :color colors/white-persist))

(defn strong-style
  [theme]
  (outgoing-strong-text-style theme)
  (strong-text-style theme))

(defn strong-emph-style
  [theme]
  (update (strong-style theme)
          :style
          assoc
          :font-style :italic))

(defn strikethrough-style
  [theme]
  (cond-> (update (default-text-style theme)
                  :style
                  assoc
                  :text-decoration-line :line-through)))

(defn edited-style
  [theme]
  (cond->
    (update (default-text-style theme)
            :style
            assoc
            :color (quo.colors/theme-colors quo.colors/neutral-40 quo.colors/neutral-50 theme)
            :font-size 13
            :line-height 18.2
            :letter-spacing (typography/tracking 13))))

(def codeblock-style
  {:padding          10
   :background-color "#2E386B"
   :border-radius    4})

(defn default-blockquote-style
  []
  {:style {:border-left-width 2
           :padding-left      3
           :border-left-color colors/gray-transparent-40}})

(defn blockquote-style
  []
  (default-blockquote-style))

(defn default-blockquote-text-style
  [theme]
  (update (default-text-style theme)
          :style
          assoc
          :line-height 19
          :font-size 14
          :color colors/black-transparent-50))

(defn outgoing-blockquote-text-style
  [theme]
  (update (default-blockquote-text-style theme)
          :style
          assoc
          :color colors/white-transparent-70-persist))

(defn blockquote-text-style
  [theme]
  (outgoing-blockquote-text-style theme)
  (default-blockquote-text-style theme))

(defn community-verified
  []
  {:border-right-width      1
   :border-left-width       1
   :border-top-width        1
   :border-left-color       colors/gray-lighter
   :border-right-color      colors/gray-lighter
   :border-top-left-radius  10
   :border-top-right-radius 10
   :padding-vertical        8
   :padding-horizontal      15
   :border-top-color        colors/gray-lighter})

(defn community-message
  [verified]
  {:flex-direction          :row
   :padding-vertical        12
   :border-top-left-radius  (when-not verified 10)
   :border-top-right-radius (when-not verified 10)
   :border-right-width      1
   :border-left-width       1
   :border-top-width        1
   :border-color            colors/gray-lighter})

(defn community-view-button
  []
  {:border-width               1
   :padding-vertical           8
   :border-bottom-left-radius  10
   :border-bottom-right-radius 10
   :border-color               colors/gray-lighter})

