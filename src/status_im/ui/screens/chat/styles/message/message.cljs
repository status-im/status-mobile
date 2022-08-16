(ns status-im.ui.screens.chat.styles.message.message
  (:require [status-im.constants :as constants]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as photos]
            [quo2.foundations.colors :as quo2.colors]))

(defn style-message-text
  []
  {:color colors/text})

(defn system-message-body
  [_]
  {:margin-top     4
   :margin-left    8
   :margin-right   8
   :align-self     :center
   :align-items    :center})

(defn message-body
  []
  (let [align     :flex-start
        direction :row]
    {:flex-direction direction
     :margin-top     4
     :align-self     align
     :align-items    align}))

(def message-timestamp
  {:font-size  10})

(defn message-status-placeholder
  []
  (merge message-timestamp {:opacity 0 :color "rgba(0,0,0,0)"}))

(defn message-timestamp-wrapper []
  {:justify-content :center
   :margin-left 12 ;; horizontal margin is only required at the adjust side of the message.
   :margin-top 0
   :opacity 0})

(defn message-timestamp-text []
  (merge message-timestamp
         {:color       colors/gray
          :text-align :center}))

(defn message-status-text []
  {:font-size 10
   :line-height 10
   :color       colors/gray})

(defn audio-message-timestamp-text
  []
  (merge message-timestamp
         {:line-height 10
          :color       colors/gray}))

(defn message-wrapper [{:keys [in-popover?]}]
  (if (not in-popover?)
    {:margin-left 10
     :padding-right 5}
    {:margin-right 10}))

(defn message-author-wrapper []
  {:flex-direction :column
   :flex-shrink 1
   :align-items :flex-start
   :margin-left 4})

(defn delivery-status []
  {:align-self    :flex-start
   :padding-left  8})

(defn pin-indicator [display-photo?]
  (merge
   {:flex-direction             :row
    :border-top-left-radius     4
    :border-top-right-radius    12
    :border-bottom-left-radius  12
    :border-bottom-right-radius 12
    :padding-left               8
    :padding-right              10
    :padding-vertical           5
    :background-color           colors/gray-lighter
    :justify-content            :center
    :max-width                  "80%"
    :align-self  :flex-start
    :align-items :flex-start}
   (when display-photo?
     {:margin-left 44})))

(defn pin-indicator-container []
  {:margin-top      2
   :justify-content :center
   :align-self   :flex-start
   :align-items  :flex-start
   :padding-left 8})

(defn pinned-by-text-icon-container []
  {:flex-direction :row
   :align-items    :flex-start
   :top            5
   :left           8
   :position       :absolute})

(defn pin-icon-container []
  {:flex-direction :row
   :margin-top     1})

(defn pin-author-text []
  {:margin-left  2
   :margin-right 12
   :padding-right 0
   :left         12
   :flex-direction :row
   :flex-shrink  1
   :align-self   :flex-start
   :overflow     :hidden})

(defn pinned-by-text []
  {:margin-left 5})

(def message-author-touchable
  {:margin-left    0
   :flex-direction :row})

(defn message-author-userpic []
  (merge
   {:width      (+ 16 photos/default-size)} ;; 16 is for the padding
   {:padding-left 8
    :padding-right      8}))

(def delivery-text
  {:color       colors/gray
   :margin-top  2
   :font-size   12})

(def not-sent-view
  {:flex-direction :row
   :margin-bottom  2
   :padding-top    2})

(def not-sent-text
  (assoc delivery-text
         :color colors/red
         :text-align :right
         :padding-top 4))

(def not-sent-icon
  {:padding-top  3
   :padding-left 3})

(defn emoji-message
  [{:keys [incoming-group]}]
  {:font-size    28
   :line-height  34                     ;TODO: Smaller crops the icon on the top
   :margin-right 0 ;; Margin to display outgoing message status
   :margin-top   (if incoming-group 4 0)})

(defn collapse-button []
  {:height         24 :width 24 :background-color colors/blue
   :border-radius  12 :align-items :center :justify-content :center
   :elevation      4
   :shadow-opacity 1
   :shadow-radius  16
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}})

(def message-view-wrapper
  {:align-self :flex-end
   :flex-direction :row-reverse})

(defn message-view
  [{:keys [content-type mentioned pinned]}]
  (merge
   {:border-radius 10}

   (cond
     pinned                                             {:background-color colors/pin-background}
     (= content-type constants/content-type-system-text) nil
     mentioned                                           {:background-color colors/mentioned-background
                                                          :border-color colors/mentioned-border
                                                          :border-width 1}
     (= content-type constants/content-type-audio)       {:background-color colors/blue
                                                          :padding-horizontal 12
                                                          :padding-top 6}
     :else                                               {:background-color colors/white})

   (when (= content-type constants/content-type-emoji)
     {:flex-direction :row})))

(defn message-view-content []
  {:padding-bottom 6
   :overflow       :hidden})

(def status-container
  {:padding-horizontal 5})

