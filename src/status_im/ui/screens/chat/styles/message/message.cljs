(ns status-im.ui.screens.chat.styles.message.message
  (:require [status-im.constants :as constants]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.photos :as photos]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.typography :as typography]))

(defn style-message-text
  [outgoing]
  {:color (if outgoing colors/white-persist colors/text)})

(defn last-message-padding
  [{:keys [first? typing]}]
  (when (and first? (not typing))
    {:padding-bottom 16}))

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
  {:font-size  10
   :align-self :flex-end})

(defn message-timestamp-placeholder
  [outgoing]
  (assoc message-timestamp
         :color (if outgoing
                  colors/blue
                  colors/blue-light)))

(defn message-timestamp-text
  [justify-timestamp? outgoing rtl?]
  (merge message-timestamp
         {:line-height 10
          :color       (if outgoing
                         colors/white-transparent-70-persist
                         colors/gray)}
         (when justify-timestamp? {:position              :absolute
                                   :bottom                9 ; 6 Bubble bottom, 3 message baseline
                                   (if rtl? :left :right) 12})))

(defn message-wrapper-base [message]
  (merge {:flex-direction   :column}
         (last-message-padding message)))

(defn message-wrapper [{:keys [outgoing] :as message}]
  (merge (message-wrapper-base message)
         (if outgoing
           {:margin-left 96}
           {:margin-right 52})))

(defn message-author-wrapper
  [outgoing display-photo?]
  (let [align (if outgoing :flex-end :flex-start)]
    (merge {:flex-direction :column
            :flex-shrink    1
            :align-items    align}
           (if outgoing
             {:margin-right 8}
             (when-not display-photo?
               {:margin-left 8})))))

(defn delivery-status [outgoing]
  (if outgoing
    {:align-self    :flex-end
     :padding-right (if platform/desktop? 24 8)}
    {:align-self    :flex-start
     :padding-left  (if platform/desktop? 24 8)}))

(def message-author-touchable
  {:margin-left      12
   :padding-vertical 2})

(defn message-author-userpic [outgoing]
  (merge
   {:width      (+ 16 photos/default-size) ;; 16 is for the padding
    :align-self :flex-end}
   (if outgoing
     {:padding-left 8}
     {:padding-horizontal 8
      :padding-right 8})))

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

(def message-activity-indicator
  {:padding-top 4})

(defn emoji-message
  [{:keys [incoming-group]}]
  {:font-size    28
   :line-height  34                     ;TODO: Smaller crops the icon on the top
   :margin-right 12
   :margin-top   (if incoming-group 4 0)})

(defn message-view
  [{:keys [content-type outgoing group-chat last-in-group?]}]
  (merge
   {:border-top-left-radius     16
    :border-top-right-radius    16
    :border-bottom-right-radius 16
    :border-bottom-left-radius  16
    :padding-vertical           6
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
     (= content-type constants/content-type-system-text) nil
     outgoing {:background-color colors/blue}
     :else {:background-color colors/blue-light})

   (when (= content-type constants/content-type-emoji)
     {:flex-direction :row})))

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

(def message-author-name-container
  {:padding-top         6
   :padding-left        12
   :padding-right       16
   :margin-right        12
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

(defn text-style [outgoing content-type]
  (cond
    (= content-type constants/content-type-system-text) (system-text-style)
    outgoing (outgoing-text-style)
    :else (default-text-style)))

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

(def monospace-fonts (if platform/ios? "Courier" "monospace"))

(def code-block-background "#2E386B")

(defn inline-code-style []
  (update (default-text-style) :style
          assoc
          :font-family monospace-fonts
          :color colors/white-persist
          :background-color code-block-background))

(def codeblock-style {:style {:padding 10
                              :background-color code-block-background
                              :border-radius 4}})

(def codeblock-text-style
  (update (default-text-style) :style
          assoc
          :font-family monospace-fonts
          :color colors/white))

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

(defn image-content [outgoing]
  {:overflow                   :hidden
   :border-top-left-radius     16
   :border-top-right-radius    16
   :border-bottom-left-radius  (if outgoing 16 4)
   :border-bottom-right-radius (if outgoing 4 16)})
