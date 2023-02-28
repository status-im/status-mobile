(ns status-im2.contexts.chat.messages.content.text.view
  (:require
    [quo2.core :as quo]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]
    [status-im2.contexts.chat.messages.content.text.style :as style]
    [status-im2.contexts.chat.messages.link-preview.view :as link-preview]
    [utils.re-frame :as rf]
    [utils.i18n :as i18n]))


(defn render-inline
  [units {:keys [type literal destination]}]
  (case (keyword type)
    :code
    (conj units [rn/view {:style (merge style/block (style/code))} [quo/text {:weight :code} literal]])

    :emph
    (conj units [quo/text {:style {:font-style :italic}} literal])

    :strong
    (conj units [quo/text {:weight :bold} literal])

    :strong-emph
    (conj units
          [quo/text
           {:weight :bold
            :style  {:font-style :italic}} literal])

    :del
    (conj units [quo/text {:style {:text-decoration-line :line-through}} literal])

    :link
    (conj units
          [quo/text
           {:style    {:color (colors/theme-colors colors/primary-50 colors/primary-60)}
            :on-press #(rf/dispatch [:browser.ui/message-link-pressed destination])}
           destination])

    :mention
    (conj
     units
     [rn/touchable-opacity
      {:active-opacity 1
       :on-press       #(rf/dispatch [:chat.ui/show-profile literal])
       :style          (merge style/block {:background-color colors/primary-50-opa-10})}
      [quo/text
       {:weight :medium
        :style  {:color (colors/theme-colors colors/primary-50 colors/primary-60)}}
       (rf/sub [:messages/resolve-mention literal])]])
    
    :edited
    (conj units [rn/text (style/edited-style) (str " (" (i18n/label :t/edited) ")")])

    :edited
    (conj units

          [quo/text
           {:weight :medium
            :style  {:font-size 11 ; Font-size must be used instead of props or the
                                   ; styles will clash with original message text
                     :color     (colors/theme-colors colors/neutral-40
                                                     colors/neutral-50)}}
           literal])

    (conj units literal)))


(defn render-block
  [blocks {:keys [type ^js literal children]} edited-at]
  (case (keyword type)
    :paragraph
    (conj blocks
          (reduce
           render-inline
           [quo/text]
           (conj children
                 (when edited-at
                   {:type :edited}))))

    :blockquote
    (conj blocks
          [rn/view {:style style/quote}
           [quo/text literal]]
          (when edited-at
            [quo/text
             {:weight :medium
              :style  {:font-size 11
                       :color (colors/theme-colors colors/neutral-40 colors/neutral-50)}}
             (str " (" (i18n/label :t/edited) ")")]))

    :codeblock
    (conj blocks
          [rn/view {:style (merge style/block (style/code))}
           [quo/text (subs literal 0 (dec (count literal)))]]
          (when edited-at
            [quo/text
             {:weight :medium
              :style  {:font-size 11
                       :color (colors/theme-colors colors/neutral-40 colors/neutral-50)}}
             (str " (" (i18n/label :t/edited) ")")]))
    blocks))

(defn add-edited-tag
  [parsed-text]
  (update parsed-text
          (dec (count parsed-text))
          (fn [last-literal]
            (update last-literal
                    :children
                    conj
                    {:literal (str " (" (i18n/label :t/edited) ")")
                     :type    :edited}))))

(defn render-parsed-text
  [{:keys [content edited-at]}]
  (reduce (fn [acc e]
            (render-block acc e edited-at))
          [:<>]
          (cond-> (:parsed-text content)
            edited-at add-edited-tag)))

(defn text-content
  [message-data context]
  [rn/view
   [render-parsed-text message-data]
   [link-preview/link-preview message-data context]])