(defn status-text []
  {:margin-top  9
   :font-size   14
   :color       colors/gray})

(defn message-author-name [chosen?]
  {:font-size           (if chosen? 13 12)
   :font-weight         (if chosen? "500" "400")
   :padding-top         6
   :padding-left        12
   :text-align-vertical :center})

(defn quoted-message-container []
  {:margin-bottom      6
   :margin-top         5
   :padding-horizontal 15})

(def quoted-message-author-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :flex-start})

(defn quoted-message-author [chosen?]
  (assoc (message-author-name chosen?)
         :padding-bottom  6
         :padding-top     0
         :padding-left    0
         :line-height     18
         :font-weight    "500"
         :color           colors/gray))

(defn quoted-message-text []
  {:font-size 14
   :color colors/gray})

(defn message-default-style []
  {:font-family "Inter-Regular"
   :color       (quo2.colors/theme-colors quo2.colors/black quo2.colors/white)
   :font-size   15
   :line-height 21.75
   :letter-spacing -0.135})

;; Markdown styles

(defn default-text-style []
  {:max-font-size-multiplier react/max-font-size-multiplier
   :style (message-default-style)})

(defn outgoing-text-style []
  (update (default-text-style) :style
          assoc :color colors/white-persist))

(defn system-text-style []
  (update (default-text-style) :style assoc
          :color colors/gray
          :line-height 20
          :font-size 14
          :text-align :center
          :font-weight "400"))

(defn text-style [content-type in-popover?]
  (merge
   (when in-popover? {:number-of-lines 2})
   (cond
     (= content-type constants/content-type-system-text) (system-text-style)
     :else (default-text-style))))

(defn emph-text-style []
  (update (default-text-style) :style
          assoc :font-style :italic))

(defn outgoing-emph-text-style []
  (update (emph-text-style) :style
          assoc :color colors/white-persist))

(defn emph-style []
  (emph-text-style))

(defn strong-text-style []
  (update (default-text-style) :style
          assoc :font-weight "700"))

(defn outgoing-strong-text-style []
  (update (strong-text-style) :style
          assoc :color colors/white-persist))

(defn strong-style []
  (outgoing-strong-text-style)
  (strong-text-style))

(defn strong-emph-style []
  (update (strong-style) :style
          assoc :font-style :italic))

(defn strikethrough-style []
  (cond-> (update (default-text-style) :style
                  assoc :text-decoration-line :line-through)))

(def code-block-background "#2E386B")

(defn inline-code-style []
  {:color            colors/white-persist
   :background-color code-block-background})

(def codeblock-style
  {:padding          10
   :background-color code-block-background
   :border-radius    4})

(def codeblock-text-style
  {:color colors/white-persist})

(defn default-blockquote-style []
  {:style {:border-left-width 2
           :padding-left 3
           :border-left-color colors/gray-transparent-40}})

(defn outgoing-blockquote-style []
  (update (default-blockquote-style) :style
          assoc
          :border-left-color colors/white-transparent-70-persist))

(defn blockquote-style []
  (default-blockquote-style))

(defn default-blockquote-text-style []
  (update (default-text-style) :style
          assoc
          :line-height 19
          :font-size 14
          :color colors/black-transparent-50))

(defn outgoing-blockquote-text-style []
  (update (default-blockquote-text-style) :style
          assoc
          :color colors/white-transparent-70-persist))

(defn blockquote-text-style []
  (outgoing-blockquote-text-style)
  (default-blockquote-text-style))

(defn image-message
  [{:keys [width height]}]
  {:overflow      :hidden
   :border-radius 8
   :width         width
   :height        height})

(defn image-message-border [opts]
  (merge (image-message opts)
         {:opacity          (:opacity opts)
          :border-width     1
          :top              0
          :left             0
          :position         :absolute
          :background-color :transparent
          :border-color     colors/black-transparent}))

(defn community-verified []
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

(defn community-message [verified]
  {:flex-direction          :row
   :padding-vertical        12
   :border-top-left-radius  (when-not verified 10)
   :border-top-right-radius (when-not verified 10)
   :border-right-width      1
   :border-left-width       1
   :border-top-width        1
   :border-color            colors/gray-lighter})

(defn community-view-button []
  {:border-width               1
   :padding-vertical           8
   :border-bottom-left-radius  10
   :border-bottom-right-radius 10
   :border-color               colors/gray-lighter})

(defn contact-request-status-label [state]
  {:width              136
   :border-radius      8
   :flex               1
   :justify-content    :center
   :align-items        :center
   :background-color   (when (= :retry state)
                         colors/blue-light)
   :border-width       1
   :border-color       (case state
                         constants/contact-request-message-state-accepted colors/green-transparent-10
                         constants/contact-request-message-state-declined colors/red-light
                         constants/contact-request-message-state-pending colors/gray-lighter)
   :padding-vertical   10
   :padding-horizontal 16})

(defn content-type-contact-request []
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
