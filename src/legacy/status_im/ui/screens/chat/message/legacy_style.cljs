(ns legacy.status-im.ui.screens.chat.message.legacy-style
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.react :as react]
    [quo.foundations.colors :as quo.colors]
    [quo.foundations.typography :as typography]
    [status-im2.constants :as constants]))

(defn style-message-text
  []
  {:color colors/text})

(defn message-wrapper
  [{:keys [in-popover?]}]
  (if (not in-popover?)
    {:margin-left   10
     :padding-right 5}
    {:margin-right 10}))

(defn emoji-message
  [{:keys [incoming-group]}]
  {:font-size    28
   :line-height  34 ;TODO: Smaller crops the icon on the top
   :margin-right 0 ;; Margin to display outgoing message status
   :margin-top   (if incoming-group 4 0)})

(defn message-view
  [{:keys [content-type]}]
  (merge
   {:border-radius 10}
   (when (= content-type constants/content-type-emoji)
     {:flex-direction :row})))

(defn message-view-content
  []
  {:padding-bottom 6
   :overflow       :hidden})

(def status-container
  {:padding-horizontal 5})

(defn status-text
  []
  {:margin-top 9
   :font-size  14
   :color      colors/gray})

(defn message-default-style
  []
  {:font-family    "Inter-Regular"
   :color          (quo.colors/theme-colors quo.colors/neutral-100 quo.colors/white)
   :font-size      15
   :line-height    21.75
   :letter-spacing -0.135})

;; Markdown styles
(defn default-text-style
  []
  {:max-font-size-multiplier react/max-font-size-multiplier
   :style                    (message-default-style)})

(defn system-text-style
  []
  (update (default-text-style)
          :style       assoc
          :color       colors/gray
          :line-height 20
          :font-size   14
          :text-align  :center
          :font-weight "400"))

(defn text-style
  [content-type in-popover?]
  (merge
   (when in-popover? {:number-of-lines 2})
   (cond
     (= content-type constants/content-type-system-text)           (system-text-style)
     (= content-type constants/content-type-system-pinned-message) (system-text-style)
     :else                                                         (default-text-style))))

(defn emph-text-style
  []
  (update (default-text-style)
          :style
          assoc
          :font-style :italic))

(defn emph-style
  []
  (emph-text-style))

(defn strong-text-style
  []
  (update (default-text-style)
          :style
          assoc
          :font-weight "700"))

(defn outgoing-strong-text-style
  []
  (update (strong-text-style)
          :style
          assoc
          :color colors/white-persist))

(defn strong-style
  []
  (outgoing-strong-text-style)
  (strong-text-style))

(defn strong-emph-style
  []
  (update (strong-style)
          :style
          assoc
          :font-style :italic))

(defn strikethrough-style
  []
  (cond-> (update (default-text-style)
                  :style
                  assoc
                  :text-decoration-line :line-through)))

(defn edited-style
  []
  (cond->
    (update (default-text-style)
            :style
            assoc
            :color (quo.colors/theme-colors quo.colors/neutral-40 quo.colors/neutral-50)
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
  []
  (update (default-text-style)
          :style
          assoc
          :line-height 19
          :font-size 14
          :color colors/black-transparent-50))

(defn outgoing-blockquote-text-style
  []
  (update (default-blockquote-text-style)
          :style
          assoc
          :color colors/white-transparent-70-persist))

(defn blockquote-text-style
  []
  (outgoing-blockquote-text-style)
  (default-blockquote-text-style))

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

(defn contact-request-status-label
  [state]
  {:width              136
   :border-radius      8
   :flex               1
   :justify-content    :center
   :align-items        :center
   :background-color   (when (= :retry state)
                         colors/blue-light)
   :border-width       1
   :border-color       (condp = state
                         constants/contact-request-message-state-accepted colors/green-transparent-10
                         constants/contact-request-message-state-declined colors/red-light
                         constants/contact-request-message-state-pending  colors/gray-lighter
                         nil)
   :padding-vertical   10
   :padding-horizontal 16})

(defn content-type-contact-request
  []
  {:width           168
   :min-height      224.71
   :border-radius   8
   :border-width    1
   :border-color    colors/gray-lighter
   :align-items     :center
   :padding-bottom  10
   :margin-vertical 4
   :align-self      :flex-start
   :margin-right    0
   :margin-left     8})
