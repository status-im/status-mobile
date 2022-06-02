(ns status-im.ui.screens.chat.styles.message.message
  (:require [status-im.constants :as constants]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as photos]
            [status-im.ui.components.typography :as typography]))

(defn style-message-text
  [outgoing]
  {:color (if outgoing colors/white-persist colors/text)})

(defn system-message-body
  [_]
  {:margin-top     4
   :margin-left    8
   :margin-right   8
   :align-self     :center
   :align-items    :center})

(defn message-body
  [{:keys [outgoing]}]
  (let [align     (if outgoing :flex-end :flex-start)
        direction (if outgoing :row-reverse :row)]
    {:flex-direction direction
     :margin-top     4
     :align-self     align
     :align-items    align}))

(def message-timestamp
  {:font-size  10})

(defn message-status-placeholder
  []
  (merge message-timestamp {:opacity 0 :color "rgba(0,0,0,0)"}))

(defn message-timestamp-wrapper [{:keys [last-in-group? outgoing group-chat]}]
  {:justify-content :center
   (if outgoing :margin-right :margin-left) 12 ;; horizontal margin is only required at the adjust side of the message.
   :margin-top (if (and last-in-group?
                        (or outgoing
                            (not group-chat)))
                 16
                 0) ;; Add gap between message groups
   :opacity 0})

(defn message-timestamp-text []
  (merge message-timestamp
         {:color       colors/gray
          :text-align :center}))

(defn message-status-text [outgoing]
  {:font-size 10
   :line-height 10
   :color       (if outgoing
                  colors/white-transparent-70-persist
                  colors/gray)})

(defn audio-message-timestamp-text
  [outgoing]
  (merge message-timestamp
         {:line-height 10
          :color       (if outgoing
                         colors/white-transparent-70-persist
                         colors/gray)}))

(defn message-wrapper [{:keys [outgoing in-popover?]}]
  (if (and outgoing (not in-popover?))
    {:margin-left 96}
    {:margin-right 96}))

(defn message-author-wrapper
  [outgoing display-photo? in-popover?]
  (let [align (if (and outgoing (not in-popover?)) :flex-end :flex-start)]
    (merge {:flex-direction :column
            :flex-shrink    1
            :align-items    align}
           (if (and outgoing (not in-popover?))
             {:margin-right 8}
             (when-not display-photo?
               {:margin-left 8})))))

(defn delivery-status [outgoing]
  (if outgoing
    {:align-self    :flex-end
     :padding-right 8}
    {:align-self    :flex-start
     :padding-left  8}))

(defn pin-indicator [outgoing display-photo?]
  (merge
   {:flex-direction             :row
    :border-top-left-radius     (if outgoing 12 4)
    :border-top-right-radius    (if outgoing 4 12)
    :border-bottom-left-radius  12
    :border-bottom-right-radius 12
    :padding-left               8
    :padding-right              10
    :padding-vertical           5
    :background-color           colors/gray-lighter
    :justify-content            :center
    :max-width                  "80%"}
   (if outgoing
     {:align-self  :flex-end
      :align-items :flex-end}
     {:align-self  :flex-start
      :align-items :flex-start})
   (when display-photo?
     {:margin-left 44})))

(defn pin-indicator-container [outgoing]
  (merge
   {:margin-top      2
    :align-items     :center
    :justify-content :center}
   (if outgoing
     {:align-self    :flex-end
      :align-items   :flex-end
      :padding-right 8}
     {:align-self   :flex-start
      :align-items  :flex-start
      :padding-left 8})))

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
  {:margin-left    12
   :flex-direction :row})

(defn message-author-userpic [outgoing]
  (merge
   {:width      (+ 16 photos/default-size) ;; 16 is for the padding
    :align-self :flex-end}
   (if outgoing
     {:padding-left 8}
     {:padding-horizontal 8
      :padding-right      8})))

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
  [{:keys [incoming-group outgoing]}]
  {:font-size    28
   :line-height  34                     ;TODO: Smaller crops the icon on the top
   :margin-right (if outgoing 18 0) ;; Margin to display outgoing message status
   :margin-top   (if incoming-group 4 0)})

(defn collapse-button []
  {:height         24 :width 24 :background-color colors/blue
   :border-radius  12 :align-items :center :justify-content :center
   :elevation      4
   :shadow-opacity 1
   :shadow-radius  16
   :shadow-color   (:shadow-01 @colors/theme)
   :shadow-offset  {:width 0 :height 4}})

