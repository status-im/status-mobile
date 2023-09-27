(ns status-im2.contexts.chat.messages.content.text.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.platform :as platform]
    [react-native.core :as rn]
    [status-im2.contexts.chat.messages.content.link-preview.view :as link-preview]
    [status-im2.contexts.chat.messages.content.text.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn render-inline
  [units {:keys [type literal destination]} chat-id style-override first-child-mention]
  (let [show-as-plain-text? (seq style-override)]
    (case (keyword type)
      :code
      (conj units
            [quo/text
             {:style  (if show-as-plain-text?
                        {:color colors/white}
                        (merge style/block (style/code)))
              :weight :code} literal])

      :emph
      (conj units
            [quo/text
             {:style {:font-style :italic
                      :color      (when show-as-plain-text? colors/white)}} literal])

      :strong
      (conj units
            [quo/text
             (cond-> {:weight :bold}
               show-as-plain-text? (assoc :style {:color colors/white})) literal])

      :strong-emph
      (conj units
            [quo/text
             {:weight :bold
              :style  {:font-style :italic
                       :color      (when show-as-plain-text? colors/white)}} literal])

      :del
      (conj units
            [quo/text
             {:style {:text-decoration-line :line-through
                      :color                (when show-as-plain-text? colors/white)}} literal])

      :link
      (conj units
            [quo/text
             {:style    {:color (colors/theme-colors colors/primary-50 colors/primary-60)}
              :on-press #(rf/dispatch [:browser.ui/message-link-pressed destination])}
             destination])

      :mention
      (conj
       units
       [rn/pressable
        {:on-press #(rf/dispatch [:chat.ui/show-profile literal])
         :style    (style/mention-tag-wrapper first-child-mention)}
        [quo/text
         {:weight :medium
          :style  style/mention-tag-text
          :size   :paragraph-1}
         (rf/sub [:messages/resolve-mention literal])]])

      :edited
      (conj units
            [quo/text
             {:weight :medium
              :style  {:font-size 11 ; Font-size must be used instead of props or the
                       ; styles will clash with original message text
                       :color     (colors/theme-colors colors/neutral-40
                                                       colors/neutral-50)}}
             literal])
      :status-tag
      (let [community-id (rf/sub [:community-id-by-chat-id chat-id])]
        (conj units
              [rn/text
               (when community-id
                 {:style    {:color                :blue
                             :text-decoration-line :underline}
                  :on-press #(rf/dispatch [:communities/status-tag-pressed community-id literal])})
               "#"
               literal]))

      (conj units literal))))

(defn first-child-mention
  [children]
  (and (> (count children) 0)
       (= (keyword (:type (second children))) :mention)
       (empty? (get-in children [0 :literal]))))

(defn render-block
  [blocks {:keys [type literal children]} chat-id style-override]
  (let [mention-first (first-child-mention children)]
    (case (keyword type)
      :paragraph
      (conj blocks
            [rn/view
             (reduce
              (fn [acc e]
                (render-inline acc e chat-id style-override mention-first))
              [quo/text
               {:style {:size          :paragraph-1
                        :margin-bottom (if mention-first (if platform/ios? 4 0) 2)
                        :margin-top    (if mention-first (if platform/ios? -4 0) 2)
                        :color         (when (seq style-override) colors/white)}}]
              children)])

      :edited-block
      (conj blocks
            (reduce
             (fn [acc e]
               (render-inline acc e chat-id style-override first-child-mention))
             [quo/text {:size :paragraph-1}]
             children))

      :blockquote
      (conj blocks
            [rn/view {:style style/quote}
             [quo/text literal]])

      :codeblock
      (conj blocks
            [rn/view {:style (merge style/block (style/code))}
             [quo/text (subs literal 0 (dec (count literal)))]])
      blocks)))

(def edited-tag
  {:literal (str "(" (i18n/label :t/edited) ")")
   :type    :edited})

(defn add-edited-tag
  [parsed-text]
  (let [items-count (count parsed-text)
        last-item   (get parsed-text (dec items-count))]
    (if (= (keyword (:type last-item)) :paragraph)
      (update parsed-text
              (dec items-count)
              (fn [last-literal]
                (update last-literal :children into [{:literal " "} edited-tag])))
      (conj parsed-text {:type :edited-block :children [edited-tag]}))))

(defn render-parsed-text
  [{:keys [content chat-id edited-at style-override on-layout]}]
  ^{:key (:parsed-text content)}
  [rn/view
   {:style     style-override
    :on-layout on-layout}
   (reduce (fn [acc e]
             (render-block acc e chat-id style-override))
           [:<>]
           (cond-> (:parsed-text content)
             edited-at
             add-edited-tag))])

(defn text-content
  [message-data]
  [:<>
   [render-parsed-text message-data]
   [link-preview/view message-data]])
