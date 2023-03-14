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
  [units {:keys [type literal destination]} chat-id]
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

    (conj units literal)))


(defn render-block
  [blocks {:keys [type literal children]} chat-id]
  (case (keyword type)
    :paragraph
    (conj blocks
          [rn/view {:style style/paragraph}
           (reduce
            (fn [acc e]
              (render-inline acc e chat-id))
            [quo/text]
            children)])

    :edited-block
    (conj blocks
          [rn/view {:style style/paragraph}
           (reduce
            (fn [acc e]
              (render-inline acc e chat-id))
            [quo/text]
            children)])

    :blockquote
    (conj blocks
          [rn/view {:style style/quote}
           [quo/text literal]])

    :codeblock
    (conj blocks
          [rn/view {:style (merge style/block (style/code))}
           [quo/text (subs literal 0 (dec (count literal)))]])
    blocks))

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
  [{:keys [content chat-id edited-at]}]
  [rn/view {:style style/parsed-text-block}
   (reduce (fn [acc e]
             (render-block acc e chat-id))
           [:<>]
           (cond-> (:parsed-text content)
             edited-at
             add-edited-tag))])

(defn text-content
  [message-data context]
  [rn/view
   [render-parsed-text message-data]
   [link-preview/link-preview message-data context]])