(defn message-view-wrapper [outgoing]
  {:align-self :flex-end
   :flex-direction (if outgoing :row :row-reverse)})

(defn message-view
  [{:keys [content-type outgoing group-chat last-in-group? mentioned pinned]}]
  (merge
   {:border-top-left-radius     16
    :border-top-right-radius    16
    :border-bottom-right-radius 16
    :border-bottom-left-radius  16
    :padding-top                6
    :padding-horizontal         12
    :border-radius              8
    :margin-top                 (if (and last-in-group?
                                         (or outgoing
                                             (not group-chat)))
                                  16
                                  0)}
   (if outgoing
     {:border-bottom-right-radius 4}
     {:border-bottom-left-radius 4})

   (cond
     pinned                                             {:background-color colors/pin-background}
     (= content-type constants/content-type-system-text) nil
     outgoing                                            {:background-color colors/blue}
     mentioned                                           {:background-color colors/mentioned-background
                                                          :border-color colors/mentioned-border
                                                          :border-width 1}
     :else                                               {:background-color colors/blue-light})

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

(defn quoted-message-container [outgoing]
  {:margin-bottom              6
   :padding-bottom             6
   :border-bottom-color        (if outgoing
                                 colors/white-transparent-10
                                 colors/black-transparent)
   :border-bottom-width        2
   :border-bottom-left-radius  2
   :border-bottom-right-radius 2})

(def quoted-message-author-container
  {:flex-direction  :row
   :align-items     :center
   :justify-content :flex-start})

(defn quoted-message-author [outgoing chosen?]
  (assoc (message-author-name chosen?)
         :padding-bottom  6
         :padding-top     0
         :padding-left    0
         :line-height     18
         :font-weight    "500"
         :color           (if outgoing
                            colors/white-transparent-70-persist
                            colors/gray)))

(defn quoted-message-text [outgoing]
  {:font-size 14
   :color (if outgoing
            colors/white-transparent-70-persist
            colors/gray)})

;; Markdown styles

(defn default-text-style []
  {:max-font-size-multiplier react/max-font-size-multiplier
   :style (assoc (typography/default-style)
                 :line-height 22)})

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

(defn text-style [outgoing content-type in-popover?]
  (merge
   (when in-popover? {:number-of-lines 2})
   (cond
     (= content-type constants/content-type-system-text) (system-text-style)
     outgoing (outgoing-text-style)
     :else (default-text-style))))

(defn emph-text-style []
  (update (default-text-style) :style
          assoc :font-style :italic))

(defn outgoing-emph-text-style []
  (update (emph-text-style) :style
          assoc :color colors/white-persist))

(defn emph-style [outgoing]
  (if outgoing
    (outgoing-emph-text-style)
    (emph-text-style)))

(defn strong-text-style []
  (update (default-text-style) :style
          assoc :font-weight "700"))

(defn outgoing-strong-text-style []
  (update (strong-text-style) :style
          assoc :color colors/white-persist))

(defn strong-style [outgoing]
  (if outgoing
    (outgoing-strong-text-style)
    (strong-text-style)))

(defn strong-emph-style [outgoing]
  (update (strong-style outgoing) :style
          assoc :font-style :italic))

(defn strikethrough-style [outgoing]
  (cond-> (update (default-text-style) :style
                  assoc :text-decoration-line :line-through)
    outgoing
    (update :style assoc :color colors/white-persist)))

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

(defn blockquote-style [outgoing]
  (if outgoing
    (outgoing-blockquote-style)
    (default-blockquote-style)))

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

(defn blockquote-text-style [outgoing]
  (if outgoing
    (outgoing-blockquote-text-style)
    (default-blockquote-text-style)))

(defn image-message
  [{:keys [outgoing width height]}]
  {:overflow                   :hidden
   :border-top-left-radius     16
   :border-top-right-radius    16
   :border-bottom-left-radius  (if outgoing 16 4)
   :border-bottom-right-radius (if outgoing 4 16)
   :width                      width
   :height                     height})

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

(defn content-type-contact-request [outgoing]
  {:width           168
   :min-height      224.71
   :border-radius   8
   :border-width    1
   :border-color    colors/gray-lighter
   :align-items     :center
   :padding-bottom  10
   :margin-vertical 4
   :align-self      (if outgoing :flex-end :flex-start)
   :margin-right    (if outgoing 8 0)
   :margin-left     (if outgoing 0 8)})
